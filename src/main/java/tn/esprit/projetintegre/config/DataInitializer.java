package tn.esprit.projetintegre.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import tn.esprit.projetintegre.repositories.BadgeRepository;
import tn.esprit.projetintegre.repositories.MedalRepository;
import tn.esprit.projetintegre.repositories.BadgeRuleRepository;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final BadgeRepository badgeRepository;
    private final MedalRepository medalRepository;
    private final BadgeRuleRepository badgeRuleRepository;

    @Override
    public void run(String... args) {
        log.info("Gamification data initialization skipped - manual management requested.");
    }
}
