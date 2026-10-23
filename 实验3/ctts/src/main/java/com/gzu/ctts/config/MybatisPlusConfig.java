package com.gzu.ctts.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 插件配置。
 *
 * <p>本类只注册一个插件：分页插件 {@link PaginationInnerInterceptor}。
 * 没有它时，{@code mapper.selectPage(page, wrapper)} 会退化成「查全表再内存截取」，
 * 数据量大时内存会被撑爆；注册之后，插件会在 SQL 执行前拦截语句，
 * 自动改写成两条数据库物理分页语句：
 * <pre>
 * SELECT COUNT(*) FROM training_project WHERE ...        -- 统计总数
 * SELECT ... FROM training_project WHERE ... LIMIT ?, ?   -- 取当前页
 * </pre>
 * 这也是「物理分页」与「内存分页」的本质区别。
 *
 * <p><b>易错点</b>：自 MyBatis-Plus 3.5.9 起，分页插件依赖的 SQL 解析器
 * 被拆分到独立的 {@code mybatis-plus-jsqlparser} 模块。若 pom.xml 中只引入
 * starter 而不引入该模块，启动时会因找不到
 * {@code net.sf.jsqlparser.*} 而报 {@code NoClassDefFoundError}。
 *
 * <p>注意：Spring Boot 3 下 MyBatis-Plus 的自动配置类已自动引入该拦截器
 * 所在的配置体系，此处只需把插件 Bean 交给容器即可。
 */
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 分页插件：显式指定数据库类型为 MySQL，插件据此选择 LIMIT 语法方言
        PaginationInnerInterceptor pagination = new PaginationInnerInterceptor(DbType.MYSQL);
        // 单页最大条数限制：超过则按上限截断，防止 pageSize=999999 拖垮数据库
        pagination.setMaxLimit(100L);
        // 溢出处理：false 表示请求页码超出总页数时返回空列表，而不是回到第一页
        pagination.setOverflow(false);

        interceptor.addInnerInterceptor(pagination);
        return interceptor;
    }
}