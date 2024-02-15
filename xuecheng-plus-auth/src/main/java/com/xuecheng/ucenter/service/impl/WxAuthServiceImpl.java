package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson2.JSON;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import com.xuecheng.ucenter.service.WxAuthService;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service("wx_authservice")
public class WxAuthServiceImpl implements AuthService, WxAuthService {
    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        return null;
    }

    @Override
    public String wxAuth(String code) {
        Map<String, String> JWTMap = getAccess_token(code);
        String string = JSON.toJSONString(JWTMap);
        return string;
//        String accessToken = JWTMap.get("access_token");
//        return accessToken;
    }


    private Map<String,String> getAccess_token(String code){
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "grant_type=authorization_code&code="+code+"&redirect_uri=http://www.51xuecheng.cn/sign.html");
        Request request = new Request.Builder()
                .url("http://localhost:63070/auth/oauth2/token")
                .method("POST", body)
//                .addHeader("User-Agent", "Apifox/1.0.0 (https://apifox.com)")
                .addHeader("Authorization", "Basic WGNXZWJBcHA6WGNXZWJBcHA=")
                .addHeader("Accept", "*/*")
                .addHeader("Host", "localhost:63070")
                .addHeader("Connection", "keep-alive")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        try {
            Response response = client.newCall(request).execute();
            String result = response.body().string();
            Map map = JSON.parseObject(result, Map.class);
            return map;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
