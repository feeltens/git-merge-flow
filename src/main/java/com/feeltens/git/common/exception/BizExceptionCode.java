package com.feeltens.git.common.exception;

/**
 * 封装业务的异常信息,模块自己实现接口
 *
 * @author ：feeltens
 * @date ：2025-08-20
 */
public interface BizExceptionCode {

    /**
     * 异常码
     * <p>
     * 由各个模块自定义模块开头，不进行枚举约束
     *
     * @return code
     */
    Integer getCode();

    /**
     * 异常信息
     *
     * @return message
     */
    String getMessage();

}