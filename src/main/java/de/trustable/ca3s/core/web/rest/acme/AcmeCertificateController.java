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
import de.trustable.ca3s.core.repository.AcmeOrderRepository;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.service.dto.acme.RevokeRequest;
import de.trustable.ca3s.core.service.dto.acme.problem.AcmeProblemException;
import de.trustable.ca3s.core.service.dto.acme.problem.ProblemDetail;
import de.trustable.ca3s.core.service.util.AcmeUtil;
import de.trustable.ca3s.core.service.util.BPMNUtil;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.asn1.x509.CRLReason;
import org.jose4j.jwt.consumer.JwtContext;
import org.jose4j.jwx.JsonWebStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.*;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;


@RestController
@RequestMapping("/acme/{realm}/cert")
public class AcmeCertificateController extends AcmeController {

    private static final Logger LOG = LoggerFactory.getLogger(AcmeCertificateController.class);

    private boolean chainIncludeRoot = true;

    final private CertificateRepository certificateRepository;
    final private CSRRepository csrRepository;


    final private CertificateUtil certificateUtil;

    final private AcmeOrderRepository acmeOrderRepository;

    final private BPMNUtil bpmnUtil;

    final private CertificateUtil certUtil;

    final private boolean certificateLocationBackwardCompat;

    public AcmeCertificateController(CertificateRepository certificateRepository, CSRRepository csrRepository, CertificateUtil certificateUtil, AcmeOrderRepository acmeOrderRepository,
                                     BPMNUtil bpmnUtil,
                                     CertificateUtil certUtil,
                                     @Value("${ca3s.acme.backward.certificate.location:false}") boolean certificateLocationBackwardCompat) {
        this.certificateRepository = certificateRepository;
        this.csrRepository = csrRepository;
        this.certificateUtil = certificateUtil;
        this.acmeOrderRepository = acmeOrderRepository;
        this.bpmnUtil = bpmnUtil;
        this.certUtil = certUtil;
        this.certificateLocationBackwardCompat = certificateLocationBackwardCompat;
    }


    @RequestMapping(value = "/{certId}", method = GET)
    public ResponseEntity<?> getCertificatePKIX(@PathVariable final long certId,
                                                @RequestHeader(name = "Accept", defaultValue = APPLICATION_PEM_CERT_CHAIN_VALUE) final String accept,
                                                @PathVariable final String realm,
                                                @RequestHeader(value = HEADER_X_CA3S_FORWARDED_HOST, required = false) String forwardedHost) {

        LOG.info("Received certificate request for id {}", certId);

        return buildCertResponseForId(certId, accept, realm, forwardedHost);
    }

    public ResponseEntity<?> buildCertResponseForId(final long certId, final String accept, final String realm, final String forwardedHost)
        throws HttpClientErrorException, AcmeProblemException {

        Optional<Certificate> certOpt = certificateRepository.findById(certId);

        if (certOpt.isEmpty()) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        } else {
            Certificate certDao = certOpt.get();

            final HttpHeaders headers = buildNonceHeader();
            if (certificateLocationBackwardCompat) {
                String certLocation = getEffectiveUriComponentsBuilder(realm, forwardedHost).build().toUriString();
                headers.add("location", certLocation);
                LOG.debug("added certificate location header '{}' for backward compatibility reasons.", certLocation);
            }

            ResponseEntity<?> resp = buildCertifcateResponse(accept, certDao, headers);

            if (resp == null) {
                String msg = "problem returning certificate with accepting type " + accept;
                LOG.info(msg);
                final ProblemDetail problem = new ProblemDetail(AcmeUtil.MALFORMED, msg,
                    UNSUPPORTED_MEDIA_TYPE, "", AcmeController.NO_INSTANCE);
                throw new AcmeProblemException(problem);
            }

            return resp;
        }
    }

    public ResponseEntity<?> buildCertifcateResponse(final String accept, Certificate certDao) {
        return buildCertifcateResponse(accept, certDao, new HttpHeaders());
    }

    /**
     * @param accept  what mime type to be served
     * @param certDao the certificate to serve
     * @param headers the list of response headers, completed with the certificate's mime type
     */
    public ResponseEntity<?> buildCertifcateResponse(final String accept, Certificate certDao, final HttpHeaders headers) {

        if (accept == null || accept.trim().isEmpty()) {
            return buildPEMResponse(certDao, headers);
        } else if (accept.contains("application/pkix-cert")) {
            return buildPkixCertResponse(certDao, headers);
        } else if (accept.contains("application/pem-certificate-chain")) {
            return buildPEMResponse(certDao, headers);
        } else if (accept.contains("application/pem-certificate")) {
            return buildPEMResponse(certDao, headers, false);
        } else if (accept.contains("*/*")) {
            return buildPEMResponse(certDao, headers);
        }

        LOG.info("unexpected accept type {}", accept);

        return null;
    }

    @RequestMapping(value = "/revoke", method = POST, consumes = APPLICATION_JOSE_JSON_VALUE)
    public ResponseEntity<?> revokeCertificate(@RequestBody final String requestBody, @PathVariable final String realm) {

        LOG.info("Received revoke request for certificate ");

        try {
            JwtContext context = jwtUtil.processFlattenedJWT(requestBody);
            JsonWebStructure webStruct = jwtUtil.getJsonWebStructure(context);

            RevokeRequest revokeReq = jwtUtil.getRevokeReq(context.getJwtClaims());
            X509Certificate x509CertPayload = certificateUtil.getCertifcateFromBase64(revokeReq.getCertificate());
            LOG.info("Revoke request for certificate {} ", x509CertPayload.getSubjectX500Principal().toString());

            // retrieve certificate object matching the revocation payload
            Certificate certDaoRevoke = certificateUtil.getCertificateByX509(x509CertPayload);
            if (certDaoRevoke == null) {
                LOG.warn("Certificate {} to be revoked not found in database", x509CertPayload.getSubjectX500Principal().toString());
                final ProblemDetail problem = new ProblemDetail(AcmeUtil.UNAUTHORIZED, "problem authenticating account / order / certificate for RevokeRequest",
                    BAD_REQUEST, "", AcmeController.NO_INSTANCE);
                throw new AcmeProblemException(problem);
            }

            String kid = jwtUtil.getKid(webStruct);
            if (kid == null) {
                LOG.info("Revocation authentication by JWK");
                Certificate certRequestSignature = null;
                try {
                    // retrieve public key from JWK header
                    PublicKey certificatePubKey = jwtUtil.getPublicKey(webStruct);
                    List<CSR> csrList = csrRepository.findByPublicKeyHash(cryptoUtil.getHashAsBase64(certificatePubKey.getEncoded()));
                    for (CSR csr : csrList) {
                        if (csr.getCertificate() != null) {
                            certRequestSignature = csr.getCertificate();
                            break;
                        }
                    }
                    if (certRequestSignature == null) {
                        final ProblemDetail problem = new ProblemDetail(AcmeUtil.UNAUTHORIZED, "Certificate used for request signature not found",
                            BAD_REQUEST, "", AcmeController.NO_INSTANCE);
                        throw new AcmeProblemException(problem);
                    }

                } catch (AcmeProblemException acmeProblemException) {
                    LOG.warn("Certificate revocation failed, neither KID nor JWK found in request", acmeProblemException);
                    final ProblemDetail problem = new ProblemDetail(AcmeUtil.UNAUTHORIZED, "Certificate revocation failed, neither KID nor JWK found in request",
                        BAD_REQUEST, "", AcmeController.NO_INSTANCE);
                    throw new AcmeProblemException(problem);
                }

                if (!Objects.equals(certDaoRevoke, certRequestSignature)) {
                    LOG.warn("Private key of certificate to be revoked did not sign the request");
                    final ProblemDetail problem = new ProblemDetail(AcmeUtil.UNAUTHORIZED, "Private key of certificate to be revoked did not sign the request",
                        BAD_REQUEST, "", AcmeController.NO_INSTANCE);
                    throw new AcmeProblemException(problem);
                }
            } else {

                LOG.info("Revocation authentication by KID");
                AcmeAccount acctDao = checkJWTSignatureForAccount(context, realm);

                // retrieve order
                Optional<AcmeOrder> orderOptional = acmeOrderRepository.findByCertificate(certDaoRevoke);
                if (orderOptional.isEmpty()) {
                    LOG.warn("No order found for certificate #{} ", certDaoRevoke.getId());
                    final ProblemDetail problem = new ProblemDetail(AcmeUtil.UNAUTHORIZED, "problem authenticating account / order / certificate for RevokeRequest",
                        BAD_REQUEST, "", AcmeController.NO_INSTANCE);
                    throw new AcmeProblemException(problem);
                }

                AcmeOrder order = orderOptional.get();
                if (!Objects.equals(order.getAccount(), acctDao)) {
                    LOG.warn("Order #{} related to certificate #{} does not belong to account #{}",
                        order.getId(),
                        certDaoRevoke.getId(),
                        acctDao.getId());
                    final ProblemDetail problem = new ProblemDetail(AcmeUtil.UNAUTHORIZED, "problem authenticating account / order / certificate for RevokeRequest",
                        BAD_REQUEST, "", AcmeController.NO_INSTANCE);
                    throw new AcmeProblemException(problem);
                }

                // match order and account
                if (acctDao.getAccountId() != Long.parseLong(certUtil.getCertAttribute(certDaoRevoke, CertificateAttribute.ATTRIBUTE_ACME_ACCOUNT_ID))) {
                    LOG.warn("Revoke request for certificate {} identified by account {} does not match cert's associated account {}",
                        x509CertPayload.getSubjectX500Principal().toString(),
                        acctDao.getAccountId(),
                        certUtil.getCertAttribute(certDaoRevoke, CertificateAttribute.ATTRIBUTE_ACME_ACCOUNT_ID));
                    final HttpHeaders headers = buildNonceHeader();
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).headers(headers).build();
                }
            }

            // check revocation status
            if (certDaoRevoke.isRevoked()) {
                final ProblemDetail problem = new ProblemDetail(AcmeUtil.ALREADY_REVOKED, "certificate already revoked",
                    BAD_REQUEST, "", AcmeController.NO_INSTANCE);
                throw new AcmeProblemException(problem);
            }

            // perform revocation
            revokeCertificate(certDaoRevoke, Integer.toString(revokeReq.getReason()));
            final HttpHeaders headers = buildNonceHeader();
            return ResponseEntity.ok().headers(headers).build();

        } catch (AcmeProblemException e) {
            return buildProblemResponseEntity(e);
        } catch (Exception e) {
            LOG.info("problem revoking certificate ", e);
            final ProblemDetail problem = new ProblemDetail(AcmeUtil.MALFORMED, "problem revoking certificate ",
                BAD_REQUEST, "", AcmeController.NO_INSTANCE);
            throw new AcmeProblemException(problem);
        }

    }

    private List<Certificate> findCertificatesInDatabase(X509Certificate x509Cert) {
        String tbsDigestBase64;
        try {
            tbsDigestBase64 = Base64.encodeBase64String(cryptoUtil.getSHA256Digest(x509Cert.getTBSCertificate())).toLowerCase();
        } catch (CertificateEncodingException | NoSuchAlgorithmException e) {
            LOG.info("problem selecting certificate for RevokeRequest", e);
            final ProblemDetail problem = new ProblemDetail(AcmeUtil.MALFORMED, "problem retrieving certificate for RevokeRequest",
                BAD_REQUEST, "", AcmeController.NO_INSTANCE);
            throw new AcmeProblemException(problem);
        }

        List<Certificate> certList = certificateRepository.findByTBSDigest(tbsDigestBase64);
        return certList;
    }

    /**
     * Retrieve a certificate as a PEM structure containing the complete chain
     * Bug in certbot: content type set to 'application/pkix-cert' despite containing a JWT in the request body, as usual.
     */
    @RequestMapping(value = "/{certId}", method = POST, consumes = {APPLICATION_JOSE_JSON_VALUE, APPLICATION_PKIX_CERT_VALUE})
    public ResponseEntity<?> retrieveCertificate(@RequestBody final String requestBody,
                                                 @RequestHeader(name = "Accept", defaultValue = APPLICATION_PEM_CERT_CHAIN_VALUE) final String accept,
                                                 @RequestHeader("Content-Type") final String contentType,
                                                 @PathVariable final long certId,
                                                 @PathVariable final String realm,
                                                 @RequestHeader(value = HEADER_X_CA3S_FORWARDED_HOST, required = false) String forwardedHost) {

        try {
            JwtContext context = jwtUtil.processFlattenedJWT(requestBody);

            AcmeAccount acctDao = checkJWTSignatureForAccount(context, realm);
            // check order for certificate matches account identified by JWT protecting key
            // ... or not, as certs a public ...

            LOG.info("Received certificate request for certifacte id {} of content-type {}, identified by account id {} ", certId, contentType, acctDao.getAccountId());

            return buildCertResponseForId(certId, accept, realm, forwardedHost);
/*
			List<Certificate> certList = certificateRepository.findByCertificateId(certId);

			if (certList.isEmpty()) {
	  		    return ResponseEntity.notFound().build();
			} else {
				Certificate certDao = certList.get(0);

				final HttpHeaders headers = buildNonceHeader();

				if ("application/pkix-cert".equalsIgnoreCase(accept) ) {
					LOG.debug("application/pkix-cert requested");
					return buildPkixCertResponse(certDao, headers);
				}else {
					LOG.debug("default handling for accept {}: returning PEM", accept);
		  			return buildPEMResponse(certDao, headers);
				}
			}
*/

        } catch (AcmeProblemException e) {
            return buildProblemResponseEntity(e);
        }

    }

    private ResponseEntity<byte[]> buildPkixCertResponse(Certificate certDao, final HttpHeaders headers) {
        LOG.info("building PKIX certificate response");

        byte[] certBytes = Base64.decodeBase64(certDao.getContent());

//		headers.setContentType(APPLICATION_PKIX_CERT);
//		ResponseEntity<byte[]> responseEntity = new ResponseEntity<byte[]>(certBytes, headers, HttpStatus.OK);

        return ResponseEntity.ok().contentType(APPLICATION_PKIX_CERT).headers(headers).body(certBytes);
    }

    private ResponseEntity<?> buildPEMResponse(Certificate certDao, final HttpHeaders headers) {
        return buildPEMResponse(certDao, headers, true);
    }

    private ResponseEntity<?> buildPEMResponse(Certificate certDao, final HttpHeaders headers, boolean includeChain) {
        LOG.info("building PEM certificate response");

        try {
            String resultPem = "";
            if (includeChain) {
                List<Certificate> chain = certUtil.getCertificateChain(certDao);

                for (Iterator<Certificate> it = chain.iterator(); it.hasNext(); ) {
                    Certificate chainCertDao = it.next();
                    // skip the last cert, the root
                    if (it.hasNext() || chainIncludeRoot) {
                        resultPem += chainCertDao.getContent();
                    }
                }
            } else {
                resultPem += certDao.getContent();
            }

            LOG.debug("returning cert and issuer : \n" + resultPem);
            return ResponseEntity.ok().contentType(APPLICATION_PEM_CERT_CHAIN).headers(headers).body(resultPem.getBytes());

        } catch (GeneralSecurityException ge) {
            String msg = "problem building certificate chain";
            LOG.info(msg, ge);
            final ProblemDetail problem = new ProblemDetail(AcmeUtil.MALFORMED, msg,
                INTERNAL_SERVER_ERROR, msg, AcmeController.NO_INSTANCE);
            throw new AcmeProblemException(problem);
        }

    }


    private void revokeCertificate(Certificate certDao, final String reason) throws Exception {


        if (certDao.isRevoked()) {
            LOG.warn("failureReason: " +
                "certificate with id '" + certDao.getId() + "' already revoked.");
        }

        CRLReason crlReason = cryptoUtil.crlReasonFromString(reason);

        String crlReasonStr = cryptoUtil.crlReasonAsString(crlReason);
        LOG.debug("crlReason : " + crlReasonStr);

        Date revocationDate = new Date();

        bpmnUtil.startCertificateRevocationProcess(certDao, crlReason, revocationDate);

        certDao.setActive(false);
        certDao.setRevoked(true);
        certDao.setRevokedSince(Instant.now());
        certDao.setRevocationReason(crlReasonStr);

        /*
         * @ todo
         */
        certDao.setRevocationExecutionId("39");

        certificateRepository.save(certDao);

    }

}
