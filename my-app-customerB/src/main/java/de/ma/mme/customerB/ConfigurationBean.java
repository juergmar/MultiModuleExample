package de.ma.mme.customerB;

import de.ma.mme.customerB.config.AppConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigurationBean {

    @Bean
    public AppConfig appConfig() {
        return new AppConfig();
    }
}
