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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * @author xiamu
 * @version 1.0
 * @description 安全配置类
 * @date 2022/9/27 12:07
 */
@EnableWebFluxSecurity
@Configuration
public class SecurityConfig {
//    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
//    String issuerUri;

    //安全拦截配置
    @Bean
    public SecurityWebFilterChain webFluxSecurityFilterChain(ServerHttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeExchange(exchanges ->
                        exchanges
                                .pathMatchers("/**").permitAll()
                                .anyExchange().authenticated()
                )
//                .oauth2ResourceServer(oauth2 -> oauth2.jwt(JwtDecoders.fromIssuerLocation(issuerUri)))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.addAllowedOriginPattern("*"); // 允许任何源
        corsConfig.addAllowedMethod("*"); // 允许任何HTTP方法
        corsConfig.addAllowedHeader("*"); // 允许任何HTTP头
        corsConfig.setAllowCredentials(true); // 允许证书（cookies）
        corsConfig.setMaxAge(3600L); // 预检请求的缓存时间（秒）

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig); // 对所有路径应用这个配置
        return source;
    }
}
