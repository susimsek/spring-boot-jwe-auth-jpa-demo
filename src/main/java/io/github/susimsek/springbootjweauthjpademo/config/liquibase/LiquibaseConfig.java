package io.github.susimsek.springbootjweauthjpademo.config.liquibase;

import io.github.susimsek.springbootjweauthjpademo.config.ApplicationProperties;
import liquibase.integration.spring.SpringLiquibase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseDataSource;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.concurrent.Executor;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(LiquibaseProperties.class)
@Slf4j
public class LiquibaseConfig {

    @Bean
    public SpringLiquibase liquibase(
        @Qualifier("applicationTaskExecutor") Executor executor,
        LiquibaseProperties liquibaseProperties,
        @LiquibaseDataSource ObjectProvider<DataSource> liquibaseDataSource,
        ObjectProvider<DataSource> dataSource,
        DataSourceProperties dataSourceProperties,
        ApplicationProperties applicationProperties
    ) {
        SpringLiquibase liquibase;
        if (Boolean.TRUE.equals(applicationProperties.getLiquibase().getAsyncStart())) {
            liquibase = SpringLiquibaseUtil.createAsyncSpringLiquibase(
                executor,
                liquibaseDataSource.getIfAvailable(),
                liquibaseProperties,
                dataSource.getIfUnique(),
                dataSourceProperties
            );
        } else {
            liquibase = SpringLiquibaseUtil.createSpringLiquibase(
                liquibaseDataSource.getIfAvailable(),
                liquibaseProperties,
                dataSource.getIfUnique(),
                dataSourceProperties
            );
        }
        liquibase.setChangeLog(liquibaseProperties.getChangeLog());
        if (!CollectionUtils.isEmpty(liquibaseProperties.getContexts())) {
            liquibase.setContexts(StringUtils.collectionToCommaDelimitedString(
                liquibaseProperties.getContexts()));
        }
        liquibase.setDefaultSchema(liquibaseProperties.getDefaultSchema());
        liquibase.setLiquibaseSchema(liquibaseProperties.getLiquibaseSchema());
        liquibase.setLiquibaseTablespace(liquibaseProperties.getLiquibaseTablespace());
        liquibase.setDatabaseChangeLogLockTable(liquibaseProperties.getDatabaseChangeLogLockTable());
        liquibase.setDatabaseChangeLogTable(liquibaseProperties.getDatabaseChangeLogTable());
        liquibase.setDropFirst(liquibaseProperties.isDropFirst());
        if (!CollectionUtils.isEmpty(liquibaseProperties.getLabelFilter())) {
            liquibase.setLabelFilter(StringUtils.collectionToCommaDelimitedString(
                liquibaseProperties.getLabelFilter()));
        }
        liquibase.setChangeLogParameters(liquibaseProperties.getParameters());
        liquibase.setRollbackFile(liquibaseProperties.getRollbackFile());
        liquibase.setTestRollbackOnUpdate(liquibaseProperties.isTestRollbackOnUpdate());
        liquibase.setShouldRun(liquibaseProperties.isEnabled());
        log.debug("Configuring Liquibase");
        return liquibase;
    }
}
