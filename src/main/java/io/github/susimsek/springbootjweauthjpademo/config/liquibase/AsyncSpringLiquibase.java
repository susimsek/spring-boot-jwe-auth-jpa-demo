package io.github.susimsek.springbootjweauthjpademo.config.liquibase;

import liquibase.exception.LiquibaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.liquibase.DataSourceClosingSpringLiquibase;
import org.springframework.util.StopWatch;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Executor;

@RequiredArgsConstructor
@Slf4j
public class AsyncSpringLiquibase extends DataSourceClosingSpringLiquibase {

    public static final String STARTING_ASYNC_MESSAGE = "Starting Liquibase asynchronously, your database might not be ready at startup!";
    public static final String STARTED_MESSAGE = "Liquibase has updated your database in {} ms";
    public static final String EXCEPTION_MESSAGE = "Liquibase could not start correctly, your database is NOT ready: {}";
    public static final long SLOWNESS_THRESHOLD = 5; // seconds
    public static final String SLOWNESS_MESSAGE = "Warning, Liquibase took more than {} seconds to start up!";

    private final Executor executor;

    @Override
    public void afterPropertiesSet() {
        handleAsyncExecution();
    }

    private void handleAsyncExecution() {
        try (Connection ignored = getDataSource().getConnection()) {
            executor.execute(() -> {
                try {
                    log.warn(STARTING_ASYNC_MESSAGE);
                    initDb();
                } catch (LiquibaseException e) {
                    log.error(EXCEPTION_MESSAGE, e.getMessage(), e);
                }
            });
        } catch (SQLException e) {
            log.error(EXCEPTION_MESSAGE, e.getMessage(), e);
        }
    }

    protected void initDb() throws LiquibaseException {
        StopWatch watch = new StopWatch();
        watch.start();
        super.afterPropertiesSet();
        watch.stop();
        log.debug(STARTED_MESSAGE, watch.getTotalTimeMillis());
        boolean isExecutionTimeLong = watch.getTotalTimeMillis() > SLOWNESS_THRESHOLD * 1000L;
        if (isExecutionTimeLong) {
            log.warn(SLOWNESS_MESSAGE, SLOWNESS_THRESHOLD);
        }
    }
}
