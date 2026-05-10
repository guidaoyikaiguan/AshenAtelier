package com.shipin.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类，用于生成和验证JWT令牌
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret-key}")
    // 密钥，实际项目中应从配置文件读取，这里为了演示使用硬编码
    private String secretKey;

    @Value("${jwt.expire-time}")
    // 令牌过期时间：1天（毫秒），默认86400000毫秒=24小时
    private long expireTime;
    
    // 获取密钥
    private Key getKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }
    
    /**
     * 生成JWT令牌
     * @param userId 用户ID
     * @param email 用户邮箱
     * @return JWT令牌
     */
    public String generateToken(Integer userId, String email) {
        // 设置令牌的声明
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        
        // 设置令牌的过期时间
        Date expireDate = new Date(System.currentTimeMillis() + expireTime);
        
        // 生成令牌
        return Jwts.builder()
                .setClaims(claims) // 设置声明
                .setIssuedAt(new Date()) // 设置签发时间
                .setExpiration(expireDate) // 设置过期时间
                .signWith(getKey(), SignatureAlgorithm.HS256) // 设置签名算法和密钥
                .compact(); // 生成令牌
    }
    
    /**
     * 验证JWT令牌
     * @param token JWT令牌
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // 令牌无效或过期
            return false;
        }
    }
    
    /**
     * 从JWT令牌中获取用户信息
     * @param token JWT令牌
     * @return 用户信息
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * 从JWT令牌中获取用户ID
     * @param token JWT令牌
     * @return 用户ID，如果令牌无效则返回null
     */
    public Integer getUserIdFromToken(String token) {
        try {
            if (token == null) {
                return null;
            }
            Claims claims = getClaimsFromToken(token);
            return claims.get("userId", Integer.class);
        } catch (Exception e) {
            // 令牌无效或过期
            return null;
        }
    }
    
    /**
     * 从JWT令牌中获取用户邮箱
     * @param token JWT令牌
     * @return 用户邮箱
     */
    public String getEmailFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("email", String.class);
    }
    
    /**
     * 获取令牌过期时间（毫秒）
     * @return 过期时间（毫秒）
     */
    public long getExpireTime() {
        return expireTime;
    }
}
