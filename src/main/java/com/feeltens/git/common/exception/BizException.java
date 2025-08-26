package com.feeltens.git.common.exception;

import lombok.Getter;

/**
 * 业务异常
 *
 * @author feeltens
 * @date 2025-08-20
 */
@Getter
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 2886794063188251637L;

    private static final int DEFAULT_ERROR_CODE = 500;

    /**
     * 异常码
     */
    protected int code;

    /**
     * 异常信息
     */
    protected String errorMessage;

    public BizException() {
    }

    public BizException(Throwable cause) {
        super(cause);
    }

    public BizException(String message) {
        super(message);
    }

    public BizException(String message, Throwable cause) {
        super(message, cause);
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(int code, String errorMessage, String message) {
        super(message);
        this.code = code;
        this.errorMessage = errorMessage;
    }

    public BizException(BizExceptionCode bizExceptionCode) {
        super(bizExceptionCode.getMessage());
        this.code = bizExceptionCode.getCode();
        this.errorMessage = bizExceptionCode.getMessage();
    }

    public BizException(BizExceptionCode bizExceptionCode, Object... args) {
        super(String.format(bizExceptionCode.getMessage(), args));
        this.code = bizExceptionCode.getCode();
    }

    public BizException(String msgFormat, Object... args) {
        super(String.format(msgFormat, args));
    }

    public BizException(int code, String msgFormat, Object... args) {
        super(String.format(msgFormat, args));
        this.code = code;
    }

}