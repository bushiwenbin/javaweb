package com.gzu.ctts.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统用户实体 —— 映射数据表 {@code sys_user}。
 *
 * <p>本类集中演示 MyBatis-Plus 的四个核心映射注解：
 * <ol>
 *   <li>{@code @TableName("sys_user")}：声明实体对应的表名。若类名与表名按
 *       「驼峰转下划线」规则一致（User → user），该注解可省略；本表名前缀为
 *       {@code sys_}，属于不一致的情况，必须显式声明。</li>
 *   <li>{@code @TableId(value = "id", type = IdType.AUTO)}：声明主键字段及其生成策略。
 *       {@code IdType.AUTO} 表示交给数据库自增（依赖建表语句的 AUTO_INCREMENT），
 *       插入后 MyBatis-Plus 会把数据库生成的 id 回填到实体对象中。</li>
 *   <li>{@code @TableField}：声明普通字段与列的映射关系，本类用到它的三个属性：
 *       <ul>
 *         <li>{@code value}：列名。属性名 {@code realName} 与列名 {@code real_name}
 *             之间符合驼峰规则，本可由 {@code map-underscore-to-camel-case} 自动完成，
 *             这里显式写出以便对照；</li>
 *         <li>{@code select = false}：查询时<b>不</b>把该列纳入 SELECT 列表。
 *             密码摘要属于敏感字段，接口返回用户信息时不应带出；</li>
 *         <li>{@code fill}：字段自动填充时机，配合 {@code MetaObjectHandler} 使用。</li>
 *       </ul>
 *   </li>
 *   <li>{@code @TableLogic}：声明逻辑删除字段。标注后，{@code deleteById} 等删除操作
 *       会被自动改写成 {@code UPDATE ... SET deleted = 1}，查询语句也会自动追加
 *       {@code AND deleted = 0}，数据不会真正从磁盘消失。</li>
 * </ol>
 */
@Data
@TableName("sys_user")
@Schema(description = "系统用户")
public class User implements Serializable {

    /** 用户主键 ID：数据库自增，插入后自动回填 */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "用户主键 ID", example = "5")
    private Long id;

    /** 登录账号，唯一索引列 */
    @Schema(description = "登录账号（唯一）", example = "2024001")
    private String username;

    /**
     * 密码摘要（32 位 MD5）。
     *
     * <p>{@code select = false} 使该列不参与查询，因此接口响应中 password 恒为 null，
     * 避免密码摘要随用户列表泄露。
     */
    @TableField(value = "password", select = false)
    @Schema(description = "密码摘要，查询接口不返回（恒为 null）", example = "null")
    private String password;

    /** 真实姓名：列名 real_name，演示列名与属性名的映射 */
    @TableField("real_name")
    @Schema(description = "真实姓名", example = "皮宸西")
    private String realName;

    /** 角色：ADMIN-管理员 TEACHER-教师 STUDENT-学生 */
    @Schema(description = "角色：ADMIN-管理员，TEACHER-教师，STUDENT-学生",
            example = "STUDENT", allowableValues = {"ADMIN", "TEACHER", "STUDENT"})
    private String role;

    /** 电子邮箱 */
    @Schema(description = "电子邮箱", example = "2400770225@qq.com")
    private String email;

    /** 手机号 */
    @Schema(description = "手机号", example = "13900000005")
    private String phone;

    /** 状态：1-启用 0-禁用 */
    @Schema(description = "状态：1-启用，0-禁用", example = "1", allowableValues = {"0", "1"})
    private Integer status;

    /** 逻辑删除标记：0-未删除 1-已删除，由 @TableLogic 自动维护 */
    @TableLogic
    @Schema(description = "逻辑删除标记：0-未删除，1-已删除", example = "0")
    private Integer deleted;

    /** 创建时间：插入时由 MetaObjectHandler 自动填充 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间，插入时自动填充", example = "2026-10-22 19:30:00")
    private LocalDateTime createTime;

    /** 更新时间：插入与更新时由 MetaObjectHandler 自动填充 */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间，插入与更新时自动填充", example = "2026-10-22 19:30:00")
    private LocalDateTime updateTime;
}