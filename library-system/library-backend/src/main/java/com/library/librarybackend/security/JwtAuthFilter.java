package com.library.librarybackend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// Runs once on every incoming HTTP request - before it reaches any controller
// Its job: check if the request has a valid JWT, and if so, tell Spring Security
// who the user is so the endpoint can trust them
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    // Spring Framework injects JwtUtil
    @Autowired
    private JwtUtil jwtUtil;

    // Intercepts every incoming HTTP request before it reaches any controller
    // Its job is to check if the request carries a valid JWT token in the Authorization header
    // If the token is valid - it stamps the request as "authenticated" so SecurityConfig
    // can allow it through to the correct endpoint
    // If there is no token, or it is invalid - the request passes through unauthenticated
    // and SecurityConfig will decide whether to block it or allow it (public endpoints still pass)
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Read the Authorization header from the incoming request
        String authHeader = request.getHeader("Authorization");

        // Check if the header exists and starts with "Bearer " - if not, skip JWT processing
        // Requests to public endpoint like /auth/login won't have this header at all
        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            // Cut off the first 7 characters ("Bearer ") to get just the raw token string
            String token = authHeader.substring(7);

            // Check if the token's signature is valid and it hasn't expired
            if (jwtUtil.validateToken(token)) {

                // Pull the user's identity out of the token - no database call needed
                // All three values were baked into the token when it was created at login
                String userId = jwtUtil.extractUserId(token);
                String role   = jwtUtil.extractRole(token);
                String email  = jwtUtil.extractEmail(token);

                // Build a Spring Security "authentication stamp" for this request
                // null in the middle = no password needed (token already provided identity)
                // ROLE_ prefix is required by Spring's hasRole() checks in securityConfig
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userId, null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                        );

                // Attach the email as extra details on the authentication object
                auth.setDetails(email);

                // Store the authentication in the security context for this request
                // SecurityConfig rules will treat the request as logged in
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        // Always pass the request to the next filter or controller regardless of login status
        // SecurityConfig will block it if the endpoint requires auth and none was found
        filterChain.doFilter(request, response);
    }

}
