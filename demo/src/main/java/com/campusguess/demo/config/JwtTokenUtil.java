package com.campusguess.demo.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtTokenUtil {

    @Value("${jwt.secret:zsu-planfinder-secret-key-change-in-production}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    // 原始方法：为 UserDetails 生成 token
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", userDetails.getUsername());
        claims.put("authorities", userDetails.getAuthorities());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // 新方法：为 User 实体生成 token（包含 userId）
    public String generateToken(com.campusguess.demo.model.entity.User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("userId", user.getId()); // 添加 userId
        claims.put("role", user.getRole()); // 添加角色
        claims.put("authorities", user.getAuthorities());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // 从请求头中提取token
    public String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // 从token中获取用户ID
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        Object userId = claims.get("userId");

        if (userId == null) {
            log.warn("Token中未找到userId");
            throw new JwtException("Token中未找到用户ID");
        }

        // 处理不同类型的返回值
        if (userId instanceof Long) {
            return (Long) userId;
        } else if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        } else if (userId instanceof String) {
            return Long.parseLong((String) userId);
        }

        throw new JwtException("无法解析用户ID");
    }

    // 从请求中获取用户ID
    public Long getUserIdFromRequest(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token == null) {
            throw new JwtException("未找到Token");
        }
        return getUserIdFromToken(token);
    }

    // 以下方法保持原有实现...
    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private Boolean isTokenExpired(String token) {
        final Date expirationDate = getExpirationDateFromToken(token);
        return expirationDate.before(new Date());
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimsFromToken(token).getExpiration();
    }

    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.error("Token已过期: {}", e.getMessage());
            throw e;
        } catch (JwtException e) {
            log.error("无效的Token: {}", e.getMessage());
            throw new JwtException("无效的Token");
        }
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}