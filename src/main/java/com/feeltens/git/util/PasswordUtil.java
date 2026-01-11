package com.feeltens.git.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码工具类
 *
 * @author feeltens
 * @date 2026-01-11
 */
public class PasswordUtil {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    /**
     * 加密密码
     */
    public static String encode(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /**
     * 验证密码
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return ENCODER.matches(rawPassword, encodedPassword);
    }

    /**
     * 生成密码（用于初始化数据）
     */
    public static void main(String[] args) {
        String password = "admin123";
        String encoded = encode(password);
        System.out.println("原始密码: " + password);
        System.out.println("加密后: " + encoded);
        System.out.println("验证结果: " + matches(password, encoded));
    }

}