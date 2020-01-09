package de.trustable.ca3s.core.bpmn.task;

import java.util.Date;
import java.util.Optional;

import org.bouncycastle.asn1.x509.CRLReason;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.repository.CAConnectorConfigRepository;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.service.util.CaConnectorAdapter;
import de.trustable.ca3s.core.service.util.ConfigUtil;
import de.trustable.ca3s.core.service.util.DateUtil;
import de.trustable.util.CryptoUtil;


@Service
public class CaBackendTask implements JavaDelegate {

	private static final Logger LOGGER = LoggerFactory.getLogger(CaBackendTask.class);

	@Autowired
	private CertificateRepository certificateRepository;

	@Autowired
	private CSRRepository csrRepository;

	@Autowired
	private CAConnectorConfigRepository caccRepo;

	@Autowired
	private ConfigUtil configUtil;
	
	@Autowired
	private CaConnectorAdapter caConnAdapter;

	@Autowired
	private CryptoUtil cryptoUtil;

	/**
	 * make a call to the CA sending the csr or revoking a given certificate
	 */
	@Transactional
	@Override
	public void execute(DelegateExecution execution) throws Exception{

		execution.setVariable("status", "Failed");
		execution.setVariable("certificateId", "");
		execution.setVariable("failureReason", "");

		String action = (String) execution.getVariable("action");
		LOGGER.debug("execution.getVariable('action') : " + action);

		if( caccRepo.count() == 0 ) {
			LOGGER.debug("CAConnectorConfig is empty");
		}

		String caConfigIdStr = execution.getVariable("caConfigId").toString();
		long caConfigId = Long.parseLong(caConfigIdStr);
		
		Optional<CAConnectorConfig> caConnOpt = caccRepo.findById(caConfigId);
		if (!caConnOpt.isPresent()) {
			execution.setVariable("failureReason", "certificate Id '" + caConfigId + "' not found.");
			return;
		}
		
		CAConnectorConfig caConfig = caConnOpt.get();
		
		if(caConfig == null) {
			LOGGER.debug("caName NOT set by calling BPNM process");
			
			caConfig = configUtil.getDefaultConfig();
			if(caConfig == null) {
				LOGGER.error("no default CA available");
				return;
			}else {
				LOGGER.debug("using '{}' as the default CA ", caConfig.getName());
			}
		}
		
		
		try {

			if ("Revoke".equals(action)) {

				String revokeCertIdStr = execution.getVariable("CertificateId").toString();
				long certificateId = Long.parseLong(revokeCertIdStr);
				LOGGER.debug("execution.getVariable('CertificateId') : " + certificateId);

				String revocationReasonStr = (String) execution.getVariable("RevocationReason");
				if (revocationReasonStr != null) {
					revocationReasonStr = revocationReasonStr.trim();
				}
				LOGGER.debug("execution.getVariable('RevocationReason') : " + revocationReasonStr);

				Optional<Certificate> certificateOpt = certificateRepository.findById(certificateId);

				if (!certificateOpt.isPresent()) {
					execution.setVariable("failureReason", "certificate Id '" + revokeCertIdStr + "' not found.");
					return;
				}

				Certificate certificateDao = certificateOpt.get();

				if (certificateDao.isRevoked()) {
					execution.setVariable("failureReason",
							"certificate with id '" + revokeCertIdStr + "' already revoked.");
				}

				CRLReason crlReason = cryptoUtil.crlReasonFromString(revocationReasonStr);

				String crlReasonStr = cryptoUtil.crlReasonAsString(crlReason);
				LOGGER.debug("crlReason : " + crlReasonStr);

				Date revocationDate = new Date();

				caConnAdapter.revokeCertificate(certificateDao, crlReason, revocationDate, caConfig);

				certificateDao.setRevoked(true);
				certificateDao.setRevokedSince( DateUtil.asLocalDate(revocationDate));
				certificateDao.setRevocationReason(crlReasonStr);
				certificateDao.setRevocationExecutionId(execution.getProcessInstanceId());

				execution.setVariable("certificateId", certificateDao.getId());
				execution.setVariable("status", "Revoked");

			} else {

//				String csrBase64 = (String) execution.getVariable("csrId");
//				LOGGER.debug("execution.getVariable('csr') : {} ", csrBase64);

				String csrIdString = execution.getVariable("csrId").toString();
				long csrId = Long.parseLong(csrIdString);


				Optional<CSR> csrOpt = csrRepository.findById(csrId);
				if (!csrOpt.isPresent()) {
					execution.setVariable("failureReason", "csr Id '" + csrId + "' not found.");
					return;
				}

				CSR csr = csrOpt.get();
				
				Certificate cert = caConnAdapter.signCertificateRequest(csr, caConfig);
				
				cert.setCreationExecutionId(execution.getProcessInstanceId());

				LOGGER.debug("certificateId " + cert.getId());

				execution.setVariable("certificateId", cert.getId());
				execution.setVariable("certificate", cert.getContent());
				execution.setVariable("status", "Created");
			}

		} catch (Exception e) {
			execution.setVariable("failureReason", e.getMessage());
			LOGGER.info("signCertificateRequest failed", e);
		}

	}

}
