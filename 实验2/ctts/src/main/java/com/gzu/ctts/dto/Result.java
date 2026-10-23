package com.gzu.ctts.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果封装。
 *
 * <p>前后端分离场景下，服务端固定返回「状态码 + 提示信息 + 业务数据」三段式 JSON，
 * 前端只需判断 {@code code} 即可区分成功与失败，无需再解析 HTTP 状态码：
 * <pre>
 * { "code": 200, "message": "success", "data": { ... } }
 * </pre>
 *
 * <p>泛型 {@code T} 表示业务数据类型，配合静态工厂方法 {@code ok()} / {@code fail()}
 * 可以让 Controller 的返回语句保持简洁。
 *
 * @param <T> 业务数据类型
 */
@Data
@Schema(description = "统一响应结果（三段式结构）")
public class Result<T> implements Serializable {

    /** 成功状态码 */
    public static final int CODE_SUCCESS = 200;

    /** 参数错误状态码 */
    public static final int CODE_BAD_REQUEST = 400;

    /** 资源不存在状态码 */
    public static final int CODE_NOT_FOUND = 404;

    /** 业务状态码 */
    @Schema(description = "业务状态码：200 成功 / 400 参数错误 / 404 资源不存在 / 500 服务端异常",
            example = "200")
    private Integer code;

    /** 提示信息 */
    @Schema(description = "提示信息", example = "success")
    private String message;

    /** 业务数据 */
    @Schema(description = "业务数据，失败时为 null")
    private T data;

    private Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /** 成功，带数据 */
    public static <T> Result<T> ok(T data) {
        return new Result<>(CODE_SUCCESS, "success", data);
    }

    /** 成功，带数据与自定义提示 */
    public static <T> Result<T> ok(String message, T data) {
        return new Result<>(CODE_SUCCESS, message, data);
    }

    /** 失败，自定义状态码与提示 */
    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, message, null);
    }
}
