package de.trustable.ca3s.core.service.est;

import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CertificateAttribute;
import de.trustable.ca3s.core.domain.Pipeline;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.service.util.CryptoService;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.cms.SignedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ESTService {
    private final Logger log = LoggerFactory.getLogger(ESTService.class);

    private final CertificateRepository certificateRepository;

    public ESTService(CertificateRepository certificateRepository) {
        this.certificateRepository = certificateRepository;
    }


    public List<X509Certificate> getESTRootCertificates(){
        List<X509Certificate> x509CertificateList = new ArrayList<>();

        List<Certificate> certList = certificateRepository.findByAttributeValue( CertificateAttribute.ATTRIBUTE_CA3S_ROOT, "true");
        for(Certificate certificate: certList) {
            try {
                X509Certificate x509Cert = CryptoService.convertPemToCertificate(certificate.getContent());
                x509CertificateList.add(x509Cert);
            } catch (GeneralSecurityException e) {
                log.info("problem handling internal root certificate: {}", e.getMessage());
            }
        }
        return x509CertificateList;
    }

    public ResponseEntity<?> enroll(HttpServletRequest request, Pipeline pipeline, byte[] csr) {
        List<X509Certificate> certList = new ArrayList<>();
        return buildPKCS7CertsResponse(certList);
    }

    public ResponseEntity<?> reenroll(HttpServletRequest request, Pipeline pipeline, byte[] csr) {
        List<X509Certificate> certList = new ArrayList<>();
        return buildPKCS7CertsResponse(certList);
    }

    public ResponseEntity<?> buildPKCS7CertsResponse(List<X509Certificate> x509CertificateList) {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Transfer-Encoding", "base64");

        try {
            ASN1EncodableVector certsVector = new ASN1EncodableVector();
            for (X509Certificate x509Certificate : x509CertificateList) {
                certsVector.add(org.bouncycastle.asn1.x509.Certificate.getInstance(x509Certificate.getEncoded()));
            }
            ASN1Set certSet = new DERSet(certsVector);

            SignedData sd = new SignedData(new DERSet(),
                new ContentInfo(CMSObjectIdentifiers.data, null),
                certSet,
                new DERSet(),
                new DERSet());

            ContentInfo ci = new ContentInfo(CMSObjectIdentifiers.signedData, sd);
            Base64 base64 = new Base64(78);
            return ResponseEntity.ok().headers(httpHeaders).body(base64.encode(ci.getEncoded("DER")));
        } catch (IOException | CertificateEncodingException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

}
