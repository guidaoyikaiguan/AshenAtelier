package com.shipin.utils;


import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

/**
 * 密码加密工具类
 */
@Component
public class PasswordUtil {

    /**
     * MD5加密密码
     * @param password 明文密码
     * @return 加密后的密码
     */
    public String encryptPassword(String password) {
        if (password == null) {
            return null;
        }
        return DigestUtils.md5Hex(password);
    }

    /**
     * 验证密码
     * @param inputPassword 输入的明文密码
     * @param dbPassword 数据库中存储的加密密码
     * @return 是否匹配
     */
    public boolean verifyPassword(String inputPassword, String dbPassword) {
        if (inputPassword == null || dbPassword == null) {
            return false;
        }
        String encryptedInput = encryptPassword(inputPassword);
        return encryptedInput.equals(dbPassword);
    }
}