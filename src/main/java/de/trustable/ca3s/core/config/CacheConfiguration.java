package de.trustable.ca3s.core.config;

import java.time.Duration;

import org.ehcache.config.builders.*;
import org.ehcache.jsr107.Eh107Configuration;

import tech.jhipster.config.JHipsterProperties;

import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
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
    public JCacheManagerCustomizer cacheManagerCustomizer() {
        return cm -> {
            createCache(cm, de.trustable.ca3s.core.repository.UserRepository.USERS_BY_LOGIN_CACHE);
            createCache(cm, de.trustable.ca3s.core.repository.UserRepository.USERS_BY_EMAIL_CACHE);
            createCache(cm, de.trustable.ca3s.core.domain.CAConnectorConfig.class.getName());
            createCache(cm, "CRLs");
            createCache(cm, de.trustable.ca3s.core.domain.CSR.class.getName());
            createCache(cm, de.trustable.ca3s.core.domain.CSR.class.getName() + ".rdns");
            createCache(cm, de.trustable.ca3s.core.domain.CSR.class.getName() + ".ras");
            createCache(cm, de.trustable.ca3s.core.domain.CSR.class.getName() + ".csrAttributes");
            createCache(cm, de.trustable.ca3s.core.domain.Pipeline.class.getName());
            createCache(cm, de.trustable.ca3s.core.domain.Pipeline.class.getName() + ".pipelineAttributes");
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
