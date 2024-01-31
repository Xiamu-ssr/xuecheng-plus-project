package com.xuecheng;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

/**
 * 内容管理服务启动类
 *
 * @author mumu
 * @date 2024/01/21
 */
@SpringBootApplication
@EnableDiscoveryClient
@Slf4j
@OpenAPIDefinition(info = @Info(title = "学成在线内容管理系统", description = "内容系统对课程相关信息进行管理", version = "1.0.0"))
//@OpenAPIDefinition(info = @Info(title = "内容管理系统", description = "对课程相关信息进行管理", version = "1.0.0"))
public class ContentApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContentApplication.class, args);
//        ConfigurableApplicationContext context = SpringApplication.run(ContentApplication.class, args);
//        Environment env = context.getEnvironment();
//        String mysqlUrl = env.getProperty("spring.datasource.url");
//        log.info("MySQL URL: {}", mysqlUrl);
    }
}
