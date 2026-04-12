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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.kovanlabs.project.security.JwtAuthenticationFilter;

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
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           DaoAuthenticationProvider provider,
                                           JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(provider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/users/register-owner", "/api/users/login-owner", "/api/users/login", "/api/users/login-jwt").permitAll()
                        .requestMatchers("/api/email/send-otp", "/api/email/verify-otp", "/api/email/forgot-password", "/api/email/reset-password").permitAll()
                        .requestMatchers("/api/email/change-password").authenticated()
                        // Needed for public owner sign-up flow (create business first, then owner user)
                        .requestMatchers(HttpMethod.POST, "/api/business").permitAll()
                        .requestMatchers("/api/users/create-manager", "/api/users/create-staff").hasRole("OWNER")
                        .requestMatchers(HttpMethod.GET, "/api/users/branch/**").hasAnyRole("OWNER", "MANAGER")
                        // Manager can register staff for their own branch
                        .requestMatchers(HttpMethod.POST, "/api/users/create-staff").hasAnyRole("OWNER", "MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/stock-requests").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/stock-requests/pending").hasRole("OWNER")
                        .requestMatchers(HttpMethod.POST, "/api/stock-requests/*/approve", "/api/stock-requests/*/reject").hasRole("OWNER")
                        .requestMatchers(HttpMethod.GET, "/api/alerts/owner/open").hasRole("OWNER")
                        .requestMatchers(HttpMethod.GET, "/api/alerts/manager/open").hasRole("MANAGER")
                        // Managers can read warehouse inventory (for ingredient suggestions in product forms)
                        .requestMatchers(HttpMethod.GET, "/api/warehouse/inventory").hasAnyRole("OWNER", "MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/warehouse/inventory/**").hasAnyRole("OWNER", "MANAGER")
                        // All other warehouse operations (create, update, delete) remain OWNER only
                        .requestMatchers("/api/warehouse/**").hasRole("OWNER")
                        .requestMatchers("/api/products/**", "/api/recipes/**", "/api/business/**", "/api/branches/**")
                        .hasAnyRole("OWNER", "MANAGER", "STAFF")
                        .requestMatchers("/api/branch-inventory/**").hasAnyRole("OWNER", "MANAGER", "STAFF")
                        .requestMatchers("/api/orders", "/api/orders/**").hasAnyRole("OWNER", "MANAGER", "STAFF")
                        .requestMatchers("/api/bills", "/api/bills/**").hasAnyRole("OWNER", "MANAGER", "STAFF")
                        .requestMatchers("/api/dashboard/**").hasAnyRole("OWNER", "MANAGER", "STAFF")
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());

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