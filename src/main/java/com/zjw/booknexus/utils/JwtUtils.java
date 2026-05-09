package com.zjw.booknexus.utils;

/**
 * JWT 令牌工具类
 * <p>基于 JJWT 库实现 JWT（JSON Web Token）的生成、解析和校验功能。
 * 采用 HMAC-SHA256 签名算法，支持 Access Token 和 Refresh Token 双令牌机制。</p>
 *
 * <p>令牌规格：</p>
 * <ul>
 *   <li>Access Token — 有效期 30 分钟，用于 API 请求认证，存储在客户端内存中</li>
 *   <li>Refresh Token — 有效期 7 天，用于刷新 Access Token，存储在 Redis 中</li>
 * </ul>
 *
 * <p>Claims 结构：</p>
 * <ul>
 *   <li>{@code userId} — 用户 ID（Long）</li>
 *   <li>{@code role} — 用户角色（String：ADMIN / USER）</li>
 *   <li>{@code type} — 令牌类型（String：access / refresh）</li>
 *   <li>{@code iat} / {@code exp} — 签发时间 / 过期时间</li>
 * </ul>
 *
 * @author 张俊文
 * @since 2026-04-30
 */

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtils {

    /** Access Token 过期时间：30 分钟 */
    private static final long ACCESS_EXPIRE = 30 * 60 * 1000L;
    /** Refresh Token 过期时间：7 天 */
    private static final long REFRESH_EXPIRE = 7 * 24 * 60 * 60 * 1000L;

    /** HMAC-SHA256 签名密钥，通过配置注入，生产环境应使用环境变量覆盖 */
    @Value("${jwt.secret}")
    private String secret;

    /** HMAC-SHA256 密钥对象（延迟初始化） */
    private volatile SecretKey secretKey;

    /**
     * 获取 HMAC-SHA256 密钥对象（双重检查锁延迟初始化）。
     * 密钥从配置属性 {@code jwt.secret} 注入，避免硬编码在源码中。
     *
     * @return SecretKey 实例
     */
    private SecretKey getSecretKey() {
        if (secretKey == null) {
            synchronized (this) {
                if (secretKey == null) {
                    secretKey = Keys.hmacShaKeyFor(secret.getBytes());
                }
            }
        }
        return secretKey;
    }

    /**
     * 生成 Access Token
     * <p>Access Token 有效期 30 分钟，用于 API 请求认证。
     * 生成后存储在客户端内存（非 localStorage），每次请求通过 Authorization 头携带。</p>
     *
     * @param userId 用户 ID
     * @param role   用户角色（ADMIN / USER）
     * @return 签名的 JWT 字符串
     */
    public String generateAccessToken(Long userId, String role) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("role", role)
                .claim("type", "access")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_EXPIRE))
                .signWith(getSecretKey())
                .compact();
    }

    /**
     * 生成 Refresh Token
     * <p>Refresh Token 有效期 7 天，仅用于在 Access Token 过期时获取新的 Access Token。
     * Refresh Token 同时存储在 Redis 中，支持服务端主动吊销。</p>
     *
     * @param userId 用户 ID
     * @param role   用户角色（ADMIN / USER）
     * @return 签名的 JWT 字符串
     */
    public String generateRefreshToken(Long userId, String role) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("role", role)
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRE))
                .signWith(getSecretKey())
                .compact();
    }

    /**
     * 解析 JWT Token
     * <p>验证签名并解析 Token 中的所有 Claims。</p>
     *
     * @param token JWT 字符串
     * @return 解析后的 Claims 对象
     * @throws JwtException 签名验证失败或 Token 已过期时抛出
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 验证 JWT Token 是否有效
     * <p>通过尝试解析 Token 来判断其签名和过期时间是否有效。</p>
     *
     * @param token JWT 字符串
     * @return true 如果 Token 有效（签名正确且未过期）
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
