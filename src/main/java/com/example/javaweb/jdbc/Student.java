package com.example.javaweb.jdbc;

import java.time.LocalDateTime;

/**
 * 学生实体（对应数据库表 student）。
 *
 * 这里用 Java 16+ 的 record，因为字段都是「装数据」的，
 * 一行就写完了 getter/setter/toString，不用再引 Lombok。
 * 课程里如果还在用 JDK 8/17，把它换成普通类 + @Data 即可。
 */
public record Student(
        Integer id,
        String name,
        String gender,
        Double score,
        String city,
        LocalDateTime createTime
) {
}