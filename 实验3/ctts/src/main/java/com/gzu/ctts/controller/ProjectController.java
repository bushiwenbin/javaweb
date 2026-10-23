package com.gzu.ctts.controller;

import com.gzu.ctts.dto.PageResult;
import com.gzu.ctts.dto.ProjectForm;
import com.gzu.ctts.dto.Result;
import com.gzu.ctts.entity.Project;
import com.gzu.ctts.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 实训项目 Controller —— 实验二的四个接口在实验三扩展为完整的 RESTful CRUD。
 *
 * <p>实验二时数据来自内存 List，接口只有「查列表、查详情、分页、新增」四个；
 * 实验三接入 MyBatis-Plus 后补全为八个，读写全部落到 {@code training_project} 表：
 * <table border="1">
 *   <caption>接口与 HTTP 方法对照</caption>
 *   <tr><th>方法</th><th>路径</th><th>语义</th></tr>
 *   <tr><td>GET</td><td>/api/projects/page</td><td>分页 + 条件查询</td></tr>
 *   <tr><td>GET</td><td>/api/projects/{id}</td><td>按主键查询</td></tr>
 *   <tr><td>POST</td><td>/api/projects</td><td>新增</td></tr>
 *   <tr><td>PUT</td><td>/api/projects/{id}</td><td>全量修改</td></tr>
 *   <tr><td>DELETE</td><td>/api/projects/{id}</td><td>物理删除</td></tr>
 *   <tr><td>GET</td><td>/api/projects/category/{category}</td><td>按类别查询（注解 SQL）</td></tr>
 *   <tr><td>PATCH</td><td>/api/projects/{id}/status</td><td>局部更新状态（注解 SQL）</td></tr>
 *   <tr><td>GET</td><td>/api/projects/statistics/category</td><td>按类别聚合统计（注解 SQL）</td></tr>
 * </table>
 */
@RestController
@RequestMapping("/api/projects")
@Validated
@Tag(name = "实训项目管理", description = "基于 MyBatis-Plus + training_project 表的实训项目增删改查接口")
public class ProjectController {

    private static final Logger log = LoggerFactory.getLogger(ProjectController.class);

    private final ProjectService projectService;

    /** 构造器注入：依赖不为 null、便于单元测试，是 Spring 官方推荐方式 */
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("/page")
    @Operation(summary = "接口1：分页查询实训项目",
            description = "status、category、keyword 均可缺省。分页由 MyBatis-Plus 分页插件"
                    + "改写为数据库 LIMIT 语句，属物理分页；结果按状态、截止日期升序排列。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "400", description = "参数越界",
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
            @Max(value = 50, message = "每页条数不能超过 50") Integer pageSize,

            @Parameter(description = "状态过滤：0-未开始 1-进行中 2-已完成", example = "1")
            @RequestParam(value = "status", required = false) Integer status,

            @Parameter(description = "类别过滤", example = "Web 后端")
            @RequestParam(value = "category", required = false) String category,

            @Parameter(description = "关键字，模糊匹配项目名称或指导教师", example = "实训")
            @RequestParam(value = "keyword", required = false) String keyword) {

        return Result.ok(projectService.page(pageNum, pageSize, status, category, keyword));
    }

    @GetMapping("/{id}")
    @Operation(summary = "接口2：按 id 查询项目详情",
            description = "对应 BaseMapper.selectById，MyBatis-Plus 自动生成 "
                    + "SELECT ... FROM training_project WHERE id = ?。id 不存在时返回 code=404。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "404", description = "实训项目不存在",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"code\":404,\"message\":\"实训项目不存在，id = 999\",\"data\":null}")))
    })
    public Result<Project> detail(
            @Parameter(description = "实训项目主键 id", example = "3", required = true)
            @PathVariable("id") Long id) {
        Project project = projectService.getById(id);
        log.info("查询实训项目详情，id = {}", id);
        return Result.ok(project);
    }

    @PostMapping
    @Operation(summary = "接口3：新增实训项目",
            description = "对应 BaseMapper.insert。插入后数据库自增主键回填到记录中；"
                    + "name 为空触发 @NotBlank 返回 code=400，deadline 格式错误同样返回 code=400。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "新增成功"),
            @ApiResponse(responseCode = "400", description = "参数校验失败",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"code\":400,\"message\":\"项目名称不能为空\",\"data\":null}")))
    })
    public Result<Project> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "新增实训项目的表单数据", required = true,
                    content = @Content(examples = @ExampleObject(name = "新增项目示例", value = """
                            {
                              "name": "实训成果答辩系统",
                              "category": "Web 后端",
                              "teacher": "丁玺润",
                              "deadline": "2027-01-08",
                              "status": 1,
                              "description": "支持答辩分组、评分与结果公示。"
                            }
                            """)))
            @Valid @RequestBody ProjectForm form) {
        log.info("接收新增实训项目请求，name = {}", form.getName());
        return Result.ok("新增实训项目成功", projectService.create(form));
    }

    @PutMapping("/{id}")
    @Operation(summary = "接口4：修改实训项目",
            description = "对应 BaseMapper.updateById。id 不存在时返回 code=404。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "修改成功"),
            @ApiResponse(responseCode = "404", description = "实训项目不存在",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"code\":404,\"message\":\"实训项目不存在，id = 999\",\"data\":null}")))
    })
    public Result<Project> update(
            @Parameter(description = "实训项目主键 id", example = "9", required = true)
            @PathVariable("id") Long id,
            @Valid @RequestBody ProjectForm form) {
        log.info("接收修改实训项目请求，id = {}", id);
        return Result.ok("修改实训项目成功", projectService.update(id, form));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "接口5：删除实训项目",
            description = "对应 BaseMapper.deleteById。Project 未使用 @TableLogic，"
                    + "因此是物理删除，数据行会真正从表中移除。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "404", description = "实训项目不存在",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"code\":404,\"message\":\"实训项目不存在，id = 999\",\"data\":null}")))
    })
    public Result<Boolean> remove(
            @Parameter(description = "实训项目主键 id", example = "9", required = true)
            @PathVariable("id") Long id) {
        log.info("接收删除实训项目请求，id = {}", id);
        return Result.ok("删除实训项目成功", projectService.remove(id));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "接口6：按类别查询实训项目",
            description = "走 ProjectMapper 中自定义的 @Select 注解 SQL，演示 BaseMapper 之外的扩展方式。")
    @ApiResponse(responseCode = "200", description = "查询成功")
    public Result<List<Project>> listByCategory(
            @Parameter(description = "项目类别", example = "Web 后端", required = true)
            @PathVariable("category") String category) {
        return Result.ok(projectService.listByCategory(category));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "接口7：修改项目状态",
            description = "走 ProjectMapper 中自定义的 @Update 注解 SQL，返回受影响行数，"
                    + "为 0 时说明 id 不存在。")
    @ApiResponse(responseCode = "200", description = "修改成功")
    public Result<Project> changeStatus(
            @Parameter(description = "实训项目主键 id", example = "3", required = true)
            @PathVariable("id") Long id,
            @Parameter(description = "状态：0-未开始 1-进行中 2-已完成", example = "2", required = true)
            @RequestParam("status") @Min(0) @Max(2) Integer status) {
        return Result.ok("修改项目状态成功", projectService.changeStatus(id, status));
    }

    @GetMapping("/statistics/category")
    @Operation(summary = "接口8：按类别统计项目数量",
            description = "走 ProjectMapper 的聚合注解 SQL：GROUP BY category + COUNT(*)，"
                    + "演示「BaseMapper 覆盖不到、必须自己写 SQL」的场景。")
    @ApiResponse(responseCode = "200", description = "统计成功")
    public Result<List<Map<String, Object>>> categoryStatistics() {
        return Result.ok(projectService.categoryStatistics());
    }
}