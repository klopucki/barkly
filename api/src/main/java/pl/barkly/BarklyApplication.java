package pl.barkly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BarklyApplication {

    public static void main(String[] args) {
        SpringApplication.run(BarklyApplication.class, args);
    }
}
