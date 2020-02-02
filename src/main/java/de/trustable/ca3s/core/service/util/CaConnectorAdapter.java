package de.trustable.ca3s.core.service.util;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;

import org.bouncycastle.asn1.x509.CRLReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.enumeration.CAConnectorType;
import de.trustable.ca3s.core.service.adcs.ADCSConnector;
import de.trustable.ca3s.core.service.cmp.CaCmpConnector;
import de.trustable.ca3s.core.service.dir.DirectoryConnector;

@Service
public class CaConnectorAdapter {

	Logger LOGGER = LoggerFactory.getLogger(CaConnectorAdapter.class);

	@Autowired
	private ADCSConnector adcsConnector;

	@Autowired
	private CaCmpConnector cmpConnector;
	
	@Autowired
	private CaInternalConnector internalConnector;
	
	@Autowired
	private DirectoryConnector dirConnector;
	
	@Autowired
	private CSRUtil csrUtil;
	
	/**
	 * 
	 * @param caConfig
	 * @return
	 */
	public CAStatus getStatus(CAConnectorConfig caConfig ) {

		if (caConfig == null) {
			LOGGER.debug("CAConnectorType need caConfig != null ");
			return CAStatus.Unknown;
		}

		if (CAConnectorType.ADCS.equals(caConfig.getCaConnectorType())) {
			LOGGER.debug("CAConnectorType ADCS at " + caConfig.getCaUrl());
			return adcsConnector.getStatus(caConfig);

		} else if (CAConnectorType.CMP.equals(caConfig.getCaConnectorType())) {
			LOGGER.debug("CAConnectorType CMP at " + caConfig.getCaUrl());
			
			return cmpConnector.getStatus(caConfig);
			
		} else if (CAConnectorType.INTERNAL.equals(caConfig.getCaConnectorType())) {
			LOGGER.debug("CAConnectorType INTERNAL is Active" );
			return CAStatus.Active;
			
		} else if (CAConnectorType.DIRECTORY.equals(caConfig.getCaConnectorType())) {
			LOGGER.debug("CAConnectorType DIRECTORY for " + caConfig.getCaUrl());
			
			return dirConnector.getStatus(caConfig);
		} else {
			LOGGER.warn("unexpected ca connector type '" + caConfig.getCaConnectorType() + "' !");
		}
		
		// no valid feedback
		return CAStatus.Unknown;
		
	}

	/**
	 * 
	 * @param csrBase64
	 * @param caConfig
	 * @return
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	public Certificate signCertificateRequest(final String csrBase64, CAConnectorConfig caConfig ) throws GeneralSecurityException, IOException {
		
		CSR csr = csrUtil.buildCSR(csrBase64, csrUtil.parseBase64CSR(csrBase64));
		return signCertificateRequest(csr, caConfig );
		
	}

	/**
	 * 
	 * @param csr
	 * @param caConfig
	 * @return
	 * @throws GeneralSecurityException
	 */
	public Certificate signCertificateRequest(CSR csr, CAConnectorConfig caConfig ) throws GeneralSecurityException  {

		if (caConfig == null) {
			throw new GeneralSecurityException("CA connector not selected !");
		}

		if (CAConnectorType.ADCS.equals(caConfig.getCaConnectorType())) {
			LOGGER.debug("CAConnectorType ADCS at " + caConfig.getCaUrl());
			return adcsConnector.signCertificateRequest(csr, caConfig);

		} else if (CAConnectorType.CMP.equals(caConfig.getCaConnectorType())) {
			LOGGER.debug("CAConnectorType CMP at " + caConfig.getCaUrl());
			return cmpConnector.signCertificateRequest(csr, caConfig);
			
		} else if (CAConnectorType.INTERNAL.equals(caConfig.getCaConnectorType())) {
			LOGGER.debug("CAConnectorType INTERNAL ");
			return internalConnector.signCertificateRequest(csr, caConfig);
						
		} else {
			throw new GeneralSecurityException("unexpected ca connector type '" + caConfig.getCaConnectorType() + "' !");
		}
	}

	/**
	 * 
	 * @param certificateDao
	 * @param crlReason
	 * @param revocationDate
	 * @param caConfig
	 * @throws GeneralSecurityException
	 */
	@Transactional
	public void revokeCertificate(Certificate certificateDao, CRLReason crlReason, Date revocationDate,
			CAConnectorConfig caConfig ) throws GeneralSecurityException {

		if (caConfig == null) {
			throw new GeneralSecurityException("CA connector not selected !");
		}

		if (CAConnectorType.ADCS.equals(caConfig.getCaConnectorType())) {
			LOGGER.debug("CAConnectorType ADCS at " + caConfig.getCaUrl());
			adcsConnector.revokeCertificate(certificateDao, crlReason, revocationDate, caConfig);

		} else if (CAConnectorType.CMP.equals(caConfig.getCaConnectorType())) {
			LOGGER.debug("CAConnectorType CMP at " + caConfig.getCaUrl());
			cmpConnector.revokeCertificate(certificateDao, crlReason, revocationDate, caConfig);
			
		} else if (CAConnectorType.INTERNAL.equals(caConfig.getCaConnectorType())) {
			LOGGER.debug("CAConnectorType INTERNAL ");
			internalConnector.revokeCertificate(certificateDao, crlReason, revocationDate, caConfig);		

		} else {
			throw new GeneralSecurityException("unexpected ca connector type '" + caConfig.getCaConnectorType() + "' !");
		}
	}

}
