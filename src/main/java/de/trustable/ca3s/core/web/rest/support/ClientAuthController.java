package de.trustable.ca3s.core.web.rest.support;

import de.trustable.ca3s.core.security.jwt.JWTFilter;
import de.trustable.ca3s.core.security.jwt.TokenProvider;
import de.trustable.ca3s.core.service.dto.UserLoginData;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.service.util.UserUtil;
import de.trustable.ca3s.core.web.rest.JWTToken;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Iterator;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/publicapi")
public class ClientAuthController {

    private final Logger LOG = LoggerFactory.getLogger(ClientAuthController.class);

    final private CertificateUtil certificateUtil;

    final private TokenProvider tokenProvider;
    final private UserUtil userUtil;

    public ClientAuthController(CertificateUtil certificateUtil, TokenProvider tokenProvider, UserUtil userUtil) {
        this.certificateUtil = certificateUtil;
        this.tokenProvider = tokenProvider;
        this.userUtil = userUtil;
    }


    @CrossOrigin(methods = {POST, GET})
    @RequestMapping(value = "/clientAuth",   method = {POST, GET})
    public ResponseEntity checkUserCertificate(HttpServletRequest request,
                                               HttpSession httpSession) {

        for (Iterator<String> it = httpSession.getAttributeNames().asIterator(); it.hasNext(); ) {
            String att = it.next();
            LOG.info("att {} : {}", att, httpSession.getAttribute(att));
        }

        HttpHeaders httpHeaders = new HttpHeaders();

        X509Certificate[] certs = (X509Certificate[])request.getAttribute("javax.servlet.request.X509Certificate");

        // from Spring Boot 3 onwards
        //        request.getAttribute("jakarta.servlet.request.X509Certificate")

        if( certs.length == 0) {
            LOG.warn("no client certificate at client auth endpoint");
            return ResponseEntity.notFound().headers(httpHeaders).build();
        }

        try {
            JcaX509ExtensionUtils util = new JcaX509ExtensionUtils();
            SubjectKeyIdentifier ski = util.createSubjectKeyIdentifier(certs[0].getPublicKey());
            String b46Ski = Base64.encodeBase64String(ski.getKeyIdentifier());

            String jwt = tokenProvider.createToken(certs[0].getSubjectX500Principal().getName(), b46Ski);
            httpHeaders.add(JWTFilter.CLIENT_CERTIFICATE_TOKEN, jwt);
            return new ResponseEntity<>(new JWTToken(jwt), HttpStatus.OK);

        } catch (GeneralSecurityException e) {
            LOG.info("problem processing client certificate", e);
        }
        return ResponseEntity.notFound().headers(httpHeaders).build();

    }
}
