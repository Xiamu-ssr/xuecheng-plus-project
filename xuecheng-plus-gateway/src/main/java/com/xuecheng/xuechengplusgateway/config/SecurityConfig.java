package com.xuecheng.xuechengplusgateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * @author Mr.M
 * @version 1.0
 * @description 安全配置类
 * @date 2022/9/27 12:07
 */
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
//    String issuerUri;
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//     return http
//             // 使用HttpSecurity来配置路径的安全控制
//             .authorizeHttpRequests(auth -> auth
//                     // 对/course/** 和 /r/** 路径进行认证
//                     .requestMatchers(new AntPathRequestMatcher("/**")).permitAll()
//                     // 其他请求允许匿名访问
//                     .anyRequest().authenticated()
//             )
//             // 配置OAuth2资源服务器JWT验证
//             .oauth2ResourceServer(oauth2 -> oauth2
//                     .jwt(jwt -> jwt.decoder(JwtDecoders.fromIssuerLocation(issuerUri)))
//             )
//             // 禁用CSRF保护，适用于无状态API服务
//             .csrf(Customizer.withDefaults())
//             .build();
//    }
//}

@EnableWebFluxSecurity
@Configuration
public class SecurityConfig {
//    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
//    String issuerUri;

    //安全拦截配置
    @Bean
    public SecurityWebFilterChain webFluxSecurityFilterChain(ServerHttpSecurity http) throws Exception {
//
        return http.authorizeExchange(exchanges ->
                        exchanges
                                .pathMatchers("/**").permitAll()
                                .anyExchange().authenticated()
                )
//                .oauth2ResourceServer(oauth2 -> oauth2.jwt(JwtDecoders.fromIssuerLocation(issuerUri)))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .csrf(csrf -> csrf.disable())
                .build();
    }
}
