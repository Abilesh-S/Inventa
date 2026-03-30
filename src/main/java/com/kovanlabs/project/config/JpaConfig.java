package com.kovanlabs.project.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = "com.kovanlabs.project")
@EnableJpaRepositories(basePackages = "com.kovanlabs.project")
@PropertySource("classpath:db.properties")
public class JpaConfig {
    private final Environment env;
    public JpaConfig(Environment env) {
        this.env=env;
    }

    @Bean
    public DataSource dataSource(){
        HikariDataSource ds=new HikariDataSource();
        ds.setDriverClassName(env.getProperty("db.driver"));
        ds.setJdbcUrl(env.getProperty("db.url"));
        ds.setUsername(env.getProperty("db.username"));
        ds.setPassword(env.getProperty("db.password"));
        return ds;
    }
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(){
        LocalContainerEntityManagerFactoryBean emf=new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource());
        emf.setPackagesToScan("com.kovanlabs.project.model");
        HibernateJpaVendorAdapter adapter=new HibernateJpaVendorAdapter();
        emf.setJpaVendorAdapter(adapter);
        Properties props=new Properties();
        props.put("hibernate.dialect","org.hibernate.dialect.MySQL8Dialect");
        props.put("hibernate.hbm2ddl.auto","update");
        props.put("hibernate.show_sql","true");
        emf.setJpaProperties(props);
        return emf;
    }
    @Bean
    public JpaTransactionManager transactionManager(){
        JpaTransactionManager tx=new JpaTransactionManager();
        tx.setEntityManagerFactory(entityManagerFactory().getObject());
        return tx;
    }
}