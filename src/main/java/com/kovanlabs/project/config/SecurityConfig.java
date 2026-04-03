package com.kovanlabs.project.config;

import com.kovanlabs.project.model.User;
import com.kovanlabs.project.repository.UserRepository;
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
    public SecurityFilterChain filterChain(HttpSecurity http, DaoAuthenticationProvider provider) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authenticationProvider(provider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/register-owner", "/api/users/login-owner", "/api/users/login").permitAll()
                        .requestMatchers("/api/users/create-manager", "/api/users/create-staff").hasRole("OWNER")
                        .requestMatchers(HttpMethod.POST, "/api/stock-requests").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/stock-requests/pending").hasRole("OWNER")
                        .requestMatchers(HttpMethod.POST, "/api/stock-requests/*/approve", "/api/stock-requests/*/reject").hasRole("OWNER")
                        .requestMatchers(HttpMethod.GET, "/api/alerts/owner/open").hasRole("OWNER")
                        .requestMatchers(HttpMethod.GET, "/api/alerts/manager/open").hasRole("MANAGER")
                        .requestMatchers("/api/warehouse/**", "/api/products/**", "/api/recipes/**", "/api/business/**", "/api/branches/**")
                        .hasRole("OWNER")
                        .requestMatchers("/api/branch-inventory/**").hasAnyRole("OWNER", "MANAGER", "STAFF")
                        .requestMatchers("/api/orders/**").hasRole("STAFF")
                        .requestMatchers("/api/bills/**").hasAnyRole("OWNER", "MANAGER", "STAFF")
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}