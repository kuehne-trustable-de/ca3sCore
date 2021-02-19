package de.trustable.ca3s.core.service.util;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.trustable.ca3s.core.domain.CsrAttribute;
import de.trustable.ca3s.core.repository.CsrAttributeRepository;
import de.trustable.ca3s.core.domain.dto.NamedValues;
import de.trustable.ca3s.core.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.audit.listener.AuditApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.Pipeline;
import de.trustable.ca3s.core.domain.enumeration.PipelineType;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.util.CryptoUtil;
import de.trustable.util.Pkcs10RequestHolder;

@Service
public class CertificateProcessingUtil {

	private final Logger LOG = LoggerFactory.getLogger(CertificateProcessingUtil.class);


	@Autowired
	private CryptoUtil cryptoUtil;

    @Autowired
    private CSRUtil csrUtil;

	@Autowired
	private CSRRepository csrRepository;

    @Autowired
    private CsrAttributeRepository csrAttRepository;

    @Autowired
    private CertificateRepository certificateRepository;

	@Autowired
	private BPMNUtil bpmnUtil;

	@Autowired
	private PipelineUtil pvUtil;

    @Autowired
    private AuditService auditService;


	/**
	 *
	 * @param csrAsPem				certificate signing request in PEM format
	 * @param requestorName			requestorName
	 * @param requestAuditType		requestAuditType
	 * @param certificateAuditType	certificateAuditType
	 * @param requestorComment		requestorComment
	 * @param pipeline				pipeline
	 * @return certificate
	 */
	public Certificate processCertificateRequest(final String csrAsPem, final String requestorName, final String requestAuditType, final String certificateAuditType, String requestorComment, Pipeline pipeline )  {

		CSR csr = buildCSR(csrAsPem, requestorName, requestAuditType, requestorComment, pipeline );
		if( csr == null) {
			LOG.info("building CSR failed");
		}
		return processCertificateRequest(csr, requestorName, certificateAuditType, pipeline );

	}

	/**
	 *
	 * @param csrAsPem				certificate signing request in PEM format
	 * @param requestorName			requestorName
	 * @param requestAuditType		requestAuditType
	 * @param requestorComment		requestorComment
	 * @param pipeline				pipeline
	 * @return csr
	 */
	public CSR buildCSR(final String csrAsPem, final String requestorName, final String requestAuditType, String requestorComment, Pipeline pipeline )  {

	    List<String> messageList = new ArrayList<>();
        NamedValues[] nvArr = new NamedValues[0];
	    return buildCSR(csrAsPem, requestorName, requestAuditType, requestorComment, pipeline, nvArr, messageList );
	}

	/**
	 *
	 * @param csrAsPem				certificate signing request in PEM format
	 * @param requestorName			requestorName
	 * @param requestAuditType		requestAuditType
	 * @param requestorComment		requestorComment
	 * @param pipeline				pipeline
	 * @param messageList			messageList
	 * @return csr
	 */
	public CSR buildCSR(final String csrAsPem, final String requestorName, final String requestAuditType, String requestorComment, Pipeline pipeline, NamedValues[] nvArr, List<String> messageList )  {

		CSR csr;
		Pkcs10RequestHolder p10ReqHolder;

		try {
			p10ReqHolder = cryptoUtil.parseCertificateRequest(csrAsPem);

			if( pipeline == null) {
				LOG.debug("CSR requested without pipeline given!", new Exception());
				csr = csrUtil.buildCSR(csrAsPem, requestorName, p10ReqHolder, PipelineType.WEB, null);
			}else {
				csr = csrUtil.buildCSR(csrAsPem, requestorName, p10ReqHolder, pipeline);
			}


			csr.setRequestorComment(requestorComment);
			csrRepository.save(csr);

			for( NamedValues nvs: nvArr){
			    for( String value: nvs.getValues()){
                    CsrAttribute csrAttr = new CsrAttribute();
                    csrAttr.setCsr(csr);
                    csrAttr.setName(nvs.getName());
                    csrAttr.setValue(value);
                    csr.getCsrAttributes().add(csrAttr);
                }
            }

            csrAttRepository.saveAll(csr.getCsrAttributes());

            auditService.createAuditTraceRequest(CryptoUtil.limitLength(requestorName, 50), "User",requestAuditType, csr);

			LOG.debug("csr contains #{} CsrAttributes, #{} RequestAttributes and #{} RDN", csr.getCsrAttributes().size(), csr.getRas().size(), csr.getRdns().size());
			for(de.trustable.ca3s.core.domain.RDN rdn:csr.getRdns()) {
				LOG.debug("RDN contains #{}", rdn.getRdnAttributes().size());
			}
		} catch (GeneralSecurityException | IOException e) {
			LOG.warn("problem building a CSR for requestor '"+requestorName+"'failed", e);
			return null;
		}

		if( pvUtil.isPipelineRestrictionsResolved(pipeline, p10ReqHolder, messageList)) {
			return csr;
		} else{
			String msg = "certificate request " + csr.getId() + " rejected";
			if( !messageList.isEmpty()) {
				msg += ", validation of restriction failed: '" + messageList.get(0) + "'";
			}

			if( messageList.size() > 1) {
				msg += ", " + (messageList.size() - 1) + " more failures.";
			}

			LOG.info("Restrictions failed {}", msg);

			HashMap<String, Object> messageMap = new HashMap<>();
			for(String msgItem: messageList) {
				messageMap.put("RequestRestriction", CryptoUtil.limitLength(msgItem, 250) );
			}

            auditService.createAuditTraceCsrRestrictionFailed(csr);
		}

		return null;
	}

	/**
	 * @param csr					certificate signing request as CSR object
	 * @param requestorName			requestorName
	 * @param certificateAuditType 	certificateAuditType
	 * @param pipeline				pipeline
	 * @return certificate
	 */
	public Certificate processCertificateRequest(CSR csr, final String requestorName, final String certificateAuditType, Pipeline pipeline )  {


		if( csr == null) {
			LOG.warn("creation of certificate requires a csr!");
			return null;
		}

		boolean bApprovalRequired = false;

		if( pipeline != null) {
			bApprovalRequired = pipeline.isApprovalRequired();
		}

		if( bApprovalRequired ){
			LOG.debug("defering certificate creation for csr #{}", csr.getId());
		} else {

			Certificate cert = bpmnUtil.startCertificateCreationProcess(csr);
			if(cert != null) {
				certificateRepository.save(cert);

                auditService.createAuditTraceCertificateCreated(certificateAuditType, cert);

				return cert;
			} else {
				LOG.warn("creation of certificate requested by {} failed ", requestorName);
			}
		}

		return null;
	}

}
