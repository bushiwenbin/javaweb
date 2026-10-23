package com.example.javaweb.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 原生 JDBC 工具类：封装 JDBC 六步流程里的【第 ① 步 加载驱动】和【第 ② 步 获取连接】。
 *
 * <p>为什么单独抽出来？
 * 因为驱动只需加载一次（JVM 类加载机制保证 static 块只跑一次），
 * 而连接每次操作都要新建、用完必须关。
 * 把这两步收口到一处，DAO 里就只剩「创建语句 → 执行 → 处理结果 → 关闭」。
 *
 * <p>六步对照：
 * <pre>
 *   ① 加载驱动   → loadDriver()     （本类 @PostConstruct，容器启动时执行一次）
 *   ② 获取连接   → getConnection()  （本类）
 *   ③ 创建语句   → StudentDao 中 conn.prepareStatement(sql)
 *   ④ 执行 SQL   → StudentDao 中 executeQuery() / executeUpdate()
 *   ⑤ 处理结果集 → StudentDao 中 while (rs.next())
 *   ⑥ 关闭资源   → try-with-resources（编译器自动生成 finally 倒序 close）
 * </pre>
 */
@Component
public class DbUtil {

    private static final Logger log = LoggerFactory.getLogger(DbUtil.class);

    private final JdbcConfig config;

    /** 容器创建本 Bean 时立刻加载驱动，保证第一次查询不会再抛 ClassNotFound */
    public DbUtil(JdbcConfig config) {
        this.config = config;
        loadDriver();
    }

    /** JDBC 第 ① 步：把驱动类加载进 JVM，触发驱动向 DriverManager 注册自己 */
    public void loadDriver() {
        try {
            Class.forName(config.getDriverClassName());
            log.info("JDBC 第①步 驱动加载成功：{}", config.getDriverClassName());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("找不到 JDBC 驱动：" + config.getDriverClassName()
                    + "，请检查 pom.xml 是否已加入 mysql-connector-j 依赖", e);
        }
    }

    /** JDBC 第 ② 步：通过 DriverManager 向驱动要一条物理连接 */
    public Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(
                config.getUrl(), config.getUsername(), config.getPassword());
        log.info("JDBC 第②步 连接已建立：{}", config.getUrl());
        return conn;
    }
}