package com.xuecheng;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * 内容管理服务启动类
 *
 * @author mumu
 * @date 2024/01/21
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.xuecheng.feign.client")
@Slf4j
@OpenAPIDefinition(info = @Info(title = "学成在线内容管理系统", description = "内容系统对课程相关信息进行管理", version = "1.0.0"))
//@OpenAPIDefinition(info = @Info(title = "内容管理系统", description = "对课程相关信息进行管理", version = "1.0.0"))
public class ContentApplication {
    public static void main(String[] args) {
        System.setProperty("nacos.logging.default.config.enabled", "false");
        SpringApplication.run(ContentApplication.class, args);
        //ConfigurableApplicationContext context = SpringApplication.run(ContentApplication.class, args);
        //Environment env = context.getEnvironment();
        //String mysqlUrl = env.getProperty("spring.datasource.url");
        //log.info("MySQL URL: {}", mysqlUrl);
        //String logging = env.getProperty("logging.config");
        //log.info("logging.config: {}", logging);
        //String freemarker = env.getProperty("spring.freemarker.template-loader-path");
        //log.info("spring.freemarker.template-loader-path: {}", freemarker);
        //String redis = env.getProperty("spring.redis.host");
        //log.info("spring.redis.host: {}", redis);
    }

    //解决空密码问题
    @Bean
    public RedissonAutoConfigurationCustomizer redissonAutoConfigurationCustomizer() {
        return configuration -> {
            if (StringUtils.isEmpty(configuration.useSingleServer().getPassword())) {
                configuration.useSingleServer().setPassword(null);
            }
        };
    }
}
