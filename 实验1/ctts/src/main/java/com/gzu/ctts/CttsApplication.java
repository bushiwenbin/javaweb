package com.gzu.ctts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 校园实训任务管理系统（Campus Training Task System，CTTS）启动类。
 *
 * <p>{@code @SpringBootApplication} 是一个组合注解，等价于：
 * <ul>
 *   <li>{@code @SpringBootConfiguration}：声明为配置类；</li>
 *   <li>{@code @EnableAutoConfiguration}：开启 Spring Boot 自动配置（"约定优于配置"的核心）；</li>
 *   <li>{@code @ComponentScan}：以本类所在包 {@code com.gzu.ctts} 为根，扫描其下的
 *       {@code @Component}/{@code @Service}/{@code @Controller} 等组件。</li>
 * </ul>
 * 因此启动类必须放在其它业务代码的最外层包，否则组件扫描不到。
 */
@SpringBootApplication
public class CttsApplication {

    public static void main(String[] args) {
        SpringApplication.run(CttsApplication.class, args);
    }

}
