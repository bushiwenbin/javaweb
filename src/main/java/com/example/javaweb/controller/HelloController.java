package com.example.javaweb.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 示例控制器：用来验证 Spring MVC 路由、JSON 序列化、路径/查询参数绑定都正常。
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class HelloController {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** GET /api/hello  → 返回一段 JSON */
    @GetMapping("/hello")
    public Map<String, Object> hello() {
        log.info("收到 /api/hello 请求");
        return ok("Hello, JavaWeb! 后端环境已就绪。");
    }

    /** GET /api/hello/皮宸西  → 路径参数绑定 */
    @GetMapping("/hello/{name}")
    public Map<String, Object> helloName(@PathVariable String name) {
        return ok("你好，" + name + "！路径参数绑定正常。");
    }

    /** GET /api/echo?msg=xxx  → 查询参数绑定 */
    @GetMapping("/echo")
    public Map<String, Object> echo(@RequestParam(defaultValue = "（没传 msg 参数）") String msg) {
        return ok("你发来的是：" + msg);
    }

    /** GET /api/time  → 返回服务器当前时间 */
    @GetMapping("/time")
    public Map<String, Object> time() {
        return ok("服务器当前时间：" + LocalDateTime.now().format(TIME_FORMAT));
    }

    /** GET /api/env  → 返回运行环境信息，方便确认用的是哪个 JDK */
    @GetMapping("/env")
    public Map<String, Object> env() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("javaVersion", System.getProperty("java.version"));
        data.put("javaVendor", System.getProperty("java.vendor"));
        data.put("javaHome", System.getProperty("java.home"));
        data.put("osName", System.getProperty("os.name"));
        data.put("userDir", System.getProperty("user.dir"));
        return ok(data);
    }

    private Map<String, Object> ok(Object data) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", 200);
        result.put("data", data);
        return result;
    }
}
