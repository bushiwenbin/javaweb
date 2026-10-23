package com.gzu.ctts.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增 / 修改用户的请求参数对象（DTO）。
 *
 * <p>为什么不直接把 {@code User} 实体当请求体？因为实体里存在
 * {@code id}、{@code createTime}、{@code deleted} 等「只能由服务端维护」的字段，
 * 若直接暴露给客户端，就可能被恶意请求篡改（例如伪造 id 覆盖他人数据）。
 * 用 DTO 只接收该由客户端提供的字段，是更稳妥的做法。
 *
 * <p>字段上的约束注解由 {@code @Valid} 触发，校验失败会被
 * {@code GlobalExceptionHandler} 统一转成 {@code code=400} 的 JSON 响应。
 */
@Data
@Schema(description = "新增/修改用户的请求体")
public class UserForm {

    @NotBlank(message = "登录账号不能为空")
    @Size(min = 3, max = 50, message = "登录账号长度需在 3~50 个字符之间")
    @Schema(description = "登录账号，必填，3~50 字符", example = "2024008",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    /** 新增时必填；修改时不传该字段表示保持原密码不变 */
    @Size(min = 6, max = 32, message = "密码长度需在 6~32 个字符之间")
    @Schema(description = "明文密码（服务端会转为 MD5 摘要后入库）。新增时必填，修改时可空",
            example = "123456")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "真实姓名长度不能超过 50 个字符")
    @Schema(description = "真实姓名，必填", example = "赵敏", requiredMode = Schema.RequiredMode.REQUIRED)
    private String realName;

    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "ADMIN|TEACHER|STUDENT", message = "角色只能是 ADMIN、TEACHER 或 STUDENT")
    @Schema(description = "角色：ADMIN-管理员，TEACHER-教师，STUDENT-学生", example = "STUDENT",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String role;

    @Email(message = "电子邮箱格式不正确")
    @Size(max = 100, message = "电子邮箱长度不能超过 100 个字符")
    @Schema(description = "电子邮箱", example = "2024008@qq.com")
    private String email;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号，11 位中国大陆号码", example = "13900000008")
    private String phone;

    /** 状态：1-启用 0-禁用，缺省为 1（启用） */
    @Min(value = 0, message = "状态只能是 0 或 1")
    @Max(value = 1, message = "状态只能是 0 或 1")
    @Schema(description = "状态：1-启用，0-禁用", example = "1", defaultValue = "1")
    private Integer status = 1;
}