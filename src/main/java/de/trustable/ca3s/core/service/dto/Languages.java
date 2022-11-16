package de.trustable.ca3s.core.service.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Languages implements Serializable {

    private final Logger LOG = LoggerFactory.getLogger(Languages.class);

    /**
	 *
	 */
	private static final long serialVersionUID = 296426474864181L;

	private String[] languageArr = {"en", "de", "pl"};

	public Languages() {}

    public Languages(String availableLanguages) {

	    List<String> langList = new ArrayList<>();
        if( availableLanguages != null ) {
            for (String lang : availableLanguages.split(",")) {
                if (!lang.isEmpty()) {
                    langList.add(lang.toLowerCase().trim());
                    LOG.debug("language added: '{}'", lang.toLowerCase().trim());
                }
            }
        }

        if( langList.isEmpty()){
            LOG.warn("No languages configured, using 'en'. Please add valid languages in property 'ca3s.ui.languages'!");
            langList.add("en");
        }
        languageArr = langList.toArray(new String[0]);
    }

    public String[] getLanguageArr() {
        return languageArr;
    }

    public void setLanguageArr(String[] languageArr) {
        this.languageArr = languageArr;
    }

    public String alignLanguage(final String language){
	    for( String availLang: languageArr){
	        if( availLang.equalsIgnoreCase(language.trim())){
	            return language.trim();
            }
        }
	    return languageArr[0];
    }
}
