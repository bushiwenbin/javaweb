**（1）梳理工程结构，建立 Controller 层**

实验一已完成 CTTS 工程骨架（`com.gzu:ctts`，Spring Boot 3.3.5，JDK 17，Maven 构建）。实验二的任务是在该骨架上补齐接口层，因此先按「接口层—业务层—模型层」的职责划分建立包结构，各包的职责见表 2.1。

表2.1  实验二新增的包及职责

| 包 | 主要类 | 职责 |
| --- | --- | --- |
| controller | ProjectController | 接收请求、绑定参数、返回 JSON |
| service | ProjectService | 业务逻辑接口 |
| service.impl | ProjectServiceImpl | 业务逻辑实现，本实验用内存模拟数据占位 |
| entity | Project | 领域对象，字段与数据库表一一对应 |
| dto | Result、PageResult、ProjectForm | 统一响应、分页结果、新增请求体 |
| exception | BizException、GlobalExceptionHandler | 业务异常与全局异常处理器 |

需要强调：Controller 只负责「协议转换」，即把 HTTP 报文翻译成 Java 方法调用、再把返回值翻译成 JSON；真正的业务规则放在 Service。这样划分后，后续把模拟数据换成 MyBatis 数据库查询时，Controller 代码无需改动。

新建后的工程目录结构如图 2.1 所示。

@@FIG_TREE@@
图2.1  CTTS 工程目录结构（实验二新增 Controller 层）

**（2）编写统一响应封装 Result**

前后端分离场景下，服务端固定返回「状态码 + 提示信息 + 业务数据」三段式 JSON，前端只需判断 `code` 就能区分成功与失败，不必再解析 HTTP 状态码。`Result<T>` 用泛型承载业务数据，并提供 `ok()`、`fail()` 静态工厂方法，使 Controller 的返回语句保持简洁。源码如图 2.2 所示。

@@FIG_RESULT@@
图2.2  统一响应封装 Result.java

其中 `code` 沿用 HTTP 语义（200 成功、400 参数错误、404 资源不存在），`data` 为泛型字段，既能装单个对象，也能装集合或分页对象。

**（3）编写请求参数对象 ProjectForm**

`@RequestBody` 需要一个与 JSON 结构对应的 Java 类。`ProjectForm` 除承载字段外，还用 `@NotBlank`、`@Size` 声明校验规则；配合 Controller 参数上的 `@Valid`，非法请求会在进入业务逻辑之前就被拦截。源码如图 2.3 所示。

@@FIG_FORM@@
图2.3  新增请求参数对象 ProjectForm.java

**（4）编写 Service：先用模拟数据返回**

按实验内容要求，本阶段的接口先返回模拟数据，不接入数据库。`ProjectServiceImpl` 用 `ArrayList` 维护 8 条实训项目记录，对外提供四个方法，见表 2.2。数据在应用重启后重置，因此与实验一建立的 `ctts` 库互不干扰。

表2.2  ProjectService 提供的方法

| 方法 | 说明 |
| --- | --- |
| `listAll()` | 返回全部实训项目 |
| `getById(Integer id)` | 按 id 查找，找不到返回 `null` |
| `page(int pageNum, int pageSize)` | 按页码切片，返回 `PageResult`（含当前页数据、总数、总页数） |
| `create(ProjectForm form)` | 追加一条记录，id 取当前最大值 +1 |

**（5）编写 ProjectController，实现三类参数接收**

控制器类上的 `@RestController` 等价于 `@Controller` 与 `@ResponseBody` 的组合：既把本类注册为 Spring MVC 的处理器，又让所有方法的返回值经 `HttpMessageConverter`（本项目是 Jackson）转成 JSON 写入响应体，而不是当成视图名去解析模板。类上的 `@RequestMapping("/api/projects")` 提供统一路径前缀，方法级注解只需写相对路径；类上的 `@Validated` 则开启方法级参数校验，使 `@RequestParam`、`@PathVariable` 上的 `@Min`、`@Max` 生效。控制器完整源码见图 2.4 至图 2.6。

@@FIG_CTRL1@@
图2.4  ProjectController.java（第 1–54 行：类声明、构造器注入与接口 1）

@@FIG_CTRL2@@
图2.5  ProjectController.java（第 55–107 行：接口 2 与接口 3）

@@FIG_CTRL3@@
图2.6  ProjectController.java（第 108–160 行：接口 4 与请求头参数）

四个接口的清单见表 2.3。四个接口统一用 `Result` 包装返回：接口 1 的数据部分是项目列表，接口 2 与接口 4 是单个项目对象，接口 3 是带分页元信息的 `PageResult`。

表2.3  实训项目接口清单

| 接口 | 请求方式与路径 | 参数接收方式 |
| --- | --- | --- |
| 接口1 | `GET /api/projects` | 无参数 |
| 接口2 | `GET /api/projects/{id}` | `@PathVariable` |
| 接口3 | `GET /api/projects/page?pageNum=1&pageSize=3` | `@RequestParam` |
| 接口4 | `POST /api/projects` | `@RequestBody`、`@RequestHeader` |

三种参数接收方式的适用场景与要点对比见表 2.4。

表2.4  三种参数接收方式对比

| 注解 | 数据来源 | 典型写法 | 关键点 |
| --- | --- | --- | --- |
| `@RequestParam` | URL 查询串 | `?pageNum=1&pageSize=3` | 可设 `required=false` 与默认值；配 `@Min`、`@Max` 校验 |
| `@PathVariable` | URL 路径占位符 | `/api/projects/3` | 名字须与占位符一致；自动做类型转换 |
| `@RequestBody` | 请求体 JSON | `{"name":"..."}` | 需 `application/json`；配 `@Valid` 触发校验 |

**（6）构建并启动项目**

工程使用 Maven 构建，先打包生成可执行 fat-jar，再启动应用，两条命令如下。

`mvn clean package -DskipTests`

`java -jar target/ctts-0.0.1-SNAPSHOT.jar --server.port=8080`

启动日志如图 2.7 所示。可以看到 Spring Boot 版本为 3.3.5，运行在 Java 17 上，内嵌 Tomcat 10.1.31 监听 8080 端口，整个启动耗时约 3.5 秒。

@@FIG_STARTUP@@
图2.7  Spring Boot 启动日志

日志中的 `No MyBatis mapper was found in '[com.gzu.ctts]'` 是一条警告而非错误：本实验的接口返回模拟数据，尚未编写 Mapper 接口，因此 MyBatis-Plus 扫描不到任何映射器，属于预期现象，后续实验接入数据库后该警告自动消失。

需说明一点：启动命令显式加了 `--server.port=8080`。原因是 Spring Boot 的配置读取优先级为「命令行参数 > 操作系统环境变量 > 配置文件」，本机环境中存在名为 `SERVER__PORT` 的环境变量，会被宽松绑定规则识别为 `server.port` 而覆盖 `application.yml` 的配置，用命令行参数可以稳定压过它。

**（7）使用 HTTP 客户端调试各接口**

下面按接口顺序逐个发起真实请求，并保留原始响应报文。

① 查询实训项目列表。路径上既无占位符也无查询参数，是最简形式的请求映射，返回 JSON 数组，如图 2.8 所示。

@@FIG_API1@@
图2.8  接口1 查询实训项目列表——请求与响应

② 按 id 查询详情。`@PathVariable` 取出 URL 中的 `{id}` 并按方法参数类型 `Integer` 自动完成字符串到数字的转换。正常请求（id=3）与两个异常分支的结果如图 2.9 所示：当 id 不存在时，Service 返回 `null`，Controller 主动抛出 `BizException`，由全局异常处理器转成 `code=404` 的 JSON；当传入非数字时，Spring 在参数绑定期就抛出 `MethodArgumentTypeMismatchException`，被统一归一为 `code=400`。

@@FIG_API2@@
图2.9  接口2 按 id 查详情（含异常分支）

③ 分页查询。`@RequestParam` 接收查询串中的 `pageNum` 与 `pageSize`，两个参数都设了 `defaultValue`，不传时分别取 1 和 3；返回体中的 `PageResult` 除当前页数据外还携带 `total`（总记录数）与 `pages`（总页数），前端据此渲染页码条。第 1 页与第 2 页的响应如图 2.10 所示，可以验证 `total=8`、`pageSize=3` 时 `pages=3`，且两页数据无重叠。

@@FIG_API3@@
图2.10  接口3 分页查询（@RequestParam 接收页码与每页条数）

不传任何参数时走默认值；`pageSize` 超过 `@Max(50)` 上限时，方法级校验拦截并返回 `code=400`，两种情形如图 2.11 所示。

@@FIG_API3B@@
图2.11  接口3 缺省参数与参数越界校验

④ 新增项目。`@RequestBody` 把请求体中的 JSON 反序列化成 `ProjectForm`，`@RequestHeader` 读取请求头中的 `Authorization` 令牌，两者在同一个方法上演示了「请求体参数」与「请求头参数」。新增成功后返回生成的 id；若 `name` 为空，`@NotBlank` 校验失败，响应体中同时给出 `code=400` 与具体原因，如图 2.12 所示。

@@FIG_API4@@
图2.12  接口4 新增项目（@RequestBody + @RequestHeader）

**（8）对照服务端日志验证参数接收**

客户端只看到响应结果，为确认参数确实按预期完成了绑定与校验，再回看服务端运行日志（图 2.13）。日志中每条 `ProjectController` 记录都打印了解析后的实际取值（如 `pageNum = 2, pageSize = 3`、`Authorization = Bearer ...`），两条 `GlobalExceptionHandler` 记录则是异常分支被统一收口的证据——二者与客户端收到的响应一一对应。

@@FIG_RUNTIME@@
图2.13  服务端运行日志——参数接收与异常处理

**（9）调试过程中遇到的问题及解决**

问题一：`GET /api/projects/list` 返回了参数类型错误而不是 404。原因是 `@GetMapping("/{id}")` 会把任何单段路径都当成 `{id}` 匹配，`list` 因此落到了接口 2 上。这说明路径变量属于「宽匹配」，设计 REST 路径时应把固定名词（如 `/page`）放在与路径变量同一层级，并让前端严格按约定调用。

问题二：`pageSize=999` 时一度返回 500。原因是初版只在 `@NotBlank` 等请求体校验上做了处理，没有覆盖方法参数校验抛出的 `ConstraintViolationException`。补充全局异常处理器后，该类异常被归一为 400，与「参数格式错误属于客户端错误」的语义保持一致。

问题三：统一响应体一度让前端无法感知失败。原因是 Controller 直接返回了业务对象，异常时由 Spring 默认错误页兜底，返回结构与其他接口不一致。引入 `Result` 后所有接口无论成功失败都是同一结构，前端只需判断 `code`。
