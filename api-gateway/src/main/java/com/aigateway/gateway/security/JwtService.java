package com.aigateway.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Everything to do with creating and checking JWT tokens lives here.
 *
 * Plain-English idea: when someone logs in successfully, we hand them
 * a signed "ticket" (the JWT). On every later request, they show us
 * that ticket instead of their password again. We just need to check
 * the ticket's signature and expiry date - we never store login
 * sessions on the server, which is what makes this "stateless".
 */
@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKeyBase64;

    @Value("${jwt.expiration-ms:3600000}")
    private long expirationMs;

    public String generateToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            Date expiration = extractAllClaims(token).getExpiration();
            return expiration.after(new Date());
        } catch (Exception ex) {
            // Any parsing/signature error means the token is not valid
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKeyBase64);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
