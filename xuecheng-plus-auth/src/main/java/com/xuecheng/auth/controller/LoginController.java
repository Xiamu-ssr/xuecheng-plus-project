package com.xuecheng.auth.controller;

import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.WxAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mr.M
 * @version 1.0
 * @description 测试controller
 * @date 2022/9/27 17:25
 */
@Slf4j
@RestController
public class LoginController {

    @Autowired
    XcUserMapper userMapper;
    @Autowired
    WxAuthService wxAuthService;


    @RequestMapping("/login-success")
    public String loginSuccess() {

        return "登录成功";
    }


    @RequestMapping("/user/{id}")
    public XcUser getuser(@PathVariable("id") String id) {
        XcUser xcUser = userMapper.selectById(id);
        return xcUser;
    }

    /**
     * 微信登录
     * 目前为Spring Authorization Server登录
     *
     * @param code 授权码
     * @return {@link String}
     * @throws IOException IOException
     */
    @GetMapping("/wxLogin")
    @CrossOrigin("*")
    public String wxLogin(String code) throws IOException {
        System.out.println("微信扫码回调");
        System.out.println(code);
        if (StringUtils.isEmpty(code)){
            return "555";
        }
        log.debug("微信扫码回调,code:{}", code);
        System.out.println("微信扫码回调");
        return wxAuthService.wxAuth(code);
    }

    @GetMapping("/custom-logout")
    @CrossOrigin("*")
    public void logoutPage(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        // 重定向到登录页面或者其他页面
//        return "redirect:http://www.51xuecheng.cn/";
    }
}
