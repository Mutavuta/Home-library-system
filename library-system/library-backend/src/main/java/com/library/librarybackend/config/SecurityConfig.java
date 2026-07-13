package com.library.librarybackend.config;

import com.library.librarybackend.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

// The rulebook for the entire API
// Decides which endpoints are public, which need login, and which are admin-only
// Also sets up CORS and password hashing
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    // Comes from application.properties - which websites can call this API
    @Value("${cors.allowed.origins}")
    private String allowedOrigins;

    // Defines the access rules for every endpoint in the system
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disabled - not needed for APIs that use JWT instead of browser sessions
                .csrf(csrf -> csrf.disable())
                // Apply the CORS rules defined below
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth

                // Public - anyone can call these without logging in
                        .requestMatchers("/auth/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/books/titles", "/books/titles/**").permitAll()

                                // Logged-in borrowers only
                                .requestMatchers(HttpMethod.GET,     "/loans/mine").authenticated()
                                .requestMatchers(HttpMethod.GET,     "/holds/mine").authenticated()
                                .requestMatchers(HttpMethod.POST,    "/holds").authenticated()
                                .requestMatchers(HttpMethod.DELETE,  "/holds/{id}").authenticated()
                                .requestMatchers(HttpMethod.GET,     "/notifications/**").authenticated()
                                .requestMatchers(HttpMethod.POST,    "/notifications/mark-read").authenticated()
                                .requestMatchers(HttpMethod.GET,     "/users/me").authenticated()

                                // Admin only
                                .requestMatchers("/books/admin/**").hasRole("ADMIN")
                                .requestMatchers("/books/barcode/**").hasRole("ADMIN")
                                .requestMatchers("/holds/admin/**").hasRole("ADMIN")
                                .requestMatchers("/loans/admin/**").hasRole("ADMIN")
                                .requestMatchers("/users/admin/**").hasRole("ADMIN")
                                // set-admin is a one-time endpoint - only an admin can call it
                                .requestMatchers("/auth/setup-admin").hasRole("ADMIN")

                                // Anything not listed above also requires login
                                .anyRequest().authenticated()
                )
                // Run our JWT filter before Spring's default login filter on every request
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Defines which websites are allowed to send requests are allowed to send requests to this API from a browser
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Split the comma-separated origins from application.properties into a list
        config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        // Allow all headers - the website will send Authorization, Content-Type etc.
        config.setAllowedHeaders(List.of("*"));
        // Required for the Authorization header to be sent with cookies
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply these CORS rules to every URL path
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // BCrypt is a slow hashing algorithm - intentionally slow to make brute-force hard
    // Used by AuthService when registering and verifying passwords
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Exposes Spring's AuthenticationManager as a bean - needed by AuthService
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}
