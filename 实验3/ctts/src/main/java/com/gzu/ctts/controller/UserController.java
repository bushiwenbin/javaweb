package com.gzu.ctts.controller;

import com.gzu.ctts.dto.PageResult;
import com.gzu.ctts.dto.Result;
import com.gzu.ctts.dto.UserForm;
import com.gzu.ctts.entity.User;
import com.gzu.ctts.mapper.UserMapper;
import com.gzu.ctts.service.UserService;
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
 * 用户管理 Controller —— 实验三新增，演示基于 MyBatis-Plus 的完整 CRUD 接口。
 *
 * <p>本类共 7 个接口，覆盖 REST 风格中常用的动作与 HTTP 方法对应关系：
 * <table border="1">
 *   <caption>接口与 HTTP 方法对照</caption>
 *   <tr><th>方法</th><th>路径</th><th>语义</th></tr>
 *   <tr><td>GET</td><td>/api/users/page</td><td>分页 + 条件查询</td></tr>
 *   <tr><td>GET</td><td>/api/users/{id}</td><td>按主键查询</td></tr>
 *   <tr><td>POST</td><td>/api/users</td><td>新增</td></tr>
 *   <tr><td>PUT</td><td>/api/users/{id}</td><td>全量修改</td></tr>
 *   <tr><td>DELETE</td><td>/api/users/{id}</td><td>删除（逻辑删除）</td></tr>
 *   <tr><td>GET</td><td>/api/users/role/{role}</td><td>按角色查询（注解 SQL）</td></tr>
 *   <tr><td>PATCH</td><td>/api/users/{id}/status</td><td>局部更新状态（注解 SQL）</td></tr>
 * </table>
 *
 * <p>Controller 层不碰数据库，只做「参数接收 → 调用 Service → 包装响应」，
 * 持久层细节全部下沉到 Service 与 Mapper。
 */
@RestController
@RequestMapping("/api/users")
@Validated
@Tag(name = "用户管理", description = "基于 MyBatis-Plus + sys_user 表的用户增删改查接口")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping("/page")
    @Operation(summary = "接口1：分页查询用户",
            description = "role 与 keyword 均可缺省。keyword 会对 username 与 real_name 做模糊匹配；"
                    + "分页由 MyBatis-Plus 分页插件生成 LIMIT 语句在数据库侧完成。")
    @ApiResponse(responseCode = "200", description = "查询成功")
    public Result<PageResult<User>> page(
            @Parameter(description = "页码，从 1 开始", example = "1")
            @RequestParam(value = "pageNum", required = false, defaultValue = "1")
            @Min(value = 1, message = "页码必须大于等于 1") Integer pageNum,

            @Parameter(description = "每页条数，范围 1~50", example = "3")
            @RequestParam(value = "pageSize", required = false, defaultValue = "3")
            @Min(value = 1, message = "每页条数必须大于等于 1")
            @Max(value = 50, message = "每页条数不能超过 50") Integer pageSize,

            @Parameter(description = "角色过滤，可选：ADMIN / TEACHER / STUDENT", example = "STUDENT")
            @RequestParam(value = "role", required = false) String role,

            @Parameter(description = "关键字，模糊匹配账号或姓名", example = "2024")
            @RequestParam(value = "keyword", required = false) String keyword) {

        PageResult<User> result = userService.page(pageNum, pageSize, role, keyword);
        return Result.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "接口2：按 id 查询用户详情",
            description = "对应 BaseMapper.selectById。password 字段因 @TableField(select = false) 不会返回。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "404", description = "用户不存在",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"code\":404,\"message\":\"用户不存在，id = 999\",\"data\":null}")))
    })
    public Result<User> detail(
            @Parameter(description = "用户主键 id", example = "5", required = true)
            @PathVariable("id") Long id) {
        return Result.ok(userService.getById(id));
    }

    @PostMapping
    @Operation(summary = "接口3：新增用户",
            description = "对应 BaseMapper.insert。插入后数据库自增主键会回填到返回对象中；"
                    + "账号重复时返回 code=400。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "新增成功"),
            @ApiResponse(responseCode = "400", description = "参数校验失败或账号已存在",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"code\":400,\"message\":\"登录账号已存在：2024001\",\"data\":null}")))
    })
    public Result<User> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "新增用户的表单数据", required = true,
                    content = @Content(examples = @ExampleObject(name = "新增用户示例", value = """
                            {
                              "username": "2024008",
                              "password": "123456",
                              "realName": "赵敏",
                              "role": "STUDENT",
                              "email": "2024008@qq.com",
                              "phone": "13900000008",
                              "status": 1
                            }
                            """)))
            @Valid @RequestBody UserForm form) {
        log.info("接收新增用户请求，username = {}", form.getUsername());
        return Result.ok("新增用户成功", userService.create(form));
    }

    @PutMapping("/{id}")
    @Operation(summary = "接口4：修改用户",
            description = "对应 BaseMapper.updateById。password 留空表示不修改原密码；"
                    + "实体为 null 的字段不会进入 SET 子句。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "修改成功"),
            @ApiResponse(responseCode = "404", description = "用户不存在",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"code\":404,\"message\":\"用户不存在，id = 999\",\"data\":null}")))
    })
    public Result<User> update(
            @Parameter(description = "用户主键 id", example = "8", required = true)
            @PathVariable("id") Long id,
            @Valid @RequestBody UserForm form) {
        log.info("接收修改用户请求，id = {}", id);
        return Result.ok("修改用户成功", userService.update(id, form));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "接口5：删除用户（逻辑删除）",
            description = "@TableLogic 使 deleteById 被改写成 UPDATE sys_user SET deleted = 1，"
                    + "数据行仍在表中，但后续查询会自动过滤。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "404", description = "用户不存在",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"code\":404,\"message\":\"用户不存在，id = 999\",\"data\":null}")))
    })
    public Result<Boolean> remove(
            @Parameter(description = "用户主键 id", example = "9", required = true)
            @PathVariable("id") Long id) {
        log.info("接收删除用户请求，id = {}", id);
        return Result.ok("删除用户成功", userService.remove(id));
    }

    @GetMapping("/role/{role}")
    @Operation(summary = "接口6：按角色查询用户",
            description = "走 UserMapper 中自定义的 @Select 注解 SQL，SQL 由开发者手写，"
                    + "逻辑删除条件 deleted = 0 也需自行维护。")
    @ApiResponse(responseCode = "200", description = "查询成功")
    public Result<List<User>> listByRole(
            @Parameter(description = "角色：ADMIN / TEACHER / STUDENT", example = "TEACHER", required = true)
            @PathVariable("role") String role) {
        return Result.ok(userService.listByRole(role));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "接口7：启用/禁用用户",
            description = "走 UserMapper 中自定义的 @Update 注解 SQL，返回受影响行数，"
                    + "为 0 时说明 id 不存在或已被逻辑删除。")
    @ApiResponse(responseCode = "200", description = "修改成功")
    public Result<User> changeStatus(
            @Parameter(description = "用户主键 id", example = "6", required = true)
            @PathVariable("id") Long id,
            @Parameter(description = "状态：1-启用，0-禁用", example = "0", required = true)
            @RequestParam("status") @Min(0) @Max(1) Integer status) {
        return Result.ok("修改用户状态成功", userService.changeStatus(id, status));
    }

    @GetMapping("/statistics/role")
    @Operation(summary = "接口8：按角色统计用户数",
            description = "演示 Mapper 聚合查询：遍历全部角色调用 countByRole 注解 SQL，"
                    + "返回「角色 → 人数」的映射。")
    @ApiResponse(responseCode = "200", description = "统计成功")
    public Result<Map<String, Long>> statistics() {
        Map<String, Long> stat = new java.util.LinkedHashMap<>();
        for (String role : List.of("ADMIN", "TEACHER", "STUDENT")) {
            stat.put(role, userMapper.countByRole(role));
        }
        return Result.ok(stat);
    }
}