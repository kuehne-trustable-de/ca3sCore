package de.trustable.ca3s.core.web.rest.est;

import de.trustable.ca3s.core.domain.Pipeline;
import de.trustable.ca3s.core.service.est.ESTService;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.DERSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.List;

@RestController
@RequestMapping("/.well-known/est")
public class ESTController {

    private static final Logger LOG = LoggerFactory.getLogger(ESTController.class);

    private final ESTService estService;

    public ESTController(ESTService estService) {
        this.estService = estService;
    }

    @GetMapping(value = "/cacerts", produces = {"application/pkcs7-mime"} )
    public ResponseEntity<?> get_Cacerts() {

        Pipeline pipeline = getESTDefaultPipeline();
        return getCacerts(pipeline);
    }

    @GetMapping(value = "/{label}/cacerts", produces = {"application/pkcs7-mime"} )
    public ResponseEntity<?> getCacertsLabel(@PathVariable("label") String label) {
        Pipeline pipeline = getESTPipelineForLabel(label);
        return getCacerts(pipeline);
    }


    @GetMapping(value = "/csrattrs", produces = {"application/csrattrs"})
    public ResponseEntity<?> get_Csrattrs() {
        Pipeline pipeline = getESTDefaultPipeline();
        return getCsrattrs(pipeline);
    }

    @GetMapping(value = "/{label}/csrattrs", produces = {"application/csrattrs"})
    public ResponseEntity<?> getCsrattrsLabel(@PathVariable("label") String label) {
        Pipeline pipeline = getESTPipelineForLabel(label);
        return getCsrattrs(pipeline);
    }


    @GetMapping(value = "/fullcmc", consumes = {"application/pkcs7-mime; smime-type=CMC-request"})
    public ResponseEntity<Void> postFullCMCLabel(@RequestBody byte[] csr ) {
        Pipeline pipeline = getESTDefaultPipeline();
        return fullcmcLabel(pipeline, csr);
    }

    @GetMapping(value = "/{label}/fullcmc", consumes = {"application/pkcs7-mime; smime-type=CMC-request"})
    public ResponseEntity<Void> postFullCMCLabel(@PathVariable("label") String label, @RequestBody byte[] csr
    ) {
        Pipeline pipeline = getESTPipelineForLabel(label);
        return fullcmcLabel(pipeline, csr);
    }


    @PostMapping(value = "/serverkeygen", consumes = {"application/pkcs10"})
    public ResponseEntity<Void> postServerkeygenLabel( @RequestBody byte[] csr
    ) {
        Pipeline pipeline = getESTDefaultPipeline();
        return serverkeygenLabel(pipeline, csr);
    }

    @PostMapping(value = "/{label}/serverkeygen", consumes = {"application/pkcs10"})
    public ResponseEntity<Void> postServerkeygenLabel(@PathVariable("label") String label, @RequestBody byte[] csr
    ) {
        Pipeline pipeline = getESTPipelineForLabel(label);
        return serverkeygenLabel(pipeline, csr);
    }



    @PostMapping(value = "/simpleenroll",
        produces = {"application/pkcs7-mime; smime-type=certs-only"},
        consumes = {"application/pkcs10"})
    public ResponseEntity<?> postSimpleenroll(HttpServletRequest request, @RequestBody byte[] csr
    ) {
        Pipeline pipeline = getESTDefaultPipeline();
        return simpleenrollLabel(request,pipeline, csr);
    }


    @PostMapping(value = "/{label}/simpleenroll",
        produces = {"application/pkcs7-mime; smime-type=certs-only"},
        consumes = {"application/pkcs10"})
    public ResponseEntity<?> postSimpleenrollLabel(HttpServletRequest request, @PathVariable("label") String label, @RequestBody byte[] csr
    ) {
        Pipeline pipeline = getESTPipelineForLabel(label);
        return simpleenrollLabel(request, pipeline, csr);
    }


    @PostMapping(value = "/simplereenroll",
        produces = {"application/pkcs7-mime; smime-type=certs-only"},
        consumes = {"application/pkcs10"})
    public ResponseEntity<?> postSimplereenroll(HttpServletRequest request, @RequestBody byte[] csr
    ) {
        Pipeline pipeline = getESTDefaultPipeline();
        return simplereenrollLabel(request, pipeline, csr);
    }

    @PostMapping(value = "/{label}/simplereenroll",
        produces = {"application/pkcs7-mime; smime-type=certs-only"},
        consumes = {"application/pkcs10"})
    public ResponseEntity<?> postSimplereenrollLabel(HttpServletRequest request, @PathVariable("label") String label, @RequestBody byte[] csr
    ) {
        Pipeline pipeline = getESTPipelineForLabel(label);
        return simplereenrollLabel(request, pipeline, csr);
    }

    private Pipeline getESTPipelineForLabel(String label) {
        return null;
    }

    private Pipeline getESTDefaultPipeline() {
        return null;
    }

    private ResponseEntity<?> getCacerts(Pipeline pipeline) {
        List<X509Certificate> x509CertificateList = estService.getESTRootCertificates();
        return estService.buildPKCS7CertsResponse(x509CertificateList);
    }

    private ResponseEntity<?> getCsrattrs(Pipeline pipeline) {
        return buildCsrAttrsResponse();
    }

    private ResponseEntity<Void> fullcmcLabel(Pipeline pipeline, byte[] csr) {
        return ResponseEntity.status(501).build();
    }

    private ResponseEntity<Void> serverkeygenLabel(Pipeline pipeline, byte[] csr) {
        return ResponseEntity.status(501).build();
    }

    private ResponseEntity<?> simpleenrollLabel(HttpServletRequest request, Pipeline pipeline, byte[] csr) {
        return estService.enroll(request, pipeline, csr);
    }

    private ResponseEntity<?> simplereenrollLabel(HttpServletRequest request, Pipeline pipeline, byte[] csr) {
        return estService.reenroll(request, pipeline, csr);
    }

    ResponseEntity<?> buildCsrAttrsResponse() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Transfer-Encoding", "base64");
        ASN1EncodableVector csrAttrs = new ASN1EncodableVector();

        try {
        Base64 base64 = new Base64(78);
        return ResponseEntity.ok().headers(httpHeaders)
            .body(base64.encode(new DERSequence(csrAttrs).getEncoded("DER")));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

}
