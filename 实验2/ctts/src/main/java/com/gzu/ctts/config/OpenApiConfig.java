package com.gzu.ctts.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 文档配置 —— 这是「CTTS 接入 Apifox」的关键一步。
 *
 * <p>springdoc-openapi 会根据本类的 {@link OpenAPI} Bean 生成
 * {@code /v3/api-docs} 接口文档，Apifox 通过读取该地址即可自动导入
 * 全部接口，无需手工一个个敲。
 *
 * <p>本类配置三部分元信息：
 * <ol>
 *   <li>{@code info}：文档标题、版本、描述，导入 Apifox 后会成为项目信息；</li>
 *   <li>{@code servers}：服务地址，Apifox 会用它作为「前置 URL」，
 *       与 Apifox 的「环境变量」配合后即可一键切换本地/测试/生产环境；</li>
 *   <li>{@code components.securitySchemes}：声明 JWT Bearer 认证方式，
 *       使 {@code Authorization} 请求头成为 Apifox「认证」面板里可填写的一项，
 *       而不是每个接口都要手工加请求头。</li>
 * </ol>
 */
@Configuration
public class OpenApiConfig {

    /** 服务地址，取自 application.yml 的 server.port，便于改端口时自动同步 */
    @Value("${server.port:8080}")
    private int serverPort;

    @Bean
    public OpenAPI cttsOpenAPI() {
        // ① 文档基础信息
        Info info = new Info()
                .title("校园实训任务管理系统（CTTS）API")
                .version("v0.0.1")
                .description("""
                        贵州大学软件工程专业 JavaWeb 课程实验二 —— Controller 接口开发与参数接收。

                        本工程集中演示三类参数接收方式：

                        - `@RequestParam`：接收 URL 查询串参数，如 `?pageNum=1&pageSize=3`
                        - `@PathVariable`：接收 URL 路径占位符，如 `/api/projects/3`
                        - `@RequestBody`：接收请求体 JSON，需配合 `Content-Type: application/json`

                        所有接口统一返回三段式结构：`{ "code": 200, "message": "success", "data": ... }`，
                        前端只需判断 `code` 即可区分成功与失败。
                        """)
                .contact(new Contact()
                        .name("皮宸西")
                        .email("2400770225@example.com"))
                .license(new License().name("Apache 2.0"));

        // ② 服务地址：Apifox 导入后作为默认「前置 URL」
        Server localServer = new Server()
                .url("http://localhost:" + serverPort)
                .description("本地开发环境");

        // ③ 鉴权声明：JWT Bearer Token，对应接口 4 的 @RequestHeader("Authorization")
        SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("登录后获得的 JWT 令牌，格式：Bearer <token>");

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", bearerScheme));
    }
}
