package ru.lvmlabs.neuronum.baseconfigs.config.hibernate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ru.lvmlabs.neuronum.users.model.User;

import javax.sql.DataSource;

import static ru.lvmlabs.neuronum.baseconfigs.config.hibernate.BaseJpaConfiguration.entityManagerFactory;
import static ru.lvmlabs.neuronum.baseconfigs.config.hibernate.BaseJpaConfiguration.transactionManager;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = {
                "ru.lvmlabs.neuronum.users.repository"
        },
        entityManagerFactoryRef = "usersEntityManagerFactory",
        transactionManagerRef = "usersTransactionManager"
)
public class UsersJpaConfiguration {
    @Value("${spring.profiles.active:}")
    private String activeProfiles;

    @Bean
    @Primary
    @ConfigurationProperties("spring.userssource")
    public DataSource usersDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean usersEntityManagerFactory() {
        return entityManagerFactory(usersDataSource(), activeProfiles.contains("dev") ? "create" : "validate", User.class);
    }

    @Bean
    @Primary
    public PlatformTransactionManager usersTransactionManager() {
        return transactionManager(usersEntityManagerFactory());
    }
}
