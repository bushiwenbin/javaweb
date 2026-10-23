-- ============================================================
-- JDBC 六步流程练习用的建库建表脚本
-- 在 Navicat / 命令行里整段执行即可
-- ============================================================

CREATE DATABASE IF NOT EXISTS jdbc_demo
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE jdbc_demo;

DROP TABLE IF EXISTS student;

CREATE TABLE student (
    id          INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键，自增',
    name        VARCHAR(50)  NOT NULL                COMMENT '姓名',
    gender      VARCHAR(10)  NOT NULL DEFAULT '未知'   COMMENT '性别 男/女',
    score       DOUBLE       NOT NULL DEFAULT 0      COMMENT '分数',
    city        VARCHAR(50)  DEFAULT NULL            COMMENT '城市',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='学生表';

-- 灌一批测试数据
INSERT INTO student (name, gender, score, city) VALUES
    ('皮宸西', '男', 92.5, '贵阳'),
    ('闫昭忻', '女', 88.0, '贵阳'),
    ('张三',   '男', 76.5, '北京'),
    ('李四',   '女', 61.0, '上海'),
    ('王五',   '男', 55.5, '广州');

SELECT * FROM student;