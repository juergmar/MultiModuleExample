package de.ma.mme.customerA;

import de.ma.mme.base.Customer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication(scanBasePackages = {"de.ma.mme.customerA", "de.ma.mme.base"})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
