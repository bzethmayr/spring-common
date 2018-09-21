package net.zethmayr.benjamin.spring.common;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("ds")
public class TestSingleDataSourceConfig {
    private String jdbcUrl;
    private String userName;
    /**
     * Sure would like this to be able to be a byte array...
     */
    private String password;

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public TestSingleDataSourceConfig setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public TestSingleDataSourceConfig setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public TestSingleDataSourceConfig setPassword(String password) {
        this.password = password;
        return this;
    }

    @Override
    public String toString() {
        return "TestSingleDataSourceConfig{" +
                "jdbcUrl='" + jdbcUrl + '\'' +
                ", userName='" + userName + '\'' +
                ", password=" + (null != password) +
                '}';
    }
}
