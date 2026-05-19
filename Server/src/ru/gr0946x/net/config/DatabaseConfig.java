package ru.gr0946x.net.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "ru.gr0946x.net.repositories")
@ComponentScan(basePackages = {"ru.gr0946x.net.services", "ru.gr0946x.net.config"})
public class DatabaseConfig {

    // In-memory база — данные стираются при закрытии
    private static final String URL = "jdbc:h2:mem:kareta_db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";
    private static final String DRIVER = "org.h2.Driver";
    private static final String USER = "sa";
    private static final String PASS = "";

    @Bean
    public DataSource dataSource() {
        var ds = new DriverManagerDataSource(URL, USER, PASS);
        ds.setDriverClassName(DRIVER);
        return ds;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        var em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        // 🔥 Сканируем ВСЕ пакеты с сущностями
        em.setPackagesToScan("ru.gr0946x.net.entities");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        var props = new Properties();
        // 🔥 ВАЖНО: create-drop создаёт таблицы при старте и удаляет при закрытии
        props.put("hibernate.hbm2ddl.auto", "create-drop");
        props.put("hibernate.show_sql", "true");  // Показывать SQL в консоли
        props.put("hibernate.format_sql", "true"); // Красивый вывод SQL
        props.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");

        em.setJpaProperties(props);
        return em;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}