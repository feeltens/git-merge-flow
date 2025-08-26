package com.feeltens.git.vo.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 响应结果 基础类
 *
 * @param <T>
 * @author feeltens
 * @date 2025-08-19
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class CloudResponse<T> {

    private int code;
    private String message;
    private T data;

    public static <T> CloudResponse<T> success() {
        return new CloudResponse<>(200, "success", null);
    }

    public static <T> CloudResponse<T> success(T data) {
        return new CloudResponse<>(200, "success", data);
    }

    public static <T> CloudResponse<T> fail(String message) {
        return fail(message, null);
    }

    public static <T> CloudResponse<T> fail(String message, T data) {
        return new CloudResponse<>(500, message, data);
    }

}