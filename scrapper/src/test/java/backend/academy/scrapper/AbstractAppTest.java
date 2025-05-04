package backend.academy.scrapper;

import backend.academy.scrapper.configuration.ScrapperConfig;
import io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerAutoConfiguration;
import io.lettuce.core.RedisURI;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import java.io.File;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Properties;
import javax.sql.DataSource;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ActiveProfiles("testDb")
@EnableJpaRepositories
@AutoConfigureTestEntityManager
@EnableConfigurationProperties(value = ScrapperConfig.class)
@Import({AbstractAppTest.TestConfig.class, CircuitBreakerAutoConfiguration.class})
@EnableTransactionManagement
@AutoConfigureDataJpa
public abstract class AbstractAppTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withMinimumRunningDuration(Duration.ofSeconds(5L))
            .withDatabaseName("scrapper")
            .withUsername("test")
            .withPassword("test")
            .withCreateContainerCmdModifier(cmd -> {
                cmd.getHostConfig().withMemory(256 * 1024 * 1024L).withMemorySwap(256 * 1024 * 1024L);
            });

    @Container
    @ServiceConnection
    static GenericContainer<?> redis = new GenericContainer<>("redis:7.4")
            .withMinimumRunningDuration(Duration.ofSeconds(5L))
            .withExposedPorts(6379);

    static {
        System.out.println(new File("../migrations").getAbsolutePath());
        redis.start();
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        registry.add("spring.data.redis.host", () -> redis.getHost());
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("spring.liquibase.url", postgres::getJdbcUrl);
        registry.add("spring.liquibase.user", postgres::getUsername);
        registry.add("spring.liquibase.password", postgres::getPassword);
    }

    @TestConfiguration
    @ComponentScan("backend")
    public static class TestConfig {
        @PostConstruct
        private void migrate() throws SQLException, LiquibaseException {
            try (var connection = DriverManager.getConnection(
                    postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())) {
                var database =
                        DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
                var liquibase = new Liquibase("test-db-init.xml", new ClassLoaderResourceAccessor(), database);
                liquibase.update(new Contexts(), new LabelExpression());
            }
        }

        @Bean
        @Primary
        RedisConnectionFactory redisConnectionFactory() {
            var cfg = LettuceConnectionFactory.createRedisConfiguration(
                    new RedisURI(redis.getHost(), redis.getMappedPort(6379), Duration.ofSeconds(2)));
            return new LettuceConnectionFactory(cfg);
        }

        @Bean
        @Primary
        public DataSource dataSource() {
            PGSimpleDataSource dataSource = new PGSimpleDataSource();
            dataSource.setUrl(postgres.getJdbcUrl());
            dataSource.setUser(postgres.getUsername());
            dataSource.setPassword(postgres.getPassword());
            return dataSource;
        }

        private Properties jpaProperties() {
            Properties properties = new Properties();
            properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            return properties;
        }

        @Bean
        public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
            LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
            em.setDataSource(dataSource());
            em.setPackagesToScan("backend.academy.scrapper");
            JpaVendorAdapter jpaAdapter = new HibernateJpaVendorAdapter();
            em.setJpaVendorAdapter(jpaAdapter);
            em.setJpaProperties(jpaProperties());

            return em;
        }

        @Bean
        public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
            JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
            jpaTransactionManager.setEntityManagerFactory(emf);

            return jpaTransactionManager;
        }
    }
}
