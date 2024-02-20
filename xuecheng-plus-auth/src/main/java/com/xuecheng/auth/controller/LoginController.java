package com.xuecheng.auth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.mapper.XcUserRoleMapper;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.model.po.XcUserRole;
import com.xuecheng.ucenter.service.LoginService;
import com.xuecheng.ucenter.service.WxAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author mumu
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
    @Autowired
    LoginService loginService;


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
    @CrossOrigin("*")
    @GetMapping("/wxLogin")
    public String wxLogin(String code) throws IOException {
//        System.out.println("微信扫码回调");
//        System.out.println(code);
        if (StringUtils.isEmpty(code)){
            return "555";
        }
        log.info("微信扫码回调,code:{}", code);
//        System.out.println("微信扫码回调");
        return wxAuthService.wxAuth(code);
    }

    @CrossOrigin("*")
    @PostMapping("/register")
    public void register(@RequestBody XcUser dto){
        loginService.register(dto);
    }

}
