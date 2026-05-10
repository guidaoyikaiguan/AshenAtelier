package com.shipin.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 统一响应结果类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    /**
     * 响应状态：success表示成功，error表示错误
     */
    private String status;

    /**
     * 响应码：200表示成功，其他表示错误
     */
    private int code;

    /**
     * 响应消息
     */
    private String msg;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 成功响应
     * @param data 响应数据
     * @param <T> 数据类型
     * @return Result对象
     */
    public static <T> Result<T> success(T data) {
        return new Result<>("success", 200, "success", data);
    }

    /**
     * 成功响应（无数据）
     * @param <T> 数据类型
     * @return Result对象
     */
    public static <T> Result<T> success() {
        return new Result<>("success", 200, "success", null);
    }

    /**
     * 错误响应
     * @param code 错误码
     * @param msg 错误消息
     * @param <T> 数据类型
     * @return Result对象
     */
    public static <T> Result<T> error(int code, String msg) {
        return new Result<>("error", code, msg, null);
    }

    /**
     * 错误响应（默认错误码）
     * @param msg 错误消息
     * @param <T> 数据类型
     * @return Result对象
     */
    public static <T> Result<T> error(String msg) {
        return new Result<>("error", 500, msg, null);
    }
}
