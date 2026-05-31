package org.example.aquabackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AquaBackEndApplication {

    public static void main(String[] args) {
        SpringApplication.run(AquaBackEndApplication.class, args);
    }

}
    
