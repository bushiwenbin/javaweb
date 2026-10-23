package com.gzu.ctts.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gzu.ctts.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 用户 Mapper 接口 —— 继承 {@link BaseMapper} 后即获得单表 CRUD 能力。
 *
 * <p><b>为什么只写接口、没有实现类？</b>
 * MyBatis 在启动时会扫描 {@code @Mapper}（或 {@code @MapperScan} 指定的包），
 * 为每个接口生成 JDK 动态代理对象。代理对象拦截方法调用，根据方法签名与注解
 * 拼出 SQL、经 SqlSession 执行并把结果集映射回实体，
 * 所以开发者只需要「声明方法」，不必写实现。
 *
 * <p><b>BaseMapper 已经提供的能力</b>（无需写任何 SQL）：
 * {@code insert}、{@code deleteById}、{@code deleteByMap}、{@code delete}、
 * {@code deleteBatchIds}、{@code updateById}、{@code update}、{@code selectById}、
 * {@code selectBatchIds}、{@code selectByMap}、{@code selectOne}、{@code selectCount}、
 * {@code selectList}、{@code selectMaps}、{@code selectPage} 等。
 *
 * <p><b>什么时候还需要自己写 SQL？</b>
 * BaseMapper 只覆盖「单表、条件由 Wrapper 表达」的场景。一旦出现多表关联、
 * 聚合统计、批量更新等复杂语句，就需要自定义方法。此时有两种写法：
 * <ul>
 *   <li>注解 SQL：直接在方法上写 {@code @Select}/{@code @Update}/{@code @Insert}/{@code @Delete}，
 *       适合短小的 SQL（本接口采用）；</li>
 *   <li>XML 映射文件：适合很长的动态 SQL，通过 {@code mybatis-plus.mapper-locations} 指定位置。</li>
 * </ul>
 * 注意：自定义 SQL 不会自动追加逻辑删除条件，需要像下面这样手写 {@code deleted = 0}。
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 按角色查询用户列表（注解 SQL 示例）。
     *
     * <p>{@code #{role}} 是 MyBatis 的占位符，最终会被编译成 JDBC 的 {@code ?}
     * 并通过 PreparedStatement 预编译传参，可以防止 SQL 注入；
     * 与之相对的 {@code ${}} 是字符串拼接，仅在动态表名/列名时使用，存在注入风险。
     *
     * <p>{@code @Param("role")} 给参数命名，供 {@code #{role}} 引用；
     * 单参数且为简单类型时可省略，多参数时必须写。
     */
    @Select("""
            SELECT id, username, real_name, role, email, phone, status, deleted, create_time, update_time
            FROM sys_user
            WHERE role = #{role} AND deleted = 0
            ORDER BY id
            """)
    List<User> selectByRole(@Param("role") String role);

    /**
     * 修改用户启用状态（注解 SQL 示例）。
     *
     * <p>返回值为受影响的行数：0 表示 id 不存在或已被逻辑删除，
     * Service 层据此判断操作是否成功。
     */
    @Update("""
            UPDATE sys_user
            SET status = #{status}, update_time = NOW()
            WHERE id = #{id} AND deleted = 0
            """)
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /** 统计某个角色下的用户数量（聚合查询示例） */
    @Select("SELECT COUNT(*) FROM sys_user WHERE role = #{role} AND deleted = 0")
    long countByRole(@Param("role") String role);
}