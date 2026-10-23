package com.gzu.ctts.exception;

/**
 * 业务异常。
 *
 * <p>Service / Controller 层遇到「项目不存在」「参数非法」等可预期的问题时抛出本异常，
 * 由 {@link GlobalExceptionHandler} 统一转换成标准 JSON 响应，
 * 避免在每个 Controller 方法里写重复的 if-else 与错误返回。
 */
public class BizException extends RuntimeException {

    /** 业务状态码 */
    private final int code;

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
