package com.gzu.ctts.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 实训项目实体 —— 映射数据表 {@code training_project}。
 *
 * <p>实验二中本类只是内存模拟数据的载体（POJO）；实验三为它加上
 * MyBatis-Plus 的映射注解，使它成为真正与数据库表一一对应的实体（Entity / PO）。
 *
 * <p>本类演示 {@code @TableField} 的两种典型用法：
 * <ul>
 *   <li>{@code @TableField("project_name")}：属性 {@code name} 与列
 *       {@code project_name} 不同名，驼峰规则无法自动推导，必须显式指定，
 *       否则 MyBatis-Plus 会按属性名去找 {@code name} 列并抛「Unknown column」；</li>
 *   <li>{@code @TableField(fill = ...)}：声明创建/更新时间的自动填充时机。</li>
 * </ul>
 * 其余属性（category、teacher、deadline、status、description）与列名完全一致，
 * 无需注解，MyBatis-Plus 会自动完成映射。
 */
@Data
@TableName("training_project")
@Schema(description = "实训项目")
public class Project implements Serializable {

    /** 项目主键 ID：数据库自增 */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "项目主键 ID", example = "3")
    private Long id;

    /** 项目名称：属性名 name 与列名 project_name 不同，需显式映射 */
    @TableField("project_name")
    @Schema(description = "项目名称", example = "实训成果统计报表")
    private String name;

    /** 项目类别，如「Web 后端」「数据分析」 */
    @Schema(description = "项目类别", example = "数据分析")
    private String category;

    /** 指导教师 */
    @Schema(description = "指导教师", example = "李敏")
    private String teacher;

    /**
     * 截止日期，格式 yyyy-MM-dd。
     *
     * <p>数据库中该列类型为 DATE，这里用 {@link LocalDate} 承接；
     * MyBatis 3.4.5 起内置了 JSR-310 类型处理器，无需额外配置即可完成
     * {@code java.sql.Date} 与 {@code LocalDate} 的互转。
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "截止日期，格式 yyyy-MM-dd", example = "2026-11-30")
    private LocalDate deadline;

    /** 状态：0-未开始 1-进行中 2-已完成 */
    @Schema(description = "状态：0-未开始，1-进行中，2-已完成", example = "1", allowableValues = {"0", "1", "2"})
    private Integer status;

    /** 项目描述 */
    @Schema(description = "项目描述", example = "按班级、项目维度统计实训提交率与平均得分。")
    private String description;

    /** 创建时间：插入时自动填充 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间，插入时自动填充", example = "2026-10-22 19:30:00")
    private LocalDateTime createTime;

    /** 更新时间：插入与更新时自动填充 */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间，插入与更新时自动填充", example = "2026-10-22 19:30:00")
    private LocalDateTime updateTime;
}