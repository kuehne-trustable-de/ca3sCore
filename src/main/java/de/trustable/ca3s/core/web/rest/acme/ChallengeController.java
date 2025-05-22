/*^
  ===========================================================================
  ACME server
  ===========================================================================
  Copyright (C) 2017-2018 DENIC eG, 60329 Frankfurt am Main, Germany
  ===========================================================================
  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:

  The above copyright notice and this permission notice shall be included in
  all copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  THE SOFTWARE.
  ===========================================================================
*/

package de.trustable.ca3s.core.web.rest.acme;

import de.trustable.ca3s.core.domain.*;
import de.trustable.ca3s.core.domain.enumeration.ChallengeStatus;
import de.trustable.ca3s.core.repository.AcmeChallengeRepository;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.dto.acme.ChallengeResponse;
import de.trustable.ca3s.core.service.dto.acme.problem.AcmeProblemException;
import de.trustable.ca3s.core.service.dto.acme.problem.ProblemDetail;
import de.trustable.ca3s.core.service.util.*;
import de.trustable.ca3s.core.web.rest.data.AcmeChallengeValidation;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.x509.GeneralName;
import org.jose4j.jwt.consumer.JwtContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import org.xbill.DNS.*;


import javax.net.ssl.*;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.io.*;
import java.net.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.xbill.DNS.Name.*;
import static org.xbill.DNS.Type.TXT;
import static org.xbill.DNS.Type.string;


@Transactional(dontRollbackOn = AcmeProblemException.class)
@RestController
@RequestMapping("/acme/{realm}/challenge")
public class ChallengeController extends AcmeController {

    private static final Logger LOG = LoggerFactory.getLogger(ChallengeController.class);

    public static final Name ACME_CHALLENGE_PREFIX = fromConstantString("_acme-challenge");

    /**
     * OID of the {@code acmeValidation} extension.
     */
    public static final String ACME_VALIDATION_OID = "1.3.6.1.5.5.7.1.31";
    public static final String ACME_TLS_1_PROTOCOL = "acme-tls/1";


    private final int[] alpnPorts;

    @Value("${ca3s.acme.reject.get:true}")
    boolean rejectGet;

    private final AcmeChallengeRepository challengeRepository;

	private final PreferenceUtil preferenceUtil;

    private final SimpleResolver dnsResolver;

    private final AuditService auditService;

    private final AcmeOrderUtil acmeOrderUtil;
    private final RateLimiterService rateLimiterService;


    public ChallengeController(AcmeChallengeRepository challengeRepository,
                               PreferenceUtil preferenceUtil,
                               AuditService auditService,
                               @Value("${ca3s.acme.alpn.ports:443}") int[] alpnPorts,
                               @Value("${ca3s.dns.server:}") String resolverHost,
                               @Value("${ca3s.dns.port:53}") int resolverPort,
                               AcmeOrderUtil acmeOrderUtil, @Value("${ca3s.acme.ratelimit.second:0}") int rateSec,
                               @Value("${ca3s.acme.ratelimit.minute:20}") int rateMin,
                               @Value("${ca3s.acme.ratelimit.hour:0}") int rateHour)
        throws UnknownHostException {

        this.challengeRepository = challengeRepository;
        this.preferenceUtil = preferenceUtil;
        this.auditService = auditService;
        this.acmeOrderUtil = acmeOrderUtil;

        this.alpnPorts = alpnPorts;

        this.dnsResolver = new SimpleResolver(resolverHost);
        this.dnsResolver.setPort(resolverPort);
        LOG.info("Applying default DNS resolver {}", this.dnsResolver.getAddress());

        this.rateLimiterService = new RateLimiterService("Challenge", rateSec, rateMin, rateHour);
    }

    @RequestMapping(value = "/{challengeId}", method = GET, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getChallenge(@PathVariable final long challengeId,
                                          @PathVariable final String realm,
                                          @RequestHeader(value=HEADER_X_CA3S_FORWARDED_HOST, required=false) String forwardedHost) {

	  	LOG.debug("Received Challenge request ");

        checkACMERateLimit(rateLimiterService,challengeId, realm);

	    final HttpHeaders additionalHeaders = buildNonceHeader();

        if( rejectGet ){
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).headers(additionalHeaders).build();
        }

        Optional<AcmeChallenge> challengeOpt = challengeRepository.findById(challengeId);
		if(challengeOpt.isEmpty()) {
		    return ResponseEntity.notFound().headers(additionalHeaders).build();
		}else {
			AcmeChallenge challengeDao = challengeOpt.get();

			LOG.debug( "returning challenge {}", challengeDao.getId());

			ChallengeResponse challenge = buildChallengeResponse(challengeDao, getEffectiveUriComponentsBuilder(realm, forwardedHost));

			if(challengeDao.getStatus() == ChallengeStatus.VALID ) {
				URI authUri = locationUriOfAuthorization(challengeDao.getAcmeAuthorization().getAcmeAuthorizationId(), getEffectiveUriComponentsBuilder(realm, forwardedHost));
			    additionalHeaders.set("Link", "<" + authUri.toASCIIString() + ">;rel=\"up\"");
			}
            return ok().headers(additionalHeaders).body(challenge);
		}
    }

    @RequestMapping(value = "/{challengeId}", method = POST, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JOSE_JSON_VALUE)
    public ResponseEntity<?> postChallenge(@RequestBody final String requestBody,
          @PathVariable final long challengeId, @PathVariable final String realm,
          @RequestHeader(value=HEADER_X_CA3S_FORWARDED_HOST, required=false) String forwardedHost) {

        LOG.debug("Received Challenge request ");

        checkACMERateLimit(rateLimiterService,challengeId, realm);

        try {
            JwtContext context = jwtUtil.processFlattenedJWT(requestBody);

            AcmeAccount acctDao = checkJWTSignatureForAccount(context, realm);

            final HttpHeaders additionalHeaders = buildNonceHeader();

            Optional<AcmeChallenge> challengeOpt = challengeRepository.findById(challengeId);
            if(challengeOpt.isEmpty()) {
                return ResponseEntity.notFound().headers(additionalHeaders).build();
            }else {
                AcmeChallenge challengeDao = challengeOpt.get();

                AcmeOrder order = challengeDao.getAcmeAuthorization().getOrder();

                if(!order.getAccount().getAccountId().equals(acctDao.getAccountId())) {
                    LOG.warn("Account of signing key {} does not match account id {} associated to given challenge{}", acctDao.getAccountId(), challengeDao.getAcmeAuthorization().getOrder().getAccount().getAccountId(), challengeId);
                    final ProblemDetail problem = new ProblemDetail(AcmeUtil.MALFORMED, "Account / Auth mismatch",
                        BAD_REQUEST, "", AcmeController.NO_INSTANCE);
                    throw new AcmeProblemException(problem);
                }

                if( Instant.now().isAfter(order.getExpires())){
                    LOG.debug("order of this challenge {} already expired", challengeId);
                }else {
                    isChallengeSolved(challengeDao);
                }

                ChallengeResponse challengeResponse = buildChallengeResponse(challengeDao, getEffectiveUriComponentsBuilder(realm, forwardedHost));

                URI authUri = locationUriOfAuthorization(challengeDao.getAcmeAuthorization().getAcmeAuthorizationId(), getEffectiveUriComponentsBuilder(realm, forwardedHost));
                additionalHeaders.set("Link", "<" + authUri.toASCIIString() + ">;rel=\"up\"");
                return ok().headers(additionalHeaders).body(challengeResponse);
            }

        } catch (AcmeProblemException e) {
            return buildProblemResponseEntity(e);
        }
    }

    public boolean isChallengeSolved(AcmeChallenge challengeDao) {
        boolean useProxy = challengeDao.getAcmeAuthorization().getOrder()
            .getAttributes().stream().anyMatch(att -> AcmeOrderAttribute.REQUEST_PROXY_ID_USED.equals(att.getName()));

        LOG.debug("useProxy: {}", useProxy);

        if (useProxy) {
            LOG.debug("challenge {} may be validated by proxy", challengeDao.getId());
            return false;
        } else {
            boolean solved = checkChallenge(challengeDao);
            if (solved) {
                LOG.debug("validation of challenge {} of type '{}' succeeded", challengeDao.getId(), challengeDao.getType());
            } else {
                LOG.warn("validation of challenge {} of type '{}' failed", challengeDao.getId(), challengeDao.getType());
            }
            return solved;
        }
    }

    public boolean checkChallenge(AcmeChallenge challengeDao) {


        LOG.debug( "checking challenge {}", challengeDao.getId());

        boolean solved = false;
        String lastError = challengeDao.getLastError();
        ChallengeStatus newChallengeState = null;
        if( AcmeChallenge.CHALLENGE_TYPE_HTTP_01.equals(challengeDao.getType())) {
            if (checkChallengeHttp(challengeDao)) {
                newChallengeState = ChallengeStatus.VALID;
                solved = true;
            } else {
                newChallengeState = ChallengeStatus.PENDING;
            }
        }else if( AcmeChallenge.CHALLENGE_TYPE_DNS_01.equals(challengeDao.getType())){
            if (checkChallengeDNS(challengeDao)) {
                newChallengeState = ChallengeStatus.VALID;
                solved = true;
            } else {
                newChallengeState = ChallengeStatus.PENDING;
            }
        }else if( AcmeChallenge.CHALLENGE_TYPE_ALPN_01.equals(challengeDao.getType())){
            if (checkChallengeALPN(challengeDao)) {
                newChallengeState = ChallengeStatus.VALID;
                solved = true;
            } else {
                newChallengeState = ChallengeStatus.PENDING;
            }
        }else{
            LOG.warn("Unexpected type '{}' of challenge{}", challengeDao.getType(), challengeDao.getId());
        }

        if( newChallengeState != null) {
            ChallengeStatus oldChallengeState = challengeDao.getStatus();
            if(!oldChallengeState.equals(newChallengeState)) {
                challengeDao.setStatus(newChallengeState);
                challengeDao.setValidated(Instant.now());
                challengeRepository.save(challengeDao);

                LOG.debug("{} challengeDao set to '{}' at {}", challengeDao.getType(), challengeDao.getStatus().toString(), challengeDao.getValidated());
            }
        }

        if(solved){
            challengeDao.setLastError("");
        }
        if(!Objects.equals(lastError, challengeDao.getLastError())){
            challengeRepository.save(challengeDao);
            LOG.debug("challenge's  #{}' last error set to '{}'", challengeDao.getId(), challengeDao.getLastError() );
        }

        acmeOrderUtil.alignOrderState(challengeDao.getAcmeAuthorization().getOrder());

        return solved;
    }

    private boolean checkChallengeDNS(AcmeChallenge challengeDao) {

        String identifierValue = challengeDao.getValue();
        String expectedContent = buildKeyAuthorizationHashBase64(challengeDao);
        LOG.info("DNS lookup: expectedContent '{}'", expectedContent);

        final Name nameToLookup;
        try {
            final Name nameOfIdentifier = fromString(identifierValue, root);
            nameToLookup = concatenate(ACME_CHALLENGE_PREFIX, nameOfIdentifier);

        } catch (TextParseException | NameTooLongException e) {
            throw new RuntimeException(identifierValue + " invalid", e);
        }

        final Lookup lookupOperation = new Lookup(nameToLookup, TXT);
        lookupOperation.setResolver(dnsResolver);
        lookupOperation.setCache(null);
        LOG.info("DNS lookup: {} records of '{}' (via resolver '{}')", string(TXT), nameToLookup, this.dnsResolver.getAddress());

        final Instant startedAt = Instant.now();
        final org.xbill.DNS.Record[] lookupResult = lookupOperation.run();
        final Duration lookupDuration = Duration.between(startedAt, Instant.now());
        LOG.info("DNS lookup yields: {} (took {})", Arrays.toString(lookupResult), lookupDuration);

        final Collection<String> retrievedToken = extractTokenFrom(lookupResult);
        if (retrievedToken.isEmpty()) {
            String msg = "Found no DNS entry solving '" + identifierValue + "'";
            LOG.info(msg);
            challengeDao.setLastError(msg);
            return false;
        } else {
            final boolean matchingDnsEntryFound = retrievedToken.stream().anyMatch(expectedContent::equals);

            logChallengeValidationOutcome(matchingDnsEntryFound, challengeDao,
                "dns challenge response matches for host '" + identifierValue + "'",
                "dns challenge response mismatch for host '" + identifierValue + "'");

            if (matchingDnsEntryFound) {
                return true;
            } else {
                String msg = "Did not find matching token '"+expectedContent+"' in TXT record DNS response";
                LOG.info(msg);
                challengeDao.setLastError(msg);
                return false;
            }
        }
    }

    private void logChallengeValidationOutcome(boolean matches, AcmeChallenge challengeDao, String matchMsg, String mismatchMsg) {
        AcmeOrder acmeOrder = challengeDao.getAcmeAuthorization().getOrder();
        if(matches) {
            auditService.saveAuditTrace(
                auditService.createAuditTraceAcmeChallengeSucceeded(acmeOrder.getAccount(), acmeOrder,
                    matchMsg));
        }else{
            auditService.saveAuditTrace(
                auditService.createAuditTraceAcmeChallengeFailed(acmeOrder.getAccount(), acmeOrder, mismatchMsg));
            LOG.info(mismatchMsg);
            challengeDao.setLastError(mismatchMsg);
        }
    }


    /**
     * @param lookupResult Optional
     * @return Never <code>null</code>
     */
    private @NotNull List<String> extractTokenFrom(final org.xbill.DNS.Record[] lookupResult) {

        List<String> tokenList = new ArrayList<>();
        if( lookupResult != null) {
            for (org.xbill.DNS.Record record : lookupResult) {
                LOG.debug("Found DNS entry solving '{}'", record);
                tokenList.addAll(((TXTRecord) record).getStrings());
            }
        }
        return tokenList;
    }


    private boolean checkChallengeHttp(AcmeChallenge challengeDao) {

		int[] ports = {80, 5544, 8800};

		long timeoutMilliSec = preferenceUtil.getAcmeHTTP01TimeoutMilliSec();
		String portList = preferenceUtil.getAcmeHTTP01CallbackPorts();

		if(portList != null && !portList.trim().isEmpty()) {
			String[] parts = portList.split(", ");
			ports = new int[parts.length];
		    for( int i = 0; i < parts.length; i++) {
		    	ports[i] = -1;
		    	try {
		    		ports[i] = Integer.parseInt(parts[i].trim());
		    		LOG.debug("checkChallengeHttp port number '" + ports[i] + "' configured for HTTP callback");
		    	} catch( NumberFormatException nfe) {
					LOG.warn("checkChallengeHttp port number parsing fails for '" + ports[i] + "', ignoring", nfe);
		    	}
		    }

		}

        AcmeOrder acmeOrder = challengeDao.getAcmeAuthorization().getOrder();
	    String token = challengeDao.getToken();
        String expectedContent = buildKeyAuthorization(challengeDao);

	    String fileNamePath = "/.well-known/acme-challenge/" + token;
	    String host = challengeDao.getAcmeAuthorization().getValue();

	    for( int port: ports) {

		    try {
                URL url = new URL("http", host, port, fileNamePath);
                LOG.debug("Opening connection to  : " + url);

                HttpClient instance = HttpClientBuilder.create()
                    .setRedirectStrategy(new LaxRedirectStrategy())
                    .build();

                HttpGet request = new HttpGet(url.toString());
                request.addHeader(HttpHeaders.USER_AGENT, "CA3S_ACME");

                RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout((int)timeoutMilliSec)
                    .setConnectTimeout((int)timeoutMilliSec)
                    .setSocketTimeout((int)timeoutMilliSec)
                    .build();

                request.setConfig(requestConfig);
                HttpResponse response = instance.execute(request);
                int responseCode = response.getStatusLine().getStatusCode();

/*
                SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
                simpleClientHttpRequestFactory.setConnectTimeout((int) timeoutMilliSec);
                simpleClientHttpRequestFactory.setReadTimeout((int) timeoutMilliSec);
                RestTemplate restTemplate = new RestTemplate(simpleClientHttpRequestFactory);

                ResponseEntity<String> challengeResponse = restTemplate.getForEntity(url.toString(), String.class);
*/

/*
				HttpURLConnection con = (HttpURLConnection) url.openConnection();

				// Just wait for two seconds
				con.setConnectTimeout((int) timeoutMilliSec);
				con.setReadTimeout((int) timeoutMilliSec);

				// optional default is GET
				con.setRequestMethod("GET");

				// add request header
				con.setRequestProperty("User-Agent", "CA3S_ACME");

				int responseCode = con.getResponseCode();
 */

//                int responseCode = challengeResponse.getStatusCodeValue();

                LOG.debug("\nSending 'GET' request to URL : " + url);
				LOG.debug("Response Code : " + responseCode);

				if( responseCode != 200) {
					String msg = "read challenge responded with unexpected code : " + responseCode;
                    LOG.info(msg);
                    challengeDao.setLastError(msg);
					continue;
				}

                String actualContent = readChallengeResponse(response.getEntity().getContent());

//                String actualContent = readChallengeResponse(con);
//                String actualContent = challengeResponse.getBody();

                LOG.debug("expected content: '{}'", expectedContent);
                boolean matches = expectedContent.equals( actualContent);
                logChallengeValidationOutcome(matches,
                    challengeDao,
                    "http challenge response matches at host '" + host + ":" + port + "'",
                    "http challenge response mismatch at host '" + host + ":" + port + "'");
                return matches;

            } catch(UnknownHostException uhe) {
//            } catch(RestClientException uhe) {
                String msg = "unable to resolve hostname: '" + host + "'";
//                auditService.saveAuditTrace(auditService.createAuditTraceAcmeChallengeFailed(acmeOrder.getAccount(), acmeOrder, msg));
                LOG.info(msg);
                challengeDao.setLastError(msg);
                // give up here, other ports won't give better results
                return false;
            } catch(SocketTimeoutException | ConnectTimeoutException ste) {
                String msg = "timeout connecting to "+host+":"+port+" for challenge id " +challengeDao.getId();
//                auditService.saveAuditTrace(auditService.createAuditTraceAcmeChallengeFailed(acmeOrder.getAccount(), acmeOrder, msg));
                LOG.info(msg);
                challengeDao.setLastError(msg);
                // go on trying other ports
		    } catch(IOException  ioe) {
				String msg = "problem reading challenge response on "+host+":"+port+" for challenge id " +challengeDao.getId()+" : " + ioe.getMessage();
                LOG.info(msg);
                challengeDao.setLastError(msg);
				LOG.debug("exception occurred reading challenge response", ioe);
                // go on trying other ports
		    }
	    }

//        auditService.saveAuditTrace(
//            auditService.createAuditTraceAcmeChallengeFailed(acmeOrder.getAccount(), acmeOrder, ioExceptionMsg));

		return false;
	}

    private String readChallengeResponse(HttpURLConnection con) throws IOException {
        return readChallengeResponse(con.getInputStream());
    }

    private String readChallengeResponse(InputStream is) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
            if (response.length() > 1000) {
                LOG.debug("limiting read of challenge response to 1000 characters.");
                break;
            }
        }
        in.close();

        String actualContent = response.toString().trim();

        if( actualContent.length() > 100){
            LOG.debug("read challenge response (truncated): " + actualContent.substring(0,100) + " ...");
        }else {
            LOG.debug("read challenge response: " + actualContent);
        }

        return actualContent;
    }

    private boolean checkChallengeALPN(AcmeChallenge challengeDao) {

        AcmeOrder acmeOrder = challengeDao.getAcmeAuthorization().getOrder();
        String expectedContent = buildKeyAuthorizationHashBase64(challengeDao);

        String host = challengeDao.getAcmeAuthorization().getValue();

        // this is rare case where a trustAll-Manager makes sense as the details of the certificate get checked later on
        // please think twice before using the trustAll-Manager in a productive context !!
        TrustManager[] trustAllCerts = { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        } };

        for( int port: alpnPorts) {

            try {
                if(validateALPNChallenge(challengeDao, expectedContent, host, trustAllCerts, port)){
                    LOG.debug("alpn challenge validation successful on '" + host + ":" + port + "' ");
                    return true;
                }

            } catch(UnknownHostException uhe) {
                String msg = "unable to resolve hostname: '" + host + "'";
                auditService.saveAuditTrace(
                    auditService.createAuditTraceAcmeChallengeFailed(acmeOrder.getAccount(), acmeOrder, msg));
                LOG.info(msg);
                challengeDao.setLastError(msg);
                return false;
            } catch(IOException ioe) {
                String msg = "problem reading alpn certificate on "+host+":"+port+" for challenge id " +challengeDao.getId()+" : " + ioe.getMessage();
                LOG.info(msg);
                challengeDao.setLastError(msg);
                LOG.debug("exception occurred reading challenge response", ioe);
            } catch (CertificateException ce) {
                String msg = "problem reading alpn challenge response in certificate provided by "+host+":"+port+" for challenge id " +challengeDao.getId()+" : " + ce.getMessage();
                LOG.info(msg);
                challengeDao.setLastError(msg);
                LOG.debug("exception occurred reading alpn challenge response certificate", ce);
            }
        }

//        auditService.saveAuditTrace(
//            auditService.createAuditTraceAcmeChallengeFailed(acmeOrder.getAccount(), acmeOrder, ioExceptionMsg));

        return false;
    }

    private boolean validateALPNChallenge(AcmeChallenge challengeDao, String expectedContent, String host, TrustManager[] trustAllCerts, int port) throws IOException, CertificateException {
        LOG.debug("Opening ALPN connection to {}:{} ", host, port);

        Certificate[] serverCerts;
        SSLSocket sslSocket = null;
        try {
            // Code for creating a client side SSLSocket
            SSLContext sslContext = SSLContext.getInstance("TLS");

            sslContext.init(null,
                trustAllCerts,
                RandomUtil.getSecureRandom());
            SSLSocketFactory sslsf = sslContext.getSocketFactory();

            sslSocket = (SSLSocket) sslsf.createSocket(host, port);

            // Get an SSLParameters object from the SSLSocket
            SSLParameters sslp = sslSocket.getSSLParameters();

            SNIHostName serverName = new SNIHostName(host);
            sslp.setServerNames(Collections.singletonList(serverName));

            // Populate SSLParameters with the ALPN values
            // On the client side the order doesn't matter as
            // when connecting to a JDK server, the server's list takes priority
            String[] clientAPs = {ACME_TLS_1_PROTOCOL};
            sslp.setApplicationProtocols(clientAPs);


            // Populate the SSLSocket object with the SSLParameters object
            // containing the ALPN values
            sslSocket.setSSLParameters(sslp);

            sslSocket.startHandshake();

            // After the handshake, get the application protocol that has been negotiated
            String ap = sslSocket.getApplicationProtocol();
            LOG.debug("Application Protocol server side: \"" + ap + "\"");

            serverCerts = sslSocket.getSession().getPeerCertificates();

        } catch (NoSuchAlgorithmException| KeyManagementException e) {
            LOG.warn("algorithm initialization problem ",e);
            return false;
        } finally {
            if( sslSocket != null) {
                sslSocket.close();
            }
        }

        if(serverCerts.length == 0){
            String msg ="no certificate available after connection with " + host + ":" + port;
            LOG.info(msg);
            challengeDao.setLastError(msg);
            return false;
        }else if(serverCerts.length > 1){
            String msg = "more than one (#"+serverCerts.length+") certificate returned "+ host + ":"+ port+", expecting a single selfsigned certificate";
            LOG.info(msg);
            challengeDao.setLastError(msg);
            return false;
        }

        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        InputStream in = new ByteArrayInputStream(serverCerts[0].getEncoded());
        X509Certificate cert = (X509Certificate)certFactory.generateCertificate(in);

        if(!validateALPNCertificate(challengeDao, host, port, cert)){
            return false;
        }

        byte[] acmeValidationExtBytes = cert.getExtensionValue(ACME_VALIDATION_OID);
        ASN1OctetString octetString = (ASN1OctetString) ASN1OctetString.fromByteArray(acmeValidationExtBytes);
        ASN1OctetString rfc8737OctetString = (ASN1OctetString) ASN1OctetString.fromByteArray(octetString.getOctets());
        String actualContent = Base64.getEncoder().encodeToString(rfc8737OctetString.getOctets());

        if( rfc8737OctetString.getOctets().length > 32){
            String msg = ("actualContent has unexpected length of rfc8737OctetString : "+ rfc8737OctetString.getOctets().length);
/*
            byte[] challenge = new byte[32];
            System.arraycopy(rfc8737OctetString.getOctets(), rfc8737OctetString.getOctets().length - 32, challenge, 0, 32);
            actualContent = Base64.getEncoder().encodeToString(challenge);
*/
            LOG.info(msg);
            challengeDao.setLastError(msg);
            return false;
        }

        LOG.debug("read challenge response: " + actualContent);
        LOG.debug("expected content: '{}'", expectedContent);

        boolean matches = expectedContent.equals(actualContent);

        logChallengeValidationOutcome(matches, challengeDao,
            "alpn challenge response matches at host '" + host + ":" + port + "'",
            "alpn challenge response mismatch at host '" + host + ":" + port + "'");
        return matches;
    }

    public static boolean validateALPNCertificate(AcmeChallenge challengeDao, String host, int port, X509Certificate cert) throws CertificateParsingException {

        if( LOG.isDebugEnabled()){
            try {
                LOG.debug("alpn certificate : {}", Base64.getEncoder().encodeToString(cert.getEncoded()));
            } catch (CertificateEncodingException e) {
                String msg = "Encoding problem parsing ALPN certificate";
                LOG.info(msg);
                challengeDao.setLastError(msg);
                LOG.debug(msg, e);
                return false;
            }
        }

        // Check SAN entry
        if( cert.getSubjectAlternativeNames() == null ||
            cert.getSubjectAlternativeNames().isEmpty()){
            String msg = "no SAN entry available in certificate provided by " + host + ":" + port;
            LOG.info(msg);
            challengeDao.setLastError(msg);
            return false;
        } else if( cert.getSubjectAlternativeNames().size() > 1){
            String msg = "more than one SAN entry (#"+cert.getSubjectAlternativeNames().size()+") included in certificate provided by " + host + ":" + port;
            LOG.info(msg);
            challengeDao.setLastError(msg);
            return false;
        }

        Collection<List<?>> altNames = cert.getSubjectAlternativeNames();
        if (altNames != null) {
            for (List<?> altName : altNames) {
                int altNameType = (Integer) altName.get(0);

                if (GeneralName.dNSName == altNameType){
                    String sanValue = "";
                    if (altName.get(1) instanceof String) {
                        sanValue = ((String) altName.get(1)).toLowerCase();
                    } else if (altName.get(1) instanceof byte[]) {
                        sanValue = new String((byte[]) (altName.get(1))).toLowerCase();
                    }

                    if( host.equalsIgnoreCase(sanValue)){
                        LOG.debug("SAN entry '{}' machtes expected host '{}'", sanValue, host);
                    }else{
                        String msg = "SAN entry value ("+ sanValue+") in alpn certificate provided by '" + host + ":" + port + "', does not match expected host '" + host + "'";
                        LOG.info(msg);
                        challengeDao.setLastError(msg);
                        return false;
                    }
                }else{
                    String msg = "unexpected SAN entry type ("+ altNameType+") in alpn certificate provided by '" + host + ":" + port + "', 'DNS' (2) expected.";
                    LOG.info(msg);
                    challengeDao.setLastError(msg);
                    return false;
                }
            }
        }

        // Check ACME extension
        if( cert.getCriticalExtensionOIDs().contains(ACME_VALIDATION_OID) ){
            LOG.debug("ACME validation oid is present and marked as critical!");
        }else{
            String msg = "ACME validation oid is NOT present and NOT marked as critical in certificate provided by '" + host + ":" + port + "'";
            LOG.info(msg);
            challengeDao.setLastError(msg);
            return false;
        }
        return true;
    }

    private String buildKeyAuthorizationHashBase64(AcmeChallenge challengeDao) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buildKeyAuthorizationHash(challengeDao));
    }

    private byte[] buildKeyAuthorizationHash(AcmeChallenge challengeDao) {

        try {
            return cryptoUtil.getSHA256Digest(buildKeyAuthorization(challengeDao).getBytes());
        } catch (NoSuchAlgorithmException e) {
            LOG.warn("Hashing challenge data failed", e);
            return new byte[0];
        }
    }

    private String buildKeyAuthorization(AcmeChallenge challengeDao){
        String token = challengeDao.getToken();
        String pkThumbprint = challengeDao.getAcmeAuthorization().getOrder().getAccount().getPublicKeyHash();
        String authorization =  token + '.' + pkThumbprint;
        LOG.debug("authorization: {}", authorization);
        return authorization;
    }

    ChallengeResponse buildChallengeResponse(final AcmeChallenge challengeDao, final UriComponentsBuilder uriBuilder){
        return new ChallengeResponse(challengeDao, locationUriOfChallenge(challengeDao.getId(), uriBuilder).toString());
    }

    private URI locationUriOfChallenge(final long challengeId, final UriComponentsBuilder uriBuilder) {
	    return challengeResourceUriBuilderFrom(uriBuilder.path("../..")).path("/").path(Long.toString(challengeId)).build().normalize().toUri();
	}

    private URI locationUriOfAuthorization(final long authorizationId, final UriComponentsBuilder uriBuilder) {
	    return authorizationResourceUriBuilderFrom(uriBuilder.path("../..")).path("/").path("..").path("/").path(Long.toString(authorizationId)).build().normalize().toUri();
	}

    public ResponseEntity<Void> checkChallengeValidation(AcmeChallengeValidation acmeChallengeValidation) {

        Long challengeId = acmeChallengeValidation.getChallengeId();
        Optional<AcmeChallenge> challengeOpt = challengeRepository.findByChallengeId(challengeId);
        if(challengeOpt.isEmpty()) {
            LOG.info("challenge validation for unknown challenge id: {}", challengeId);
            return ResponseEntity.notFound().build();
        }else {
            AcmeChallenge challengeDao = challengeOpt.get();

            AcmeOrder order = challengeDao.getAcmeAuthorization().getOrder();

            if( Instant.now().isAfter(order.getExpires())){
                LOG.info("order of this challenge {} already expired", challengeId);
                return ResponseEntity.badRequest().build();
            }

            if( !ChallengeStatus.PENDING.equals(challengeDao.getStatus())){
                LOG.info("challenge has unexpected status '{}' != PENDING", challengeDao.getStatus());
                return ResponseEntity.badRequest().build();
            }

            if( ChallengeStatus.INVALID.equals(acmeChallengeValidation.getStatus()) ){
                challengeDao.setValidated(Instant.now());
                challengeDao.setLastError(acmeChallengeValidation.getError());
                challengeDao.setStatus(ChallengeStatus.INVALID);
                challengeRepository.save(challengeDao);
            }else if( ChallengeStatus.VALID.equals(acmeChallengeValidation.getStatus()) ){
                challengeDao.setValidated(Instant.now());

                if( AcmeChallenge.CHALLENGE_TYPE_HTTP_01.equals(challengeDao.getType())) {

                    String expectedContent = buildKeyAuthorization(challengeDao);
                    if(Arrays.asList(acmeChallengeValidation.getResponses()).contains(expectedContent)) {
                        LOG.info("proxy validated http-01 challenge id '{}' successfully", challengeDao.getId());
                        challengeDao.setStatus(ChallengeStatus.VALID);
                    }else{
                        LOG.info("proxy failed validation of http-01 challenge id '{}'", challengeDao.getId());
                    }
                }else if( AcmeChallenge.CHALLENGE_TYPE_DNS_01.equals(challengeDao.getType())){
                    String expectedContent = buildKeyAuthorizationHashBase64(challengeDao);
                    if(Arrays.asList(acmeChallengeValidation.getResponses()).contains(expectedContent)) {
                        LOG.info("proxy validated dns-01 challenge id '{}' successfully", challengeDao.getId());
                        challengeDao.setStatus(ChallengeStatus.VALID);
                    }else{
                        LOG.info("proxy failed validation of dns-01 challenge id '{}'", challengeDao.getId());
                    }
                }else if( AcmeChallenge.CHALLENGE_TYPE_ALPN_01.equals(challengeDao.getType())){
                    String expectedContent = buildKeyAuthorizationHashBase64(challengeDao);
                    if(Arrays.asList(acmeChallengeValidation.getResponses()).contains(expectedContent)) {
                        LOG.info("proxy validated alpn-01 challenge id '{}' successfully", challengeDao.getId());
                        challengeDao.setStatus(ChallengeStatus.VALID);
                    }else{
                        LOG.info("proxy failed validation of alpn-01 challenge id '{}'", challengeDao.getId());
                    }

                }else{
                    LOG.warn("Unexpected type '{}' of challenge{}", challengeDao.getType(), challengeDao.getId());
                }

                challengeRepository.save(challengeDao);
                acmeOrderUtil.alignOrderState(order);
            }
        }
        return ResponseEntity.ok().build();
    }
}
