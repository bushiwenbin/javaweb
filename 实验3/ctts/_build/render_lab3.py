# -*- coding: utf-8 -*-
"""实验三报告配图渲染。

复用实验二已调好的渲染器（render_figs.py，本目录下），
只覆盖「trace 解析」与「配图清单」两部分：
  * 代码截图：直接读取工程真实源码；
  * 运行截图：读取真实启动日志 app.log、真实 SQL 日志、真实 HTTP 报文 api_trace.txt。
所有图片宽度统一 950px，便于在 A4 正文（14cm）内缩放显示而不溢出。
"""
import json
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import render_figs as R          # noqa: E402  复用渲染基础设施
from render_figs import (        # noqa: E402
    T, T_wrap, render_lines, render_code, render_lines as RL,
    json_colorize, fmt_json_lines, wrap_line, truncate_records,
    C_TEXT, C_KEY, C_STR, C_CMT, C_TYPE, C_NUM, C_HDRV, C_OK, C_ERR,
    C_GUTTER, C_DIM, C_CMDTXT, OUT,
)

CTTS = os.path.dirname(HERE)
SRC = os.path.join(CTTS, "src", "main", "java", "com", "gzu", "ctts")
APP_LOG = os.path.join(CTTS, "app.log")
TRACE = os.path.join(HERE, "api_trace.txt")
DB_STATE = os.path.join(HERE, "db_state.txt")
DB_SCHEMA = os.path.join(HERE, "db_schema.txt")
SQL = os.path.join(CTTS, "sql", "ctts.sql")


# ============================================================ trace 解析
def load_blocks(path):
    """解析 run_trace.ps1 生成的报文块，返回 label/desc/cmd/status/body/reqbody。"""
    txt = open(path, encoding="utf-8").read()
    blocks = []
    for chunk in txt.split("═" * 56):
        chunk = chunk.strip()
        if not chunk:
            continue
        m = re.search(r"【(.+?)】\s*(.*)", chunk)
        cmdm = re.search(r"^\$ (.+)$", chunk, re.M)
        stm = re.search(r"^(HTTP/\S+.*)$", chunk, re.M)
        # 响应体必须取「状态行之后」的第一个 JSON 行：
        # 带请求体的报文块里，最先出现的 { 属于请求体，不能误当响应体。
        body = ""
        if stm:
            bm = re.search(r"^(\{.*)$", chunk[stm.end():], re.M)
            if bm:
                body = bm.group(1).strip()
        req = ""
        rq = re.search(r"请求体（[^）]*）：\n(.*?)\n\n", chunk, re.S)
        if rq:
            req = rq.group(1).strip()
        blocks.append(dict(label=m.group(1), desc=m.group(2),
                           cmd=cmdm.group(1).strip() if cmdm else "",
                           status=stm.group(1).strip() if stm else "",
                           body=body, reqbody=req))
    return blocks


def req_rows(b, mark, color="#E8C07D", keep=3):
    """渲染一个报文块：请求行 + 请求体 + 状态行 + 响应 JSON。"""
    rows = [T(mark + "  " + b["desc"], color, True), T("")]
    rows += T_wrap("$ " + b["cmd"], C_CMDTXT, True, cont="      ")
    rows.append(T(""))
    if b["reqbody"]:
        rows.append(T("请求体：", C_DIM))
        for raw in b["reqbody"].split("\n"):
            rows.append(json_colorize(raw.rstrip()))
        rows.append(T(""))
    rows.append(T(b["status"].strip(), C_OK if " 200" in b["status"] else C_HDRV, True))
    rows.append(T("Content-Type: application/json", C_DIM))
    rows.append(T(""))
    if b["body"]:
        try:
            obj = json.loads(b["body"])
            obj, omitted = truncate_records(obj, keep)
            for raw in fmt_json_lines(obj):
                for w in wrap_line(raw):
                    rows.append(json_colorize(w))
            if omitted:
                rows.append(T("      …… 其余 %d 条结构相同，此处省略 ……" % omitted, C_CMT))
        except Exception:
            for w in wrap_line(b["body"]):
                rows.append(T(w))
    rows.append(T(""))
    return rows


# ============================================================ 文本文件着色
SQL_KW = ("CREATE TABLE", "DROP TABLE", "INSERT INTO", "SELECT", "FROM", "WHERE", "VALUES",
          "PRIMARY KEY", "UNIQUE KEY", "KEY ", "ENGINE", "DEFAULT", "NOT NULL", "COMMENT",
          "ORDER BY", "GROUP BY", "LIMIT", "UPDATE", "SET ", "AND", "ON UPDATE")
YAML_KW = ("server:", "spring:", "mybatis-plus:", "configuration:", "global-config:", "db-config:",
           "datasource:", "hikari:", "logging:", "info:", "springdoc:", "level:", "pattern:",
           "api-docs:", "swagger-ui:", "app:")


def tok_runs(line, mode):
    """按模式做轻量着色，够用即可（不做完整语法分析）。"""
    s = line.rstrip()
    stripped = s.strip()
    if not stripped:
        return T("")
    if stripped.startswith("#") or stripped.startswith("--") or stripped.startswith("<!--") \
            or stripped.startswith("/*") or stripped.startswith("*"):
        return T(s, C_CMT)
    runs, pos = [], 0
    if mode == "yaml":
        m = re.match(r"(\s*)([\w.\-]+)(:)(.*)", s)
        if m:
            runs.append((m.group(1) + m.group(2) + m.group(3), C_HDRV, False))
            rest = m.group(4)
            if "#" in rest:
                k = rest.index("#")
                runs.append((rest[:k], C_TEXT, False))
                runs.append((rest[k:], C_CMT, False))
            else:
                runs.append((rest, C_TEXT, False))
            return runs
        return T(s)
    if mode == "sql":
        for kw in SQL_KW:
            if stripped.upper().startswith(kw.upper()):
                runs.append((s[:len(s) - len(s.lstrip())] + s.strip()[:len(kw)], C_KEY, True))
                pos = len(s) - len(s.lstrip()) + len(kw)
                break
        rest = s[pos:]
        # 单引号字符串高亮
        for part in re.split(r"('(?:[^']|'')*')", rest):
            if part.startswith("'") and part.endswith("'") and len(part) > 1:
                runs.append((part, C_STR, False))
            else:
                runs.append((part, C_TEXT, False))
        return runs
    if mode == "xml":
        for part in re.split(r"(<[^>]*>)", s):
            runs.append((part, C_KEY if part.startswith("<") else C_TEXT, False))
        return runs
    return T(s)


def text_rows(path, start, end, mode="plain", gutter=True):
    lines = open(path, encoding="utf-8").read().split("\n")[start - 1:end]
    rows = []
    for n, ln in enumerate(lines, start):
        row = ([("%4d  " % n, C_GUTTER, False)] if gutter else []) + tok_runs(ln, mode)
        rows.append(row)
    return rows


def find_line(path, needle, nth=1):
    lines = open(path, encoding="utf-8").read().split("\n")
    c = 0
    for i, l in enumerate(lines, 1):
        if needle in l:
            c += 1
            if c == nth:
                return i
    raise SystemExit("未找到锚点 %r (第%d次) in %s" % (needle, nth, path))


def code_range(path, out, title, start_anchor, end_anchor=None, n=None, nth_start=1):
    s = find_line(path, start_anchor, nth_start)
    if end_anchor:
        e = find_line(path, end_anchor)
        e = e - 1
    else:
        e = s + (n or 40) - 1
    render_code(path, s, e, out, "%s（第 %d–%d 行）" % (title, s, e))


# ============================================================ SQL 日志视图
def read_log(path):
    """自适应读取日志：PowerShell 的 Tee-Object 在 5.1 下写出 UTF-16，需按 BOM 判断。"""
    raw = open(path, "rb").read()
    if raw[:2] in (b"\xff\xfe", b"\xfe\xff"):
        return raw.decode("utf-16")
    try:
        return raw.decode("utf-8")
    except UnicodeDecodeError:
        return raw.decode("gbk", "replace")


# ================================================================ 出图
def sql_rows(anchor, before=1, after=7, nth=1):
    """取 app.log 中以某条 SQL 为中心的窗口，返回着色后的行。"""
    lines = read_log(APP_LOG).split("\n")
    idx, c = None, 0
    for i, l in enumerate(lines):
        if anchor in l:
            c += 1
            if c == nth:
                idx = i
                break
    if idx is None:
        raise SystemExit("app.log 中未找到：" + anchor)
    rows = []
    for l in lines[max(0, idx - before):min(len(lines), idx + after)]:
        if not l.strip():
            continue
        if l.startswith("==>  Preparing:"):
            rows += T_wrap(l, C_KEY, True, cont="        ")
        elif l.startswith("==> Parameters:"):
            rows += T_wrap(l, C_NUM, False, cont="        ")
        elif l.startswith("<==        Row:") or l.startswith("<==    Columns:"):
            rows += T_wrap(l, C_TEXT, False, cont="        ")
        elif l.startswith("<==      Total:") or l.startswith("<==    Updates:"):
            rows += T_wrap(l, C_OK, True, cont="        ")
        elif "MyMetaObjectHandler" in l or "ServiceImpl" in l or "Controller" in l:
            rows += T_wrap(l, C_HDRV, False, cont="        ")
        elif ("Creating a new SqlSession" in l or "Transaction synchronization" in l
              or "Releasing transactional" in l or "JDBC Connection" in l
              or "Fetched SqlSession" in l):
            rows += T_wrap(l, C_DIM, False, cont="        ")
        else:
            rows += T_wrap(l)
    return rows


def tfig(path, out, title, s_anchor, e_anchor, mode="plain", s_off=0, e_off=-1):
    """按锚点区间渲染文本文件（pom / yml / sql）。"""
    s = find_line(path, s_anchor) + s_off
    e = find_line(path, e_anchor) + e_off
    render_lines(text_rows(path, s, e, mode), out, "%s（第 %d–%d 行）" % (title, s, e))


if __name__ == "__main__":
    bl = load_blocks(TRACE)
    print("trace 块数 =", len(bl))

    # ---------------- 图 3.1 工程目录结构
    rows = [T(r"…\实验3\ctts> tree /F src", C_CMDTXT, True), T("")]
    for root, dirs, files in os.walk(os.path.join(CTTS, "src")):
        dirs.sort()
        rel = os.path.relpath(root, CTTS).replace("\\", "/")
        depth = 0 if rel == "src" else rel.count("/")
        rows.append(T("  " * depth + ("src" if rel == "src" else os.path.basename(root)), C_TYPE))
        for f in sorted(files):
            rows.append(T("  " * (depth + 1) + f))
    rows += [T(""),
             T("说明：实验三在实验二基础上新增 mapper 包（MyBatis-Plus Mapper 接口），", C_CMT),
             T("并扩充 entity（User）、config（MybatisPlusConfig、MyMetaObjectHandler）、", C_CMT),
             T("dto（UserForm）、service（UserService）、controller（UserController）。", C_CMT)]
    render_lines(rows, "fig_tree.png", "图 3.1  CTTS 工程目录结构（实验三新增持久层）")

    # ---------------- 图 3.2/3.3 pom 与 yml 配置
    tfig(os.path.join(CTTS, "pom.xml"), "fig_pom.png",
         "图 3.2  pom.xml MyBatis-Plus 依赖配置",
         "持久层：MyBatis-Plus", "MySQL 数据库驱动", "xml", -1, -2)
    tfig(os.path.join(CTTS, "src", "main", "resources", "application.yml"), "fig_yml.png",
         "图 3.3  application.yml MyBatis-Plus 配置",
         "# MyBatis-Plus 配置", "# 应用元信息", "yaml", 0, -2)

    # ---------------- 图 3.4/3.5 建表 SQL
    tfig(SQL, "fig_sql_user.png", "图 3.4  sys_user 建表语句",
         "实验三新增：用户表", "实验三新增：实训项目表", "sql", 0, -3)
    tfig(SQL, "fig_sql_project.png", "图 3.5  training_project 建表语句",
         "实验三新增：实训项目表", "-- 初始化数据", "sql", 0, -3)

    # ---------------- 图 3.6/3.7 实体映射
    code_range(os.path.join(SRC, "entity", "User.java"), "fig_entity_user.png",
               "图 3.6  User.java 实体映射注解", "@Data", None, 62)
    code_range(os.path.join(SRC, "entity", "Project.java"), "fig_entity_proj.png",
               "图 3.7  Project.java 实体映射注解", "@Data", None, 56)

    # ---------------- 图 3.8/3.9 Mapper
    code_range(os.path.join(SRC, "mapper", "UserMapper.java"), "fig_mapper_user.png",
               "图 3.8  UserMapper.java（继承 BaseMapper 并补充注解 SQL）",
               "public interface UserMapper", None, 48)
    code_range(os.path.join(SRC, "mapper", "ProjectMapper.java"), "fig_mapper_proj.png",
               "图 3.9  ProjectMapper.java（@Results 显式声明结果映射）",
               "public interface ProjectMapper", None, 46)

    # ---------------- 图 3.10/3.11 分页插件与自动填充
    code_range(os.path.join(SRC, "config", "MybatisPlusConfig.java"), "fig_config_mp.png",
               "图 3.10  MybatisPlusConfig.java（注册分页插件）",
               "public MybatisPlusInterceptor", None, 22)
    code_range(os.path.join(SRC, "config", "MyMetaObjectHandler.java"), "fig_handler.png",
               "图 3.11  MyMetaObjectHandler.java（字段自动填充）",
               "public void insertFill", None, 20)

    # ---------------- 图 3.12 Service 分页
    code_range(os.path.join(SRC, "service", "impl", "UserServiceImpl.java"), "fig_svc_user.png",
               "图 3.12  UserServiceImpl.java 分页与条件构造",
               "public PageResult<User> page", "public User getById", None)

    # ---------------- 图 3.13 启动日志
    al = read_log(APP_LOG).split("\n")
    s = next(i for i, l in enumerate(al) if ":: Spring Boot ::" in l) - 8
    e = next(i for i, l in enumerate(al) if "Started CttsApplication" in l) + 1
    rows = []
    for l in al[max(0, s):e]:
        if not l.strip():
            continue
        if "Started CttsApplication" in l or "Tomcat started on port" in l:
            rows += T_wrap(l, C_OK, True)
        elif "HikariPool" in l:
            rows += T_wrap(l, C_TYPE, False)
        elif "MyBatis" in l or "mybatis" in l:
            rows += T_wrap(l, C_HDRV, False)
        elif ":: Spring Boot ::" in l:
            rows += T_wrap(l, C_TYPE, True)
        elif any(k in l for k in (".   ____", "/\\ ", "( ( )", "\\\\/ ", "'  |____", "=====")):
            rows += T_wrap(l, C_KEY)
        else:
            rows += T_wrap(l)
    render_lines(rows, "fig_startup.png", "图 3.13  应用启动日志（数据源与 MyBatis-Plus 装配）")

    # ---------------- 图 3.14 SQL 证据（分页 / 条件 / 插入 / 别名）
    rows = [T("① 分页插件把「查全部」改写为「COUNT + LIMIT」两条物理分页语句", "#E8C07D", True), T("")]
    rows += sql_rows("SELECT COUNT(*) AS total FROM sys_user WHERE deleted = 0", 1, 8, 2)
    rows += [T(""), T("② 条件构造器拼出的 WHERE 与 ORDER BY（role = ? AND (username LIKE ? OR real_name LIKE ?)）",
                     "#E8C07D", True), T("")]
    rows += sql_rows("ORDER BY id DESC LIMIT ?", 1, 6, 2)
    rows += [T(""), T("③ 插入语句：@TableField(fill) 让 create_time / update_time 自动进入字段列表",
                     "#E8C07D", True), T("")]
    rows += sql_rows("INSERT INTO sys_user", 1, 7)
    render_lines(rows, "fig_sql_page.png",
                 "图 3.14  MyBatis-Plus 自动生成的 SQL（物理分页 / 条件构造 / 自动填充）")

    # ---------------- 图 3.15 逻辑删除与自动别名
    rows = [T("④ @TableLogic：deleteById 被改写为 UPDATE ... SET deleted=1，数据行并未真正删除",
              "#E8C07D", True), T("")]
    rows += sql_rows("UPDATE sys_user SET update_time=?, deleted=1", 2, 8)
    rows += [T(""), T("⑤ 实体属性 name 与列 project_name 不同名时，MyBatis-Plus 自动生成别名 project_name AS name",
                     "#E8C07D", True), T("")]
    rows += sql_rows("SELECT id,project_name AS name", 1, 4)
    rows += [T(""), T("⑥ 对比：自定义注解 SQL 不走实体映射，需 @Results 显式声明（否则 name 恒为 null）",
                     "#E8C07D", True), T("")]
    rows += sql_rows("SELECT id, project_name, category, teacher", 1, 5)
    render_lines(rows, "fig_sql_logic.png",
                 "图 3.15  逻辑删除改写、自动列别名与自定义 SQL 结果映射")

    # ---------------- 图 3.16–3.21 用户模块报文
    render_lines(req_rows(bl[0], "①") + req_rows(bl[1], "① 条件查询"),
                 "fig_api_user_page.png", "图 3.16  用户分页查询与条件模糊查询")
    render_lines(req_rows(bl[3], "②") + req_rows(bl[4], "② 异常") + req_rows(bl[5], "② 异常"),
                 "fig_api_user_detail.png", "图 3.17  按 id 查用户详情（含 404 与类型不匹配）")
    render_lines(req_rows(bl[2], "① 异常") + req_rows(bl[8], "③ 异常"),
                 "fig_api_user_validate.png", "图 3.18  参数校验失败的两类响应（方法参数 与 请求体字段）")
    render_lines(req_rows(bl[6], "③") + req_rows(bl[7], "③ 异常"),
                 "fig_api_user_create.png", "图 3.19  新增用户与账号唯一性校验")
    render_lines(req_rows(bl[9], "④") + req_rows(bl[10], "⑦"),
                 "fig_api_user_update.png", "图 3.20  修改用户与启用/禁用状态（注解 SQL）")
    render_lines(req_rows(bl[11], "⑥") + req_rows(bl[12], "⑧"),
                 "fig_api_user_role.png", "图 3.21  按角色查询与按角色统计（自定义注解 SQL）")
    render_lines(req_rows(bl[13], "⑤") + req_rows(bl[14], "⑤ 验证"),
                 "fig_api_user_delete.png", "图 3.22  逻辑删除用户及删除后查询返回 404")

    # ---------------- 图 3.23–3.28 项目模块报文
    render_lines(req_rows(bl[15], "①") + req_rows(bl[16], "① 条件查询"),
                 "fig_api_proj_page.png", "图 3.23  实训项目分页查询与条件查询")
    render_lines(req_rows(bl[17], "②") + req_rows(bl[18], "② 异常"),
                 "fig_api_proj_detail.png", "图 3.24  按 id 查项目详情与不存在分支")
    render_lines(req_rows(bl[19], "③") + req_rows(bl[20], "③ 异常"),
                 "fig_api_proj_create.png", "图 3.25  新增实训项目与项目名校验")
    render_lines(req_rows(bl[21], "③ 异常"),
                 "fig_api_proj_dateerr.png", "图 3.26  截止日期格式校验失败（Jackson 类型转换异常）")
    render_lines(req_rows(bl[22], "④") + req_rows(bl[23], "⑦"),
                 "fig_api_proj_update.png", "图 3.27  修改实训项目与状态流转（注解 SQL）")
    render_lines(req_rows(bl[24], "⑥") + req_rows(bl[25], "⑧"),
                 "fig_api_proj_annotation.png", "图 3.28  按类别查询与分组聚合统计（注解 SQL）")
    render_lines(req_rows(bl[26], "⑤") + req_rows(bl[27], "⑤ 验证"),
                 "fig_api_proj_delete.png", "图 3.29  物理删除实训项目及删除后查询返回 404")

    # ---------------- 图 3.29 数据库最终状态
    rows = []
    for l in open(DB_STATE, encoding="utf-8").read().split("\n"):
        if not l.strip():
            rows.append(T(""))
        elif l.startswith("mysql>"):
            rows.append(T(l, C_KEY, True))
        elif l.startswith("id\t") or l.startswith("valid_users") or l.startswith("all_users"):
            rows += T_wrap(l, C_TYPE, True)
        else:
            rows += T_wrap(l)
    rows += [T(""),
             T("说明：id = 8 的用户（2024008）已被删除，但数据行仍在表中，仅 deleted 置为 1，", C_CMT),
             T("故「有效用户数」为 7 而「全部用户数」为 8 —— 这正是逻辑删除与物理删除的区别。", C_CMT)]
    render_lines(rows, "fig_db_state.png", "图 3.30  数据库最终状态（MyBatis-Plus 操作结果落库验证）")

    print("ALL DONE ->", OUT)