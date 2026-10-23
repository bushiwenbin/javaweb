package com.gzu.ctts.controller;

import com.gzu.ctts.dto.PageResult;
import com.gzu.ctts.dto.ProjectForm;
import com.gzu.ctts.dto.Result;
import com.gzu.ctts.entity.Project;
import com.gzu.ctts.exception.BizException;
import com.gzu.ctts.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 实训项目 Controller —— 实验二核心类。
 *
 * <p>负责把 HTTP 请求映射到具体方法，并把 Service 返回的业务对象序列化成 JSON。
 * 本类集中演示三类参数接收方式：
 * <ol>
 *   <li>{@code @RequestParam}：接收查询串参数，如 {@code ?pageNum=1&pageSize=3}；</li>
 *   <li>{@code @PathVariable}：接收 URL 路径占位符，如 {@code /api/projects/3}；</li>
 *   <li>{@code @RequestBody}：接收请求体中的 JSON 并反序列化为对象。</li>
 * </ol>
 *
 * <p>注意类上的 {@code @RestController} 是 {@code @Controller} 与 {@code @ResponseBody}
 * 的组合注解：前者把本类注册为 Spring MVC 的处理器，后者让所有方法的返回值
 * 都经 HttpMessageConverter（这里是 Jackson）转成 JSON 写入响应体，
 * 而不是当成视图名去解析模板。
 *
 * <p>类上的 {@code @Validated} 开启方法级参数校验，
 * 使 {@code @RequestParam}/{@code @PathVariable} 上的 {@code @Min}、{@code @Max} 生效。
 *
 * <p>类上的 {@code @Tag} 是 springdoc-openapi 的注解：导入 Apifox 后，
 * 本类下的四个接口会被归到「实训项目管理」这个目录分组里，
 * 避免与其它 Controller 的接口混在一起。
 */
@RestController
@RequestMapping("/api/projects")
@Validated
@Tag(name = "实训项目管理", description = "实训项目的查询、分页与新增接口")
public class ProjectController {

    private static final Logger log = LoggerFactory.getLogger(ProjectController.class);

    private final ProjectService projectService;

    /**
     * 构造器注入。相比字段注入 {@code @Autowired}，
     * 构造器注入能保证依赖不为 null、便于单元测试，也是 Spring 官方推荐的方式。
     */
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * 接口 1：查询实训项目列表（模拟数据）。
     *
     * <pre>
     * GET /api/projects
     * </pre>
     *
     * <p>路径上没有任何占位符，也没有查询参数，属于最简形式的请求映射。
     * 返回值是 {@code List<Project>}，Jackson 会把它序列化成 JSON 数组。
     */
    @GetMapping
    @Operation(
            summary = "接口1：查询实训项目列表",
            description = "返回全部实训项目（本实验为 8 条内存模拟数据）。路径上无占位符也无查询参数，"
                    + "是最简形式的请求映射。")
    @ApiResponse(responseCode = "200", description = "查询成功")
    public Result<List<Project>> list() {
        List<Project> projects = projectService.listAll();
        log.info("查询实训项目列表，共 {} 条", projects.size());
        return Result.ok(projects);
    }

    /**
     * 接口 2：按 id 查询实训项目详情。
     *
     * <pre>
     * GET /api/projects/{id}      例如：GET /api/projects/3
     * </pre>
     *
     * <p>{@code @PathVariable} 取出 URL 中的路径变量 {@code {id}}，
     * 并按方法参数类型 {@code Integer} 自动完成字符串到数字的类型转换；
     * 若传入非数字（如 {@code /api/projects/abc}），
     * Spring 会抛 {@code MethodArgumentTypeMismatchException}，由全局异常处理器兜底。
     *
     * <p>{@code @PathVariable("id")} 中的名字必须与 {@code @GetMapping} 里的占位符一致，
     * 方法参数名与占位符同名时该值可省略。
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "接口2：按 id 查询项目详情",
            description = "使用 @PathVariable 接收 URL 路径变量。id 不存在时返回 code=404；"
                    + "传入非数字（如 abc）时由全局异常处理器归一为 code=400。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "404", description = "实训项目不存在",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"code\":404,\"message\":\"实训项目不存在，id = 999\",\"data\":null}")))
    })
    public Result<Project> detail(
            @Parameter(description = "实训项目主键 id", example = "3", required = true)
            @PathVariable("id") Integer id) {
        Project project = projectService.getById(id);
        if (project == null) {
            // 抛出业务异常，由 GlobalExceptionHandler 统一转成 404 的 JSON 响应
            throw new BizException(Result.CODE_NOT_FOUND, "实训项目不存在，id = " + id);
        }
        log.info("查询实训项目详情，id = {}", id);
        return Result.ok(project);
    }

    /**
     * 接口 3：分页查询实训项目。
     *
     * <pre>
     * GET /api/projects/page?pageNum=2&amp;pageSize=3
     * </pre>
     *
     * <p>{@code @RequestParam} 用于接收查询串（query string）中的参数：
     * <ul>
     *   <li>{@code required = false} + {@code defaultValue} 让参数可缺省，
     *       不传时使用默认值，避免「Required request parameter is not present」错误；</li>
     *   <li>配合类上的 {@code @Validated}，{@code @Min}/{@code @Max} 会对取值做范围校验，
     *       例如 {@code pageSize=999} 会被拦截并返回 400。</li>
     * </ul>
     *
     * <p>返回值封装成 {@code Result<PageResult<Project>>}：外层是统一响应，
     * 内层 {@code PageResult} 携带当前页数据与总数、总页数等分页元信息。
     */
    @GetMapping("/page")
    @Operation(
            summary = "接口3：分页查询实训项目",
            description = "使用 @RequestParam 接收查询串参数。两个参数都可缺省（不传时取默认值 1 和 3）；"
                    + "pageSize 超过 @Max(50) 上限时返回 code=400。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "分页查询成功"),
            @ApiResponse(responseCode = "400", description = "参数越界或类型错误",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"code\":400,\"message\":\"pageSize 每页条数不能超过 50\",\"data\":null}")))
    })
    public Result<PageResult<Project>> page(
            @Parameter(description = "页码，从 1 开始", example = "1")
            @RequestParam(value = "pageNum", required = false, defaultValue = "1")
            @Min(value = 1, message = "页码必须大于等于 1") Integer pageNum,

            @Parameter(description = "每页条数，范围 1~50", example = "3")
            @RequestParam(value = "pageSize", required = false, defaultValue = "3")
            @Min(value = 1, message = "每页条数必须大于等于 1")
            @Max(value = 50, message = "每页条数不能超过 50") Integer pageSize) {

        PageResult<Project> pageResult = projectService.page(pageNum, pageSize);
        log.info("分页查询实训项目：pageNum = {}, pageSize = {}, 命中 {} 条",
                pageNum, pageSize, pageResult.getRecords().size());
        return Result.ok(pageResult);
    }

    /**
     * 接口 4：新增实训项目。
     *
     * <pre>
     * POST /api/projects
     * Content-Type: application/json
     * Authorization: Bearer &lt;token&gt;
     *
     * { "name": "xxx", "category": "Web 后端", ... }
     * </pre>
     *
     * <p>{@code @RequestBody} 告诉 Spring：请求体不是表单键值对，
     * 而是 JSON，请用 Jackson 反序列化成 {@code ProjectForm} 对象。
     * 它常与 {@code @Valid} 搭配，触发 DTO 字段上声明的 {@code @NotBlank}、
     * {@code @Size} 等约束；校验不通过时抛 {@code MethodArgumentNotValidException}。
     *
     * <p>{@code @RequestHeader} 用于读取请求头，这里取出 {@code Authorization} 令牌，
     * 演示「请求头参数」的接收方式。设 {@code required = false} 是让本实验调试更方便，
     * 真实项目中应设为 true 并由拦截器统一校验。
     */
    @PostMapping
    @Operation(
            summary = "接口4：新增实训项目",
            description = "使用 @RequestBody 接收请求体 JSON，并用 @RequestHeader 读取 Authorization 令牌。"
                    + "必须携带 Content-Type: application/json；name 为空时触发 @NotBlank 校验返回 code=400。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "新增成功"),
            @ApiResponse(responseCode = "400", description = "请求体参数校验失败",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"code\":400,\"message\":\"项目名称不能为空\",\"data\":null}")))
    })
    public Result<Project> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "新增实训项目的表单数据",
                    required = true,
                    content = @Content(examples = @ExampleObject(
                            name = "新增项目示例",
                            value = """
                                    {
                                      "name": "实训成果答辩系统",
                                      "category": "Web 后端",
                                      "teacher": "丁玺润",
                                      "deadline": "2027-01-08",
                                      "status": 1,
                                      "description": "支持答辩分组、评分与结果公示。"
                                    }
                                    """)))
            @Valid @RequestBody ProjectForm form,
            @Parameter(description = "JWT 令牌，格式 Bearer <token>",
                    example = "Bearer eyJhbGciOiJIUzI1NiJ9.demo")
            @RequestHeader(value = "Authorization", required = false)
            String token) {
        log.info("接收新增实训项目请求，Authorization = {}", token);
        Project created = projectService.create(form);
        log.info("新增成功，生成的 id = {}", created.getId());
        return Result.ok("新增实训项目成功", created);
    }
}
