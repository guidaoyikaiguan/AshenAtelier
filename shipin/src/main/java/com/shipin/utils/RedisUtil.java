package com.shipin.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类
 */
@Component
public class RedisUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // ============================== 通用方法 ==============================

    /**
     * 判断key是否存在
     * @param key 键
     * @return true 存在 false 不存在
     */
    public boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除key
     * @param key 键
     * @return true 删除成功 false 删除失败
     */
    public boolean del(String key) {
        try {
            return redisTemplate.delete(key);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除多个key
     * @param keys 键集合
     * @return 删除的数量
     */
    public long del(String... keys) {
        try {
            List<String> keyList = new ArrayList<>();
            for (String key : keys) {
                keyList.add(key);
            }
            return redisTemplate.delete(keyList);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 设置过期时间
     * @param key 键
     * @param time 时间(秒)
     * @return true 设置成功 false 设置失败
     */
    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取过期时间
     * @param key 键
     * @return 时间(秒) -1 表示永久有效
     */
    public long getExpire(String key) {
        try {
            return redisTemplate.getExpire(key, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    // ============================== String类型 ==============================

    /**
     * 获取值
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 设置值
     * @param key 键
     * @param value 值
     * @return true 设置成功 false 设置失败
     */
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 设置值并设置过期时间
     * @param key 键
     * @param value 值
     * @param time 时间(秒) -1 表示永久有效
     * @return true 设置成功 false 设置失败
     */
    public boolean set(String key, Object value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 递增
     * @param key 键
     * @param delta 递增量
     * @return 递增后的值
     */
    public long incr(String key, long delta) {
        try {
            return redisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 递减
     * @param key 键
     * @param delta 递减量
     * @return 递减后的值
     */
    public long decr(String key, long delta) {
        try {
            return redisTemplate.opsForValue().increment(key, -delta);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // ============================== 分布式锁 ==============================

    /**
     * 获取分布式锁
     * @param key 锁的键
     * @param value 锁的值（建议使用UUID，用于释放锁时的验证）
     * @param expireTime 过期时间（毫秒）
     * @return true 获取成功 false 获取失败
     */
    public boolean acquireLock(String key, String value, long expireTime) {
        try {
            return redisTemplate.opsForValue().setIfAbsent(key, value, expireTime, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 释放分布式锁
     * @param key 锁的键
     * @param value 锁的值（用于验证，防止误释放）
     * @return true 释放成功 false 释放失败
     */
    public boolean releaseLock(String key, String value) {
        try {
            // 先获取锁的值，验证是否是自己的锁
            Object currentValue = redisTemplate.opsForValue().get(key);
            if (currentValue != null && currentValue.equals(value)) {
                // 是自己的锁，释放
                return redisTemplate.delete(key);
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ============================== 限流 ==============================

    /**
     * 基于Redis的令牌桶算法实现限流
     * @param key 限流的键（如接口路径或用户ID）
     * @param limit 时间窗口内的最大请求数
     * @param window 时间窗口大小（毫秒）
     * @return true 允许请求 false 拒绝请求
     */
    public boolean isAllowed(String key, int limit, long window) {
        try {
            // 当前时间戳
            long currentTime = System.currentTimeMillis();
            // 计算时间窗口的开始时间
            long windowStart = currentTime - window;
            
            // 使用Redis的ZSET存储请求记录，score为时间戳
            String zsetKey = "rate_limit:" + key;
            
            // 移除时间窗口外的请求记录
            redisTemplate.opsForZSet().removeRangeByScore(zsetKey, 0, windowStart);
            
            // 获取当前时间窗口内的请求数
            long count = redisTemplate.opsForZSet().zCard(zsetKey);
            
            if (count < limit) {
                // 请求数未达到限制，添加当前请求记录
                redisTemplate.opsForZSet().add(zsetKey, String.valueOf(currentTime), currentTime);
                // 设置ZSET的过期时间，避免内存泄漏
                redisTemplate.expire(zsetKey, window, TimeUnit.MILLISECONDS);
                return true;
            } else {
                // 请求数达到限制，拒绝请求
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 异常时默认允许请求，避免因Redis问题导致服务不可用
            return true;
        }
    }
}
