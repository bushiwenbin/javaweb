package com.example.javaweb.jdbc.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 全局异常处理：把 JDBC 抛的 SQLException 转成结构化 JSON，
 * 免得前端看到一长串 HTML 报错页。
 */
@RestControllerAdvice(basePackages = "com.example.javaweb.jdbc.controller")
public class JdbcExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(JdbcExceptionHandler.class);

    @ExceptionHandler(SQLException.class)
    public Map<String, Object> handleSqlException(SQLException e) {
        log.error("SQL 执行失败", e);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", 500);
        result.put("message", "数据库操作失败：" + e.getMessage());
        result.put("sqlState", e.getSQLState());
        result.put("errorCode", e.getErrorCode());
        return result;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Map<String, Object> handleIllegalArgument(IllegalArgumentException e) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", 400);
        result.put("message", e.getMessage());
        return result;
    }
}