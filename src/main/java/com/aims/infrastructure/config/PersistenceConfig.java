package com.aims.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;
import java.util.Properties;

/**
 * Persistence configuration for JPA and database settings
 * Configures entity scanning, repository interfaces, and transaction management
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.aims.infrastructure.persistence.jpa",
    transactionManagerRef = "jpaTransactionManager"
)
@EntityScan(basePackages = {
    "com.aims.domain.product.entity",
    "com.aims.domain.cart.entity", 
    "com.aims.domain.order.entity",
    "com.aims.domain.user.entity",
    "com.aims.domain.payment.entity"
})
@EnableJpaAuditing
public class PersistenceConfig {

    /**
     * Production database configuration
     */
    @Bean
    @Profile("prod")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource productionDataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * Development database configuration (H2 in-memory)
     */
    @Bean
    @Profile({"dev", "default"})
    public DataSource developmentDataSource() {
        return DataSourceBuilder.create()
                .driverClassName("org.h2.Driver")
                .url("jdbc:h2:mem:aimsdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
                .username("sa")
                .password("")
                .build();
    }



    /**
     * JPA Entity Manager Factory
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan(
            "com.aims.domain.product.entity",
            "com.aims.domain.cart.entity",
            "com.aims.domain.order.entity", 
            "com.aims.domain.user.entity",
            "com.aims.domain.payment.entity"
        );

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(hibernateProperties());

        return em;
    }

    /**
     * JPA Transaction Manager
     */
    @Bean
    public JpaTransactionManager jpaTransactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }

    /**
     * Hibernate-specific properties
     */
    private Properties hibernateProperties() {
        Properties properties = new Properties();
        properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.format_sql", "true");
        properties.put("hibernate.use_sql_comments", "true");
        properties.put("hibernate.jdbc.batch_size", "20");
        properties.put("hibernate.jdbc.fetch_size", "50");
        return properties;
    }
}
