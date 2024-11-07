package com.demo.jpa.config;

import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 作者：Liuk
 * 创建日期：2023-11-30
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = {"com.demo.jpa.ds2.dao"},
        entityManagerFactoryRef = "ds2EntityManagerFactory",
        transactionManagerRef = "ds2TransactionManagerMaster"
)
public class JpaDataSource2 {

    //数据源实体类包所在位置
    private static final String entityPackagesToScan = "com.demo.jpa.ds2.entity";

    @Resource
    private JpaProperties jpaProperties;

    @Resource
    private HibernateProperties hibernateProperties;

    @Bean(value = "ds2")
    @ConfigurationProperties(prefix = "spring.datasource.ds2")
    public DataSource dataSource2(){
        return DataSourceBuilder.create().build();
    }

    @Bean("ds2EntityManager")
    public EntityManager entityManager(EntityManagerFactoryBuilder builder){
        return Objects.requireNonNull(ds2EntityManagerFactory(builder).getObject()).createEntityManager();
    }

    @Bean(name = "ds2EntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean ds2EntityManagerFactory (EntityManagerFactoryBuilder builder){
        Map<String , Object> properties =
                hibernateProperties.determineHibernateProperties(
                        jpaProperties.getProperties(),
                        new HibernateSettings()
                );
        return builder.dataSource(dataSource2())
                .properties(properties)
                .packages(entityPackagesToScan)
                .persistenceUnit("ds2PersistenceUnit")
                .build();
    }
    @Bean(name = "ds2TransactionManagerMaster")
    public PlatformTransactionManager ds2TransactionManagerMaster(EntityManagerFactoryBuilder builder){
        return new JpaTransactionManager(Objects.requireNonNull(ds2EntityManagerFactory(builder).getObject()));
    }
}
