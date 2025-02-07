package de.ma.mme.customerB;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = {"de.ma.mme.base"})
@EnableJpaRepositories(basePackages = {"de.ma.mme.base"})
public class JpaConfig {
}
