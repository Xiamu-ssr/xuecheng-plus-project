package com.xuecheng.content.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;


@Configuration
public class JwtDecoderConfig {
    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    String jwkSetUri;
    @Bean
    public JwtDecoder jwtDecoderLocal() {
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }
}
