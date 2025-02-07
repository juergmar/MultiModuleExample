package de.ma.mme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication(scanBasePackages = {"de.ma.mme"})
@EntityScan(
        basePackages = {"com.example.base.entity", "com.example.customerA.entity"},
        excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {Customer.class}  // Exclude the base Customer entity
        )
)
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
