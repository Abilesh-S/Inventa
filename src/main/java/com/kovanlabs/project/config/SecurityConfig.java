package com.kovanlabs.project.config;

import com.kovanlabs.project.model.User;
import com.kovanlabs.project.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> {
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

            UserDetails principal = org.springframework.security.core.userdetails.User
                    .withUsername(user.getEmail())
                    .password(user.getPassword())
                    .roles(user.getRole().name())
                    .build();

            return principal;
        };
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(DaoAuthenticationProvider provider) {
        return new ProviderManager(provider);
    }
    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");

            response.getWriter().write("""
            {
                "error": "Invalid username or password"
            }
        """);
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, DaoAuthenticationProvider provider) throws Exception {
        http

                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authenticationProvider(provider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/register/**","/api/users/register-owner", "/api/users/login-owner", "/api/users/login").permitAll()
                        .requestMatchers("/api/users/create-manager", "/api/users/create-staff").hasRole("OWNER")
                        .requestMatchers(HttpMethod.POST, "/api/stock-requests").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/stock-requests/pending").hasRole("OWNER")
                        .requestMatchers(HttpMethod.POST, "/api/stock-requests/*/approve", "/api/stock-requests/*/reject").hasRole("OWNER")
                        .requestMatchers(HttpMethod.GET, "/api/alerts/owner/open").hasRole("OWNER")
                        .requestMatchers(HttpMethod.GET, "/api/alerts/manager/open").hasRole("MANAGER")
                        .requestMatchers("/api/warehouse/**").hasRole("OWNER")
                        .requestMatchers("/api/products/**", "/api/recipes/**", "/api/business/**", "/api/branches/**")
                        .hasAnyRole("OWNER", "MANAGER", "STAFF")
                        .requestMatchers("/api/branch-inventory/**").hasAnyRole("OWNER", "MANAGER", "STAFF")
                        .requestMatchers("/api/orders", "/api/orders/**").hasRole("STAFF")
                        .requestMatchers("/api/bills", "/api/bills/**").hasAnyRole("OWNER", "MANAGER", "STAFF")
                        .requestMatchers("/api/dashboard/**").hasAnyRole("OWNER", "MANAGER", "STAFF")
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .formLogin(form -> form
                        .failureHandler(authenticationFailureHandler())
                );;

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}