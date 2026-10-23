-- ============================================================
-- 校园实训任务管理系统（CTTS）数据库初始化脚本
-- 与 application.yml 中的数据源配置对应：
--   jdbc:mysql://localhost:3306/ctts
-- ============================================================

CREATE DATABASE IF NOT EXISTS `ctts`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE `ctts`;

-- 实训任务表
DROP TABLE IF EXISTS `tb_task`;
CREATE TABLE `tb_task` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `title`       VARCHAR(100) NOT NULL                COMMENT '任务标题',
    `content`     VARCHAR(500) DEFAULT NULL            COMMENT '任务内容',
    `publisher`   VARCHAR(50)  DEFAULT NULL            COMMENT '发布教师',
    `deadline`    DATETIME     DEFAULT NULL            COMMENT '截止时间',
    `status`      TINYINT      NOT NULL DEFAULT 0      COMMENT '状态：0-未开始 1-进行中 2-已完成',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_status_deadline` (`status`, `deadline`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '实训任务表';

-- 学生提交记录表
DROP TABLE IF EXISTS `tb_submission`;
CREATE TABLE `tb_submission` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `task_id`      BIGINT       NOT NULL                COMMENT '任务 ID',
    `student_no`   VARCHAR(20)  NOT NULL                COMMENT '学号',
    `student_name` VARCHAR(50)  DEFAULT NULL            COMMENT '姓名',
    `repo_url`     VARCHAR(255) DEFAULT NULL            COMMENT '提交仓库地址',
    `score`        DECIMAL(5,2) DEFAULT NULL            COMMENT '得分',
    `submit_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    PRIMARY KEY (`id`),
    KEY `idx_task_student` (`task_id`, `student_no`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '学生提交记录表';

-- 初始化一条演示数据，便于后续联调
INSERT INTO `tb_task` (`title`, `content`, `publisher`, `deadline`, `status`)
VALUES ('实验一：Spring Boot 项目搭建与 Maven 管理',
        '使用 Spring Initializr 创建 CTTS 后端骨架，配置 pom.xml 与 application.yml，验证项目可正常启动。',
        '丁玺润',
        '2026-09-18 23:59:59',
        1);
