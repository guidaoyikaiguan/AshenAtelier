package com.shipin.utils;

/**
 * 用户上下文工具类，用于存储线程本地的用户信息
 */
public class UserContext {
    // 存储用户ID的ThreadLocal
    private static final ThreadLocal<Integer> userIdThreadLocal = new ThreadLocal<>();
    // 存储用户名的ThreadLocal
    private static final ThreadLocal<String> userNameThreadLocal = new ThreadLocal<>();

    /**
     * 设置用户ID
     * @param userId 用户ID
     */
    public static void setUserId(Integer userId) {
        userIdThreadLocal.set(userId);
    }

    /**
     * 获取用户ID
     * @return 用户ID
     */
    public static Integer getUserId() {
        return userIdThreadLocal.get();
    }

    /**
     * 设置用户名
     * @param userName 用户名
     */
    public static void setUserName(String userName) {
        userNameThreadLocal.set(userName);
    }

    /**
     * 获取用户名
     * @return 用户名
     */
    public static String getUserName() {
        return userNameThreadLocal.get();
    }

    /**
     * 清除上下文信息
     */
    public static void clear() {
        userIdThreadLocal.remove();
        userNameThreadLocal.remove();
    }
}