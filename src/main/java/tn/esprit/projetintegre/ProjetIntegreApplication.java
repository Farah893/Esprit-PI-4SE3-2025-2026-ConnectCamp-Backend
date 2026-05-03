package tn.esprit.projetintegre;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableScheduling   // ← AJOUTÉ : active le moteur de scheduling Spring


public class ProjetIntegreApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjetIntegreApplication.class, args);
    }
}
