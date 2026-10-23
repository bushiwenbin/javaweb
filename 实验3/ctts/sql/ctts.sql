-- ============================================================
-- 校园实训任务管理系统（CTTS）数据库初始化脚本
-- 实验三：MyBatis-Plus 持久层开发
-- 与 application.yml 中的数据源配置对应：
--   jdbc:mysql://localhost:3306/ctts
--
-- 执行方式（任选其一）：
--   1) 命令行： mysql -uroot -p123456 < sql/ctts.sql
--   2) Navicat：新建查询，粘贴本文件内容后运行
-- ============================================================

CREATE DATABASE IF NOT EXISTS `ctts`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE `ctts`;

-- ------------------------------------------------------------
-- 实验一遗留：实训任务表 / 学生提交记录表
-- （实验三的持久层暂未映射这两张表，保留以维持建库脚本的完整性）
-- ------------------------------------------------------------
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

-- ============================================================
-- 实验三新增：用户表（对应实体类 com.gzu.ctts.entity.User）
--
-- 字段命名刻意覆盖实体映射的几种典型场景：
--   * real_name / create_time 等下划线列名 —— 演示「下划线转驼峰」与 @TableField
--   * password                —— 演示 @TableField(select = false) 查询时不返回
--   * deleted                 —— 演示 @TableLogic 逻辑删除
--   * create_time / update_time —— 演示 @TableField(fill = ...) 自动填充
-- ============================================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT              COMMENT '用户主键 ID',
    `username`    VARCHAR(50)  NOT NULL                             COMMENT '登录账号，唯一',
    `password`    VARCHAR(64)  NOT NULL                             COMMENT '密码摘要（本实验存 32 位 MD5）',
    `real_name`   VARCHAR(50)  DEFAULT NULL                         COMMENT '真实姓名',
    `role`        VARCHAR(20)  NOT NULL DEFAULT 'STUDENT'           COMMENT '角色：ADMIN-管理员 TEACHER-教师 STUDENT-学生',
    `email`       VARCHAR(100) DEFAULT NULL                         COMMENT '电子邮箱',
    `phone`       VARCHAR(20)  DEFAULT NULL                         COMMENT '手机号',
    `status`      TINYINT      NOT NULL DEFAULT 1                   COMMENT '状态：1-启用 0-禁用',
    `deleted`     TINYINT      NOT NULL DEFAULT 0                   COMMENT '逻辑删除标记：0-未删除 1-已删除',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_role_status` (`role`, `status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '系统用户表';

-- ------------------------------------------------------------
-- 实验三新增：实训项目表（对应实体类 com.gzu.ctts.entity.Project）
-- 列名 project_name 与实体属性 name 不同名，
-- 用于演示 @TableField("project_name") 的显式列名映射
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `training_project`;
CREATE TABLE `training_project` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT            COMMENT '项目主键 ID',
    `project_name` VARCHAR(100) NOT NULL                           COMMENT '项目名称',
    `category`     VARCHAR(50)  DEFAULT NULL                       COMMENT '项目类别',
    `teacher`      VARCHAR(50)  DEFAULT NULL                       COMMENT '指导教师',
    `deadline`     DATE         DEFAULT NULL                       COMMENT '截止日期',
    `status`       TINYINT      NOT NULL DEFAULT 0                 COMMENT '状态：0-未开始 1-进行中 2-已完成',
    `description`  VARCHAR(500) DEFAULT NULL                       COMMENT '项目描述',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_status_deadline` (`status`, `deadline`),
    KEY `idx_category` (`category`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '实训项目表';

-- ============================================================
-- 初始化数据
-- ============================================================

-- 用户：密码统一为 123456 的 MD5 摘要（e10adc3949ba59abbe56e057f20f883e）
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `role`, `email`, `phone`, `status`) VALUES
('admin',    'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 'ADMIN',   'admin@ctts.edu.cn',   '13900000001', 1),
('dingxr',   'e10adc3949ba59abbe56e057f20f883e', '丁玺润',     'TEACHER', 'dingxr@ctts.edu.cn',  '13900000002', 1),
('limin',    'e10adc3949ba59abbe56e057f20f883e', '李敏',       'TEACHER', 'limin@ctts.edu.cn',   '13900000003', 1),
('wangqiang','e10adc3949ba59abbe56e057f20f883e', '王强',       'TEACHER', 'wangq@ctts.edu.cn',   '13900000004', 1),
('2024001',  'e10adc3949ba59abbe56e057f20f883e', '皮宸西',     'STUDENT', '2400770225@qq.com',   '13900000005', 1),
('2024002',  'e10adc3949ba59abbe56e057f20f883e', '张伟',       'STUDENT', '2024002@qq.com',      '13900000006', 1),
('2024003',  'e10adc3949ba59abbe56e057f20f883e', '李娜',       'STUDENT', '2024003@qq.com',      '13900000007', 0);

-- 实训项目：8 条，便于验证分页效果（与实验二的内存模拟数据保持一致）
INSERT INTO `training_project` (`project_name`, `category`, `teacher`, `deadline`, `status`, `description`) VALUES
('校园实训任务管理系统（CTTS）后端', 'Web 后端', '丁玺润', '2026-12-31', 1, '基于 Spring Boot 3 构建实训任务发布、提交与评分接口。'),
('学生信息管理模块',               'Web 后端', '丁玺润', '2026-11-15', 1, '实现学生信息的增删改查接口，并完成参数校验。'),
('实训成果统计报表',               '数据分析', '李敏',   '2026-11-30', 0, '按班级、项目维度统计实训提交率与平均得分。'),
('前后端分离接口联调',             'Web 后端', '丁玺润', '2026-12-10', 1, '使用 Apifox 对全部 REST 接口进行调试与文档生成。'),
('任务提交记录导出',               '工具开发', '王强',   '2026-12-20', 0, '支持将学生提交记录导出为 Excel 文件。'),
('登录鉴权与 Token 校验',          'Web 后端', '李敏',   '2026-12-05', 2, '基于拦截器实现 JWT 登录鉴权与接口权限控制。'),
('接口自动化测试用例',             '测试',     '王强',   '2026-12-25', 0, '为 Controller 层接口编写 JUnit 5 + MockMvc 自动化测试。'),
('大数据量分页性能优化',           '性能优化', '丁玺润', '2027-01-05', 0, '对比物理分页与内存分页在大数据量下的性能差异。');

-- 实验一遗留表的一条演示数据
INSERT INTO `tb_task` (`title`, `content`, `publisher`, `deadline`, `status`)
VALUES ('实验一：Spring Boot 项目搭建与 Maven 管理',
        '使用 Spring Initializr 创建 CTTS 后端骨架，配置 pom.xml 与 application.yml，验证项目可正常启动。',
        '丁玺润',
        '2026-09-18 23:59:59',
        1);