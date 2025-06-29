package io.github.susimsek.springbootjweauthjpademo.config.liquibase;

import liquibase.integration.spring.SpringLiquibase;
import lombok.experimental.UtilityClass;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.liquibase.DataSourceClosingSpringLiquibase;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.sql.DataSource;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

@UtilityClass
public class SpringLiquibaseUtil {

    public static SpringLiquibase createSpringLiquibase(
        DataSource liquibaseDatasource,
        LiquibaseProperties liquibaseProperties,
        DataSource dataSource,
        DataSourceProperties dataSourceProperties
    ) {
        SpringLiquibase liquibase;
        DataSource liquibaseDataSource = getDataSource(liquibaseDatasource, liquibaseProperties, dataSource);
        if (liquibaseDataSource != null) {
            liquibase = new SpringLiquibase();
            liquibase.setDataSource(liquibaseDataSource);
            return liquibase;
        }
        liquibase = new DataSourceClosingSpringLiquibase();
        liquibase.setDataSource(createNewDataSource(liquibaseProperties, dataSourceProperties));
        return liquibase;
    }

    public AsyncSpringLiquibase createAsyncSpringLiquibase(
        Executor executor,
        DataSource liquibaseDatasource,
        LiquibaseProperties liquibaseProperties,
        DataSource dataSource,
        DataSourceProperties dataSourceProperties
    ) {
        AsyncSpringLiquibase liquibase = new AsyncSpringLiquibase(executor);
        DataSource liquibaseDataSource = getDataSource(liquibaseDatasource, liquibaseProperties, dataSource);
        if (liquibaseDataSource != null) {
            liquibase.setCloseDataSourceOnceMigrated(false);
            liquibase.setDataSource(liquibaseDataSource);
        } else {
            liquibase.setDataSource(createNewDataSource(liquibaseProperties, dataSourceProperties));
        }
        return liquibase;
    }

    private static DataSource getDataSource(
        DataSource liquibaseDataSource,
        LiquibaseProperties liquibaseProperties,
        DataSource dataSource
    ) {
        if (liquibaseDataSource != null) {
            return liquibaseDataSource;
        }
        if (liquibaseProperties.getUrl() == null && liquibaseProperties.getUser() == null) {
            return dataSource;
        }
        return null;
    }

    private static DataSource createNewDataSource(LiquibaseProperties liquibaseProperties,
                                                  DataSourceProperties dataSourceProperties) {
        String url = getProperty(liquibaseProperties::getUrl, dataSourceProperties::determineUrl);
        String user = getProperty(liquibaseProperties::getUser, dataSourceProperties::determineUsername);
        String password = getProperty(liquibaseProperties::getPassword, dataSourceProperties::determinePassword);
        return DataSourceBuilder.create().url(url).username(user).password(password).build();
    }

    private static String getProperty(Supplier<String> property, Supplier<String> defaultValue) {
        return Optional.of(property).map(Supplier::get).orElseGet(defaultValue);
    }
}
