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

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequestUri;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import de.trustable.ca3s.core.domain.AcmeAuthorization;
import de.trustable.ca3s.core.domain.AcmeOrder;
import de.trustable.ca3s.core.domain.enumeration.AcmeOrderStatus;
import de.trustable.ca3s.core.repository.AcmeOrderRepository;
import org.jose4j.jwt.consumer.JwtContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.UriComponentsBuilder;

import de.trustable.ca3s.core.domain.ACMEAccount;
import de.trustable.ca3s.core.domain.AcmeChallenge;
import de.trustable.ca3s.core.domain.enumeration.ChallengeStatus;
import de.trustable.ca3s.core.repository.AcmeChallengeRepository;
import de.trustable.ca3s.core.service.dto.acme.ChallengeResponse;
import de.trustable.ca3s.core.service.dto.acme.problem.AcmeProblemException;
import de.trustable.ca3s.core.service.dto.acme.problem.ProblemDetail;
import de.trustable.ca3s.core.service.util.ACMEUtil;
import de.trustable.ca3s.core.service.util.PreferenceUtil;
import org.xbill.DNS.*;

import javax.validation.constraints.NotNull;

import static org.xbill.DNS.Name.*;
import static org.xbill.DNS.Type.TXT;
import static org.xbill.DNS.Type.string;


@Controller
@RequestMapping("/acme/{realm}/challenge")
public class ChallengeController extends ACMEController {

    private static final Logger LOG = LoggerFactory.getLogger(ChallengeController.class);

    public static final Name ACME_CHALLENGE_PREFIX = fromConstantString("_acme-challenge");

    @Value("${ca3s.acme.reject.get:true}")
    boolean rejectGet;

    private final AcmeChallengeRepository challengeRepository;

    private final AcmeOrderRepository orderRepository;

	private final PreferenceUtil preferenceUtil;

    private final SimpleResolver dnsResolver;

    public ChallengeController(AcmeChallengeRepository challengeRepository,
                               AcmeOrderRepository orderRepository,
                               PreferenceUtil preferenceUtil,
                               @Value("${ca3s.dns.server:}") String resolverHost,
                               @Value("${ca3s.dns.port:53}") int resolverPort) throws UnknownHostException {
        this.challengeRepository = challengeRepository;
        this.orderRepository = orderRepository;
        this.preferenceUtil = preferenceUtil;

        this.dnsResolver = new SimpleResolver(resolverHost);
        this.dnsResolver.setPort(resolverPort);
        LOG.info("Applying default DNS resolver {}", this.dnsResolver.getAddress());
    }

    @RequestMapping(value = "/{challengeId}", method = GET, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getChallenge(@PathVariable final long challengeId) {

	  	LOG.debug("Received Challenge request ");

	    final HttpHeaders additionalHeaders = buildNonceHeader();

        if( rejectGet ){
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).headers(additionalHeaders).build();
        }

        Optional<AcmeChallenge> challengeOpt = challengeRepository.findById(challengeId);
		if(!challengeOpt.isPresent()) {
		    return ResponseEntity.notFound().headers(additionalHeaders).build();
		}else {
			AcmeChallenge challengeDao = challengeOpt.get();

			LOG.debug( "returning challenge {}", challengeDao.getId());

			ChallengeResponse challenge = buildChallengeResponse(challengeDao);

			if(challengeDao.getStatus() == ChallengeStatus.VALID ) {
				URI authUri = locationUriOfAuthorization(challengeDao.getAcmeAuthorization().getAcmeAuthorizationId(), fromCurrentRequestUri());
			    additionalHeaders.set("Link", "<" + authUri.toASCIIString() + ">;rel=\"up\"");
			    return ok().headers(additionalHeaders).body(challenge);
			}else {
			    return ok().headers(additionalHeaders).body(challenge);
			}
		}

    }

    @RequestMapping(value = "/{challengeId}", method = POST, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JOSE_JSON_VALUE)
    public ResponseEntity<?> postChallenge(@RequestBody final String requestBody,
          @PathVariable final long challengeId, @PathVariable final String realm) {

        LOG.debug("Received Challenge request ");

        try {
            JwtContext context = jwtUtil.processFlattenedJWT(requestBody);

            ACMEAccount acctDao = checkJWTSignatureForAccount(context, realm);

            final HttpHeaders additionalHeaders = buildNonceHeader();

            Optional<AcmeChallenge> challengeOpt = challengeRepository.findById(challengeId);
            if(!challengeOpt.isPresent()) {
                return ResponseEntity.notFound().headers(additionalHeaders).build();
            }else {
                AcmeChallenge challengeDao = challengeOpt.get();

                AcmeOrder order = challengeDao.getAcmeAuthorization().getOrder();

                if(!order.getAccount().getAccountId().equals(acctDao.getAccountId())) {
                    LOG.warn("Account of signing key {} does not match account id {} associated to given challenge{}", acctDao.getAccountId(), challengeDao.getAcmeAuthorization().getOrder().getAccount().getAccountId(), challengeId);
                    final ProblemDetail problem = new ProblemDetail(ACMEUtil.MALFORMED, "Account / Auth mismatch",
                            BAD_REQUEST, "", ACMEController.NO_INSTANCE);
                    throw new AcmeProblemException(problem);
                }

                LOG.debug( "checking challenge {}", challengeDao.getId());

                boolean solved = false;
                ChallengeStatus newChallengeState = null;
                if( AcmeChallenge.CHALLENGE_TYPE_HTTP_01.equals(challengeDao.getType())) {
                    if (checkChallengeHttp(challengeDao)) {
                        newChallengeState = ChallengeStatus.VALID;
                        solved = true;
                    } else {
                        newChallengeState = ChallengeStatus.INVALID;
                    }
                }else if( AcmeChallenge.CHALLENGE_TYPE_DNS_01.equals(challengeDao.getType())){
                    if (checkChallengeDNS(challengeDao)) {
                        newChallengeState = ChallengeStatus.VALID;
                        solved = true;
                    } else {
                        newChallengeState = ChallengeStatus.INVALID;
                    }
                }else{
                    LOG.warn("Unexpected type '{}' of challenge{}", challengeDao.getType(), challengeId);
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

                alignOrderState(order);

                ChallengeResponse challenge = buildChallengeResponse(challengeDao);

                if( solved) {
                    URI authUri = locationUriOfAuthorization(challengeDao.getAcmeAuthorization().getAcmeAuthorizationId(), fromCurrentRequestUri());
                    additionalHeaders.set("Link", "<" + authUri.toASCIIString() + ">;rel=\"up\"");
                }else {
                    LOG.warn("validation of challenge{} of type '{}' failed", challengeId, challengeDao.getType());
                }
                return ok().headers(additionalHeaders).body(challenge);
            }

        } catch (AcmeProblemException e) {
            return buildProblemResponseEntity(e);
        }
    }

    void alignOrderState(AcmeOrder orderDao){

        if( orderDao.getStatus().equals(AcmeOrderStatus.READY) ){
          LOG.info("order status already '{}', no re-check after challenge state change required", orderDao.getStatus() );
          return;
        }

        if( orderDao.getStatus() != AcmeOrderStatus.PENDING) {
          LOG.warn("unexpected order status '{}' (!= Pending), no re-check after challenge state change required", orderDao.getStatus() );
          return;
        }

        boolean orderReady = true;

        /*
        * check all authorizations having at least one successfully validated challenge
        */
        for (AcmeAuthorization authDao : orderDao.getAcmeAuthorizations()) {

            boolean authReady = false;
            for (AcmeChallenge challDao : authDao.getChallenges()) {
                if (challDao.getStatus() == ChallengeStatus.VALID) {
                    LOG.debug("challenge {} of type {} is valid ", challDao.getChallengeId(), challDao.getType());
                    authReady = true;
                    break;
                }
            }
            if (authReady) {
                LOG.debug("found valid challenge, authorization id {} is valid ", authDao.getAcmeAuthorizationId());
            } else {
                LOG.debug("no valid challange, authorization id {} and order {} fails ",
                    authDao.getAcmeAuthorizationId(), orderDao.getOrderId());
                orderReady = false;
                break;
            }
        }
        if( orderReady ){
          LOG.debug("order status set to READY" );
          orderDao.setStatus(AcmeOrderStatus.READY);
          orderRepository.save(orderDao);
        }
    }

    private boolean checkChallengeDNS(AcmeChallenge challengeDao) {

        String identifierValue = challengeDao.getValue();
        String token = challengeDao.getToken();

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
            LOG.info("Found no DNS entry solving '{}'", identifierValue);
            return false;
        } else {
            final boolean matchingDnsEntryFound = retrievedToken.stream().anyMatch(token::equals);
            if (matchingDnsEntryFound) {
                return true;
            } else {
                LOG.info("Did not find matching token '{}' in TXT record DNS response", token);
                return false;
            }
        }
    }

    /**
     * @param lookupResult Optional
     * @return Never <code>null</code>
     */
    private @NotNull List<String> extractTokenFrom(final Record[] lookupResult) {

        List<String> tokenList = new ArrayList<>();
        if( lookupResult != null) {
            for (Record record : lookupResult) {
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
			String[] parts = portList.split(",");
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

	    String token = challengeDao.getToken();
	    String pkThumbprint = challengeDao.getAcmeAuthorization().getOrder().getAccount().getPublicKeyHash();
        String expectedContent = token + '.' + pkThumbprint;

	    String fileNamePath = "/.well-known/acme-challenge/" + token;
	    String host = challengeDao.getAcmeAuthorization().getValue();

	    for( int port: ports) {

		    try {
				URL url = new URL("http", host, port, fileNamePath);
				LOG.debug("Opening connection to  : " + url);

				HttpURLConnection con = (HttpURLConnection) url.openConnection();

				// Just wait for two seconds
				con.setConnectTimeout((int) timeoutMilliSec);
				con.setReadTimeout((int) timeoutMilliSec);

				// optional default is GET
				con.setRequestMethod("GET");

				// add request header
				con.setRequestProperty("User-Agent", "CA3S_ACME");

				int responseCode = con.getResponseCode();
				LOG.debug("\nSending 'GET' request to URL : " + url);
				LOG.debug("Response Code : " + responseCode);

				if( responseCode != 200) {
					LOG.info("read challenge responded with unexpected code : " + responseCode);
					continue;
				}

				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				String actualContent = response.toString().trim();
				LOG.debug("read challenge response: " + actualContent);
				LOG.debug("expected content: '{}'", expectedContent);

				return ( expectedContent.equals( actualContent));

		    } catch(UnknownHostException uhe) {
				LOG.debug("unable to resolve hostname ", uhe);
				return false;
		    } catch(IOException ioe) {
				LOG.info("problem reading challenge response on {}:{} for {} : {}", host, port, challengeDao.getId(), ioe.getMessage());
				LOG.debug("exception occurred reading challenge response", ioe);
		    }
	    }
		return false;
	}

    ChallengeResponse buildChallengeResponse(final AcmeChallenge challengeDao){
        return new ChallengeResponse(challengeDao, locationUriOfChallenge(challengeDao.getId(), fromCurrentRequestUri()).toString());
    }

    private URI locationUriOfChallenge(final long challengeId, final UriComponentsBuilder uriBuilder) {
	    return challengeResourceUriBuilderFrom(uriBuilder.path("../..")).path("/").path(Long.toString(challengeId)).build().normalize().toUri();
	}

    private URI locationUriOfAuthorization(final long authorizationId, final UriComponentsBuilder uriBuilder) {
	    return authorizationResourceUriBuilderFrom(uriBuilder.path("../..")).path("/").path("..").path("/").path(Long.toString(authorizationId)).build().normalize().toUri();
	}

}
