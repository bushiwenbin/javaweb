# JDBC 六步流程 · 项目内完整实现

原生 JDBC 编写，**没有用** JdbcTemplate / MyBatis，目的是让六步每一步在代码里都看得见。

## 一、六步对照表

| 步骤 | 做什么 | 本项目代码位置 | 关键 API |
|---|---|---|---|
| ① 加载驱动 | 驱动类加载进 JVM，向 DriverManager 注册 | `DbUtil` 构造方法 | `Class.forName("com.mysql.cj.jdbc.Driver")` |
| ② 获取连接 | 建立一条物理 TCP 连接 | `DbUtil.getConnection()` | `DriverManager.getConnection(url, user, pwd)` |
| ③ 创建语句 | 创建预编译语句，用 `?` 占位 | `StudentDao` 各方法 | `conn.prepareStatement(sql)` |
| ④ 执行 SQL | 查询 / 增删改 / 批处理 | `StudentDao` 各方法 | `executeQuery()`、`executeUpdate()`、`executeBatch()` |
| ⑤ 处理结果集 | 指针下移逐行读字段 | `StudentDao.readAll()` / `mapRow()` | `while(rs.next())`、`rs.getXxx()` |
| ⑥ 关闭资源 | 倒序关闭 rs → pstmt → conn | 全项目的 try-with-resources | 编译器自动生成 finally |

## 二、文件清单

```
src/main/java/com/example/javaweb/jdbc/
├── Student.java                    实体（record，字段 id/name/gender/score/city/createTime）
├── JdbcConfig.java                 数据源配置（@Value 读 application.properties 的 jdbc.*）
├── DbUtil.java                     封装第 ① ② 步
├── StudentDao.java                 第 ③④⑤⑥ 步的完整 CRUD + 批处理 + 事务
├── JdbcMainDemo.java               纯 main 版教学演示（IDEA 右键 Run 即可）
└── controller/
    ├── StudentController.java      REST 接口，路径 /api/students/**
    └── JdbcExceptionHandler.java   SQLException → 结构化 JSON

src/main/resources/
├── application.properties          新增 jdbc.driver-class-name / url / username / password
└── db/schema.sql                   建库建表 + 初始测试数据
```

## 三、数据库

- 库名：`jdbc_demo`，表：`student`
- 账号：`root` / `123456`（本机 MySQL 8.0.29，在 `C:\mysql-8.0.29`）
- 建表脚本：`src/main/resources/db/schema.sql`

```bash
"C:\mysql-8.0.29\bin\mysql.exe" -uroot -p123456 < src/main/resources/db/schema.sql
```

## 四、跑起来

### 方式 1：启动 Spring Boot（接口 + 控制台六步日志）

```bash
# 注意：8080 常被 Docker nginx 占用，本项目用 8082
java -jar target/javaweb-0.0.1-SNAPSHOT.jar --server.port=8082
```

启动后控制台会按 ①~⑥ 顺序打印一遍完整流程（由 `JdbcSixStepsDemo` 这个 `CommandLineRunner` 输出）。

### 方式 2：只跑 main 演示（推荐对照流程图单步调试）

IDEA 里右键 `JdbcMainDemo` → Run。命令行等价写法：

```bash
java -Dfile.encoding=UTF-8 -cp "target/classes;C:\Users\asus\.m2\repository\com\mysql\mysql-connector-j\8.3.0\mysql-connector-j-8.3.0.jar" com.example.javaweb.jdbc.JdbcMainDemo
```

## 五、接口清单

| 方法 | 路径 | 对应 JDBC 执行方式 |
|---|---|---|
| GET | `/api/students?minScore=60` | `executeQuery()` |
| GET | `/api/students/{id}` | `executeQuery()` |
| POST | `/api/students` | `executeUpdate()` |
| PUT | `/api/students/{id}/score?score=99` | `executeUpdate()` |
| DELETE | `/api/students/{id}` | `executeUpdate()` |
| POST | `/api/students/batch` | `executeBatch()` |

curl 示例（注意本机沙箱要加 `--noproxy '*'`，否则会被代理转成 502）：

```bash
curl --noproxy '*' "http://localhost:8082/api/students?minScore=80"

curl --noproxy '*' -X POST "http://localhost:8082/api/students" \
  -H "Content-Type: application/json" \
  -d '{"name":"新同学","gender":"女","score":88.8,"city":"厦门"}'

curl --noproxy '*' -X PUT "http://localhost:8082/api/students/1/score?score=99"

curl --noproxy '*' -X DELETE "http://localhost:8082/api/students/2"
```

## 六、几个易错点（本项目代码里已规避）

1. **`SQLException` 是受检异常** —— Controller 方法必须 `throws SQLException`，否则编译不过。
2. **`setXxx` 下标从 1 开始**，不是 0。
3. **`rs.next()` 第一次调用才定位到第一行**（指针初始在行首之前），所以 `while` 写法不会漏第一行。
4. **绝不用字符串拼接 SQL** —— 一律用 `?` 占位，这是防 SQL 注入的根本手段。
5. **关闭顺序由 try-with-resources 保证**：声明顺序 `conn → pstmt → rs`，实际关闭是逆序 `rs → pstmt → conn`。
6. **每次操作都新建连接**（未接连接池）—— 生产项目应引入 HikariCP，本项目为教学清晰暂不引入。

## 七、下一步可以做的

- 引入 `spring-boot-starter-jdbc` + HikariCP 连接池，对比 `JdbcTemplate` 五行搞定查询的写法
- 加一个 `DbConnectionPool` 手写连接池，理解池化解决什么问题
- 把 `Student` 从 record 换成普通类 + Lombok `@Data`，对比两种写法