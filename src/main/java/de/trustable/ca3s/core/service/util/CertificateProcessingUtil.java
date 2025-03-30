package de.trustable.ca3s.core.service.util;

import de.trustable.ca3s.core.domain.*;
import de.trustable.ca3s.core.domain.enumeration.CsrStatus;
import de.trustable.ca3s.core.domain.enumeration.PipelineType;
import de.trustable.ca3s.core.exception.CAFailureException;
import de.trustable.ca3s.core.exception.KeyApplicableException;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.repository.CsrAttributeRepository;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.dto.ARARestriction;
import de.trustable.ca3s.core.service.dto.NamedValues;
import de.trustable.ca3s.core.service.dto.TypedValue;
import de.trustable.util.CryptoUtil;
import de.trustable.util.Pkcs10RequestHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class CertificateProcessingUtil {

	private final Logger LOG = LoggerFactory.getLogger(CertificateProcessingUtil.class);

	private final CryptoUtil cryptoUtil;
    private final CSRUtil csrUtil;
	private final CSRRepository csrRepository;
    private final CsrAttributeRepository csrAttRepository;
    private final CertificateRepository certificateRepository;
	private final BPMNUtil bpmnUtil;
	private final PipelineUtil pvUtil;
    private final AuditService auditService;

    public CertificateProcessingUtil(CryptoUtil cryptoUtil,
                                     CSRUtil csrUtil,
                                     CSRRepository csrRepository,
                                     CsrAttributeRepository csrAttRepository,
                                     CertificateRepository certificateRepository,
                                     BPMNUtil bpmnUtil,
                                     PipelineUtil pvUtil,
                                     AuditService auditService) {
        this.cryptoUtil = cryptoUtil;
        this.csrUtil = csrUtil;
        this.csrRepository = csrRepository;
        this.csrAttRepository = csrAttRepository;
        this.certificateRepository = certificateRepository;
        this.bpmnUtil = bpmnUtil;
        this.pvUtil = pvUtil;
        this.auditService = auditService;
    }


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
	public CSR buildCSR(final String csrAsPem, final String requestorName, final String requestAuditType,
                        String requestorComment, Pipeline pipeline )  {

        List<String> messageList = new ArrayList<>();
        NamedValues[] nvArr = new NamedValues[0];

        try {
            return buildCSR(csrAsPem, requestorName, requestAuditType, requestorComment, pipeline,
                null,
                nvArr, messageList);
        } catch (KeyApplicableException e) {
            return null;
        }
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
	public CSR buildCSR(final String csrAsPem, final String requestorName, final String requestAuditType,
                        String requestorComment, Pipeline pipeline,
                        AcmeOrder acmeOrder,
                        NamedValues[] araArr,
                        List<String> messageList ) throws KeyApplicableException {

		CSR csr;
		Pkcs10RequestHolder p10ReqHolder;
		try {
			p10ReqHolder = cryptoUtil.parseCertificateRequest(csrAsPem);

            if( !pvUtil.isPublicKeyApplicable(pipeline, p10ReqHolder, acmeOrder, messageList) ){
                throw new KeyApplicableException();
            }

            ARARestriction[] restrictionArr = new ARARestriction[0];
			if( pipeline == null) {
//				LOG.debug("CSR requested without pipeline given!", new Exception());
				csr = csrUtil.buildCSR(csrAsPem, requestorName, p10ReqHolder, PipelineType.WEB, null);
			}else {
                csr = csrUtil.buildCSR(csrAsPem, requestorName, p10ReqHolder, pipeline);
                restrictionArr = pvUtil.initAraRestrictions(pipeline);
            }


			csr.setRequestorComment(requestorComment);
			csrRepository.save(csr);

            if( araArr != null) {
                for( ARARestriction restriction : restrictionArr) {
                    Optional<NamedValues> optionalNamedValues = findNVSByName(araArr, restriction.getName());
                    if(optionalNamedValues.isPresent()){
                        NamedValues nvs = optionalNamedValues.get();
                        for (TypedValue typedValue : nvs.getValues()) {
                            if( typedValue.getValue() != null || !typedValue.getValue().isEmpty()) {
                                CsrAttribute csrAttr = new CsrAttribute();
                                csrAttr.setCsr(csr);
                                csrAttr.setName(CsrAttribute.ARA_PREFIX + nvs.getName());
                                csrAttr.setValue(typedValue.getValue());
                                csr.getCsrAttributes().add(csrAttr);
                            }
                        }
                    }
                }
            }

            csrAttRepository.saveAll(csr.getCsrAttributes());

            auditService.saveAuditTrace(auditService.createAuditTraceRequest(requestAuditType, csr));

			LOG.debug("csr contains #{} CsrAttributes, #{} RequestAttributes and #{} RDN", csr.getCsrAttributes().size(), csr.getRas().size(), csr.getRdns().size());
			for(de.trustable.ca3s.core.domain.RDN rdn:csr.getRdns()) {
				LOG.debug("RDN contains #{}", rdn.getRdnAttributes().size());
			}
		} catch (GeneralSecurityException | IOException e) {
			LOG.warn("problem building a CSR for requestor '"+requestorName+"'failed", e);
			return null;
		}

		if( pvUtil.isPipelineRestrictionsResolved(pipeline, p10ReqHolder, araArr, messageList)) {
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

			csrUtil.setStatusAndRejectionReason(csr, CsrStatus.REJECTED, msg);
            auditService.saveAuditTrace(auditService.createAuditTraceCsrRestrictionFailed(csr));
		}

		return null;
	}

    private Optional<NamedValues> findNVSByName(NamedValues[] araArr, final String name) {
        return Arrays.stream(araArr).filter(nvs -> name.equals(nvs.getName())).findFirst();
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
            LOG.debug("deferring certificate creation for csr #{}", csr.getId());
        } else {

            return processCertificateRequestImmediate(csr, requestorName, certificateAuditType );
        }

        return null;
    }

    /**
     * @param csr					certificate signing request as CSR object
     * @param requestorName			requestorName
     * @param certificateAuditType 	certificateAuditType
     * @return certificate
     */
    public Certificate processCertificateRequestImmediate(CSR csr, final String requestorName, final String certificateAuditType )  {


        if( csr == null) {
            LOG.warn("creation of certificate requires a csr!");
            return null;
        }

        try {
            Certificate cert = bpmnUtil.startCertificateCreationProcess(csr);

            if(cert != null) {
                certificateRepository.save(cert);

                auditService.saveAuditTrace(auditService.createAuditTraceCertificate(certificateAuditType, cert));

                return cert;
            } else {
                LOG.warn("creation of certificate requested by {} failed ", requestorName);
            }
        } catch( CAFailureException caFailureException){
            auditService.saveAuditTrace(auditService.createAuditTraceCsrSigningFailed(csr, caFailureException.getMessage()));

            caFailureException.printStackTrace();
            throw caFailureException;
        }

        return null;
    }

    public Certificate processCertificateRequest(CSR csr, final String requestorName, final String certificateAuditType, CAConnectorConfig caConfig )  {

        if( csr == null) {
            LOG.warn("creation of certificate requires a csr!");
            return null;
        }

        Certificate cert = bpmnUtil.startCertificateCreationProcess(csr, caConfig, null);

        if(cert != null) {
            certificateRepository.save(cert);

            auditService.saveAuditTrace(auditService.createAuditTraceCertificate(certificateAuditType, cert));

            return cert;
        } else {
            LOG.warn("creation of certificate requested by {} failed ", requestorName);
        }

        return null;
    }

}
