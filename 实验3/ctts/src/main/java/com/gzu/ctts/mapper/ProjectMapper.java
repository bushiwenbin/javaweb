package com.gzu.ctts.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gzu.ctts.entity.Project;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 实训项目 Mapper 接口 —— 继承 {@link BaseMapper} 获得单表 CRUD，并补充注解 SQL。
 *
 * <p>与 {@link UserMapper} 的分工一致：能用 Wrapper 表达的查询交给 BaseMapper，
 * 需要聚合统计或显式控制列的场景用注解 SQL 补齐。
 *
 * <p><b>本接口踩过的一个坑（值得记录）：</b>
 * 实体上的 {@code @TableField("project_name")} 只对 MyBatis-Plus
 * <i>自动生成</i>的 SQL 生效（BaseMapper 的方法），对开发者手写的注解 SQL 无效。
 * 手写 SQL 的结果集映射由 MyBatis 原生规则处理：开启
 * {@code map-underscore-to-camel-case} 后，列 {@code project_name} 会被映射到
 * 属性 {@code projectName}，而实体里根本没有这个属性，于是 {@code name} 恒为 null。
 * 解决办法有两种，本接口采用第一种：
 * <ol>
 *   <li>用 {@code @Results}/{@code @Result} 显式声明列与属性的对应关系（见下方
 *       {@code projectResultMap}）；</li>
 *   <li>在 SQL 里直接取别名：{@code SELECT project_name AS name, ...}。</li>
 * </ol>
 */
@Mapper
public interface ProjectMapper extends BaseMapper<Project> {

    /**
     * 按项目类别查询，用于演示注解 SQL 中的 WHERE 条件与排序。
     *
     * <p>{@code @Results} 中的 {@code id = "projectResultMap"} 给该映射关系命名，
     * 其它自定义查询可用 {@code @ResultMap("projectResultMap")} 复用，避免重复书写。
     */
    @Results(id = "projectResultMap", value = {
            @Result(column = "id", property = "id", id = true),
            @Result(column = "project_name", property = "name"),
            @Result(column = "category", property = "category"),
            @Result(column = "teacher", property = "teacher"),
            @Result(column = "deadline", property = "deadline"),
            @Result(column = "status", property = "status"),
            @Result(column = "description", property = "description"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "update_time", property = "updateTime")
    })
    @Select("""
            SELECT id, project_name, category, teacher, deadline, status, description, create_time, update_time
            FROM training_project
            WHERE category = #{category}
            ORDER BY deadline
            """)
    List<Project> selectByCategory(@Param("category") String category);

    /** 按主键修改项目状态，返回受影响行数 */
    @Update("""
            UPDATE training_project
            SET status = #{status}, update_time = NOW()
            WHERE id = #{id}
            """)
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 按类别分组统计项目数量。
     *
     * <p>返回值用 {@code Map} 承接「别名 → 列值」，适合结构不固定的聚合结果；
     * 每个 Map 的键即 SELECT 中的列别名（category、cnt）。
     */
    @Select("""
            SELECT category, COUNT(*) AS cnt
            FROM training_project
            GROUP BY category
            ORDER BY cnt DESC, category
            """)
    List<Map<String, Object>> countGroupByCategory();
}