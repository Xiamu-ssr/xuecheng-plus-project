package com.xuecheng.auth.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;


/**
 * @author Mr.M
 * @version 1.0
 * @description 安全管理配置
 * @date 2022/9/26 20:53
 */
//@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
@Configuration
@EnableWebSecurity
public class AuthServerSecurityConfig {
    // 配置用户信息服务
//    @Bean
//    public UserDetailsService userDetailsService() {
//        UserDetails userDetails = User
//                .withUsername("lisi")
//                .password("456")
//                .roles("read")
//                .build();
//        return new InMemoryUserDetailsManager(userDetails);
//    }

    // 密码编码器
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 密码为明文方式
//        return NoOpPasswordEncoder.getInstance();
        // 或使用 BCryptPasswordEncoder
         return new BCryptPasswordEncoder();
    }
    //oauth2 过滤链
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
            throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .oidc(Customizer.withDefaults());	// Enable OpenID Connect 1.0
        http
                // Redirect to the login page when not authenticated from the
                // authorization endpoint
                .exceptionHandling((exceptions) -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                )
                // Accept access tokens for User Info and/or Client Registration
                .oauth2ResourceServer((resourceServer) -> resourceServer
                        .jwt(Customizer.withDefaults()));

        return http.build();
    }

    //用于身份验证的 Spring Security 过滤器链
    @Bean
    @Order(2)
    public SecurityFilterChain defaultFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((authorize) ->
                                authorize
                                        .requestMatchers(new AntPathRequestMatcher("/actuator/**")).permitAll()
                                        .requestMatchers(new AntPathRequestMatcher("/login")).permitAll()
                                        .requestMatchers(new AntPathRequestMatcher("/logout")).permitAll()
                                        .requestMatchers(new AntPathRequestMatcher("/wxLogin")).permitAll()
                                        .requestMatchers(new AntPathRequestMatcher("/register")).permitAll()
                                        .requestMatchers(new AntPathRequestMatcher("/oauth2/**")).permitAll()
                                        .requestMatchers(new AntPathRequestMatcher("/**/*.html")).permitAll()
                                        .requestMatchers(new AntPathRequestMatcher("/**/*.json")).permitAll()
                                        .requestMatchers(new AntPathRequestMatcher("/auth/**")).permitAll()
                        .anyRequest().authenticated()
//                        .anyRequest().permitAll()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .addLogoutHandler(new SecurityContextLogoutHandler())
                        .logoutSuccessUrl("http://www.51xuecheng.cn")
                )
                .formLogin(Customizer.withDefaults())
                .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                        .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
        );

        return http.build();
        //                .formLogin(formLogin ->
//                        formLogin.successForwardUrl("/login-success") // 登录成功跳转到/login-success
//                );

        //                .authorizeRequests(authorizeRequests ->
//                        authorizeRequests
//                                .requestMatchers("/r/**").authenticated() // 访问/r开始的请求需要认证通过
//                                .anyRequest().permitAll() // 其它请求全部放行
//                )
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        // 此处可以添加自定义逻辑来提取JWT中的权限等信息
        // jwtConverter.setJwtGrantedAuthoritiesConverter(...);
        return jwtConverter;
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("XcWebApp")
//                .clientSecret("{noop}XcWebApp")
//                .clientSecret("XcWebApp")
                .clientSecret(passwordEncoder().encode("XcWebApp"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .redirectUri("http://www.51xuecheng.cn")
                .redirectUri("http://localhost:63070/auth/wxLogin")
                .redirectUri("http://www.51xuecheng.cn/sign.html")
//                .postLogoutRedirectUri("http://localhost:63070/login?logout")
                .scope("all")
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope("message.read")
                .scope("message.write")
                .scope("read")
                .scope("write")
                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofHours(2))  // 设置访问令牌的有效期
                        .refreshTokenTimeToLive(Duration.ofDays(3))  // 设置刷新令牌的有效期
                        .reuseRefreshTokens(true)                   // 是否重用刷新令牌
                        .build())
                .build();

        return new InMemoryRegisteredClientRepository(registeredClient);
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }
//    @Bean
//    public JWKSource<SecurityContext> jwkSource() {
//        try {
//            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//            // 从文件、环境变量或配置中读取公钥和私钥
//            String publicKeyContent ="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAo4gY3HP1z6eBUtxVVilNSHrNz1Nw5GvZbm63fJhDUl3F45GqBnuwBFr/Jpjf7IgvOnoUgovbSxKnOKvhAYS0z73ml7oOf0JPelfy2LFgf+Fc+4wtdKKpyhIuoUJNNz0F2tPLkxWuULp1qCfoaaqrC/GRnQwFEZTcqz3vrXGWU59NFAjK8YtL2QKGqYJUjtAGfM9OiTmosiclh4MZA5HURQ6YUQpdnhVtc3TlCienGupHxaHuaotAEYhyGZpODYpVJSwktwg/QbB1Q5RUCSe10RWnNQsNqJO8p9xcsl9Mwp3FMk+aUkXiSpPt4nNYhgXzVLtxo0JHK4OwukXZu64rewIDAQAB";
//            String privateKeyContent = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCjiBjcc/XPp4FS3FVWKU1Ies3PU3Dka9lubrd8mENSXcXjkaoGe7AEWv8mmN/siC86ehSCi9tLEqc4q+EBhLTPveaXug5/Qk96V/LYsWB/4Vz7jC10oqnKEi6hQk03PQXa08uTFa5QunWoJ+hpqqsL8ZGdDAURlNyrPe+tcZZTn00UCMrxi0vZAoapglSO0AZ8z06JOaiyJyWHgxkDkdRFDphRCl2eFW1zdOUKJ6ca6kfFoe5qi0ARiHIZmk4NilUlLCS3CD9BsHVDlFQJJ7XRFac1Cw2ok7yn3FyyX0zCncUyT5pSReJKk+3ic1iGBfNUu3GjQkcrg7C6Rdm7rit7AgMBAAECggEAFAq6AUq09Z4x21Xln4mwTxG954ryawxMuZwYIM0Icy/K8PkJPYIrMF12p+RUjUijgSc1HErHtYuzst5d1THOdydu+2lyHWajFvtwZ95VVVbpbmrfp0vIQ1u3G0xk6kAwV4Fdkck2c+5mPRWnBkxEalJQ5k5y0JTN9q7AkSE6q0TInaKYaDJe1ZG2lY1RYAbwolmBaB5KyopJekPKz2CvRyMatdgboZcNyaszFE1SBCvKp8RxAlwnKuTrzSFY22ZmCqkOz7yKCZADHKlQXYzShxber0J592awQUeP9s1pLuL3i3HmK3EMUs3oMdPBNYxhieqiQdrEZfFACSGT9xc8AQKBgQDUVEWuW9mK9/8azQy9amPN14rjQG+ld+i0B++7myIVXM5Xhhdb1olWwNfNg91lLXhtC5ij2JkBrInttFAuWGUMUI2yA1RTpKbno4LnmhoUtXG1/3Jxlv+wvRThtKlbD3i1gdEZTWKnEb5ez7jd4R/HLiQ2REiJ9n71eggpj0zfkQKBgQDFKn8YKT1CJ+FgaRV/Qj7QUU+EuwegA4QuCdyjK+UUVMvsXTfN9uNPykuaFxsjytZanP9Sgz90OcWyvJVoOcJOaAawIHj2o1iz+nHmmv/boPNP3diSEImac7WXmdeaGsoVWX/7fom9piZKvzlcxRp49DoH+hkoRq/jNPhtF5/sSwKBgD1JufdTMd8IKI2u5F+EZxySe9eO0Os9SmE07UEEzXjHGhRvcyyiJ3BwJ5p91pkO3/Tx5PReYAP4rrN7Wa2W/EvqsIvSpDOkkjzImM+LTr3thc4X1wvsnw9/9JgV0tCjDZ+uwhGAodpBp+asJNt+0PJoYjF70khoa0smF1cPswvRAoGAPzOwKf6ONHa0OEN3MKP7nqtx4gpSF2kJJfjjUSrw8+N6uvnmuY86rokaUvq1KHQM4l8ROVH5NTiPtwvcmNxq/Nc7zZmbLPSPqqHNgS6OdcjSNffXRHsooOoWe9JE2pFb1hwqemPFo5VvEObbbHGCWuNu9r+k8NQ37Y09VTsNeKECgYA8cWxwmLGEEJFnvOd4vjVVUe5leugGr6lENwUU9d0TiiUrWCN7op+7d5HadI4VTaYVkoAJibDcFvnDzE+dAV4MLAITObLMzxITQvCh6XpQrvU3t8QSpEYc/RLdxggr6YoNiCuH0dorgowEuZzgMCADbY5ojeCZzV+Mam+DMSRmkg=="; // 私钥内容
//
//            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.getMimeDecoder().decode(publicKeyContent));
//            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(Base64.getMimeDecoder().decode(privateKeyContent));
//
//            RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);
//            RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(privateKeySpec);
//
//            RSAKey rsaKey = new RSAKey.Builder(publicKey)
//                    .privateKey(privateKey)
//                    .keyID(UUID.randomUUID().toString()) // 可选：为密钥指定一个ID
//                    .build();
//
//            return new ImmutableJWKSet<>(new JWKSet(rsaKey));
//        } catch (Exception e) {
//            throw new IllegalStateException(e);
//        }
//    }

    private static KeyPair generateRsaKey() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        }
        catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        return keyPair;
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }

}