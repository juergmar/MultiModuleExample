package de.ma.mme.customerA;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import de.ma.mme.annotation.GenerateTranslations;

@SpringBootApplication(scanBasePackages = {"de.ma.mme.customerA", "de.ma.mme.base"})
@GenerateTranslations(
        value = "translations.json",
        packageName = "de.ma.mme.customerA",
        enumName = "Translations",
        useLombok = true
)
public class CustomerAApplication {
    public static void main(String[] args) {
        SpringApplication.run(CustomerAApplication.class, args);
    }
}
