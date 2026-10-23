# -*- coding: utf-8 -*-
"""由老师模板生成实验三报告。

模板：C:\\Users\\asus\\Downloads\\03.实验三 MyBatis-Plus 持久层开发.docx
产物：…\\实验3\\实验三 皮宸西 2400770225.docx

排版约定（与实验一、实验二报告保持一致）：
  * 正文：宋体 / Times New Roman 小四(12pt)，行距 1.5，无首行缩进；
  * 小节标题：（1）（2）… 加粗；
  * 图注：图片下方居中，五号(10.5pt) 加粗；
  * 表注：表格上方居中，五号(10.5pt) 加粗；
  * 正文中的 `代码` 用 Consolas 呈现。
"""
import os
import re

from docx import Document
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Cm, Pt

TEMPLATE = r"C:\Users\asus\Downloads\03.实验三 MyBatis-Plus 持久层开发.docx"
OUT_DOC = r"C:\Users\asus\OneDrive\Desktop\javaweb\实验3\实验三 皮宸西 2400770225.docx"
FIGS = os.path.join(os.path.dirname(os.path.abspath(__file__)), "figs")

STUDENT = {
    "班级": "软件工程2403",
    "学号": "2400770225",
    "姓名": "皮宸西",
    "小组": "皮宸西",
}
DATE_TEXT = "2026年10月22日"
FIG_WIDTH_CM = 14.0


# ============================================================ 底层排版工具
def style_run(run, size=12, bold=False, mono=False):
    run.font.size = Pt(size)
    run.font.bold = bold
    if mono:
        run.font.name = "Consolas"
        run._element.rPr.rFonts.set(qn("w:eastAsia"), "Consolas")
    else:
        run.font.name = "Times New Roman"
        run._element.rPr.rFonts.set(qn("w:eastAsia"), "宋体")


def add_rich(par, text, size=12, bold=False):
    """把 `代码` 片段渲染成 Consolas 字体，其余用宋体。"""
    for i, seg in enumerate(re.split(r"`([^`]*)`", text)):
        if not seg:
            continue
        run = par.add_run(seg)
        style_run(run, size=size, bold=bold, mono=(i % 2 == 1))


def body_par(cell, text, bold=False, indent=False, space_before=0):
    p = cell.add_paragraph()
    pf = p.paragraph_format
    pf.line_spacing = 1.5
    pf.space_before = Pt(space_before)
    pf.space_after = Pt(0)
    if indent:
        pf.first_line_indent = Pt(24)
    add_rich(p, text, size=12, bold=bold)
    return p


def caption_par(cell, text, before=False):
    """图注（图片下方）或表注（表格上方）：居中、加粗、五号。"""
    p = cell.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    pf = p.paragraph_format
    pf.line_spacing = 1.5
    pf.space_before = Pt(6 if before else 2)
    pf.space_after = Pt(6 if not before else 2)
    add_rich(p, text, size=10.5, bold=True)
    return p


def add_figure(cell, filename, caption):
    path = os.path.join(FIGS, filename)
    if not os.path.exists(path):
        raise SystemExit("缺少配图：" + path)
    from PIL import Image
    with Image.open(path) as im:
        w, h = im.size
    height_cm = FIG_WIDTH_CM * h / w
    if height_cm > 21.5:                       # 超过一页高度的图强制压到 21.5cm
        height_cm = 21.5
    p = cell.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_before = Pt(6)
    p.paragraph_format.space_after = Pt(0)
    p.paragraph_format.line_spacing = 1.0
    p.add_run().add_picture(path, width=Cm(FIG_WIDTH_CM), height=Cm(height_cm))
    caption_par(cell, caption)


def shade(cell, color="D9E2F3"):
    tcPr = cell._tc.get_or_add_tcPr()
    shd = OxmlElement("w:shd")
    shd.set(qn("w:val"), "clear")
    shd.set(qn("w:color"), "auto")
    shd.set(qn("w:fill"), color)
    tcPr.append(shd)


def set_borders(table):
    tblPr = table._tbl.tblPr
    borders = OxmlElement("w:tblBorders")
    for edge in ("top", "left", "bottom", "right", "insideH", "insideV"):
        el = OxmlElement("w:" + edge)
        el.set(qn("w:val"), "single")
        el.set(qn("w:sz"), "4")
        el.set(qn("w:space"), "0")
        el.set(qn("w:color"), "808080")
        borders.append(el)
    tblPr.append(borders)


def add_data_table(cell, caption, header, rows, widths=None):
    """表注在上方居中，紧跟一张带边框的表格。"""
    caption_par(cell, caption, before=True)
    t = cell.add_table(rows=1 + len(rows), cols=len(header))
    t.alignment = WD_TABLE_ALIGNMENT.CENTER
    t.autofit = True
    set_borders(t)
    for j, htxt in enumerate(header):
        c = t.cell(0, j)
        c.text = ""
        p = c.paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p.paragraph_format.line_spacing = 1.0
        p.paragraph_format.space_before = Pt(2)
        p.paragraph_format.space_after = Pt(2)
        add_rich(p, htxt, size=10, bold=True)
        shade(c)
    for i, row in enumerate(rows, start=1):
        for j, val in enumerate(row):
            c = t.cell(i, j)
            c.text = ""
            p = c.paragraphs[0]
            p.paragraph_format.line_spacing = 1.0
            p.paragraph_format.space_before = Pt(1)
            p.paragraph_format.space_after = Pt(1)
            add_rich(p, val, size=10)
    if widths:
        for j, w in enumerate(widths):
            for i in range(len(rows) + 1):
                t.cell(i, j).width = Cm(w)
    p = cell.add_paragraph()
    p.paragraph_format.space_after = Pt(0)
    p.paragraph_format.line_spacing = 1.0
    return t


# ============================================================ 报告正文内容
C = []          # (类型, 参数...)


def H(text):
    C.append(("h", text))


def P(text):
    C.append(("p", text))


def FIG(f, cap):
    C.append(("fig", f, cap))


def TBL(cap, header, rows, widths=None):
    C.append(("tbl", cap, header, rows, widths))


# ---------------------------------------------------------------- （1）
H("（1）梳理工程结构，明确实验三的任务边界")
P("实验一完成了 CTTS 工程骨架（`com.gzu:ctts`，Spring Boot 3.3.5，JDK 17，Maven 构建），"
  "实验二补齐了 Controller 层的接口，但数据保存在 Service 层的一个 "
  "`CopyOnWriteArrayList` 中——进程一重启数据全部还原，持久层实际上是空的。"
  "实验三的任务就是把这套“内存数据库”换成真正的 MySQL：引入 MyBatis-Plus，"
  "为 `sys_user` 与 `training_project` 两张表建立实体类与 Mapper 接口，"
  "把 Service 的读写改造为数据库访问，并把接口从“只能查和增”补全为完整的增删改查。")
P("实验三新增与改造的类及其职责见表 3.1，改造后的工程目录结构如图 3.1 所示。")
TBL("表3.1 实验三新增与改造的类及职责",
    ["所在包", "类 / 文件", "职责"],
    [["com.gzu.ctts.entity", "User.java（新增）", "映射 `sys_user` 表，演示 `@TableName`、`@TableId`、`@TableField`、`@TableLogic`"],
     ["com.gzu.ctts.entity", "Project.java（改造）", "映射 `training_project` 表，属性 `name` 对应列 `project_name`"],
     ["com.gzu.ctts.mapper", "UserMapper.java（新增）", "继承 `BaseMapper<User>`，并补充 `@Select`/`@Update` 注解 SQL"],
     ["com.gzu.ctts.mapper", "ProjectMapper.java（新增）", "继承 `BaseMapper<Project>`，用 `@Results` 显式声明结果映射"],
     ["com.gzu.ctts.config", "MybatisPlusConfig.java（新增）", "注册分页插件 `PaginationInnerInterceptor`"],
     ["com.gzu.ctts.config", "MyMetaObjectHandler.java（新增）", "创建时间、更新时间的自动填充"],
     ["com.gzu.ctts.service", "UserService / UserServiceImpl（新增）", "用户业务逻辑，数据来源改为 `UserMapper`"],
     ["com.gzu.ctts.service", "ProjectService / ProjectServiceImpl（改造）", "由内存 List 改为调用 `ProjectMapper`"],
     ["com.gzu.ctts.controller", "UserController.java（新增）", "用户模块 8 个 REST 接口"],
     ["com.gzu.ctts.controller", "ProjectController.java（改造）", "实训项目模块 8 个 REST 接口"],
     ["com.gzu.ctts.dto", "UserForm.java（新增）", "用户请求参数对象及其校验规则"],
     ["sql/ctts.sql", "建库脚本（扩充）", "新增 `sys_user`、`training_project` 两张表及初始化数据"]],
    widths=[4.0, 5.0, 6.6])
FIG("fig_tree.png", "图 3.1  CTTS 工程目录结构（实验三新增持久层）")

# ---------------------------------------------------------------- （2）
H("（2）在 pom.xml 中引入 MyBatis-Plus 与分页插件依赖")
P("MyBatis-Plus 不在 Spring Boot 官方父工程的依赖管理范围内，必须先在 `properties` 中"
  "显式声明版本号，再在 `dependencies` 中引用，否则 Maven 会因找不到版本而构建失败。"
  "此外还有一个容易踩的坑：自 MyBatis-Plus 3.5.9 起，分页插件依赖的 SQL 解析器被拆分到"
  "独立的 `mybatis-plus-jsqlparser` 模块，若只引入 starter 而不引入该模块，"
  "配置分页插件时会抛 `NoClassDefFoundError`。本次实验在构建阶段就遇到了这个问题，"
  "补充依赖后构建通过。依赖配置如图 3.2 所示，各依赖的作用见表 3.2。")
FIG("fig_pom.png", "图 3.2  pom.xml MyBatis-Plus 依赖配置")
TBL("表3.2 pom.xml 新增依赖及作用",
    ["依赖坐标", "版本", "作用"],
    [["`spring-boot-starter-web`", "3.3.5（父工程仲裁）", "内嵌 Tomcat + Spring MVC + Jackson"],
     ["`mybatis-plus-spring-boot3-starter`", "3.5.9", "MyBatis-Plus 的 Spring Boot 3 专用起步依赖，"
      "提供 `BaseMapper`、条件构造器与自动配置"],
     ["`mybatis-plus-jsqlparser`", "3.5.9", "分页插件所需的 SQL 解析器，3.5.9 起必须单独引入"],
     ["`mysql-connector-j`", "父工程仲裁", "MySQL 8 的 JDBC 驱动（runtime 作用域）"],
     ["`spring-boot-starter-validation`", "3.3.5", "提供 `@NotBlank`、`@Size` 等 JSR-380 校验注解"],
     ["`springdoc-openapi-starter-webmvc-ui`", "2.6.0", "生成 `/v3/api-docs`，供 Apifox 一键导入接口"]],
    widths=[6.2, 3.4, 6.0])

# ---------------------------------------------------------------- （3）
H("（3）完善 application.yml 中的 MyBatis-Plus 配置")
P("配置分为三部分：`configuration` 控制 MyBatis 原生行为，`global-config` 控制"
  "MyBatis-Plus 的全局策略，`type-aliases-package` 为实体类注册别名。"
  "其中把 `log-impl` 设为 `StdOutImpl` 是本次实验的关键调试手段——"
  "打开后控制台会打印每条真正执行的 SQL 及其参数，"
  "后面“MyBatis-Plus 到底生成了什么 SQL”的证据正来源于此。"
  "配置内容如图 3.3 所示，关键配置项的含义见表 3.3。")
FIG("fig_yml.png", "图 3.3  application.yml MyBatis-Plus 配置")
TBL("表3.3 application.yml 中 MyBatis-Plus 关键配置项",
    ["配置项", "取值", "作用"],
    [["`type-aliases-package`", "`com.gzu.ctts.entity`", "为实体类注册类型别名，注解 SQL 中可直接写类名"],
     ["`map-underscore-to-camel-case`", "`true`", "下划线列名自动映射为驼峰属性，如 `real_name` → `realName`"],
     ["`log-impl`", "`StdOutImpl`", "把执行的 SQL 与参数打印到控制台，便于调试与取证"],
     ["`id-type`", "`auto`", "全局主键策略为数据库自增，等价于 `@TableId(type = IdType.AUTO)`"],
     ["`logic-delete-value` / `logic-not-delete-value`", "`1` / `0`", "逻辑删除的“已删除/未删除”取值约定"]],
    widths=[5.6, 3.6, 6.4])

# ---------------------------------------------------------------- （4）
H("（4）启动 MySQL 并执行建库脚本，创建两张实验表")
P("实验环境中的 MySQL 8.0.29 为免安装版，需先用 `mysqld --defaults-file` 启动服务，"
  "确认 3306 端口进入 LISTENING 状态后，再执行工程提供的 `sql/ctts.sql` 完成建库建表。"
  "脚本除保留实验一的 `tb_task`、`tb_submission` 外，新增实验三要使用的两张表："
  "`sys_user`（系统用户表，7 条初始数据）与 `training_project`（实训项目表，8 条初始数据）。")
P("`sys_user` 的字段设计刻意覆盖了几种典型的映射场景：`real_name`、`create_time` 等"
  "下划线列名用于验证驼峰映射；`password` 用于演示 `@TableField(select = false)`；"
  "`deleted` 用于演示 `@TableLogic` 逻辑删除。建表语句如图 3.4 所示，字段说明见表 3.4。")
FIG("fig_sql_user.png", "图 3.4  sys_user 建表语句")
TBL("表3.4 sys_user 表字段说明",
    ["字段名", "类型", "约束 / 默认值", "说明", "对应实体属性"],
    [["`id`", "BIGINT", "PK, AUTO_INCREMENT", "用户主键", "`id`（`@TableId`）"],
     ["`username`", "VARCHAR(50)", "NOT NULL, UNIQUE", "登录账号，唯一", "`username`"],
     ["`password`", "VARCHAR(64)", "NOT NULL", "密码摘要（MD5）", "`password`（`select=false`）"],
     ["`real_name`", "VARCHAR(50)", "NULL", "真实姓名", "`realName`（`@TableField`）"],
     ["`role`", "VARCHAR(20)", "NOT NULL, DEFAULT 'STUDENT'", "角色：ADMIN/TEACHER/STUDENT", "`role`"],
     ["`email`", "VARCHAR(100)", "NULL", "电子邮箱", "`email`"],
     ["`phone`", "VARCHAR(20)", "NULL", "手机号", "`phone`"],
     ["`status`", "TINYINT", "NOT NULL, DEFAULT 1", "状态：1-启用 0-禁用", "`status`"],
     ["`deleted`", "TINYINT", "NOT NULL, DEFAULT 0", "逻辑删除标记", "`deleted`（`@TableLogic`）"],
     ["`create_time`", "DATETIME", "DEFAULT CURRENT_TIMESTAMP", "创建时间", "`createTime`（自动填充）"],
     ["`update_time`", "DATETIME", "ON UPDATE CURRENT_TIMESTAMP", "更新时间", "`updateTime`（自动填充）"]],
    widths=[3.0, 2.4, 4.2, 3.6, 3.4])
P("`training_project` 表中列名 `project_name` 与实体属性 `name` 不同名，"
  "是本次实验专门设置的“不一致场景”，用于验证 `@TableField(\"project_name\")` 的必要性。"
  "建表语句如图 3.5 所示，字段说明见表 3.5。")
FIG("fig_sql_project.png", "图 3.5  training_project 建表语句")
TBL("表3.5 training_project 表字段说明",
    ["字段名", "类型", "约束 / 默认值", "说明", "对应实体属性"],
    [["`id`", "BIGINT", "PK, AUTO_INCREMENT", "项目主键", "`id`（`@TableId`）"],
     ["`project_name`", "VARCHAR(100)", "NOT NULL", "项目名称", "`name`（`@TableField`）"],
     ["`category`", "VARCHAR(50)", "NULL", "项目类别", "`category`"],
     ["`teacher`", "VARCHAR(50)", "NULL", "指导教师", "`teacher`"],
     ["`deadline`", "DATE", "NULL", "截止日期", "`deadline`（`LocalDate`）"],
     ["`status`", "TINYINT", "NOT NULL, DEFAULT 0", "状态：0/1/2", "`status`"],
     ["`description`", "VARCHAR(500)", "NULL", "项目描述", "`description`"],
     ["`create_time`", "DATETIME", "DEFAULT CURRENT_TIMESTAMP", "创建时间", "`createTime`（自动填充）"],
     ["`update_time`", "DATETIME", "ON UPDATE CURRENT_TIMESTAMP", "更新时间", "`updateTime`（自动填充）"]],
    widths=[3.0, 2.4, 4.2, 3.6, 3.4])

# ---------------------------------------------------------------- （5）
H("（5）编写实体类，完成 Java 对象与数据表的映射")
P("实体类是 MyBatis-Plus 全部能力的起点：`BaseMapper` 之所以能“不写 SQL 就能增删改查”，"
  "正是因为它在运行期读取实体上的映射注解来推导表名、列名与主键策略。"
  "本次实验用到的四个注解及其含义见表 3.6，两个实体类的源码分别如图 3.6、图 3.7 所示。")
TBL("表3.6 实体映射注解说明",
    ["注解", "本实验中的用法", "作用"],
    [["`@TableName`", "`@TableName(\"sys_user\")`", "声明实体对应的表名；类名与表名不一致时必写"],
     ["`@TableId`", "`@TableId(value = \"id\", type = IdType.AUTO)`", "声明主键字段与主键生成策略，"
      "`AUTO` 表示数据库自增，插入后主键回填到实体"],
     ["`@TableField`", "`value` 指定列名；`select = false` 查询不返回；`fill` 指定自动填充时机",
      "声明普通字段与列的映射关系，是 `@TableName`/`@TableId` 之外最常用的注解"],
     ["`@TableLogic`", "`@TableLogic` 标注 `deleted` 字段", "声明逻辑删除字段，"
      "删除操作被改写为 UPDATE，查询自动追加未删除条件"]],
    widths=[3.0, 6.6, 6.0])
FIG("fig_entity_user.png", "图 3.6  User.java 实体映射注解")
FIG("fig_entity_proj.png", "图 3.7  Project.java 实体映射注解")

# ---------------------------------------------------------------- （6）
H("（6）编写 Mapper 接口，继承 BaseMapper 获得 CRUD 能力")
P("Mapper 接口只有方法声明、没有实现类：MyBatis 启动时扫描 `@MapperScan` 指定的包，"
  "为每个接口生成 JDK 动态代理对象，方法调用被代理拦截后，"
  "依据方法签名（以及注解）装配 SQL 并交给 SqlSession 执行。"
  "继承 `BaseMapper<T>` 后，单表增删改查全部无需手写 SQL，"
  "常用的方法见表 3.7；当遇到聚合统计、多表关联等 `BaseMapper` 覆盖不到的场景，"
  "则用 `@Select`、`@Update` 等注解 SQL 补齐。两个 Mapper 的源码如图 3.8、图 3.9 所示。")
TBL("表3.7 BaseMapper 提供的常用方法（本实验实际调用）",
    ["方法", "对应 SQL", "本实验调用位置"],
    [["`insert(entity)`", "INSERT INTO … VALUES (…)", "新增用户 / 新增实训项目"],
     ["`deleteById(id)`", "DELETE … / UPDATE … SET deleted=1（逻辑删除时）", "删除用户、删除实训项目"],
     ["`updateById(entity)`", "UPDATE … SET … WHERE id = ?", "修改用户、修改实训项目"],
     ["`selectById(id)`", "SELECT … WHERE id = ?", "用户/项目详情、Service 层存在性校验"],
     ["`selectCount(wrapper)`", "SELECT COUNT(*) … WHERE …", "新增用户时的账号唯一性校验"],
     ["`selectPage(page, wrapper)`", "SELECT COUNT(*) … 与 SELECT … LIMIT ?, ?", "两个模块的分页查询"],
     ["`selectList(wrapper)`", "SELECT … WHERE …", "用户列表、项目列表查询"]],
    widths=[4.2, 6.4, 5.0])
FIG("fig_mapper_user.png", "图 3.8  UserMapper.java（继承 BaseMapper 并补充注解 SQL）")
P("这里记录一个实际踩到的坑。`ProjectMapper.selectByCategory` 最初写成普通的 `@Select`，"
  "查询返回的数据里 `name` 恒为 `null`。原因是：实体上的 `@TableField(\"project_name\")`"
  "只对 MyBatis-Plus 自动生成的 SQL 生效，对开发者手写的注解 SQL 无效；"
  "手写 SQL 的结果集映射由 MyBatis 原生规则处理，开启驼峰映射后列 `project_name`"
  "会被映射到并不存在的属性 `projectName`，于是 `name` 就一直是空。"
  "解决办法是用 `@Results`/`@Result` 显式声明列与属性的对应关系（或在 SQL 中直接取别名 "
  "`project_name AS name`）。修正后的写法如图 3.9 所示。")
FIG("fig_mapper_proj.png", "图 3.9  ProjectMapper.java（@Results 显式声明结果映射）")

# ---------------------------------------------------------------- （7）
H("（7）注册分页插件与字段自动填充处理器")
P("`Page` 对象本身并不产生 `LIMIT` 语句，真正起作用的是分页插件 "
  "`PaginationInnerInterceptor`：它在 SQL 执行前拦截语句，改写成"
  "“`SELECT COUNT(*)` 统计总数 + `SELECT … LIMIT ?, ?` 取当前页”两条物理分页语句。"
  "若不注册该插件，`selectPage` 会退化成查全表再在内存里截取。插件配置如图 3.10 所示。")
FIG("fig_config_mp.png", "图 3.10  MybatisPlusConfig.java（注册分页插件）")
P("字段自动填充解决的是“创建时间、更新时间几乎每张表都有，却要在每个 Service 方法里"
  "重复赋值”的问题。实现 `MetaObjectHandler` 并配合实体上的 "
  "`@TableField(fill = FieldFill.INSERT / INSERT_UPDATE)`，"
  "插入与更新时 MyBatis-Plus 会自动回调填充，业务代码中不再出现 "
  "`setCreateTime(...)`。处理器实现如图 3.11 所示。")
FIG("fig_handler.png", "图 3.11  MyMetaObjectHandler.java（字段自动填充）")

# ---------------------------------------------------------------- （8）
H("（8）改造 Service 层：由内存 List 切换为 Mapper 访问数据库")
P("Service 层的改造是本次实验工作量最集中的部分。以用户分页查询为例，"
  "实验二需要手工计算起始下标再在内存里 `skip/limit`；"
  "实验三只需构造 `Page` 与 `LambdaQueryWrapper` 两个对象，"
  "把条件用方法引用的形式交给 MyBatis-Plus 拼 SQL，分页由数据库完成。"
  "用 `LambdaQueryWrapper` 而非字符串列名的好处是：属性改名时编译期就会报错，"
  "不至于等到运行时才发现“列名写错”。分页方法的实现如图 3.12 所示。")
FIG("fig_svc_user.png", "图 3.12  UserServiceImpl.java 分页与条件构造")
P("查询条件全部采用“条件重载”写法，第一个参数为 `false` 时该条件不参与拼接，"
  "因此不必在方法里写一串 `if`。需要特别注意模糊查询的分组："
  "`and(w -> w.like(...).or().like(...))` 必须把两个 `or` 条件整体括起来，"
  "否则 `or` 会与前面的 `role` 条件平级，把查询范围意外放大——"
  "这一点在服务端打印的 SQL 中可以直观看到括号的存在。")

# ---------------------------------------------------------------- （9）
H("（9）实现用户与实训项目的增删改查接口")
P("两个 Controller 各提供 8 个接口，覆盖 REST 风格中常用的动作与 HTTP 方法的对应关系，"
  "接口清单见表 3.8 与表 3.9。Controller 层不接触数据库，"
  "只负责“参数接收 → 调用 Service → 包装统一响应”，持久层细节全部下沉到 Service 与 Mapper。")
TBL("表3.8 用户模块接口清单（/api/users）",
    ["序号", "HTTP 方法", "路径", "说明", "涉及的 MyBatis-Plus 能力"],
    [["1", "GET", "/page", "分页 + 角色/关键字条件查询", "`selectPage` + 分页插件"],
     ["2", "GET", "/{id}", "按主键查询详情", "`selectById`"],
     ["3", "POST", "/", "新增用户", "`insert` + 主键回填"],
     ["4", "PUT", "/{id}", "修改用户", "`updateById`"],
     ["5", "DELETE", "/{id}", "删除用户（逻辑删除）", "`deleteById` + `@TableLogic`"],
     ["6", "GET", "/role/{role}", "按角色查询", "`@Select` 注解 SQL"],
     ["7", "PATCH", "/{id}/status", "启用 / 禁用用户", "`@Update` 注解 SQL"],
     ["8", "GET", "/statistics/role", "按角色统计人数", "`@Select` 聚合 SQL"]],
    widths=[1.4, 2.4, 3.4, 4.4, 5.0])
TBL("表3.9 实训项目模块接口清单（/api/projects）",
    ["序号", "HTTP 方法", "路径", "说明", "涉及的 MyBatis-Plus 能力"],
    [["1", "GET", "/page", "分页 + 状态/类别/关键字条件查询", "`selectPage` + 分页插件"],
     ["2", "GET", "/{id}", "按主键查询详情", "`selectById`"],
     ["3", "POST", "/", "新增实训项目", "`insert` + 列名映射"],
     ["4", "PUT", "/{id}", "修改实训项目", "`updateById`"],
     ["5", "DELETE", "/{id}", "删除实训项目（物理删除）", "`deleteById`"],
     ["6", "GET", "/category/{category}", "按类别查询", "`@Select` + `@Results`"],
     ["7", "PATCH", "/{id}/status", "修改项目状态", "`@Update` 注解 SQL"],
     ["8", "GET", "/statistics/category", "按类别分组统计", "`@Select` 聚合 SQL"]],
    widths=[1.4, 2.4, 3.4, 4.4, 5.0])

# ---------------------------------------------------------------- （10）
H("（10）构建并启动应用，确认持久层装配生效")
P("在工程根目录执行 `mvn -B clean package`，Maven 依次完成 clean、resources、compile、"
  "testCompile、test、jar、spring-boot:repackage 等阶段，输出 BUILD SUCCESS 并生成可执行 jar；"
  "随后执行 `java -jar target/ctts-0.0.1-SNAPSHOT.jar` 启动应用。"
  "启动日志如图 3.13 所示：HikariCP 连接池启动并连接 MySQL，"
  "MyBatis 以 `StdOutImpl` 作为日志实现初始化，内嵌 Tomcat 监听 8080 端口，"
  "最终输出 Started CttsApplication，说明数据源、持久层与 Web 容器的装配全部成功。")
FIG("fig_startup.png", "图 3.13  应用启动日志（数据源与 MyBatis-Plus 装配）")
P("接口被调用后，服务端打印出 MyBatis-Plus 自动生成的 SQL，这是“持久层确实在工作”"
  "最直接的证据，如图 3.14 所示。可以看到三件事：其一，分页查询被改写成 "
  "`SELECT COUNT(*)` 与带 `LIMIT ?` 的两条语句，属数据库侧物理分页；"
  "其二，条件构造器拼出的 `WHERE` 中，模糊查询的两个 `or` 条件被一对括号正确分组；"
  "其三，`INSERT` 语句的字段列表里自动带上了 `create_time`、`update_time`，"
  "这正是 `@TableField(fill = ...)` 与 `MetaObjectHandler` 配合的结果。")
FIG("fig_sql_page.png", "图 3.14  MyBatis-Plus 自动生成的 SQL（物理分页 / 条件构造 / 自动填充）")
P("图 3.15 进一步给出两组对比证据。第一组是逻辑删除：删除用户时执行的其实是 "
  "`UPDATE sys_user SET update_time=?, deleted=1 WHERE id=? AND deleted=0`，"
  "数据行并未真正消失，而所有查询语句都被自动追加了 `deleted = 0`。"
  "第二组是列名映射：实体属性 `name` 对应列 `project_name` 时，"
  "MyBatis-Plus 自动生成了别名 `project_name AS name`；"
  "而手写的注解 SQL 不会享受这一待遇，必须靠 `@Results` 显式声明。"
  "这两组现象恰好解释了“为什么 BaseMapper 能直接用、自己写的 SQL 却要额外配置”。")
FIG("fig_sql_logic.png", "图 3.15  逻辑删除改写、自动列别名与自定义 SQL 结果映射")

# ---------------------------------------------------------------- （11）
H("（11）使用 Apifox 验证全部接口")
P("应用启动后访问 `http://localhost:8080/v3/api-docs` 即得到符合 OpenAPI 3 规范的接口文档，"
  "在 Apifox 中新建项目并以“URL 导入”方式填入该地址，即可把 16 个接口连同参数示例、"
  "响应结构一次性导入，无需逐个手工录入；导入后把前置 URL 设为 "
  "`http://localhost:8080` 便可直接发送请求。"
  "下面按模块给出各接口的真实请求与响应，请求体统一为 `Content-Type: application/json`。")
P("① 用户分页查询与条件模糊查询（图 3.16）。分页接口返回 `records`、`total`、`pages` 等"
  "分页元信息；加上 `role=STUDENT` 与 `keyword=2024` 后，"
  "服务端 SQL 相应追加了角色等值与账号/姓名模糊匹配条件。")
FIG("fig_api_user_page.png", "图 3.16  用户分页查询与条件模糊查询")
P("② 按 id 查详情与异常分支（图 3.17）。`password` 字段因 `@TableField(select = false)` "
  "在响应中恒为 `null`，密码摘要不会随接口泄露；id 不存在时返回 `code=404`；"
  "路径变量写成非数字（`abc`）时，由全局异常处理器归一为 `code=400`。")
FIG("fig_api_user_detail.png", "图 3.17  按 id 查用户详情（含 404 与类型不匹配）")
P("③ 参数校验的两类失败（图 3.18）。`pageSize=999` 触发方法参数上的 `@Max(50)`；"
  "新增用户时请求体中的账号过短、姓名为空、角色非法、邮箱格式错误则触发字段级校验，"
  "多条错误信息用分号拼接后返回，前端可直接提示用户。")
FIG("fig_api_user_validate.png", "图 3.18  参数校验失败的两类响应（方法参数与请求体字段）")
P("④ 新增用户与账号唯一性校验（图 3.19）。`BaseMapper.insert` 执行后，"
  "数据库生成的 6 号主键被自动回填到返回对象中；"
  "`createTime`、`updateTime` 由 `MetaObjectHandler` 自动填充。"
  "当账号 `admin` 已存在时，Service 层的前置校验返回 `code=400`；"
  "即便并发下漏过该校验，`sys_user.username` 上的唯一索引也会由数据库兜底。")
FIG("fig_api_user_create.png", "图 3.19  新增用户与账号唯一性校验")
P("⑤ 修改用户与状态变更（图 3.20）。修改接口中 `password` 留空表示不修改原密码——"
  "MyBatis-Plus 默认的更新策略为 NOT_NULL，值为 `null` 的字段不会进入 `SET` 子句，"
  "因此不会把密码覆盖成空；状态变更走的是自定义的 `@Update` 注解 SQL。")
FIG("fig_api_user_update.png", "图 3.20  修改用户与启用/禁用状态（注解 SQL）")
P("⑥ 按角色查询与按角色统计（图 3.21）。两个接口都走自定义注解 SQL，"
  "前者查询指定角色的用户列表，后者用 `COUNT(*)` 聚合出各角色人数。"
  "需要强调的是：自定义 SQL 不会自动追加逻辑删除条件，"
  "`deleted = 0` 需要在 SQL 中手写。")
FIG("fig_api_user_role.png", "图 3.21  按角色查询与按角色统计（自定义注解 SQL）")
P("⑦ 删除用户与逻辑删除验证（图 3.22）。调用删除接口后再次查询该 id，"
  "返回 `code=404`，但数据行仍在表中，只是 `deleted` 被置为 1。")
FIG("fig_api_user_delete.png", "图 3.22  逻辑删除用户及删除后查询返回 404")
P("⑧ 实训项目的分页与条件查询（图 3.23）。逻辑与用户模块一致，"
  "结果按“状态升序、截止日期升序”排列，便于按紧急程度查看。")
FIG("fig_api_proj_page.png", "图 3.23  实训项目分页查询与条件查询")
P("⑨ 按 id 查项目详情与不存在分支（图 3.24）。")
FIG("fig_api_proj_detail.png", "图 3.24  按 id 查项目详情与不存在分支")
P("⑩ 新增实训项目与项目名校验（图 3.25）。该接口同时验证了中文、日期与列名映射："
  "请求体中的 `deadline` 是 `yyyy-MM-dd` 字符串，"
  "经 Jackson 反序列化为 `LocalDate` 后写入 DATE 列；"
  "`name` 属性最终落库到 `project_name` 列。项目名为空时返回 `code=400`。")
FIG("fig_api_proj_create.png", "图 3.25  新增实训项目与项目名校验")
P("⑪ 日期格式校验失败（图 3.26）。把 `deadline` 写成 `2027/01/08` 时，"
  "Jackson 无法按 `yyyy-MM-dd` 解析，抛出 `HttpMessageNotReadableException`，"
  "由全局异常处理器归一为 `code=400` 而不是 500——"
  "“参数格式错误”属于客户端错误，不应算作服务端故障。")
FIG("fig_api_proj_dateerr.png", "图 3.26  截止日期格式校验失败（Jackson 类型转换异常）")
P("⑫ 修改实训项目与状态流转（图 3.27）。修改走 `updateById`，"
  "状态变更走自定义注解 SQL，并在返回结果中体现自动刷新后的 `updateTime`。")
FIG("fig_api_proj_update.png", "图 3.27  修改实训项目与状态流转（注解 SQL）")
P("⑬ 注解 SQL 的两个典型场景（图 3.28）。按类别查询验证了 `@Results` 结果映射修正后的效果，"
  "`name` 字段已能正确返回；分组统计则演示了 `GROUP BY` + `COUNT(*)` 这类"
  "`BaseMapper` 无法直接表达的查询。")
FIG("fig_api_proj_annotation.png", "图 3.28  按类别查询与分组聚合统计（注解 SQL）")
P("⑭ 物理删除实训项目（图 3.29）。`Project` 未使用 `@TableLogic`，"
  "因此删除是真正的 `DELETE FROM training_project WHERE id=?`，"
  "删除后再次查询该 id 返回 `code=404`。与用户模块的逻辑删除形成对照。")
FIG("fig_api_proj_delete.png", "图 3.29  物理删除实训项目及删除后查询返回 404")
P("全部接口验证完成后，直接查询数据库确认落库结果，如图 3.30 所示。"
  "被删除的用户（id = 8）数据行仍然存在，仅 `deleted` 为 1，"
  "故“有效用户数”为 7 而“全部用户数”为 8；"
  "被删除的实训项目则已从表中消失。至此，16 个接口全部验证通过，"
  "接口验证结果汇总见表 3.10。")
FIG("fig_db_state.png", "图 3.30  数据库最终状态（MyBatis-Plus 操作结果落库验证）")
TBL("表3.10 接口验证结果汇总",
    ["模块", "接口数", "验证通过的场景", "结果"],
    [["用户", "8", "分页、条件模糊查询、详情、404、类型不匹配、\n"
      "参数校验、新增与主键回填、账号重复、修改、\n"
      "状态变更、按角色查询、聚合统计、逻辑删除及验证", "全部通过"],
     ["实训项目", "8", "分页、条件查询、详情、404、新增、项目名校验、\n"
      "日期格式校验、修改、状态流转、按类别查询（注解 SQL）、\n"
      "分组统计、物理删除及验证", "全部通过"]],
    widths=[2.4, 1.8, 8.6, 2.8])