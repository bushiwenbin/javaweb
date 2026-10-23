package com.gzu.ctts.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 新增 / 修改实训项目的请求参数对象（DTO）。
 *
 * <p>相比实验二的同名类，本类的 {@code deadline} 由 {@code String} 改为
 * {@link LocalDate}：请求体中的 {@code "2026-11-30"} 由 Jackson 直接
 * 反序列化成日期对象，Service 层不必再做字符串解析。
 * 若格式不合法（如写成 {@code "2026/11/30"}），Jackson 会抛
 * {@code HttpMessageNotReadableException}，由全局异常处理器归一为 {@code code=400}。
 */
@Data
@Schema(description = "新增/修改实训项目的请求体")
public class ProjectForm {

    @NotBlank(message = "项目名称不能为空")
    @Size(max = 100, message = "项目名称长度不能超过 100 个字符")
    @Schema(description = "项目名称，必填，最长 100 字符", example = "实训成果答辩系统",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Size(max = 50, message = "项目类别长度不能超过 50 个字符")
    @Schema(description = "项目类别", example = "Web 后端")
    private String category;

    @Size(max = 50, message = "指导教师长度不能超过 50 个字符")
    @Schema(description = "指导教师", example = "丁玺润")
    private String teacher;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "截止日期，格式 yyyy-MM-dd", example = "2027-01-08")
    private LocalDate deadline;

    /** 状态：0-未开始 1-进行中 2-已完成，缺省为 0 */
    @Min(value = 0, message = "状态只能是 0、1 或 2")
    @Max(value = 2, message = "状态只能是 0、1 或 2")
    @Schema(description = "状态：0-未开始，1-进行中，2-已完成", example = "1", defaultValue = "0")
    private Integer status = 0;

    @Size(max = 500, message = "项目描述长度不能超过 500 个字符")
    @Schema(description = "项目描述，最长 500 字符", example = "支持答辩分组、评分与结果公示。")
    private String description;
}