package com.xuecheng.xuechengplusgateway.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;


@Configuration
public class JwtDecoderConfig {

//    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
//    String issuerUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    String jwkSetUri;
//
//    @Bean
//    public JwtDecoder jwtDecoderLocal() throws NoSuchAlgorithmException, InvalidKeySpecException {
//        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//        // 从文件、环境变量或配置中读取公钥和私钥
//        String publicKeyContent ="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAo4gY3HP1z6eBUtxVVilNSHrNz1Nw5GvZbm63fJhDUl3F45GqBnuwBFr/Jpjf7IgvOnoUgovbSxKnOKvhAYS0z73ml7oOf0JPelfy2LFgf+Fc+4wtdKKpyhIuoUJNNz0F2tPLkxWuULp1qCfoaaqrC/GRnQwFEZTcqz3vrXGWU59NFAjK8YtL2QKGqYJUjtAGfM9OiTmosiclh4MZA5HURQ6YUQpdnhVtc3TlCienGupHxaHuaotAEYhyGZpODYpVJSwktwg/QbB1Q5RUCSe10RWnNQsNqJO8p9xcsl9Mwp3FMk+aUkXiSpPt4nNYhgXzVLtxo0JHK4OwukXZu64rewIDAQAB";
////        String privateKeyContent = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCjiBjcc/XPp4FS3FVWKU1Ies3PU3Dka9lubrd8mENSXcXjkaoGe7AEWv8mmN/siC86ehSCi9tLEqc4q+EBhLTPveaXug5/Qk96V/LYsWB/4Vz7jC10oqnKEi6hQk03PQXa08uTFa5QunWoJ+hpqqsL8ZGdDAURlNyrPe+tcZZTn00UCMrxi0vZAoapglSO0AZ8z06JOaiyJyWHgxkDkdRFDphRCl2eFW1zdOUKJ6ca6kfFoe5qi0ARiHIZmk4NilUlLCS3CD9BsHVDlFQJJ7XRFac1Cw2ok7yn3FyyX0zCncUyT5pSReJKk+3ic1iGBfNUu3GjQkcrg7C6Rdm7rit7AgMBAAECggEAFAq6AUq09Z4x21Xln4mwTxG954ryawxMuZwYIM0Icy/K8PkJPYIrMF12p+RUjUijgSc1HErHtYuzst5d1THOdydu+2lyHWajFvtwZ95VVVbpbmrfp0vIQ1u3G0xk6kAwV4Fdkck2c+5mPRWnBkxEalJQ5k5y0JTN9q7AkSE6q0TInaKYaDJe1ZG2lY1RYAbwolmBaB5KyopJekPKz2CvRyMatdgboZcNyaszFE1SBCvKp8RxAlwnKuTrzSFY22ZmCqkOz7yKCZADHKlQXYzShxber0J592awQUeP9s1pLuL3i3HmK3EMUs3oMdPBNYxhieqiQdrEZfFACSGT9xc8AQKBgQDUVEWuW9mK9/8azQy9amPN14rjQG+ld+i0B++7myIVXM5Xhhdb1olWwNfNg91lLXhtC5ij2JkBrInttFAuWGUMUI2yA1RTpKbno4LnmhoUtXG1/3Jxlv+wvRThtKlbD3i1gdEZTWKnEb5ez7jd4R/HLiQ2REiJ9n71eggpj0zfkQKBgQDFKn8YKT1CJ+FgaRV/Qj7QUU+EuwegA4QuCdyjK+UUVMvsXTfN9uNPykuaFxsjytZanP9Sgz90OcWyvJVoOcJOaAawIHj2o1iz+nHmmv/boPNP3diSEImac7WXmdeaGsoVWX/7fom9piZKvzlcxRp49DoH+hkoRq/jNPhtF5/sSwKBgD1JufdTMd8IKI2u5F+EZxySe9eO0Os9SmE07UEEzXjHGhRvcyyiJ3BwJ5p91pkO3/Tx5PReYAP4rrN7Wa2W/EvqsIvSpDOkkjzImM+LTr3thc4X1wvsnw9/9JgV0tCjDZ+uwhGAodpBp+asJNt+0PJoYjF70khoa0smF1cPswvRAoGAPzOwKf6ONHa0OEN3MKP7nqtx4gpSF2kJJfjjUSrw8+N6uvnmuY86rokaUvq1KHQM4l8ROVH5NTiPtwvcmNxq/Nc7zZmbLPSPqqHNgS6OdcjSNffXRHsooOoWe9JE2pFb1hwqemPFo5VvEObbbHGCWuNu9r+k8NQ37Y09VTsNeKECgYA8cWxwmLGEEJFnvOd4vjVVUe5leugGr6lENwUU9d0TiiUrWCN7op+7d5HadI4VTaYVkoAJibDcFvnDzE+dAV4MLAITObLMzxITQvCh6XpQrvU3t8QSpEYc/RLdxggr6YoNiCuH0dorgowEuZzgMCADbY5ojeCZzV+Mam+DMSRmkg=="; // 私钥内容
//        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.getMimeDecoder().decode(publicKeyContent));
//        RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);
//        return NimbusJwtDecoder.withPublicKey(publicKey).build();
//    }
    @Bean
    public JwtDecoder jwtDecoderLocal() {
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }
}
