package net.zethmayr.benjamin.spring.common;

import net.zethmayr.benjamin.spring.common.service.Breaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@SpringBootApplication
public class TestApplication {

    @Autowired
    private TestSingleDataSourceConfig dsConfig;

    @Autowired
    private Breaker breaker;

    @Bean
    public DataSource ds() {
        final DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.h2.Driver");
        ds.setUrl(dsConfig.getJdbcUrl());
        ds.setUsername(dsConfig.getUserName());
        ds.setPassword(dsConfig.getPassword());
        return ds;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(ds());
    }
    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(ds());
    }
}
