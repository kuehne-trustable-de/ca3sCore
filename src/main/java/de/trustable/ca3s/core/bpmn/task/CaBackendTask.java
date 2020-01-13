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

				Certificate revokeCert = (Certificate)execution.getVariable("certificate");
				
				if( revokeCert == null ) {
					String revokeCertIdStr = execution.getVariable("certificateId").toString();
					
					long certificateId = -1;
					try {
						certificateId = Long.parseLong(revokeCertIdStr);
						LOGGER.debug("execution.getVariable('certificateId') : " + certificateId);
						
						Optional<Certificate> certificateOpt = certificateRepository.findById(certificateId);

						if (!certificateOpt.isPresent()) {
							execution.setVariable("failureReason", "certificate Id '" + revokeCertIdStr + "' not found.");
							return;
						}

						revokeCert = certificateOpt.get();

					}catch( NumberFormatException nfe) {
						String msg = "unparsable cert id '"+revokeCertIdStr+"'";
						LOGGER.warn(msg);
						execution.setVariable("failureReason", msg);
						return;
					}
				}
				
				String revocationReasonStr = (String) execution.getVariable("revocationReason");
				if (revocationReasonStr != null) {
					revocationReasonStr = revocationReasonStr.trim();
				}
				LOGGER.debug("execution.getVariable('revocationReason') : " + revocationReasonStr);


				if (revokeCert.isRevoked()) {
					execution.setVariable("failureReason",
							"certificate with id '" + revokeCert.getId() + "' already revoked.");
				}

				CRLReason crlReason = cryptoUtil.crlReasonFromString(revocationReasonStr);

				String crlReasonStr = cryptoUtil.crlReasonAsString(crlReason);
				LOGGER.debug("crlReason : " + crlReasonStr);

				Date revocationDate = new Date();

				caConnAdapter.revokeCertificate(revokeCert, crlReason, revocationDate, caConfig);

				revokeCert.setRevoked(true);
				revokeCert.setRevokedSince( DateUtil.asLocalDate(revocationDate));
				revokeCert.setRevocationReason(crlReasonStr);
				revokeCert.setRevocationExecutionId(execution.getProcessInstanceId());

				execution.setVariable("status", "Revoked");

			} else {

//				String csrBase64 = (String) execution.getVariable("csrId");
//				LOGGER.debug("execution.getVariable('csr') : {} ", csrBase64);

				execution.setVariable("certificateId", "");

				CSR csr = (CSR)execution.getVariable("csr");
				if( csr == null) {
					String csrIdString = execution.getVariable("csrId").toString();
					long csrId = Long.parseLong(csrIdString);
	
	
					Optional<CSR> csrOpt = csrRepository.findById(csrId);
					if (!csrOpt.isPresent()) {
						execution.setVariable("failureReason", "csr Id '" + csrId + "' not found.");
						return;
					}
	
					csr = csrOpt.get();
				}
				
				Certificate cert = caConnAdapter.signCertificateRequest(csr, caConfig);
				
				cert.setCreationExecutionId(execution.getProcessInstanceId());

				LOGGER.debug("certificateId " + cert.getId());

				execution.setVariable("certificateId", cert.getId());
				execution.setVariable("certificate", cert);
				execution.setVariable("status", "Created");
			}

		} catch (Exception e) {
			execution.setVariable("failureReason", e.getMessage());
			LOGGER.info("signCertificateRequest failed", e);
		}

	}

}
