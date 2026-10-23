package com.example.javaweb;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 两个测试：
 *  1. Spring 容器能正常加载
 *  2. 真的起一个随机端口，发 HTTP 请求验证接口返回
 * 用来确认 VS Code 的 Testing / Debug 面板都工作正常。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JavawebApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void contextLoads() {
        assertThat(port).isGreaterThan(0);
    }

    @Test
    void helloApiShouldReturn200() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("http://localhost:" + port + "/api/hello", String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).contains("Hello, JavaWeb!");
    }
}
