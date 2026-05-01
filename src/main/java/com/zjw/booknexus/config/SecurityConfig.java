package com.zjw.booknexus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Spring Security 安全配置
 * <p>提供密码编码器等核心安全组件，用于用户密码的加密存储与校验。
 * 当前仅注册 {@link BCryptPasswordEncoder}，后续可扩展
 * Spring Security 过滤器链、CSRF 防护、会话管理等高级安全功能。</p>
 *
 * <p><b>涉及框架：</b>Spring Security（BCrypt 加密）</p>
 *
 * @author 张俊文
 * @since 2026-04-30
 */
@Configuration
public class SecurityConfig {

    /**
     * 创建密码编码器
     * <p>使用 BCrypt 强哈希算法对用户密码进行加密存储。
     * BCrypt 自动加盐且哈希强度可调（默认 10 轮），
     * 能够有效抵御彩虹表攻击与暴力破解。</p>
     *
     * <p>在用户注册时加密密码存入数据库，登录时使用相同编码器
     * 比对明文密码与密文是否匹配。</p>
     *
     * @return PasswordEncoder BCrypt 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
