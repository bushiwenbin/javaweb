package com.example.javaweb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 首页：返回一个静态 HTML，方便启动后直接用浏览器打开确认环境正常。
 */
@Controller
public class IndexController {

    @GetMapping("/")
    @ResponseBody
    public String index() {
        return """
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>JavaWeb 环境自检</title>
                  <style>
                    body{font-family:-apple-system,"Segoe UI","Microsoft YaHei",sans-serif;
                         background:#f6f7f9;color:#1f2329;margin:0;padding:48px 24px;}
                    .card{max-width:640px;margin:0 auto;background:#fff;border-radius:14px;
                          padding:32px 36px;box-shadow:0 2px 16px rgba(0,0,0,.06);}
                    h1{font-size:22px;margin:0 0 6px;}
                    p.sub{color:#646a73;margin:0 0 24px;font-size:14px;}
                    .ok{display:inline-block;background:#e8f5e9;color:#1e7b34;
                        padding:4px 12px;border-radius:20px;font-size:13px;font-weight:600;}
                    ul{list-style:none;padding:0;margin:20px 0 0;}
                    li{padding:11px 0;border-bottom:1px solid #f0f1f3;font-size:14px;
                       display:flex;justify-content:space-between;gap:16px;}
                    li:last-child{border-bottom:none;}
                    code{background:#f2f3f5;padding:2px 7px;border-radius:5px;
                         font-family:Consolas,Monaco,monospace;font-size:13px;color:#c7254e;}
                    a{color:#1470cc;text-decoration:none;}
                    a:hover{text-decoration:underline;}
                  </style>
                </head>
                <body>
                  <div class="card">
                    <span class="ok">● 环境正常</span>
                    <h1>JavaWeb 后端环境自检通过</h1>
                    <p class="sub">Spring Boot 已启动，内嵌 Tomcat 正在处理请求。</p>
                    <ul>
                      <li><span>基础接口</span><a href="/api/hello"><code>GET /api/hello</code></a></li>
                      <li><span>路径参数</span><a href="/api/hello/world"><code>GET /api/hello/{name}</code></a></li>
                      <li><span>查询参数</span><a href="/api/echo?msg=test"><code>GET /api/echo?msg=test</code></a></li>
                      <li><span>服务器时间</span><a href="/api/time"><code>GET /api/time</code></a></li>
                      <li><span>运行环境信息</span><a href="/api/env"><code>GET /api/env</code></a></li>
                    </ul>
                    <h1 style="font-size:17px;margin:28px 0 4px;">JDBC 六步流程接口</h1>
                    <p class="sub" style="margin:0 0 12px;">原生 JDBC 编写，未使用 JdbcTemplate，每一步都在代码里可见。</p>
                    <ul>
                      <li><span>查询列表</span><a href="/api/students"><code>GET /api/students?minScore=60</code></a></li>
                      <li><span>按主键查询</span><a href="/api/students/1"><code>GET /api/students/{id}</code></a></li>
                      <li><span>新增</span><code>POST /api/students</code></li>
                      <li><span>改分数</span><code>PUT /api/students/{id}/score?score=99</code></li>
                      <li><span>删除</span><code>DELETE /api/students/{id}</code></li>
                      <li><span>批量新增</span><code>POST /api/students/batch</code></li>
                    </ul>
                  </div>
                </body>
                </html>
                """;
    }
}
