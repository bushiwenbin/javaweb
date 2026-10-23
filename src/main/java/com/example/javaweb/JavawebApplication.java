package com.example.javaweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

/**
 * 应用入口。
 *
 * 启动方式（任选其一）：
 *  1. VS Code 里按 F5，选「启动 JavawebApplication」
 *  2. 终端执行 mvn spring-boot:run
 *  3. 打包后执行 java -jar target/javaweb-0.0.1-SNAPSHOT.jar
 */
@SpringBootApplication
public class JavawebApplication {

    public static void main(String[] args) {
        Environment env = SpringApplication.run(JavawebApplication.class, args).getEnvironment();

        String port = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path", "");

        System.out.println();
        System.out.println("----------------------------------------------------------");
        System.out.println("  应用已启动，访问地址：");
        System.out.println("  本机:   http://localhost:" + port + contextPath + "/");
        System.out.println("  接口:   http://localhost:" + port + contextPath + "/api/hello");
        System.out.println("  Java:   " + System.getProperty("java.version"));
        System.out.println("----------------------------------------------------------");
        System.out.println();
    }
}
