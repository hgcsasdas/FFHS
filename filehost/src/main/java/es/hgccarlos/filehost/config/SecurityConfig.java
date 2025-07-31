package es.hgccarlos.filehost.config;

import es.hgccarlos.filehost.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {
   private final UserService userService;

   public SecurityConfig(UserService userService) {
       this.userService = userService;
   }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
    @Bean
    public UserDetailsService userDetailsService(
            @Value("${app.user}") String apiUsername,
            @Value("${app.password}") String apiPassword
    ) {
        return username -> {
            if (username.equals(apiUsername)) {
                return org.springframework.security.core.userdetails.User
                        .withUsername(apiUsername)
                        .password(new BCryptPasswordEncoder().encode(apiPassword))
                        .roles("ADMIN")
                        .build();
            }
            var u = userService.getByUsername(username);
            return org.springframework.security.core.userdetails.User
                    .withUsername(u.getUsername())
                    .password(u.getPassword())
                    .roles(u.getRole())
                    .disabled(!u.isEnabled())
                    .build();
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService uds,
            PasswordEncoder encoder
    ) {
        var provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(uds);
        provider.setPasswordEncoder(encoder);
        return new ProviderManager(provider);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           IpRateLimitFilter rateLimitFilter,
                                           ApiKeyFilter apiKeyFilter,
                                           JwtAuthFilter jwtAuthFilter
    ) throws Exception {
        http
                .addFilterBefore(rateLimitFilter, BasicAuthenticationFilter.class)
                .addFilterAfter(apiKeyFilter, BasicAuthenticationFilter.class)
                .addFilterBefore(jwtAuthFilter, BasicAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/api/buckets/**").hasRole("ADMIN")
                        .requestMatchers("/api/files/**").authenticated()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        .anyRequest().permitAll()
                )

                .httpBasic(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

}