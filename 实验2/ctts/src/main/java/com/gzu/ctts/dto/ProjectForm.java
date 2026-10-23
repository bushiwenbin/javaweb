package com.gzu.ctts.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增实训项目的请求参数对象（DTO）。
 *
 * <p>用于演示 {@code @RequestBody} 接收 JSON 请求体：
 * Spring MVC 借助 Jackson 把请求体里的 JSON 反序列化成该对象，
 * 再配合 {@code @Valid} 触发 JSR-380 参数校验。
 */
@Data
@Schema(description = "新增实训项目的请求体")
public class ProjectForm {

    @NotBlank(message = "项目名称不能为空")
    @Size(max = 100, message = "项目名称长度不能超过 100 个字符")
    @Schema(description = "项目名称，必填，最长 100 字符", example = "实训成果答辩系统", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "项目类别", example = "Web 后端")
    private String category;

    @Schema(description = "指导教师", example = "丁玺润")
    private String teacher;

    @Schema(description = "截止日期，格式 yyyy-MM-dd", example = "2027-01-08")
    private String deadline;

    /** 状态：0-未开始 1-进行中 2-已完成，缺省为 0 */
    @Schema(description = "状态：0-未开始，1-进行中，2-已完成", example = "1", defaultValue = "0")
    private Integer status = 0;

    @Size(max = 500, message = "项目描述长度不能超过 500 个字符")
    @Schema(description = "项目描述，最长 500 字符", example = "支持答辩分组、评分与结果公示。")
    private String description;
}
