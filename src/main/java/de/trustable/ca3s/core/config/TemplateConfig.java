package de.trustable.ca3s.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;

@Configuration
public class TemplateConfig {

    final private boolean templateCacheable;
    final private String templatePrefix;
    final private String templatePostfix;

    public TemplateConfig(@Value("${ca3s.template.cacheable:false}") boolean templateCacheable,
                          @Value("${ca3s.template.email.filePrefix:classpath:/templates/}") String templatePrefix,
                          @Value("${ca3s.template.email.filePostfix:.html}") String templatePostfix) {
        this.templateCacheable = templateCacheable;
        this.templatePrefix = templatePrefix;
        this.templatePostfix = templatePostfix;
    }

    @Bean
    public SpringResourceTemplateResolver templateResolver() {
        final SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setCacheable(templateCacheable);
        templateResolver.setPrefix(templatePrefix);
        templateResolver.setSuffix(templatePostfix);
        return templateResolver;
    }
}
