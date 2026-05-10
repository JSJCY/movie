package com.movie.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

public class JwtUtil {

    private static final String SECRET = "movie-system-jwt-secret-key-2026-must-be-at-least-256-bits-long!!";
    private static final long ACCESS_TOKEN_EXPIRE = 1000L * 60 * 60 * 24;    // 24 小时
    private static final long REFRESH_TOKEN_EXPIRE = 1000L * 60 * 60 * 24 * 7; // 7 天

    private static SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    public static String createAccessToken(Long userId, String username, String role) {
        return Jwts.builder()
                .claims(Map.of("userId", userId, "username", username, "role", role))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRE))
                .signWith(getKey())
                .compact();
    }

    public static String createRefreshToken(Long userId) {
        return Jwts.builder()
                .claims(Map.of("userId", userId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRE))
                .signWith(getKey())
                .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static Long getUserId(Claims claims) {
        return claims.get("userId", Long.class);
    }

    public static String getUsername(Claims claims) {
        return claims.get("username", String.class);
    }

    public static String getRole(Claims claims) {
        return claims.get("role", String.class);
    }

    public static boolean isAdmin(Claims claims) {
        return "ADMIN".equals(getRole(claims));
    }
}
