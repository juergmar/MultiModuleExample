package de.ma.mme.customerA;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"de.ma.mme.customerA", "de.ma.mme.base"})
public class CustomerAApplication {
    public static void main(String[] args) {
        SpringApplication.run(CustomerAApplication.class, args);
    }
}
