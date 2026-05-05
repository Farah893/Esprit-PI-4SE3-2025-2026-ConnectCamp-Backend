package tn.esprit.projetintegre.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:4200", "https://lively-wave-019e62b03.7.azurestaticapps.net")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }
}
