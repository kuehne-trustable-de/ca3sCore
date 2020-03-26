package de.trustable.ca3s.core.web.rest.support;

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.domain.ProtectedContent;
import de.trustable.ca3s.core.domain.enumeration.ContentRelationType;
import de.trustable.ca3s.core.domain.enumeration.ProtectedContentType;
import de.trustable.ca3s.core.repository.CAConnectorConfigRepository;
import de.trustable.ca3s.core.service.util.CAStatus;
import de.trustable.ca3s.core.service.util.CaConnectorAdapter;
import de.trustable.ca3s.core.service.util.ProtectedContentUtil;
import de.trustable.ca3s.core.web.rest.CAConnectorConfigResource;

/**
 * REST controller for processing PKCS10 requests and Certificates.
 */
@RestController
@RequestMapping("/api")
public class UIDatasetSupport {

	private final Logger LOG = LoggerFactory.getLogger(UIDatasetSupport.class);

	@Autowired
	private CAConnectorConfigRepository caConnConfRepo;

	@Autowired
	private CaConnectorAdapter caConnectorAdapter;

	@Autowired
	private ProtectedContentUtil protUtil;

	
    /**
     * {@code GET  /cert-generators} : get all elements able to create a certificate.
     *
     * @return the {@link CAConnectorConfig} .
     */
    @GetMapping("/ca-connector-configs/cert-generators")
    public List<CAConnectorConfig> getAllCAConnectorConfigs() {

        return caConnConfRepo.findAllCertGenerators();
	}

   
    /**
     * {@code POST  /cert-generators} : get all elements able to create a certificate.
     * 
     * @param caConnConfig
     * @return the {@link CAStatus} .
     */
    @PostMapping("/ca-connector-configs/getStatus")
    public CAStatus getCAConnectorStatus( @Valid @RequestBody CAConnectorConfig cAConnectorConfig) {

    	
        if((cAConnectorConfig.getPlainSecret() == null) || (cAConnectorConfig.getPlainSecret().trim().length() == 0))  {
	        cAConnectorConfig.setSecret(null);
        }else if(CAConnectorConfigResource.PLAIN_SECRET_PLACEHOLDER.equals(cAConnectorConfig.getPlainSecret().trim())) {
        	// no passphrase change received from the UI, just do nothing
        	return CAStatus.Unknown;
        }else {	
	        ProtectedContent protSecret = protUtil.createProtectedContent(cAConnectorConfig.getPlainSecret(), ProtectedContentType.PASSWORD, ContentRelationType.CONNECTION, cAConnectorConfig.getId());
	        cAConnectorConfig.setSecret(protSecret);
        }

    	LOG.debug("checking status for {}", cAConnectorConfig);
    	
    	CAStatus status = caConnectorAdapter.getStatus(cAConnectorConfig);

    	LOG.debug("CA status for {} is {}", cAConnectorConfig.getName(), status);
    	return status;
	}

}
