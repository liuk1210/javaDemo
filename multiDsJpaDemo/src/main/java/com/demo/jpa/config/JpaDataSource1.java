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
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Objects;

/**
 *
 * 主库
 * <p>
 * 作者：Liuk
 * 创建日期：2023-11-30
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = {"com.demo.jpa.ds1.dao"},
        entityManagerFactoryRef = "ds1EntityManagerFactory",
        transactionManagerRef = "ds1TransactionManagerMaster"
)
public class JpaDataSource1 {

    //数据源实体类包所在位置
    private static final String entityPackagesToScan = "com.demo.jpa.ds1.entity";

    @Resource
    private JpaProperties jpaProperties;

    @Resource
    private HibernateProperties hibernateProperties;

    @Primary
    @Bean(value = "ds1")
    @ConfigurationProperties(prefix = "spring.datasource.ds1")
    public DataSource dataSource1(){
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean("ds1EntityManager")
    public EntityManager entityManager(EntityManagerFactoryBuilder builder){
        return Objects.requireNonNull(ds1EntityManagerFactory(builder).getObject()).createEntityManager();
    }

    @Primary
    @Bean(name = "ds1EntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean ds1EntityManagerFactory (EntityManagerFactoryBuilder builder){
        Map<String , Object> properties =
                hibernateProperties.determineHibernateProperties(
                        jpaProperties.getProperties(),
                        new HibernateSettings()
                );
        return builder.dataSource(dataSource1())
                .properties(properties)
                .packages(entityPackagesToScan)
                .persistenceUnit("ds1PersistenceUnit")
                .build();
    }
    @Primary
    @Bean(name = "ds1TransactionManagerMaster")
    public PlatformTransactionManager ds1TransactionManagerMaster(EntityManagerFactoryBuilder builder){
        return new JpaTransactionManager(Objects.requireNonNull(ds1EntityManagerFactory(builder).getObject()));
    }
}
