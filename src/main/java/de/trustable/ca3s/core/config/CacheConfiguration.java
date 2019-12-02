package de.trustable.ca3s.core.config;

import java.time.Duration;

import org.ehcache.config.builders.*;
import org.ehcache.jsr107.Eh107Configuration;

import org.hibernate.cache.jcache.ConfigSettings;
import io.github.jhipster.config.JHipsterProperties;

import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.*;

@Configuration
@EnableCaching
public class CacheConfiguration {

    private final javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration;

    public CacheConfiguration(JHipsterProperties jHipsterProperties) {
        JHipsterProperties.Cache.Ehcache ehcache = jHipsterProperties.getCache().getEhcache();

        jcacheConfiguration = Eh107Configuration.fromEhcacheCacheConfiguration(
            CacheConfigurationBuilder.newCacheConfigurationBuilder(Object.class, Object.class,
                ResourcePoolsBuilder.heap(ehcache.getMaxEntries()))
                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(ehcache.getTimeToLiveSeconds())))
                .build());
    }

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(javax.cache.CacheManager cacheManager) {
        return hibernateProperties -> hibernateProperties.put(ConfigSettings.CACHE_MANAGER, cacheManager);
    }

    @Bean
    public JCacheManagerCustomizer cacheManagerCustomizer() {
        return cm -> {
            createCache(cm, de.trustable.ca3s.core.repository.UserRepository.USERS_BY_LOGIN_CACHE);
            createCache(cm, de.trustable.ca3s.core.repository.UserRepository.USERS_BY_EMAIL_CACHE);
            createCache(cm, de.trustable.ca3s.core.domain.User.class.getName());
            createCache(cm, de.trustable.ca3s.core.domain.Authority.class.getName());
            createCache(cm, de.trustable.ca3s.core.domain.User.class.getName() + ".authorities");
            createCache(cm, de.trustable.ca3s.core.domain.Certificate.class.getName());
            createCache(cm, de.trustable.ca3s.core.domain.Certificate.class.getName() + ".certificateAttributes");
            createCache(cm, de.trustable.ca3s.core.domain.CSR.class.getName());
            createCache(cm, de.trustable.ca3s.core.domain.CSR.class.getName() + ".rdns");
            createCache(cm, de.trustable.ca3s.core.domain.CSR.class.getName() + ".ras");
            createCache(cm, de.trustable.ca3s.core.domain.CSR.class.getName() + ".csrAttributes");
            createCache(cm, de.trustable.ca3s.core.domain.CAConnectorConfig.class.getName());
            createCache(cm, de.trustable.ca3s.core.domain.CertificateAttribute.class.getName());
            createCache(cm, de.trustable.ca3s.core.domain.CsrAttribute.class.getName());
            createCache(cm, de.trustable.ca3s.core.domain.RDN.class.getName());
            createCache(cm, de.trustable.ca3s.core.domain.RDN.class.getName() + ".rdnAttributes");
            createCache(cm, de.trustable.ca3s.core.domain.RDNAttribute.class.getName());
            createCache(cm, de.trustable.ca3s.core.domain.RequestAttribute.class.getName());
            createCache(cm, de.trustable.ca3s.core.domain.RequestAttribute.class.getName() + ".requestAttributeValues");
            createCache(cm, de.trustable.ca3s.core.domain.RequestAttributeValue.class.getName());
            createCache(cm, de.trustable.ca3s.core.domain.ACMEAccount.class.getName());
            createCache(cm, de.trustable.ca3s.core.domain.ACMEAccount.class.getName() + ".contacts");
            createCache(cm, de.trustable.ca3s.core.domain.ACMEAccount.class.getName() + ".orders");
            createCache(cm, de.trustable.ca3s.core.domain.AcmeContact.class.getName());
            createCache(cm, de.trustable.ca3s.core.domain.AcmeOrder.class.getName());
            createCache(cm, de.trustable.ca3s.core.domain.AcmeOrder.class.getName() + ".authorizations");
            createCache(cm, de.trustable.ca3s.core.domain.AcmeOrder.class.getName() + ".identifiers");
            createCache(cm, de.trustable.ca3s.core.domain.Identifier.class.getName());
            createCache(cm, de.trustable.ca3s.core.domain.Authorization.class.getName());
            createCache(cm, de.trustable.ca3s.core.domain.Authorization.class.getName() + ".challenges");
            createCache(cm, de.trustable.ca3s.core.domain.AcmeChallenge.class.getName());
            createCache(cm, de.trustable.ca3s.core.domain.Nonce.class.getName());
            // jhipster-needle-ehcache-add-entry
        };
    }

    private void createCache(javax.cache.CacheManager cm, String cacheName) {
        javax.cache.Cache<Object, Object> cache = cm.getCache(cacheName);
        if (cache != null) {
            cm.destroyCache(cacheName);
        }
        cm.createCache(cacheName, jcacheConfiguration);
    }
}
