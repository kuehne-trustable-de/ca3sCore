package de.trustable.ca3s.core.web.rest.support;

import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CertificateAttribute;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.security.jwt.JWTFilter;
import de.trustable.ca3s.core.service.dto.UserLoginData;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.service.util.UserUtil;
import de.trustable.ca3s.core.web.rest.JWTToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Iterator;

@RestController
@RequestMapping("/publicapi")
public class ClientAuthController {

    private final Logger LOG = LoggerFactory.getLogger(ClientAuthController.class);

    final private CertificateUtil certificateUtil;
    final private UserUtil userUtil;

    public ClientAuthController(CertificateUtil certificateUtil, UserUtil userUtil) {
        this.certificateUtil = certificateUtil;
        this.userUtil = userUtil;
    }
/*
    @RequestMapping(value="/clientAuth", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> corsOptions() {

        LOG.info("calling Options ...");

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Access-Control-Allow-Origin", "https://localhost:8443");
        httpHeaders.add("Access-Control-Allow-Methods", "POST");

        return ResponseEntity
            .ok()
            .headers(httpHeaders)
            .build();
    }

 */
    @CrossOrigin()
    @RequestMapping(value = "/clientAuth")
    public ResponseEntity checkUserCertificate(@Valid @RequestBody UserLoginData userLoginData,
                                               HttpServletRequest request,
                                               HttpSession httpSession) {

        for (Iterator<String> it = httpSession.getAttributeNames().asIterator(); it.hasNext(); ) {
            String att = it.next();
            LOG.info("att {} : {}", att, httpSession.getAttribute(att));
        }

        HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.add("Access-Control-Allow-Origin", "https://localhost:8443");
//        httpHeaders.add("Access-Control-Allow-Methods", "POST");

        X509Certificate[] certs = (X509Certificate[])request.getAttribute("javax.servlet.request.X509Certificate");

        // from Spring Boot 3 onwards
        //        request.getAttribute("jakarta.servlet.request.X509Certificate")

        if( certs.length == 0) {
            LOG.warn("no client certificate at client auth endpoiint");
            return ResponseEntity.notFound().headers(httpHeaders).build();
        }

        try {
            Certificate certificate = certificateUtil.getCertificateByX509(certs[0]);
            if( !certificate.isActive() || certificate.isRevoked() ){
                return ResponseEntity.badRequest().headers(httpHeaders).build();
            }

            String userId = certificateUtil.getCertAttribute(certificate, CertificateAttribute.ATTRIBUTE_USER_CLIENT_CERT);
            User user = userUtil.getUserByLogin( userLoginData.getLogin());

            if (userId != null && user != null &&
                Long.parseLong(userId) == user.getId()) {

                String jwt = userUtil.validateCredentials( userLoginData);
                httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
//                return new ResponseEntity<>(new JWTToken(jwt), httpHeaders, HttpStatus.OK);
                return new ResponseEntity<>(new JWTToken(jwt), HttpStatus.OK);

            }

        } catch (GeneralSecurityException e) {
            LOG.info("problem finding client certificate", e);
        }
        return ResponseEntity.notFound().headers(httpHeaders).build();

    }
}
