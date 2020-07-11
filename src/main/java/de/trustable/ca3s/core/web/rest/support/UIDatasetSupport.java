package de.trustable.ca3s.core.web.rest.support;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.domain.Pipeline;
import de.trustable.ca3s.core.domain.ProtectedContent;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.domain.UserPreference;
import de.trustable.ca3s.core.domain.enumeration.ContentRelationType;
import de.trustable.ca3s.core.domain.enumeration.PipelineType;
import de.trustable.ca3s.core.domain.enumeration.ProtectedContentType;
import de.trustable.ca3s.core.repository.CAConnectorConfigRepository;
import de.trustable.ca3s.core.repository.PipelineRepository;
import de.trustable.ca3s.core.repository.UserPreferenceRepository;
import de.trustable.ca3s.core.service.UserService;
import de.trustable.ca3s.core.service.util.CAStatus;
import de.trustable.ca3s.core.service.util.CaConnectorAdapter;
import de.trustable.ca3s.core.service.util.ProtectedContentUtil;
import de.trustable.ca3s.core.web.rest.CAConnectorConfigResource;
import de.trustable.ca3s.core.web.rest.data.CertificateFilterList;

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
	private PipelineRepository pipelineRepo;
	
	@Autowired
	private ProtectedContentUtil protUtil;

    @Autowired
    private UserPreferenceRepository userPreferenceRepository;

    @Autowired
    private UserService userService;

	
    /**
     * {@code GET  /pipeline/getWebPipelines} : get all pipelines for web upload.
     *
     * @return the {@link Pipeline} .
     */
    @GetMapping("/pipeline/getWebPipelines")
    public List<Pipeline> getWebPipelines() {

        return pipelineRepo.findByType(PipelineType.WEB);
	}

   
    /**
     * {@code GET  /ca-connector-configs/cert-generators} : get all elements able to create a certificate.
     *
     * @return the {@link CAConnectorConfig} .
     */
    @GetMapping("/ca-connector-configs/cert-generators")
    public List<CAConnectorConfig> getAllCAConnectorConfigs() {

        return caConnConfRepo.findAllCertGenerators();
	}

   
    /**
     * {@code POST  /ca-connector-configs/getStatus} : get all elements able to create a certificate.
     * 
     * @param cAConnectorConfig CAConnectorConfig
     * @return the {@link CAStatus} .
     */
    @PostMapping("/ca-connector-configs/getStatus")
    public CAStatus getCAConnectorStatus( @Valid @RequestBody CAConnectorConfig cAConnectorConfig) {


    	LOG.debug("checking status for {}", cAConnectorConfig);

        if((cAConnectorConfig.getPlainSecret() == null) || (cAConnectorConfig.getPlainSecret().trim().length() == 0))  {
	        cAConnectorConfig.setSecret(null);
        }else if(CAConnectorConfigResource.PLAIN_SECRET_PLACEHOLDER.equals(cAConnectorConfig.getPlainSecret().trim())) {
        	// no passphrase change received from the UI, use the existing 'secret' object
        	
        	Optional<CAConnectorConfig> optCcc = caConnConfRepo.findById(cAConnectorConfig.getId());
        	if(optCcc.isPresent()) {
    	        cAConnectorConfig.setSecret(optCcc.get().getSecret());
        	}else {
        		return CAStatus.Unknown;
        	}
        }else {	
	        ProtectedContent protSecret = protUtil.createProtectedContent(cAConnectorConfig.getPlainSecret(), ProtectedContentType.PASSWORD, ContentRelationType.CONNECTION, -1L);
	        cAConnectorConfig.setSecret(protSecret);
        }
    	
    	CAStatus status = caConnectorAdapter.getStatus(cAConnectorConfig);

    	LOG.debug("CA status for {} is {}", cAConnectorConfig.getName(), status);
    	return status;
	}

	
    /**
     * {@code GET  userProperties/{name} } : get user properties for the given name and the logged-in user.
     *
     * @return the {@link UserPreference} .
     */
    @GetMapping("/userProperties/{name}")
    public ResponseEntity<UserPreference> getUserProperties(@PathVariable String name) {

    	final Optional<User> optUser = userService.getUserWithAuthorities();
    	
    	if(optUser.isPresent()) {
    		Optional<UserPreference> optUP = userPreferenceRepository.findByNameforUser(name, optUser.get().getId() );
    		if( optUP.isPresent()) {
    			return ResponseEntity.ok(optUP.get());
    		}
    	}

		return ResponseEntity.noContent().build();
	}


    /**
     * {@code GET  userProperties/{name} } : get user properties for the given name and the logged-in user.
     *
     * @return the {@link UserPreference} .
     */
    @GetMapping("/userProperties/filterList/{name}")
    public ResponseEntity<CertificateFilterList> getFilterList(@PathVariable String name) {

    	final Optional<User> optUser = userService.getUserWithAuthorities();
    	
    	if(optUser.isPresent()) {
    		Optional<UserPreference> optUP = userPreferenceRepository.findByNameforUser(name, optUser.get().getId() );
    		if( optUP.isPresent()) {
        		ObjectMapper objectMapper = new ObjectMapper();
        		CertificateFilterList filterList;
				try {
					filterList = objectMapper.readValue(optUP.get().getContent(), CertificateFilterList.class);
	    			return ResponseEntity.ok(filterList);
				} catch (IOException e) {
	        		LOG.debug("unmarshalling filterList", e);
					return ResponseEntity.badRequest().build();
				}  
    		}
    	}else {
    		LOG.info("reading user properties for unknown user");
    	}

		return ResponseEntity.notFound().build();
	}


    /**
     * {@code PUT  userProperties/filterList/{name} } : put the filter list settings for this user into his properties.
     *
     * @return the {@link Pipeline} .
     */
    @PutMapping("/userProperties/filterList/{name}")
    public ResponseEntity<UserPreference> putUserProperties(@PathVariable String name, @RequestBody CertificateFilterList filterList) {

    	final Optional<User> optUser = userService.getUserWithAuthorities();
    	
    	if(optUser.isPresent()) {
    		UserPreference up = new UserPreference();
    		up.setName(name);
    		
    		Optional<UserPreference> optUP = userPreferenceRepository.findByNameforUser(name, optUser.get().getId() );
    		if( optUP.isPresent()) {
    			up = optUP.get();
        		LOG.debug("using existing user properties");
    		}
    		
    		up.setUserId(optUser.get().getId());
    		
    		ObjectMapper objectMapper = new ObjectMapper();
    		try {
				up.setContent(objectMapper.writeValueAsString(filterList));
			} catch (JsonProcessingException e) {
        		LOG.debug("marshalling filterList", e);
				return ResponseEntity.badRequest().build();
			}
    		
    		userPreferenceRepository.save(up);
    		
    	}else {
    		LOG.info("reading user properties for unknown user");
    	}

		return ResponseEntity.noContent().build();
	}


}
