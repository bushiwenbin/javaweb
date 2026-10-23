package com.gzu.ctts.exception;

import com.gzu.ctts.dto.Result;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器。
 *
 * <p>{@code @RestControllerAdvice} 会拦截所有 Controller 抛出的异常，
 * 由下面的 {@code @ExceptionHandler} 方法按异常类型分别转换成统一 JSON 响应，
 * 这样每个接口只需要关心「成功路径」，异常分支被集中收口。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 业务异常：如项目不存在 */
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException e) {
        log.warn("业务异常：{}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * @RequestBody 上的 @Valid 校验失败。
     * 把每个字段的校验提示用「;」拼起来返回，前端可直接提示用户。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("请求体参数校验失败：{}", message);
        return Result.fail(Result.CODE_BAD_REQUEST, message);
    }

    /** 表单/参数绑定校验失败 */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return Result.fail(Result.CODE_BAD_REQUEST, message);
    }

    /** 方法参数（@RequestParam / @PathVariable）上的约束校验失败 */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .collect(Collectors.joining("; "));
        log.warn("方法参数校验失败：{}", message);
        return Result.fail(Result.CODE_BAD_REQUEST, message);
    }

    /**
     * 参数类型不匹配。
     *
     * <p>典型场景：{@code GET /api/projects/abc} 中 {@code {id}} 声明为 {@code Integer}，
     * 但请求传入的 "abc" 无法转换为整数。Spring 会抛
     * {@code MethodArgumentTypeMismatchException}，这里把它归一成 400，
     * 而不是让它落到兜底分支变成 500 —— 「参数格式错误」属于客户端错误，不是服务端故障。
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String message = "参数 " + e.getName() + " 类型不正确，期望 "
                + (e.getRequiredType() == null ? "未知" : e.getRequiredType().getSimpleName())
                + "，实际收到：'" + e.getValue() + "'";
        log.warn("参数类型不匹配：{}", message);
        return Result.fail(Result.CODE_BAD_REQUEST, message);
    }

    /** 请求方式不支持，例如用 GET 访问只接受 POST 的接口 */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方式不支持：{}", e.getMessage());
        return Result.fail(405, "请求方式不支持：" + e.getMethod());
    }

    /** 缺少必填的请求参数，例如 @RequestParam 未设默认值且未传参 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingParam(MissingServletRequestParameterException e) {
        log.warn("缺少必填请求参数：{}", e.getParameterName());
        return Result.fail(Result.CODE_BAD_REQUEST, "缺少必填参数：" + e.getParameterName());
    }

    /** 兜底：未预期的异常统一返回 500，同时把堆栈打到日志便于排查 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.fail(500, "服务器内部错误：" + e.getMessage());
    }
}
