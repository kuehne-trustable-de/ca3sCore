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

package de.trustable.ca3s.core.web.rest.support;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.io.IOException;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.*;

import de.trustable.ca3s.core.config.CryptoConfiguration;
import de.trustable.ca3s.core.domain.Tenant;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.security.AuthoritiesConstants;
import de.trustable.ca3s.core.security.SecurityUtils;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.util.UserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;

import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.service.dto.acme.problem.AcmeProblemException;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.service.util.CryptoService;
import de.trustable.ca3s.core.service.util.ProtectedContentUtil;
import de.trustable.ca3s.core.web.rest.acme.AcmeController;

@Controller
@RequestMapping("/publicapi")
public class CertificateDownloadController {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateDownloadController.class);

    private boolean chainIncludeRoot = true;

    private final CryptoConfiguration cryptoConfiguration;

    private final CertificateRepository certificateRepository;

    private final CertificateUtil certUtil;

    private final ProtectedContentUtil protContentUtil;

    private final UserUtil userUtil;

    private final AuditService auditService;

    private final String certificateStoreIsolation;

    private final boolean pkcs12LogDownload;


    public CertificateDownloadController(CryptoConfiguration cryptoConfiguration,
                                         CertificateRepository certificateRepository,
                                         CertificateUtil certUtil,
                                         ProtectedContentUtil protContentUtil,
                                         UserUtil userUtil, AuditService auditService,
                                         @Value("${ca3s.ui.certificate-store.isolation:none}")String certificateStoreIsolation,
                                         @Value("${ca3s.ui.pkcs12.log.download:true}") boolean pkcs12LogDownload) {
        this.cryptoConfiguration = cryptoConfiguration;
        this.certificateRepository = certificateRepository;
        this.certUtil = certUtil;
        this.protContentUtil = protContentUtil;
        this.userUtil = userUtil;
        this.auditService = auditService;
        this.certificateStoreIsolation = certificateStoreIsolation;
        this.pkcs12LogDownload = pkcs12LogDownload;
    }

    /**
     * Public certificate download endpoint providing DER format
     *
     * @param certId the internal certificate id
     * @return the binary certificate
     */
    @RequestMapping(value = "/certPKIX/{certId}/{filename}",
        method = GET,
        produces = AcmeController.APPLICATION_PKIX_CERT_VALUE)
    public ResponseEntity<byte[]> getCertificatePKIX(@PathVariable final long certId, @PathVariable final String filename) throws NotFoundException {

        LOG.info("Received certificate download request (PKIX) for id {} as file '{}' ", certId, filename);

        if (SecurityContextHolder.getContext() == null) {
            throw new NotFoundException("Authentication required");
        }

        try {
            return buildByteArrayResponseForId(certId, AcmeController.APPLICATION_PKIX_CERT_VALUE, "", filename);
        } catch (HttpClientErrorException | AcmeProblemException | GeneralSecurityException e) {
            throw new NotFoundException(e.getMessage());
        }

    }


    /**
     * Public certificate download endpoint providing PEM format including the certificate chain
     *
     * @param certId the internal certificate id
     * @return the PEM-encoded certificate chain
     */
    @RequestMapping(value = "/certPEMChain/{certId}/{filename}", method = GET)
    public ResponseEntity<?> getCertificatePEMChain(@PathVariable final long certId, @PathVariable final String filename) {

        LOG.info("Received certificate download request (PEM with chain) for id {} as file '{}'", certId, filename);
        return buildCertResponseForId(certId, AcmeController.APPLICATION_PEM_CERT_CHAIN_VALUE, filename);
    }

    /**
     * Public certificate download endpoint providing PEM format including the certificate chain
     *
     * @param certId the internal certificate id
     * @return the PEM-encoded certificate chain
     */
    @RequestMapping(value = "/certPEMPart/{certId}/{filename}", method = GET)
    public ResponseEntity<?> getCertificatePEMPart(@PathVariable final long certId, @PathVariable final String filename) {

        LOG.info("Received certificate download request (PEM with partial chain) for id {} as file '{}'", certId, filename);
        return buildCertResponseForId(certId, AcmeController.APPLICATION_X_PEM_CERT_CHAIN_VALUE, filename);
    }

    /**
     * Public certificate download endpoint providing PEM format including the certificate chain
     *
     * @param certId the internal certificate id
     * @return the PEM-encoded certificate chain
     */
    @RequestMapping(value = "/certPEMFull/{certId}/{filename}", method = GET)
    public ResponseEntity<?> getCertificatePEMFull(@PathVariable final long certId, @PathVariable final String filename) {

        LOG.info("Received certificate download request (PEM with full chain) for id {} as file '{}'", certId, filename);
        return buildCertResponseForId(certId, AcmeController.APPLICATION_PEM_CERT_CHAIN_VALUE, filename);
    }

    /**
     * Public certificate download endpoint providing PEM format
     *
     * @param certId the internal certificate id
     * @return the PEM-encoded certificate
     */
    @RequestMapping(value = "/certPEM/{certId}/{filename}", method = GET)
    public ResponseEntity<?> getCertificatePEM(@PathVariable final long certId, @PathVariable final String filename) {

        LOG.info("Received certificate download request (PEM) for id {} as file '{}'", certId, filename);
        return buildCertResponseForId(certId, AcmeController.APPLICATION_PEM_CERT_VALUE, filename);
    }

    /**
     * Public certificate download endpoint
     *
     * @param certId the internal certificate id
     * @param accept the description of the requested format
     * @return the certificate in the requested encoded form
     */
    @RequestMapping(value = "/cert/{certId}", method = GET)
    public ResponseEntity<?> getCertificate(@PathVariable final long certId,
                                            @RequestHeader(name = "Accept", defaultValue = AcmeController.APPLICATION_PKIX_CERT_VALUE) final String accept) {

        LOG.info("Received certificate request for id {}", certId);

        return buildCertResponseForId(certId, accept, "cert_" + certId + ".cer");
    }


    /**
     * Keystore download endpoint
     *
     * @param certId   the internal certificate id
     * @param filename the requested file name, for logging purposes only
     * @param alias    the identification id within the keystore
     * @param accept   the description of the requested format
     * @return the certificate in the requested encoded form
     * @throws NotFoundException
     */
    @RequestMapping(value = "/keystore/{certId}/{filename}/{alias}",
        method = GET,
        produces = AcmeController.APPLICATION_PKCS12_VALUE)
    public ResponseEntity<byte[]> getKeystore(@PathVariable final long certId,
                                              @PathVariable final String filename,
                                              @PathVariable final String alias,
                                              @RequestHeader(name = "Accept", defaultValue = AcmeController.APPLICATION_PKCS12_VALUE) final String accept) throws NotFoundException, UnauthorizedException {

        LOG.info("Received keystore request for id '{}' for filename '{}' with alias '{}'", certId, filename, alias);

        try {
            return buildByteArrayResponseForId(certId, accept, alias, filename);
        } catch (AccessControlException ace) {
            throw new UnauthorizedException(ace.getMessage());
        } catch (HttpClientErrorException | AcmeProblemException | GeneralSecurityException e) {
            throw new NotFoundException(e.getMessage());
        }
    }

    /**
     * @param certId
     * @param accept
     * @param filename
     * @return
     * @throws HttpClientErrorException
     * @throws AcmeProblemException
     */
    public ResponseEntity<?> buildCertResponseForId(final long certId, final String accept, String filename)
        throws HttpClientErrorException, AcmeProblemException {

        Optional<Certificate> certOpt = certificateRepository.findById(certId);

        if (!certOpt.isPresent()) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        } else {
            Certificate certDao = certOpt.get();
            checkTenant(certDao);

            final HttpHeaders headers = new HttpHeaders();
            headers.set("content-disposition", "inline; filename=\"" + filename + "\"");
//			headers.set("content-length", String.valueOf(2000));
            ResponseEntity<?> resp = buildCertifcateResponse(accept, certDao, headers);

            if (resp == null) {
                String msg = "problem returning certificate with accepting type " + accept;
                LOG.info(msg);

                return ResponseEntity.badRequest().build();
            }

            return resp;
        }
    }

    /**
     * @param certId
     * @param accept
     * @param alias
     * @param filename
     * @return
     * @throws HttpClientErrorException
     * @throws AcmeProblemException
     * @throws GeneralSecurityException
     */
    public ResponseEntity<byte[]> buildByteArrayResponseForId(final long certId, final String accept, final String alias, String filename)
        throws HttpClientErrorException, AcmeProblemException, GeneralSecurityException {

        Optional<Certificate> certOpt = certificateRepository.findById(certId);

        if (!certOpt.isPresent()) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        } else {
            Certificate certDao = certOpt.get();

            checkTenant(certDao);

            final HttpHeaders headers = new HttpHeaders();
            headers.set("content-disposition", "inline; filename=\"" + filename + "\"");

            if (AcmeController.APPLICATION_PKIX_CERT_VALUE.equalsIgnoreCase(accept)) {
                return buildPkixCertResponse(certDao, headers);
            } else if (AcmeController.APPLICATION_PKCS12_VALUE.equalsIgnoreCase(accept)) {
                return buildPKCS12Response(certDao, alias, headers);
            }

            throw new HttpClientErrorException(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }
    }

    private void checkTenant(Certificate cert) {

        if("none".equalsIgnoreCase(certificateStoreIsolation)){
            return;
        }

        if( !userUtil.isAdministrativeUser() ){
            User currentUser = userUtil.getCurrentUser();
            Tenant tenant = currentUser.getTenant();
            if( tenant == null ) {
                // null == default tenant
            } else if( !tenant.equals(cert.getTenant())){
                if( cert.isEndEntity()) {
                    LOG.info("user [{}] tried to download EE certificate [{}] of tenant [{}]",
                        currentUser.getLogin(), cert.getId(), tenant.getLongname());
                    throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
                }
            }
        }
    }

    /**
     * @param accept
     * @param certDao
     * @param headers
     */
    public ResponseEntity<?> buildCertifcateResponse(final String accept, Certificate certDao, final HttpHeaders headers) {

        if ("*/*".equalsIgnoreCase(accept)) {
            return buildPEMResponse(certDao, headers, AcmeController.APPLICATION_PEM_CERT_CHAIN, true, false);
//		}else if(AcmeController.APPLICATION_PKIX_CERT_VALUE.equalsIgnoreCase(accept)){
//			return buildPkixCertResponse(certDao, headers);
        } else if (AcmeController.APPLICATION_X_PEM_CERT_CHAIN_VALUE.equalsIgnoreCase(accept)) {
            return buildPEMResponse(certDao, headers, AcmeController.APPLICATION_PEM_CERT_CHAIN, true, false);
        } else if (AcmeController.APPLICATION_PEM_CERT_CHAIN_VALUE.equalsIgnoreCase(accept)) {
            return buildPEMResponse(certDao, headers, AcmeController.APPLICATION_PEM_CERT_CHAIN, true, true);
        } else if (AcmeController.APPLICATION_PEM_CERT_VALUE.equalsIgnoreCase(accept)) {
            return buildPEMResponse(certDao, headers, AcmeController.APPLICATION_PEM_CERT, false, false);
//		}else if(AcmeController.APPLICATION_PKCS12_VALUE.equalsIgnoreCase(accept)){
//			return buildPKCS12Response(certDao, headers);
        }

        LOG.info("unexpected accept type {}", accept);

        return null;
    }


    private ResponseEntity<?> buildPEMResponse(Certificate certDao, final HttpHeaders headers,
                                               MediaType mediaType, boolean includeChain, boolean includeRoot) {
        LOG.info("building PEM certificate response");

        try {
            String resultPem = "";

            if (includeChain) {
                List<Certificate> chain = certUtil.getCertificateChain(certDao);
                boolean isFirst = true;
                for (Iterator<Certificate> it = chain.iterator(); it.hasNext(); ) {
                    Certificate chainCertDao = it.next();
                    // skip the last cert, the root
                    if (it.hasNext() || includeRoot) {
                        // adde some descriptive text into the PEM file
                        if (isFirst) {
                            isFirst = false;
                        } else {
                            resultPem += "# issued by\n";
                        }
                        resultPem += "# Subject: " + chainCertDao.getSubject() + "\n";
                        resultPem += "# Issuer: " + chainCertDao.getIssuer() + "\n";
                        resultPem += "# Serial: " + chainCertDao.getSerial() + "\n";
                        resultPem += "# valid from : " + chainCertDao.getValidFrom() + " to " + chainCertDao.getValidTo() + "\n";
                        resultPem += "#" + "\n";
                        resultPem += chainCertDao.getContent();
                    }
                }
            } else {
                resultPem += certDao.getContent();
            }

            LOG.debug("returning cert and issuer : \n" + resultPem);
            headers.set("content-length", String.valueOf(resultPem.getBytes().length));
            return ResponseEntity.ok().contentType(mediaType).headers(headers).body(resultPem.getBytes());

        } catch (GeneralSecurityException ge) {
            String msg = "problem building certificate chain";
            LOG.info(msg, ge);
            return ResponseEntity.badRequest().build();
        }

    }


    private ResponseEntity<byte[]> buildPkixCertResponse(Certificate certDao, final HttpHeaders headers) throws GeneralSecurityException {
        LOG.info("building PKIX certificate response");

        try {
            X509Certificate x509Cert = CryptoService.convertPemToCertificate(certDao.getContent());
            byte[] contentBytes = x509Cert.getEncoded();
            headers.set("content-length", String.valueOf(contentBytes.length));
            return ResponseEntity.ok().contentType(AcmeController.APPLICATION_PKIX_CERT).headers(headers).body(contentBytes);
        } catch (GeneralSecurityException gse) {
            LOG.info("problem downloading certificate content for cert id " + certDao.getId(), gse);
            throw gse;
        }
    }


    private ResponseEntity<byte[]> buildPKCS12Response(Certificate certDao, final String alias, final HttpHeaders headers) throws GeneralSecurityException {
        LOG.info("building PKCS12 container response");

        String entryAlias = "entry";
        if (alias != null && !alias.trim().isEmpty()) {
            entryAlias = alias;
        }

        CSR csr = certDao.getCsr();
        if (csr == null) {
            throw new GeneralSecurityException("problem downloading keystore content for cert id " + certDao.getId() + ": no csr object available ");
        }

        if (SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.ADMIN) ||
            SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.RA_OFFICER)) {
            LOG.debug("Admins and RA Officers are allowed to download P12 files");
        } else if (SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.DOMAIN_RA_OFFICER)) {
            LOG.debug("Admins and RA Officers are allowed to download P12 files");
        } else {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userName = auth.getName();
            if (userName == null) {
                throw new GeneralSecurityException("problem downloading keystore content for csr id " + csr.getId() + ":  user name not available");
            }
            if (!userName.equals(csr.getRequestedBy())) {
                throw new AccessControlException("problem downloading keystore content for csr id " + csr.getId() + ": user does not match initial requestor");
            }
        }

        boolean keyEx = false;
        List<String> keyExHeaderList = headers.get("X_keyEx");
        if (keyExHeaderList != null && !keyExHeaderList.isEmpty()) {
            keyEx = Boolean.parseBoolean(keyExHeaderList.get(0));
        }
        LOG.info("PKCS12: keyEx flag: {} ", keyEx);

        String passwordProtectionAlgo = cryptoConfiguration.getDefaultPBEAlgo();
        List<String> algoHeaderList = headers.get("X_pbeAlgo");
        if (algoHeaderList != null && !algoHeaderList.isEmpty()) {
            String reqAlgo = algoHeaderList.get(0).trim();
            if (cryptoConfiguration.isPBEAlgoAllowed(reqAlgo)) {
                passwordProtectionAlgo = reqAlgo;
            } else {
                LOG.info("requested PKCS12 pbe algo '{}' not in list of valid algos, using default '{}' ", reqAlgo, passwordProtectionAlgo);
            }
        }
        LOG.info("PKCS12: using algo {} ", passwordProtectionAlgo);

        try {
            byte[] contentBytes = certUtil.getContainerBytes(certDao, entryAlias, csr, passwordProtectionAlgo);
            headers.set("content-length", String.valueOf(contentBytes.length));

            if( pkcs12LogDownload ){
                auditService.saveAuditTrace(auditService.createAuditTracePKCS12CertificateDownload(certDao));
            }

            return ResponseEntity.ok().contentType(AcmeController.APPLICATION_PKCS12).headers(headers).body(contentBytes);

        } catch (IOException gse) {
            throw new GeneralSecurityException("problem downloading keystore content for cert id " + certDao.getId());
        }
    }
}

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Not Found")
class NotFoundException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -7873233893252750875L;

    public NotFoundException() {
    }

    public NotFoundException(String message) {
        super(message);
    }
}

@ResponseStatus(code = HttpStatus.UNAUTHORIZED, reason = "Unauthorized")
class UnauthorizedException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -7873296392052750875L;

    public UnauthorizedException() {
    }

    public UnauthorizedException(String message) {
        super(message);
    }
}
