package ru.lvmlabs.neuronum.baseconfigs.config.hibernate;

import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;

public abstract class BaseJpaConfiguration {
    public static LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource, String action, Class<?> model) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan(model.getPackageName());
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        em.setPersistenceUnitName(model.getSimpleName().toLowerCase());
        em.setJpaPropertyMap(new HashMap<>() {{
            put("hibernate.jdbc.time_zone", "Asia/Yekaterinburg");
            put("hibernate.show_sql", true);
            put("hibernate.hbm2ddl.auto", action);
        }});

        return em;
    }

    public static PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean entityManagerFactoryBean) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactoryBean.getObject());
        return transactionManager;
    }
}
