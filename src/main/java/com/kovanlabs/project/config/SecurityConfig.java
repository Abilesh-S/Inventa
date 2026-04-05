package com.kovanlabs.project.config;

import com.kovanlabs.project.model.User;
import com.kovanlabs.project.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // ✅ USER DETAILS SERVICE
    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> {
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getEmail())
                    .password(user.getPassword())
                    .roles(user.getRole().name())
                    .build();
        };
    }

    // ✅ AUTH PROVIDER
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

    // ✅ AUTH MANAGER
    @Bean
    public AuthenticationManager authenticationManager(DaoAuthenticationProvider provider) {
        return new ProviderManager(provider);
    }

    // ✅ SINGLE FILTER CHAIN
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           DaoAuthenticationProvider provider) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authenticationProvider(provider)
                .authorizeHttpRequests(auth -> auth
                        // PUBLIC
                        .requestMatchers(
                                "/",
                                "/login",
                                "/api/users/register-owner",
                                "/api/users/login-owner",
                                "/api/users/login",
                                "/ingredients/**",
                                "/products/**",
                                "/oauth2/**",
                                "/oauth2login/**"
                        ).permitAll()
                        // OWNER
                        .requestMatchers("/api/users/create-manager", "/api/users/create-staff").hasRole("OWNER")
                        .requestMatchers(HttpMethod.GET, "/api/stock-requests/pending").hasRole("OWNER")
                        .requestMatchers(HttpMethod.POST,
                                "/api/stock-requests/*/approve",
                                "/api/stock-requests/*/reject").hasRole("OWNER")
                        .requestMatchers(HttpMethod.GET, "/api/alerts/owner/open").hasRole("OWNER")
                        .requestMatchers("/api/warehouse/**",
                                "/api/products/**",
                                "/api/recipes/**",
                                "/api/business/**",
                                "/api/branches/**").hasRole("OWNER")

                        // MANAGER
                        .requestMatchers(HttpMethod.POST, "/api/stock-requests").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/alerts/manager/open").hasRole("MANAGER")

                        // MULTI ROLE
                        .requestMatchers("/api/branch-inventory/**").hasAnyRole("OWNER", "MANAGER", "STAFF")
                        .requestMatchers("/api/bills/**").hasAnyRole("OWNER", "MANAGER", "STAFF")

                        // STAFF
                        .requestMatchers("/api/orders/**").hasRole("STAFF")

                        .anyRequest().authenticated()
                )

                // ✅ BASIC AUTH (for API)
                .httpBasic(Customizer.withDefaults())

                // ✅ OAUTH2 LOGIN (DEFAULT BEHAVIOR)
                .oauth2Login(oauth -> oauth
                        .loginPage("/login")
                        .defaultSuccessUrl("/home" , true)// redirects to your normal login page
                );

        return http.build();
    }
}