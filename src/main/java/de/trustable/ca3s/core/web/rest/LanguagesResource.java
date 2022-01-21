package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.domain.UserPreference;
import de.trustable.ca3s.core.service.UserPreferenceService;
import de.trustable.ca3s.core.service.dto.Languages;
import de.trustable.ca3s.core.service.dto.Preferences;
import de.trustable.ca3s.core.service.util.PreferenceUtil;
import tech.jhipster.web.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for reading supported languages .
 * Just read-only access to this resource.
 *
 */
@RestController
@RequestMapping("/api")
public class LanguagesResource {

	private final Logger log = LoggerFactory.getLogger(LanguagesResource.class);

    @Value("${ca3s.ui.languages:en,de,pl}")
    private String availableLanguages;


    /**
     * {@code GET  /languages} : get all languages.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body containing all supported languages.
     */
    @GetMapping("/languages")
    public ResponseEntity<Languages> getlanguages() {

        Languages languages = new Languages(availableLanguages);

   		return new ResponseEntity<>(languages, HttpStatus.OK);
    }


}
