package de.trustable.ca3s.core.web.rest.support;

import de.trustable.ca3s.core.config.CryptoConfiguration;
import de.trustable.ca3s.core.domain.*;
import de.trustable.ca3s.core.domain.enumeration.CsrUsage;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.repository.PipelineRepository;
import de.trustable.ca3s.core.security.AuthoritiesConstants;
import de.trustable.ca3s.core.security.SecurityUtils;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.dto.ContainerSecret;
import de.trustable.ca3s.core.service.dto.KeyAlgoLengthOrSpec;
import de.trustable.ca3s.core.service.dto.NamedValues;
import de.trustable.ca3s.core.service.dto.TypedValue;
import de.trustable.ca3s.core.service.dto.acme.problem.AcmeProblemException;
import de.trustable.ca3s.core.service.impl.CertificateServiceImpl;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.service.util.CryptoService;
import de.trustable.ca3s.core.service.util.PipelineUtil;
import de.trustable.ca3s.core.service.util.UserUtil;
import de.trustable.ca3s.core.web.rest.acme.AcmeController;
import org.bouncycastle.operator.OperatorCreationException;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping("/publicapi")
public class CertificateDownloadController {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateDownloadController.class);

    private final CryptoConfiguration cryptoConfiguration;

    private final CertificateServiceImpl certificateServiceImpl;
    private final CertificateRepository certificateRepository;

    private final CertificateUtil certUtil;

    private final UserUtil userUtil;

    private final AuditService auditService;

    private final String certificateStoreIsolation;

    private final boolean pkcs12LogDownload;

    private final String pkcs12SecretRegexp;
    private final Pattern pkcs12SecretPattern;
    private final PipelineRepository pipelineRepository;


    public CertificateDownloadController(CryptoConfiguration cryptoConfiguration,
                                         CertificateServiceImpl certificateServiceImpl, CertificateRepository certificateRepository,
                                         CertificateUtil certUtil,
                                         UserUtil userUtil,
                                         AuditService auditService,
                                         @Value("${ca3s.ui.certificate-store.isolation:none}")String certificateStoreIsolation,
                                         @Value("${ca3s.ui.pkcs12.log.download:true}") boolean pkcs12LogDownload,
                                         @Value("${ca3s.pkcs12.secret.regexp:^(?=.*\\d)(?=.*[a-z]).{6,100}$}") String pkcs12SecretRegexp,
                                         PipelineRepository pipelineRepository) {

        this.cryptoConfiguration = cryptoConfiguration;
        this.certificateServiceImpl = certificateServiceImpl;
        this.certificateRepository = certificateRepository;
        this.certUtil = certUtil;
        this.userUtil = userUtil;
        this.auditService = auditService;
        this.certificateStoreIsolation = certificateStoreIsolation;
        this.pkcs12LogDownload = pkcs12LogDownload;
        this.pkcs12SecretRegexp = pkcs12SecretRegexp;
        this.pkcs12SecretPattern = Pattern.compile(pkcs12SecretRegexp);

        this.pipelineRepository = pipelineRepository;
    }

    @RequestMapping(value ={"/certPKIX/{certId}/{filename}","/certPEMChain/{certId}/{filename}"}, method = GET)
    public RedirectView certForward(@PathVariable final long certId, @PathVariable final String filename) {
        RedirectView redirectView = new RedirectView();
        redirectView.setStatusCode(HttpStatus.FOUND);
        redirectView.setUrl("/cert-info?certificateId=" + certId);
        return redirectView;
    }

    @RequestMapping(value ="/cert/{certId}}", method = GET)
    public RedirectView certForward(@PathVariable final long certId) {
        RedirectView redirectView = new RedirectView();
        redirectView.setStatusCode(HttpStatus.FOUND);
        redirectView.setUrl("/cert-info?certificateId=" + certId);
        return redirectView;
    }


    /**
     * Public certificate download endpoint providing DER format
     *
     * @param ski the internal certificate id
     * @return the binary certificate
     */
    @RequestMapping(value = "/certPKIX/{certId}/ski/{ski}/{filename}",
        method = GET,
        produces = AcmeController.APPLICATION_PKIX_CERT_VALUE)
    public ResponseEntity<byte[]> getCertificatePKIX(@PathVariable final long certId,
                                                     @PathVariable final String ski,
                                                     @PathVariable final String filename)
        throws NotFoundException, UnauthorizedException {

        LOG.info("Received certificate download request (PKIX) for ski {} as file '{}' ", ski, filename);

        try {
            return buildByteArrayResponseForId(getCertificateByIdSKI(certId, ski),
                AcmeController.APPLICATION_PKIX_CERT_VALUE, "", filename);
        } catch (HttpClientErrorException | AcmeProblemException | GeneralSecurityException e) {
            throw new NotFoundException(e.getMessage());
        }

    }

    Certificate getCertificateByIdSKI(final long certId, final String ski) throws NotFoundException {


        Optional<Certificate> certOpt = certificateRepository.findById(certId);
        if( (ski != null ) && certOpt.isPresent()) {
            Certificate cert = certOpt.get();
            List<String> skiList = certUtil.getCertAttributes(cert, CertificateAttribute.ATTRIBUTE_SKI);
            String normalizedSki = ski.replace('-', '+').replace('_', '/');
            if (skiList.contains(normalizedSki)) {
                return cert;
            }
        }
        throw new NotFoundException("certificate id " + certId + " with ski '" + ski + "' not found");
    }

    /**
     * Public certificate download endpoint providing PEM format including the certificate chain
     *
     * @param ski the internal certificate ski
     * @return the PEM-encoded certificate chain
     */
    @RequestMapping(value = "/certPEMChain/{certId}/ski/{ski}/{filename}", method = GET)
    public ResponseEntity<?> getCertificatePEMChain(@PathVariable final long certId,
                                                    @PathVariable final String ski,
                                                    @PathVariable final String filename) throws NotFoundException {

        LOG.info("Received certificate download request (PEM with chain) for ski {} as file '{}'", ski, filename);

        return buildCertResponseForId(getCertificateByIdSKI(certId, ski),
            AcmeController.APPLICATION_PEM_CERT_CHAIN_VALUE, filename);
    }

    /**
     * Public certificate download endpoint providing PEM format including the certificate chain
     *
     * @param ski the internal certificate ski
     * @return the PEM-encoded certificate chain
     */
    @RequestMapping(value = "/certPEMPart/{certId}/ski/{ski}/{filename}", method = GET)
    public ResponseEntity<?> getCertificatePEMPart(@PathVariable final long certId,
                                                   @PathVariable final String ski,
                                                   @PathVariable final String filename) throws NotFoundException {

        LOG.info("Received certificate download request (PEM with partial chain) for ski {} as file '{}'", ski, filename);
        return buildCertResponseForId(getCertificateByIdSKI(certId, ski),
            AcmeController.APPLICATION_X_PEM_CERT_CHAIN_VALUE, filename);
    }

    /**
     * Public certificate download endpoint providing PEM format including the certificate chain
     *
     * @param ski the internal certificate ski
     * @return the PEM-encoded certificate chain
     */
    @RequestMapping(value = "/certPEMFull/{certId}/ski/{ski}/{filename}", method = GET)
    public ResponseEntity<?> getCertificatePEMFull(@PathVariable final long certId,
                                                   @PathVariable final String ski,
                                                   @PathVariable final String filename) throws NotFoundException {

        LOG.info("Received certificate download request (PEM with full chain) for id {} as file '{}'", ski, filename);
        return buildCertResponseForId(getCertificateByIdSKI(certId, ski),
            AcmeController.APPLICATION_PEM_CERT_CHAIN_VALUE, filename);
    }

    /**
     * Public certificate download endpoint providing PEM format
     *
     * @param ski the internal certificate ski
     * @return the PEM-encoded certificate
     */
    @RequestMapping(value = "/certPEM/{certId}/ski/{ski}/{filename}", method = GET)
    public ResponseEntity<?> getCertificatePEM(@PathVariable final long certId,
                                               @PathVariable final String ski,
                                               @PathVariable final String filename) throws NotFoundException {

        LOG.info("Received certificate download request (PEM) for ski {} as file '{}'", ski, filename);
        return buildCertResponseForId(getCertificateByIdSKI(certId, ski),
            AcmeController.APPLICATION_PEM_CERT_VALUE, filename);
    }


    /**
     * Public certificate download endpoint
     *
     * @param ski the internal certificate ski
     * @param accept the description of the requested format
     * @return the certificate in the requested encoded form
     */
    @RequestMapping(value = "/cert/ski/{ski}", method = GET)
    public ResponseEntity<?> getCertificate(@PathVariable final String ski,
                                            @RequestHeader(name = "Accept", defaultValue = AcmeController.APPLICATION_PKIX_CERT_VALUE) final String accept) throws NotFoundException {

        LOG.info("Received certificate request for id {}", ski);
        List<Certificate> certificateList = certificateRepository.findBySKI(ski);
        if( certificateList.isEmpty()){
            throw new NotFoundException("ski '" + ski + "' not found");
        }
        Certificate certificate = certificateList.get(0);
        return buildCertResponseForId(certificate.getId(),
            accept, "cert_" + certificate.getSubject() + ".cer");
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
                                              @RequestHeader(name = "Accept", defaultValue = AcmeController.APPLICATION_PKCS12_VALUE) final String accept)
        throws NotFoundException, UnauthorizedException {

        LOG.info("Received keystore request for id '{}' for filename '{}' with alias '{}'", certId, filename, alias);

        try {
            return buildByteArrayResponseForId(certId, accept, alias, filename);
        } catch (HttpClientErrorException | AcmeProblemException | GeneralSecurityException e) {
            throw new NotFoundException(e.getMessage());
        }
    }

    /**
     * Keystore download endpoint
     *
     * @param login    the presumed user name. Will be checked against JWT user
     * @param filename the requested file name, for logging purposes only
     * @param containerSecret   the PKCS12 protection secret
     * @return the certificate in the requested encoded form
     * @throws NotFoundException
     */
    @RequestMapping(value = "/clientAuthKeystore/{login}/{filename}",
        method = POST,
        produces = AcmeController.APPLICATION_PKCS12_VALUE)
    public ResponseEntity<byte[]> getClientAuthKeystore(@PathVariable final String login,
                                                                @PathVariable final String filename,
                                                                @RequestBody final ContainerSecret containerSecret )
        throws NotFoundException, UnauthorizedException {

        // @ToDo drop secret
        LOG.info("Received clientAuth keystore request for login '{}' for filename '{}' with secret {}", login, filename,
            containerSecret.getSecret());

        if( !pkcs12SecretPattern.matcher(containerSecret.getSecret()).matches()){
            LOG.warn("PKCS12 secret does not match pattern '" + pkcs12SecretRegexp + "'");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            CSR csr = buildCSRForClientCert(login, containerSecret.getSecret());

            Optional<Certificate> certOpt = Optional.of(csr.getCertificate());

            final HttpHeaders headers = new HttpHeaders();
            headers.add("x-cert-id", certOpt.get().getId().toString());
            byte[] p12ContenBytes = buildByteArrayResponseForId(certOpt,
                AcmeController.APPLICATION_PKCS12_VALUE,
                "alias", filename).getBody();

            return ResponseEntity.ok().headers(headers).body(p12ContenBytes);

        } catch (HttpClientErrorException | AcmeProblemException | GeneralSecurityException | IOException |
                 OperatorCreationException e) {
            LOG.warn("Problem building PKCS12 container", e);

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

        if (certOpt.isEmpty()) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        } else {
            Certificate certDao = certOpt.get();
            return buildCertResponseForId(certDao, accept, filename);
        }
    }

    /**
     * @param certDao
     * @param accept
     * @param filename
     * @return
     * @throws HttpClientErrorException
     * @throws AcmeProblemException
     */
    public ResponseEntity<?> buildCertResponseForId(final Certificate certDao, final String accept, String filename)
        throws HttpClientErrorException, AcmeProblemException {

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
        throws HttpClientErrorException, AcmeProblemException, GeneralSecurityException, UnauthorizedException {

        Optional<Certificate> certOpt = certificateRepository.findById(certId);
        return buildByteArrayResponseForId(certOpt,accept,alias,filename);
    }

    /**
     * @param login
     * @param secret
     * @return
     * @throws HttpClientErrorException
     * @throws AcmeProblemException
     * @throws GeneralSecurityException
     */
    public CSR buildCSRForClientCert(final String login, final String secret)
        throws HttpClientErrorException, AcmeProblemException,
        GeneralSecurityException, UnauthorizedException,
        IOException, OperatorCreationException {

        User currentUser = userUtil.getCurrentUser();
        if (currentUser == null ||
            !currentUser.getLogin().equals(login)) {
            throw new UnauthorizedException("User not authenticated");
        }
        NamedValues[] certAttributeArr = new NamedValues[1];
        certAttributeArr[0] = new NamedValues();
        certAttributeArr[0].setName("CN");
        TypedValue typedValue = new TypedValue(login);
        certAttributeArr[0].setValues(new TypedValue[]{typedValue});

        List<Pipeline> pipelineList = pipelineRepository.findByAttributePresent(PipelineUtil.CAN_ISSUE_2_FACTOR_CLIENT_CERTS);
        if( pipelineList.isEmpty() ){
            throw new UnauthorizedException("No pipeline defined for second factor client certificate creation");
        }

        return certificateServiceImpl.createServersideKeyAndCertificate(Optional.of(pipelineList.get(0)),
            KeyAlgoLengthOrSpec.RSA_4096,
            certAttributeArr,
            new NamedValues[0],
            CsrUsage.TLS_CLIENT,
            currentUser,
            secret,
            "",
            true,
            false,
            "",
            false);

    }

    public ResponseEntity<byte[]> buildByteArrayResponseForId(final Optional<Certificate> certOpt,
                                                              final String accept,
                                                              final String alias,
                                                              final String filename)
        throws HttpClientErrorException, AcmeProblemException, GeneralSecurityException, UnauthorizedException {

        if (!certOpt.isPresent()) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        } else {

            return buildByteArrayResponseForId(certOpt.get(),
            accept,
            alias,
            filename);
        }
    }
    public ResponseEntity<byte[]> buildByteArrayResponseForId(final Certificate cert,
                                                              final String accept,
                                                              final String alias,
                                                              final String filename)
        throws HttpClientErrorException, AcmeProblemException, GeneralSecurityException, UnauthorizedException {

        checkTenant(cert);

        final HttpHeaders headers = new HttpHeaders();
        headers.set("content-disposition", "inline; filename=\"" + filename + "\"");

        if (AcmeController.APPLICATION_PKIX_CERT_VALUE.equalsIgnoreCase(accept)) {
            return buildPkixCertResponse(cert, headers);
        } else if (AcmeController.APPLICATION_PKCS12_VALUE.equalsIgnoreCase(accept)) {
            return buildPKCS12Response(cert, alias, headers);
        }

        throw new HttpClientErrorException(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    private void checkTenant(Certificate cert) {

        if("none".equalsIgnoreCase(certificateStoreIsolation)){
            return;
        }

        if( !userUtil.isRaRoleUser() ){
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


    private ResponseEntity<byte[]> buildPKCS12Response(Certificate certDao, final String alias, final HttpHeaders headers) throws GeneralSecurityException, UnauthorizedException {
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
                throw new UnauthorizedException("problem downloading keystore content for csr id " + csr.getId() + ": user does not match initial requestor");
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
