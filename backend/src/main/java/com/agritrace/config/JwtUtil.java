package com.agritrace.config;

import com.agritrace.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    @Value("${app.security.jwt-secret}")
    private String jwtSecret;

    @Value("${app.security.token-expire-hours}")
    private long tokenExpireHours;

    public String generateToken(User user) {
        Date issuedAt = new Date();
        Date expiration = new Date(issuedAt.getTime() + tokenExpireHours * 60 * 60 * 1000);

        return Jwts.builder()
                .setSubject(user.getUserId())
                .claim("username", user.getUsername())
                .claim("role", user.getRole())
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
    }
}
