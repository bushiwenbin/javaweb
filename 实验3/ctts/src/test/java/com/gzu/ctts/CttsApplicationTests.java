package com.gzu.ctts;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 冒烟测试：验证 Spring 容器（含 MyBatis-Plus、数据源等自动配置）能够正常装配。
 */
@SpringBootTest
class CttsApplicationTests {

    @Test
    void contextLoads() {
        // 只要上下文能加载成功，即说明自动配置装配正确
    }

}
