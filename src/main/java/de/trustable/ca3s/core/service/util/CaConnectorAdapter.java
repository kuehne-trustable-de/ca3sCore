package de.trustable.ca3s.core.service.util;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.trustable.ca3s.core.repository.CAConnectorConfigRepository;
import de.trustable.ca3s.core.service.dto.CAConnectorStatus;
import de.trustable.ca3s.core.service.dto.CAStatus;
import org.bouncycastle.asn1.x509.CRLReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CsrAttribute;
import de.trustable.ca3s.core.domain.enumeration.CAConnectorType;
import de.trustable.ca3s.core.domain.enumeration.PipelineType;
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
    private CAConnectorConfigRepository caConfigRepository;

    @Autowired
	private CSRUtil csrUtil;

    private List<CAConnectorStatus> caConnectorStatus = new ArrayList<>();

        /**
         *
         * @param caConfig CAConnectorConfig
         * @return CAStatus
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
	 * @param csrBase64 csr as Base64 String
	 * @param caConfig	CAConnectorConfig
	 * @return certificate
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	public Certificate signCertificateRequest(final String csrBase64, CAConnectorConfig caConfig ) throws GeneralSecurityException, IOException {

		CSR csr = csrUtil.buildCSR(csrBase64, CsrAttribute.REQUESTOR_SYSTEM, csrUtil.parseBase64CSR(csrBase64), PipelineType.INTERNAL, null);
		return signCertificateRequest(csr, caConfig );

	}

	/**
	 *
	 * @param csr 		csr as CSR object
	 * @param caConfig	CAConnectorConfig
	 * @return certificate
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
	 * @param certificateDao	certificateDao for revocation
	 * @param crlReason			crl reason for revocation
	 * @param revocationDate	date for revocation
	 * @param caConfig			CAConnectorConfig
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

    /**
     * get the current status for each ca connectors
     *
     * @return the list of id / status pairs
     */
    public List<CAConnectorStatus> getCAConnectorStatus(){
	    return caConnectorStatus;
    }

    /**
     * update the status list
     */
    public void updateCAConnectorStatus() {

        List<CAConnectorStatus> caStatusList = new ArrayList<>();

        for (CAConnectorConfig cAConnectorConfig : caConfigRepository.findAll()) {

            CAStatus status;
            if( cAConnectorConfig.isActive()) {
                try {
                    status = getStatus(cAConnectorConfig);
                }catch( Exception ex){
                    status = CAStatus.Problem;
                }
            }else{
                status = CAStatus.Deactivated;
            }

            CAConnectorStatus stat = new CAConnectorStatus(cAConnectorConfig.getId(), cAConnectorConfig.getName(), status);
            caStatusList.add(stat);

            LOGGER.debug("CA status for {} is {}", cAConnectorConfig.getName(), status);
        }

        this.caConnectorStatus = caStatusList;
    }
}
