package com.example.javaweb.jdbc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JDBC 连接配置。
 *
 * 这里刻意【不】用 Spring Boot 的 spring-boot-starter-jdbc 自动配置，
 * 而是把 url/账号/密码读出来自己走原生 JDBC 六步流程，
 * 这样每一步在代码里都看得见，方便对照流程图学习。
 */
@Component
public class JdbcConfig {

    /** JDBC 第 ① 步要加载的驱动类全名 */
    private final String driverClassName;

    /** 连接地址（协议://主机:端口/库名?参数） */
    private final String url;

    private final String username;

    private final String password;

    public JdbcConfig(
            @Value("${jdbc.driver-class-name}") String driverClassName,
            @Value("${jdbc.url}") String url,
            @Value("${jdbc.username}") String username,
            @Value("${jdbc.password}") String password) {
        this.driverClassName = driverClassName;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}