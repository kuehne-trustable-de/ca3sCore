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
import de.trustable.ca3s.core.domain.enumeration.CAConnectorType;
import de.trustable.ca3s.core.repository.CAConnectorConfigRepository;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.service.adcs.ADCSConnector;
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
	private ADCSConnector adcsController;

//	@Autowired
//	private CaCmpConnector caCmpConnector;

	@Autowired
	private CryptoUtil cryptoUtil;

	/**
	 * make a call to the CA sending the csr or revoking a given certificate
	 */
	@Transactional
	@Override
	public void execute(DelegateExecution execution) throws Exception{

/*		
		variables.put("csrId", csr.getId());
		variables.put("caConfigId", caConfig.getId());
		variables.put("status", "Failed");
		variables.put("certificateId", certificateId);
		variables.put("failureReason", failureReason);
*/
		
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
		
/*		
		if( caName != null ) {
			LOGGER.debug("caName set by calling BPNM process to '{}'", caName);
			List<CAConnectorConfig> caConfigList = caccRepo. .findByName(caName);
			if( !caConfigList.isEmpty()) {
				caConfig = caConfigList.get(0);
				LOGGER.debug("workflow selected ca : " + caConfig.getName());
			}
			
			if(caConfig == null) {
				LOGGER.warn("unable to find CA identified by name '{}'", caName);
			}
		}
*/
		if(caConfig == null) {
			LOGGER.debug("caName NOT set by calling BPNM process");
			Iterable<CAConnectorConfig> configList = caccRepo.findAll();
			for( CAConnectorConfig cfg : configList ) {
				LOGGER.debug("checking '{}': isDefaultCA {}, isActive {}", cfg.getName(), cfg.isDefaultCA(), cfg.isActive());
				if( cfg.isDefaultCA() && cfg.isActive()) {
					caConfig = cfg;
					LOGGER.debug("using '{}' as the default CA ", cfg.getName());
					break;
				}
			}
			if(caConfig == null) {
				LOGGER.error("no default CA available");
				return;
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

				revokeCertificate(certificateDao, crlReason, revocationDate, caConfig);

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
				
				Certificate cert = signCertificateRequest(csr, caConfig);
				
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

	private Certificate signCertificateRequest(CSR csr, CAConnectorConfig caConfig ) throws Exception {

		if (caConfig == null) {
			throw new Exception("CA connector not selected !");
		}

		if (CAConnectorType.ADCS.equals(caConfig.getCaConnectorType())) {
			LOGGER.debug("CAConnectorType ADCS at " + caConfig.getCaUrl());
			return adcsController.signCertificateRequest(csr, caConfig);

		} else if (CAConnectorType.CMP.equals(caConfig.getCaConnectorType())) {
			LOGGER.debug("CAConnectorType CMP at " + caConfig.getCaUrl());
			throw new Exception("ca connector type '" + caConfig.getCaConnectorType() + "' not implemented, yet!");
/*
			String pemCert = caCmpConnector.signCertificateRequest(csr, caConfig.getSecret(), caConfig.getCaUrl(),
					caConfig.getName());
			return cryptoUtil.createCertificateDao(pemCert, csr, null);
*/			
		} else {
			throw new Exception("unexpected ca connector type '" + caConfig.getCaConnectorType() + "' !");
		}
	}

	private void revokeCertificate(Certificate certificateDao, CRLReason crlReason, Date revocationDate,
			CAConnectorConfig caConfig ) throws Exception {

		if (caConfig == null) {
			throw new Exception("CA connector not selected !");
		}

		if (CAConnectorType.ADCS.equals(caConfig.getCaConnectorType())) {
			LOGGER.debug("CAConnectorType ADCS at " + caConfig.getCaUrl());
			adcsController.revokeCertificate(certificateDao, crlReason, revocationDate, caConfig);

		} else if (CAConnectorType.CMP.equals(caConfig.getCaConnectorType())) {
			LOGGER.debug("CAConnectorType CMP at " + caConfig.getCaUrl());

			throw new Exception("ca connector type '" + caConfig.getCaConnectorType() + "' not implemented, yet!");
/*
			X509Certificate x509Cert = cryptoUtil.convertPemToCertificate(certificateDao.getContent());

			caCmpConnector.revokeCertificate(x509Cert, crlReason, caConfig.getSecret(), caConfig.getCaUrl(),
					caConfig.getName());
*/					
		} else {
			throw new Exception("unexpected ca connector type '" + caConfig.getCaConnectorType() + "' !");
		}
	}

}
