package de.trustable.ca3s.core.service.util;

import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.repository.CAConnectorConfigRepository;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;

@Service
public class VaultUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(VaultUtil.class);

    private final ProtectedContentUtil protUtil;

    public VaultUtil(ProtectedContentUtil protUtil) {
        this.protUtil = protUtil;
    }


    public @NotNull VaultTemplate getVaultTemplate(CAConnectorConfig caConfig) throws GeneralSecurityException {
        VaultEndpoint endpoint = null;
        try {
            endpoint = VaultEndpoint.from(new URI(caConfig.getCaUrl()));
        } catch (URISyntaxException e) {
            LOGGER.warn("problem processing vault url '{}' : {}", caConfig.getCaUrl(), e.getMessage());
            throw new GeneralSecurityException(e);
        }

        String plaintextToken = protUtil.unprotectString( caConfig.getSecret().getContentBase64());
        return new VaultTemplate(endpoint, new TokenAuthentication(plaintextToken));
    }


}
