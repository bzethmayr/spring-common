package net.zethmayr.benjamin.spring.common;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("ds")
@Accessors(chain = true)
@Getter
@Setter
public class TestSingleDataSourceConfig {
    private String jdbcUrl;
    private String userName;
    /**
     * Sure would like this to be able to be a byte array...
     */
    private String password;

    @Override
    public String toString() {
        return "TestSingleDataSourceConfig{" +
                "jdbcUrl='" + jdbcUrl + '\'' +
                ", userName='" + userName + '\'' +
                ", password=" + (null != password) +
                '}';
    }
}
