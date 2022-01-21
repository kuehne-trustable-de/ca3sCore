package de.trustable.ca3s.core.config;

import tech.jhipster.config.JHipsterConstants;
import tech.jhipster.config.liquibase.SpringLiquibaseUtil;
import liquibase.integration.spring.SpringLiquibase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseDataSource;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

import javax.sql.DataSource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;

@Configuration
public class LiquibaseConfiguration {

    private final Logger log = LoggerFactory.getLogger(LiquibaseConfiguration.class);

    private final Environment env;

	@Value("${spring.liquibase.changeLog:classpath:config/liquibase/master.xml}")
	private String certificateImportActive;

    public LiquibaseConfiguration(Environment env) {
        this.env = env;
    }

    @Bean
    public SpringLiquibase liquibase(@Qualifier("taskExecutor") Executor executor,
            @LiquibaseDataSource ObjectProvider<DataSource> liquibaseDataSource, LiquibaseProperties liquibaseProperties,
            ObjectProvider<DataSource> dataSource, DataSourceProperties dataSourceProperties) {

        // If you don't want Liquibase to start asynchronously, substitute by this:
        // SpringLiquibase liquibase = SpringLiquibaseUtil.createSpringLiquibase(liquibaseDataSource.getIfAvailable(), liquibaseProperties, dataSource.getIfUnique(), dataSourceProperties);
        SpringLiquibase liquibase = SpringLiquibaseUtil.createAsyncSpringLiquibase(this.env, executor, liquibaseDataSource.getIfAvailable(), liquibaseProperties, dataSource.getIfUnique(), dataSourceProperties);

        log.debug("Using liquibase files from '{}'", certificateImportActive);

        liquibase.setChangeLog(certificateImportActive);

        liquibase.setContexts(liquibaseProperties.getContexts());
        liquibase.setDefaultSchema(liquibaseProperties.getDefaultSchema());

		try {
	        // liquibase.setLiquibaseSchema(liquibaseProperties.getLiquibaseSchema());
			Method method = liquibase.getClass().getMethod("setLiquibaseSchema", String.class);
			method.invoke(liquibase, liquibaseProperties.getLiquibaseSchema());

	        //liquibase.setLiquibaseTablespace(liquibaseProperties.getLiquibaseTablespace());
			method = liquibase.getClass().getMethod("setLiquibaseTablespace", String.class);
			method.invoke(liquibase, liquibaseProperties.getLiquibaseTablespace());

//	        liquibase.setDatabaseChangeLogLockTable(liquibaseProperties.getDatabaseChangeLogLockTable());
			method = liquibase.getClass().getMethod("setDatabaseChangeLogLockTable", String.class);
			method.invoke(liquibase, liquibaseProperties.getDatabaseChangeLogLockTable());

//	        liquibase.setDatabaseChangeLogTable(liquibaseProperties.getDatabaseChangeLogTable());
			method = liquibase.getClass().getMethod("setDatabaseChangeLogTable", String.class);
			method.invoke(liquibase, liquibaseProperties.getDatabaseChangeLogTable());

//	        liquibase.setTestRollbackOnUpdate(liquibaseProperties.isTestRollbackOnUpdate());
			method = liquibase.getClass().getMethod("setTestRollbackOnUpdate", Boolean.class);
			method.invoke(liquibase, liquibaseProperties.isTestRollbackOnUpdate());

		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            log.debug("Old Liquibase version, not supporting more recent methods");
		}

        liquibase.setDropFirst(liquibaseProperties.isDropFirst());
        liquibase.setLabels(liquibaseProperties.getLabels());
        liquibase.setChangeLogParameters(liquibaseProperties.getParameters());
        liquibase.setRollbackFile(liquibaseProperties.getRollbackFile());

        if (env.acceptsProfiles(Profiles.of(JHipsterConstants.SPRING_PROFILE_NO_LIQUIBASE))) {
            liquibase.setShouldRun(false);
        } else {
            liquibase.setShouldRun(liquibaseProperties.isEnabled());
            log.debug("Configuring Liquibase");
        }
        return liquibase;
    }
}
