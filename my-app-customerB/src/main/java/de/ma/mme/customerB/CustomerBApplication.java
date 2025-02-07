package de.ma.mme.customerB;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"de.ma.mme.base", "de.ma.mme.customerB"})
public class CustomerBApplication {
    public static void main(String[] args) {
        SpringApplication.run(CustomerBApplication.class, args);
    }
}
