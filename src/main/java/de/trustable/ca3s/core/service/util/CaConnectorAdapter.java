package de.trustable.ca3s.core.service.util;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.trustable.ca3s.core.repository.CAConnectorConfigRepository;
import de.trustable.ca3s.core.service.cmp.CaCmpConnector;
import de.trustable.ca3s.core.service.dto.CAConnectorStatus;
import de.trustable.ca3s.core.service.dto.CAStatus;
import de.trustable.ca3s.core.service.vault.VaultPKIConnector;
import org.bouncycastle.asn1.x509.CRLReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CsrAttribute;
import de.trustable.ca3s.core.domain.enumeration.CAConnectorType;
import de.trustable.ca3s.core.domain.enumeration.PipelineType;
import de.trustable.ca3s.core.service.adcs.ADCSConnector;
import de.trustable.ca3s.core.service.dir.DirectoryConnector;

@Service
public class CaConnectorAdapter {

	Logger LOGGER = LoggerFactory.getLogger(CaConnectorAdapter.class);

	private final ADCSConnector adcsConnector;

    private final CaCmpConnector cmpConnector;
    private final VaultPKIConnector vaultPKIConnector;

	private final CaInternalConnector internalConnector;

	private final DirectoryConnector dirConnector;

    private final CAConnectorConfigRepository caConfigRepository;

	private final CSRUtil csrUtil;

    private List<CAConnectorStatus> caConnectorStatus = new ArrayList<>();

    public CaConnectorAdapter(ADCSConnector adcsConnector,
                              CaCmpConnector cmpConnector,
                              VaultPKIConnector vaultPKIConnector,
                              CaInternalConnector internalConnector,
                              DirectoryConnector dirConnector,
                              CAConnectorConfigRepository caConfigRepository,
                              CSRUtil csrUtil) {
        this.adcsConnector = adcsConnector;
        this.cmpConnector = cmpConnector;
        this.vaultPKIConnector = vaultPKIConnector;
        this.internalConnector = internalConnector;
        this.dirConnector = dirConnector;
        this.caConfigRepository = caConfigRepository;
        this.csrUtil = csrUtil;
    }

    /**
         *
         * @param caConfig CAConnectorConfig
         * @return CAStatus
         */
	public CAStatus getStatus(CAConnectorConfig caConfig ) {

		if (caConfig == null) {
			LOGGER.debug("CAConnectorType caConfig == null !");
			return CAStatus.Unknown;
		}

		if( !caConfig.isActive()){
            LOGGER.debug("CAConnector '" + caConfig.getName() + "' is disabled");
		    // not active, no need for a check running into a timeout ....
            return CAStatus.Deactivated;
        }

		if (CAConnectorType.ADCS.equals(caConfig.getCaConnectorType())) {
			LOGGER.debug("CAConnectorType ADCS at " + caConfig.getCaUrl());
			return adcsConnector.getStatus(caConfig);

        } else if (CAConnectorType.ADCS_CERTIFICATE_INVENTORY.equals(caConfig.getCaConnectorType())) {
            LOGGER.debug("CAConnectorType ADCS_CERTIFICATE_INVENTORY at " + caConfig.getCaUrl());
            return adcsConnector.getStatus(caConfig);

        } else if (CAConnectorType.CMP.equals(caConfig.getCaConnectorType())) {
			LOGGER.debug("CAConnectorType CMP at " + caConfig.getCaUrl());
			return cmpConnector.getStatus(caConfig);
		} else if (CAConnectorType.INTERNAL.equals(caConfig.getCaConnectorType())) {
			LOGGER.debug("CAConnectorType INTERNAL is enabled" );
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

        Certificate cert = signCSR(csr, caConfig);


        return cert;
    }

    private Certificate signCSR(CSR csr, CAConnectorConfig caConfig) throws GeneralSecurityException {
        if (CAConnectorType.ADCS.equals(caConfig.getCaConnectorType())) {
            LOGGER.debug("CAConnectorType ADCS at {} signs CSR", caConfig.getCaUrl());
            return adcsConnector.signCertificateRequest(csr, caConfig);

        } else if (CAConnectorType.CMP.equals(caConfig.getCaConnectorType())) {
            LOGGER.debug("CAConnectorType CMP at {} signs CSR", caConfig.getCaUrl());
            return cmpConnector.signCertificateRequest(csr, caConfig);

        } else if (CAConnectorType.VAULT.equals(caConfig.getCaConnectorType())) {
            LOGGER.debug("CAConnectorType Vault at {} signs CSR", caConfig.getCaUrl());
            return vaultPKIConnector.signCertificateRequest(csr, caConfig);

        } else if (CAConnectorType.INTERNAL.equals(caConfig.getCaConnectorType())) {
            LOGGER.debug("CAConnectorType INTERNAL signs CSR");
            return internalConnector.signCertificateRequest(csr, caConfig);

        } else if (CAConnectorType.ACME_CLIENT.equals(caConfig.getCaConnectorType())) {
            LOGGER.debug("CAConnectorType ACME_CLIENT signs CSR");
            throw new GeneralSecurityException("To Do implement ca connector type '" + caConfig.getCaConnectorType() + "' !");
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
			LOGGER.debug("CAConnectorType ADCS at {} revokes certificate id {}", caConfig.getCaUrl(), certificateDao.getId());
			adcsConnector.revokeCertificate(certificateDao, crlReason, revocationDate, caConfig);

		} else if (CAConnectorType.CMP.equals(caConfig.getCaConnectorType())) {
			LOGGER.debug("CAConnectorType CMP at {} revokes certificate id {}", caConfig.getCaUrl(), certificateDao.getId());
			cmpConnector.revokeCertificate(certificateDao, crlReason, revocationDate, caConfig);

		} else if (CAConnectorType.INTERNAL.equals(caConfig.getCaConnectorType())) {
			LOGGER.debug("CAConnectorType INTERNAL revokes certificate id {}", certificateDao.getId());
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

            LOGGER.debug("Connector {} is {}, status: {}", cAConnectorConfig.getName(),
                cAConnectorConfig.isActive() ? "enabled": "disabled",
                status );
        }

        this.caConnectorStatus = caStatusList;
    }
}
