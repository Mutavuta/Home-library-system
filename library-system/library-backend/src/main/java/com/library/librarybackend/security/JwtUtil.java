package com.library.librarybackend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;

// Utility class for everything JWT-related
// creating tokens on login and reading them back on every protected request
@Component
public class JwtUtil {

    // Injected from application.properties - the secret key used to sign tokens
    @Value("${jwt.secret}")
    private String secret;

    // Injected from application.properties - how long a token lives in milliseconds
    @Value("${jwt.expiration}")
    private long expiration;

    // Converts the plain-text secret string into cryptographic key
    // Called internally before every sign/parse operation
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Builds and returns a signed JWT token
    // Called by AuthService right after a successful login
    public String generateToken(String userId, String email, String role) {
        return Jwts.builder()
                // userId goes in the "sub" (subject) field - main identity in the token
                .setSubject(userId)
                // extra claims embedded inside the token - readable without a DB call
                .addClaims(Map.of("email", email, "role", role))
                .setIssuedAt(new Date())
                // token expires after jwt.expiration milliseconds (default 24h)
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Reads the userId (subject) out of a token - used by JwtAuthFilter
    public String extractUserId(String token) {
        return parseClaims(token).getSubject();
    }

    // Reads the role claim - used by JwtAuthFilter to set Spring authorities
    public String extractRole(String token) {
        return (String) parseClaims(token).get("role");
    }

    // Reads the email claim - attached to the security context for convenience
    public String extractEmail(String token) {
        return (String) parseClaims(token).get("email");
    }

    // Returns true if the token signature is valid and it has not expired
    // Returns false for any invalid, tampered, or expired token
    public boolean validateToken(String token) {
        try{
            parseClaims(token);
            return true;
        }catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Parses the token and returns its claims (the payload data)
    // Throws JwtException if the token is invalid or expired
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}
