# 实验三：MyBatis-Plus 持久层开发

本目录为 JavaWeb 实验三的完整交付物。实验二结束时的 Service 层数据还放在内存
`CopyOnWriteArrayList` 里，本次把它换成了真实的 MySQL 持久化。

## 一、交付物清单

| 文件 | 说明 |
|---|---|
| `实验三 皮宸西 2400770225.docx` | 实验报告（Word，44 页，30 图 + 10 表） |
| `实验三 皮宸西 2400770225.pdf` | 同上报告的 PDF 版本 |
| `ctts/` | CTTS 后端工程（实验三版本） |

工程内部：

| 路径 | 说明 |
|---|---|
| `ctts/sql/ctts.sql` | 建库脚本，含 `sys_user`、`training_project` 两张实验表及初始化数据 |
| `ctts/src/main/java/com/gzu/ctts/entity/` | `User.java`（新增）、`Project.java`（改造为数据库实体） |
| `ctts/src/main/java/com/gzu/ctts/mapper/` | `UserMapper`、`ProjectMapper`（继承 `BaseMapper`） |
| `ctts/src/main/java/com/gzu/ctts/config/` | `MybatisPlusConfig`（分页插件）、`MyMetaObjectHandler`（字段自动填充） |
| `ctts/src/main/java/com/gzu/ctts/controller/` | 用户 8 个接口 + 实训项目 8 个接口 |
| `ctts/_build/` | 报告生成工具链与全部真实运行证据（见下） |

## 二、实验三做了什么

1. **集成 MyBatis-Plus**：`pom.xml` 引入 `mybatis-plus-spring-boot3-starter` 3.5.9
   与 `mybatis-plus-jsqlparser`（3.5.9 起分页插件必须单独引入，否则启动报
   `NoClassDefFoundError`）；`application.yml` 配置驼峰映射、SQL 日志、主键策略、
   逻辑删除取值。
2. **建表与实体映射**：新增 `sys_user`（7 条初始数据）、`training_project`（8 条）；
   实体类上用 `@TableName`、`@TableId`、`@TableField`、`@TableLogic` 完成映射，
   其中实体属性 `name` 对应列 `project_name`，用于验证显式列名映射。
3. **Mapper 与注解 SQL**：两个 Mapper 继承 `BaseMapper<T>` 获得单表 CRUD；
   分页、按类别/角色查询、分组统计、状态变更等场景用 `@Select`/`@Update` 注解 SQL 补齐。
4. **CRUD 接口与验证**：用户、实训项目各 8 个接口，全部经真实 HTTP 请求验证并落库确认。

## 三、真实运行证据（`ctts/_build/`）

| 文件 | 内容 |
|---|---|
| `api_trace.txt` | 28 个报文块：请求命令、请求体、状态行与响应 JSON（curl 真实输出） |
| `app.log` | 应用启动日志与 MyBatis 打印的全部 SQL（含参数与结果行） |
| `db_schema.txt` | 两张表的 `SHOW CREATE TABLE` 结果 |
| `db_state.txt` | 操作完成后数据库的最终查询结果 |
| `figs/` | 报告中 30 张配图的 PNG 原图 |
| `render_lab3.py` | 由真实源码/日志/报文渲染配图的脚本 |
| `run_trace.ps1` | 接口回归脚本（重跑全部请求并重新生成 `api_trace.txt`） |
| `report_content.py` / `build_docx.py` | 报告正文内容与 docx 生成脚本 |

## 四、如何复现

```powershell
# 1) 启动 MySQL（免安装版）
Start-Process "C:\mysql-8.0.29\bin\mysqld.exe" -ArgumentList "--defaults-file=C:\mysql-8.0.29\my.ini" -WindowStyle Minimized

# 2) 建库建表（路径含中文，先复制到纯 ASCII 路径再执行）
Copy-Item "sql\ctts.sql" "$env:TEMP\ctts_lab3.sql" -Force
& "C:\mysql-8.0.29\bin\mysql.exe" -uroot -p123456 --default-character-set=utf8mb4 `
    -e "source $($env:TEMP -replace '\\','/')/ctts_lab3.sql"

# 3) 构建并启动
$env:JAVA_HOME = "C:\dev\jdk-17"
& "C:\dev\maven\bin\mvn.cmd" -B clean package
& "C:\dev\jdk-17\bin\java.exe" -jar "target\ctts-0.0.1-SNAPSHOT.jar" --server.port=8080

# 4) 重跑接口回归（另开一个窗口）
powershell -NoProfile -ExecutionPolicy Bypass -File "_build\run_trace.ps1"

# 5) 重新生成报告
python "_build\render_lab3.py"
python "_build\build_docx.py"
```

## 五、接口速查

应用启动后，OpenAPI 规范文档位于 `http://localhost:8080/v3/api-docs`，
在 Apifox 中新建项目 → 导入 → URL 导入 → 填入该地址，即可一次性导入全部 16 个接口；
内置可视化调试界面为 `http://localhost:8080/swagger-ui.html`。

- 用户模块 `/api/users`：分页查询、详情、新增、修改、删除（逻辑删除）、按角色查询、状态变更、按角色统计
- 实训项目模块 `/api/projects`：分页查询、详情、新增、修改、删除（物理删除）、按类别查询、状态变更、分组统计

## 六、本次踩到的两个坑（报告"实验总结"中有详细分析）

1. **自定义注解 SQL 不走实体映射**：`@TableField("project_name")` 只对 MyBatis-Plus
   自动生成的 SQL 生效；手写 `@Select` 时列 `project_name` 会被按驼峰规则映射到并不存在的
   属性 `projectName`，导致 `name` 恒为 `null`。解决办法是加 `@Results`/`@Result`
   显式声明映射，或在 SQL 中直接取别名。
2. **分页插件需单独引入依赖**：MyBatis-Plus 3.5.9 起 `PaginationInnerInterceptor`
   依赖的 SQL 解析器被拆分到 `mybatis-plus-jsqlparser` 模块，只引 starter 会构建失败。