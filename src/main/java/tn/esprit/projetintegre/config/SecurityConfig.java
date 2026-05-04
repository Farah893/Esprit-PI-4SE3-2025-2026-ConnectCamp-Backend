package tn.esprit.projetintegre.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import tn.esprit.projetintegre.security.JwtAuthenticationFilter;

import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final UserDetailsService userDetailsService;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .cors(cors -> cors.configurationSource(request -> {
                                        CorsConfiguration config = new CorsConfiguration();
                                        config.setAllowedOrigins(List.of(
                                                        "http://localhost:4200",
                                                        "http://localhost:3000"));
                                        config.setAllowedMethods(
                                                        List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                                        config.setAllowedHeaders(List.of("*"));
                                        config.setAllowCredentials(true);
                                        return config;
                                }))
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/auth/**",
                                                                "/api/public/**",
                                                                "/api/coupons/**",
                                                                "/api/wallets/**",
                                                                "/api/products/price/**",
                                                                "/uploads/**",
                                                                "/v3/api-docs/**",
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html",
                                                                "/error",
                                                                "/actuator/health",
                                                                "/actuator/info",
                                                                "/actuator/prometheus")
                                                .permitAll()
                                                .requestMatchers(org.springframework.http.HttpMethod.GET,
                                                                "/api/events/organizer/**")
                                                .authenticated()
                                                .requestMatchers(org.springframework.http.HttpMethod.GET,
                                                                "/api/sites/**",
                                                                "/api/reviews/site/**",
                                                                "/api/camp-highlights/site/**",
                                                                "/api/virtual-tours/site/**",
                                                                "/api/certifications/site/**",
                                                                "/api/sponsors/**",
                                                                "/api/events/**",
                                                                "/api/general-reviews/**",
                                                                "/api/event-services/**",
                                                                "/api/camping-services/**",
                                                                "/api/candidatures/**",
                                                                "/api/packs/**",
                                                                "/api/badges/**",
                                                                "/api/medals/**",
                                                                "/api/user-badges/**",
                                                                "/api/user-medals/**",
                                                                "/api/badge-rules/**")
                                                .permitAll()
                                                .requestMatchers(org.springframework.http.HttpMethod.POST,
                                                                "/api/reservations/event",
                                                                "/api/reservations/site",
                                                                "/api/tickets/purchase")
                                                .permitAll()
                                                .requestMatchers(org.springframework.http.HttpMethod.DELETE,
                                                                "/api/packs/**")
                                                .authenticated()
                                                .requestMatchers(org.springframework.http.HttpMethod.PATCH,
                                                                "/api/packs/**")
                                                .authenticated()
                                                .requestMatchers(org.springframework.http.HttpMethod.PUT,
                                                                "/api/packs/**")
                                                .authenticated()
                                                .requestMatchers(org.springframework.http.HttpMethod.POST,
                                                                "/api/badges/**",
                                                                "/api/medals/**",
                                                                "/api/badge-rules/**")
                                                .hasRole("ADMIN")
                                                .requestMatchers(org.springframework.http.HttpMethod.PUT,
                                                                "/api/badges/**",
                                                                "/api/medals/**",
                                                                "/api/badge-rules/**")
                                                .hasRole("ADMIN")
                                                .requestMatchers(org.springframework.http.HttpMethod.DELETE,
                                                                "/api/badges/**",
                                                                "/api/medals/**",
                                                                "/api/badge-rules/**")
                                                .hasRole("ADMIN")
                                                .anyRequest().authenticated())
                                .authenticationProvider(authenticationProvider())
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public AuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
                authProvider.setUserDetailsService(userDetailsService);
                authProvider.setPasswordEncoder(passwordEncoder());
                return authProvider;
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}
