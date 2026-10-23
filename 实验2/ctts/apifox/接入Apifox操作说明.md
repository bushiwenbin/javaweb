# CTTS 项目接入 Apifox 操作说明

> 贵州大学软件工程 2403 · 皮宸西 · 2400770225
> 对应工程：`javaweb/实验2/ctts`（Spring Boot 3.3.5 + JDK 17 + Maven）

---

## 一、思路

传统做法是手工在 Apifox 里一个个敲接口，路径、参数、请求体全靠手抄，容易抄错，且后端一改代码文档就过期。

本方案改为**让代码自己产出文档**：

```
CTTS 工程（springdoc-openapi 依赖）
        │  启动后自动扫描 @RestController、@Operation、@Schema
        ▼
   GET /v3/api-docs        ← 符合 OpenAPI 3.0 规范的 JSON
        │
        ▼
   Apifox「导入数据 → OpenAPI / Swagger」
        │
        ▼
   4 个接口自动出现在 Apifox 里（含参数、请求体、响应示例、字段说明）
```

好处：接口签名与文档**同源**，改代码即改文档；新增接口时只需重新导入一次，不用手工维护。

---

## 二、工程侧改动（已完成）

### 1. `pom.xml` 新增依赖

```xml
<properties>
    <springdoc.version>2.6.0</springdoc.version>
</properties>

<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>${springdoc.version}</version>
</dependency>
```

> 版本对应关系：Spring Boot 3.x 用 springdoc-openapi **2.x**，Spring Boot 2.x 才用 1.x。
> 本项目是 Spring Boot 3.3.5，因此取 2.6.0。

### 2. `application.yml` 新增配置

```yaml
springdoc:
  api-docs:
    path: /v3/api-docs      # OpenAPI 规范文件地址（Apifox 读这个）
    enabled: true
  swagger-ui:
    path: /swagger-ui.html  # 内置可视化调试界面
    enabled: true
    tags-sorter: alpha
    operations-sorter: method
  packages-to-scan: com.gzu.ctts
```

### 3. 新增 `config/OpenApiConfig.java`

配置文档标题、版本、服务地址（`servers`）与 JWT Bearer 鉴权方式。`servers` 这一项很关键 —— 它就是 Apifox 导入后的「前置 URL」。

### 4. 补充 OpenAPI 注解

| 位置 | 注解 | 作用 |
| --- | --- | --- |
| `ProjectController` 类 | `@Tag` | 导入后归入「实训项目管理」目录分组 |
| 四个方法 | `@Operation` | 接口名称与说明（替代 Apifox 里的手工命名） |
| 方法参数 | `@Parameter` | 参数中文说明与示例值 |
| 方法 | `@ApiResponse` | 声明 200 / 400 / 404 响应及错误示例 |
| `ProjectForm` / `Project` / `Result` / `PageResult` | `@Schema` | 字段中文说明，导入后直接显示为文档 |

---

## 三、验证文档已生成

构建并启动（**注意端口**，见下文「坑位」一节）：

```bash
# 打包
mvn clean package -DskipTests

# 启动
java -jar target/ctts-0.0.1-SNAPSHOT.jar --server.port=8081
```

浏览器或 curl 打开文档地址：

```bash
# OpenAPI 规范文件（Apifox 要用的）
curl http://localhost:8081/v3/api-docs

# 可视化界面（人工核对用）
# 浏览器打开 http://localhost:8081/swagger-ui/index.html
```

实测结果（本机真实输出）：

```
GET /v3/api-docs        →  HTTP 200，8033 字节
GET /swagger-ui/index.html →  HTTP 200

openapi: 3.0.1
info.title: 校园实训任务管理系统（CTTS）API
servers: ['http://localhost:8081']
tags: ['实训项目管理']
securitySchemes: ['bearerAuth']

paths:
  GET    /api/projects          接口1：查询实训项目列表
  POST   /api/projects          接口4：新增实训项目
  GET    /api/projects/{id}     接口2：按 id 查询项目详情
  GET    /api/projects/page     接口3：分页查询实训项目
```

四个接口全部被正确扫描，`ProjectForm`、`PageResult`、`Result` 等 schema 也一并导出。

---

## 四、Apifox 侧操作步骤

### 方式 A（推荐）：按 URL 导入，可随时重新同步

1. 打开 Apifox，进入目标项目。
2. 左侧选择 **接口** → 顶部 **导入数据**（或 `+` → 导入）。
3. 数据类型选 **OpenAPI / Swagger**。
4. 导入方式选 **URL 导入**，地址填：
   ```
   http://localhost:8081/v3/api-docs
   ```
   > 前提：CTTS 应用处于运行状态，否则 Apifox 拉不到。
5. 高级设置里把**数据模型**、**接口**都勾上，导入模式选「**智能合并**」
   —— 这样以后后端新增接口，再导入一次即可增量更新，不会丢掉你在 Apifox 里写的手工用例。
6. 点 **确定**。4 个接口会出现在「实训项目管理」目录下。

### 方式 B：按文件导入，无需后端运行

工程内已导出好两个文件，直接拖进 Apifox 即可：

| 文件 | 说明 |
| --- | --- |
| `apifox/ctts-openapi.json` | OpenAPI 3.0 规范（JSON） |
| `apifox/ctts-openapi.yaml` | OpenAPI 3.0 规范（YAML） |
| `apifox/ctts-postman-collection.json` | Postman 集合格式，Apifox 原生支持；已把 baseUrl 抽成变量 `{{baseUrl}}` |

导入时选对应格式，导入方式选 **文件导入**。

---

## 五、导入后在 Apifox 里必做的两件事

### 1. 配置环境变量 `baseUrl`

导入的接口地址若写死成 `http://localhost:8081`，一旦换端口就得全改。所以建环境：

**Apifox → 环境管理 → 新建环境「本地开发」**，添加变量：

| 变量名 | 值 |
| --- | --- |
| `baseUrl` | `http://localhost:8081` |

然后在接口的前置 URL 里引用 `{{baseUrl}}`。以后加「测试环境」只需再建一个环境、把 `baseUrl` 换成测试机地址，接口本身一行都不用改。

> 用方式 A 导入时，springdoc 的 `servers` 会带出 `http://localhost:8081`；
> 用 `ctts-postman-collection.json` 导入时，baseUrl 与 token 已经是变量了，开箱可用。

### 2. 填写鉴权 Token

接口 4 需要 `Authorization` 请求头。在 Apifox 里配成**全局认证**：

- **环境管理 → 「本地开发」环境 → 全局参数**，加一个 Header：

| 参数名 | 值 |
| --- | --- |
| `Authorization` | `Bearer eyJhbGciOiJIUzI1NiJ9.demo` |

配好后所有接口自动带上该请求头，不用逐个接口加。

---

## 六、接口清单与参数对照

| 接口 | 请求方式与路径 | 参数位置 | 参数 |
| --- | --- | --- | --- |
| 接口1 | `GET /api/projects` | — | 无参数 |
| 接口2 | `GET /api/projects/{id}` | 路径 | `id`（Integer，例 3） |
| 接口3 | `GET /api/projects/page` | 查询串 | `pageNum`（默认 1）、`pageSize`（默认 3，上限 50） |
| 接口4 | `POST /api/projects` | 请求体 + 请求头 | JSON 体（`name` 必填）；`Authorization` 头 |

### 实测响应样例

**接口1** `GET /api/projects`
```json
{"code":200,"message":"success","data":[{"id":1,"name":"校园实训任务管理系统（CTTS）后端","category":"Web 后端",...}]}
```

**接口2** `GET /api/projects/3`
```json
{"code":200,"message":"success","data":{"id":3,"name":"实训成果统计报表","category":"数据分析","teacher":"李敏","deadline":"2026-11-30","status":0,...}}
```

**接口3** `GET /api/projects/page?pageNum=2&pageSize=3`
```json
{"code":200,"message":"success","data":{"records":[{"id":4,...}],"total":8,"pageNum":2,"pageSize":3,"pages":3}}
```

**接口4** `POST /api/projects`
```json
{"code":200,"message":"新增实训项目成功","data":{"id":9,"name":"Apifox 接口联调",...}}
```

**异常分支**（可用于 Apifox 的测试用例断言）
```
GET  /api/projects/page?pageNum=1&pageSize=999  →  {"code":400,"message":"page.pageSize 每页条数不能超过 50","data":null}
GET  /api/projects/999                          →  {"code":404,"message":"实训项目不存在，id = 999","data":null}
GET  /api/projects/abc                          →  {"code":400,"message":"参数 id 类型不正确，期望 Integer，实际收到：'abc'","data":null}
POST /api/projects  {"name":""}                 →  {"code":400,"message":"项目名称不能为空","data":null}
```

---

## 七、进阶：在 Apifox 里做自动化测试

导入后可以为接口 4 加一条**断言**，验证统一响应的约定：

```
// 断言业务状态码
pm.test("code 为 200", () => pm.expect(pm.response.json().code).to.eq(200));
// 断言新增返回了自增 id
pm.test("返回了新生成的 id", () => pm.expect(pm.response.json().data.id).to.be.a('number'));
```

把接口 1→2→3→4 串成一个**测试场景**依次执行，就得到了实验二报告里「缺少对接口的自动化回归测试」这一改进项的落地。

---

## 八、踩过的坑

### 1. 8080 端口被 Docker 容器占用

按惯例用 8080 启动时报：

```
Web server failed to start. Port 8080 was already in use.
```

排查发现 8080 被 `wslrelay.exe`（PID 26744）占用，实际是**Docker 里跑着的 nginx 容器**（`docker run -p 8080:80` 那个，访问 8080 返回 `Hello from Docker!`）。

**不要直接杀这个进程** —— 它是 Docker 的端口转发，杀了会影响容器。改端口即可：

```bash
java -jar target/ctts-0.0.1-SNAPSHOT.jar --server.port=8081
```

因此本文档中 Apifox 的 `baseUrl` 统一用 `http://localhost:8081`。

### 2. `OpenAPI.addParameters()` 方法不存在

初版 `OpenApiConfig` 里想加一个全局 `Authorization` 参数，写了 `.addParameters("Authorization", authHeader)`，编译报：

```
找不到符号: 方法 addParameters(java.lang.String, io.swagger.v3.oas.models.parameters.Parameter)
```

原因是 `io.swagger.v3.oas.models.OpenAPI` 并没有这个方法。正确做法是用 `components.addSecuritySchemes()` 声明鉴权方案（已改为这种写法），全局请求头参数则在 Apifox 里通过环境变量配置。

### 3. 启动命令必须显式指定端口

Spring Boot 配置优先级为「命令行参数 > 操作系统环境变量 > 配置文件」。本机存在名为 `SERVER__PORT` 的环境变量，会被宽松绑定识别为 `server.port` 从而覆盖 `application.yml`。用 `--server.port=8081` 可以稳定压过它。

---

## 九、附：相关文件

| 路径 | 说明 |
| --- | --- |
| `ctts/pom.xml` | 已加 springdoc 依赖 |
| `ctts/src/main/resources/application.yml` | 已加 springdoc 配置 |
| `ctts/src/main/java/com/gzu/ctts/config/OpenApiConfig.java` | 文档元信息与鉴权声明 |
| `ctts/src/main/java/com/gzu/ctts/controller/ProjectController.java` | 已加 `@Tag`/`@Operation`/`@Parameter`/`@ApiResponse` |
| `ctts/src/main/java/com/gzu/ctts/dto/` | 已加 `@Schema` |
| `ctts/apifox/ctts-openapi.json` | 导出的 OpenAPI JSON |
| `ctts/apifox/ctts-openapi.yaml` | 导出的 OpenAPI YAML |
| `ctts/apifox/ctts-postman-collection.json` | Postman 集合（含 baseUrl/token 变量） |
