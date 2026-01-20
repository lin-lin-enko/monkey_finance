package com.lin.monkey.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
// Claims is what gets put into the token (e.g. username, roles etc.)
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

/* Means an object of this class will be created and put into container
 * So JwtUtil could be injected into any controller/service through
 * a constructor
 */

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secretString;

    private Key getSigningKey() {
        byte[] keyBytes = secretString.getBytes();
        if (keyBytes.length < 64) {
            throw new IllegalArgumentException("JWT secret key must be minimum 64 bytes");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private static final long EXPIRATION_TIME = 86400000;

    public String generateToken(UUID userId) {
        // a new jwt token is generated using a username
        return Jwts.builder()
                .setSubject(userId.toString()) // putting username into the token
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey()) // signing token with a random secret key
                .compact(); // assembling everything into a single string
    }

    private Claims extractClaims(String token) {
        // parsing a token
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey()) // using the signing key to validate token sign
                .build()// building a parser
                .parseClaimsJws(token) // turning token into JWS
                .getBody(); // getting Claims obj from parsing result
    }

    public UUID extractUserId(String token) {
        String subject = extractClaims(token).getSubject();
        return UUID.fromString(subject);
    }

    public boolean validateToken(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (Exception e) {
            System.out.println("Invalid JWT: " + e.getMessage());
            return false;
        }
    }
}
