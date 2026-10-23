package com.gzu.ctts.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实训项目实体。
 *
 * <p>本实验阶段只作为内存中的模拟数据载体（POJO），
 * 后续实验接入 MyBatis-Plus 后，本类将加上 {@code @TableName("tb_task")} 等注解，
 * 变成真正的数据库映射实体（Entity / PO）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "实训项目")
public class Project {

    /** 项目主键 ID */
    @Schema(description = "项目主键 ID", example = "3")
    private Integer id;

    /** 项目名称 */
    @Schema(description = "项目名称", example = "实训成果统计报表")
    private String name;

    /** 项目类别，如「Web 后端」「算法训练」 */
    @Schema(description = "项目类别", example = "数据分析")
    private String category;

    /** 指导教师 */
    @Schema(description = "指导教师", example = "李敏")
    private String teacher;

    /** 截止日期，格式 yyyy-MM-dd */
    @Schema(description = "截止日期，格式 yyyy-MM-dd", example = "2026-11-30")
    private String deadline;

    /** 状态：0-未开始 1-进行中 2-已完成 */
    @Schema(description = "状态：0-未开始，1-进行中，2-已完成", example = "1", allowableValues = {"0", "1", "2"})
    private Integer status;

    /** 项目描述 */
    @Schema(description = "项目描述", example = "按班级、项目维度统计实训提交率与平均得分。")
    private String description;
}
