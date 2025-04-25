package org.example.onlinegradebookapp.security;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.io.Decoders;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    // Secret key in Base64 format
    private static final String JWT_SECRET = "d6tKZMHgcGE9M6g9sJER8JBXaimddV3R5dXn6nSBvQU3E5Amm7BWm9iNjnhKmAgR";

    // Generate token JWT for user/student
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails);
    }

    // Create token with additional data(claims) and UserDetails
    private String createToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts
                .builder()
                .setClaims(extraClaims) // Additional data
                .setSubject(userDetails.getUsername()) // email as subject
                .setIssuedAt(new Date(System.currentTimeMillis())) // generation timestamp
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 10)) // expiration time: 10min
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // signed with key
                .compact();
    }

    // Return signing key from encoded secret value
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(JWT_SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Extract username from token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extract expiration data from token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extract any data from token with function
    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    // Parse token and return all data(claims)
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Check if token is expired
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Check if token is valid and assigned to given user/student
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

}
