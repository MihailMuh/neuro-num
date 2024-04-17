package ru.lvmlabs.neuronum.baseconfigs.config.hibernate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ru.lvmlabs.neuronum.calls.model.Call;

import javax.sql.DataSource;

import static ru.lvmlabs.neuronum.baseconfigs.config.hibernate.BaseJpaConfiguration.entityManagerFactory;
import static ru.lvmlabs.neuronum.baseconfigs.config.hibernate.BaseJpaConfiguration.transactionManager;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "ru.lvmlabs.neuronum.calls.repository",
        entityManagerFactoryRef = "callsEntityManagerFactory",
        transactionManagerRef = "callsTransactionManager"
)
class CallsJpaConfiguration {
    @Value("${spring.profiles.active:}")
    private String activeProfiles;

    @Bean
    @ConfigurationProperties("spring.callssource")
    public DataSource callsDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean callsEntityManagerFactory() {
        return entityManagerFactory(callsDataSource(), activeProfiles.contains("dev") ? "create" : "validate", Call.class);
    }

    @Bean
    public PlatformTransactionManager callsTransactionManager() {
        return transactionManager(callsEntityManagerFactory());
    }
}
