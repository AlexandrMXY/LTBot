package backend.academy.scrapper;

import backend.academy.scrapper.entities.TrackedLink;
import backend.academy.scrapper.repositories.LinkRepository;
import backend.academy.scrapper.repositories.UserRepository;
import jakarta.persistence.EntityManagerFactory;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.Repository;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import javax.sql.DataSource;
import java.util.Properties;

@Testcontainers
@ActiveProfiles("testDb")
@EnableJpaRepositories
@AutoConfigureTestEntityManager
@EnableConfigurationProperties(value = ScrapperConfig.class)
@Import(AbstractDatabaseTest.DatabaseTestConfiguration.class)
@EnableTransactionManagement
@AutoConfigureDataJpa
public abstract class AbstractDatabaseTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("scrapper")
        .withUsername("test")
        .withPassword("test")
        .withInitScript("test-db-init.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }


    @TestConfiguration
    @ComponentScan("backend")
    public static class DatabaseTestConfiguration {
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


