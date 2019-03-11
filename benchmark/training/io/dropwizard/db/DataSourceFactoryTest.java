package io.dropwizard.db;


import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.util.Duration;
import io.dropwizard.validation.BaseValidator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;
import org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer;
import org.junit.jupiter.api.Test;


public class DataSourceFactoryTest {
    private final MetricRegistry metricRegistry = new MetricRegistry();

    private DataSourceFactory factory;

    @Nullable
    private ManagedDataSource dataSource;

    @Test
    public void testInitialSizeIsZero() throws Exception {
        factory.setUrl("nonsense invalid url");
        factory.setInitialSize(0);
        ManagedDataSource dataSource = factory.build(metricRegistry, "test");
        dataSource.start();
    }

    @Test
    public void buildsAConnectionPoolToTheDatabase() throws Exception {
        try (Connection connection = dataSource().getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("select 1")) {
                try (ResultSet set = statement.executeQuery()) {
                    while (set.next()) {
                        assertThat(set.getInt(1)).isEqualTo(1);
                    } 
                }
            }
        }
    }

    @Test
    public void testNoValidationQueryTimeout() throws Exception {
        try (Connection connection = dataSource().getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("select 1")) {
                assertThat(statement.getQueryTimeout()).isEqualTo(0);
            }
        }
    }

    @Test
    public void testValidationQueryTimeoutIsSet() throws Exception {
        factory.setValidationQueryTimeout(Duration.seconds(3));
        try (Connection connection = dataSource().getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("select 1")) {
                assertThat(statement.getQueryTimeout()).isEqualTo(3);
            }
        }
    }

    @Test
    public void invalidJDBCDriverClassThrowsSQLException() {
        final DataSourceFactory factory = new DataSourceFactory();
        factory.setDriverClass("org.example.no.driver.here");
        assertThatExceptionOfType(SQLException.class).isThrownBy(() -> factory.build(metricRegistry, "test").getConnection());
    }

    @Test
    public void testCustomValidator() throws Exception {
        factory.setValidatorClassName(Optional.of(CustomConnectionValidator.class.getName()));
        try (Connection connection = dataSource().getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("select 1")) {
                try (ResultSet rs = statement.executeQuery()) {
                    assertThat(rs.next()).isTrue();
                    assertThat(rs.getInt(1)).isEqualTo(1);
                }
            }
        }
        assertThat(CustomConnectionValidator.loaded).isTrue();
    }

    @Test
    public void testJdbcInterceptors() throws Exception {
        factory.setJdbcInterceptors(Optional.of("StatementFinalizer;ConnectionState"));
        final ManagedPooledDataSource source = ((ManagedPooledDataSource) (dataSource()));
        assertThat(source.getPoolProperties().getJdbcInterceptorsAsArray()).extracting("interceptorClass").contains(StatementFinalizer.class, ConnectionState.class);
    }

    @Test
    public void createDefaultFactory() throws Exception {
        final DataSourceFactory factory = new io.dropwizard.configuration.YamlConfigurationFactory(DataSourceFactory.class, BaseValidator.newValidator(), Jackson.newObjectMapper(), "dw").build(new ResourceConfigurationSourceProvider(), "yaml/minimal_db_pool.yml");
        assertThat(factory.getDriverClass()).isEqualTo("org.postgresql.Driver");
        assertThat(factory.getUser()).isEqualTo("pg-user");
        assertThat(factory.getPassword()).isEqualTo("iAMs00perSecrEET");
        assertThat(factory.getUrl()).isEqualTo("jdbc:postgresql://db.example.com/db-prod");
        assertThat(factory.getValidationQuery()).isEqualTo("/* Health Check */ SELECT 1");
        assertThat(factory.getValidationQueryTimeout()).isEqualTo(Optional.empty());
    }

    @Test
    public void metricsRecorded() throws Exception {
        dataSource();
        Map<String, Gauge> poolMetrics = metricRegistry.getGauges(MetricFilter.startsWith("io.dropwizard.db.ManagedPooledDataSource.test."));
        assertThat(poolMetrics.keySet()).contains("io.dropwizard.db.ManagedPooledDataSource.test.active", "io.dropwizard.db.ManagedPooledDataSource.test.idle", "io.dropwizard.db.ManagedPooledDataSource.test.waiting", "io.dropwizard.db.ManagedPooledDataSource.test.size", "io.dropwizard.db.ManagedPooledDataSource.test.created", "io.dropwizard.db.ManagedPooledDataSource.test.borrowed", "io.dropwizard.db.ManagedPooledDataSource.test.reconnected", "io.dropwizard.db.ManagedPooledDataSource.test.released", "io.dropwizard.db.ManagedPooledDataSource.test.releasedIdle", "io.dropwizard.db.ManagedPooledDataSource.test.returned", "io.dropwizard.db.ManagedPooledDataSource.test.removeAbandoned");
    }
}
