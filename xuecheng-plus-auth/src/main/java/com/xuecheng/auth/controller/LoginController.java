package com.xuecheng.auth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    XcUserMapper xcUserMapper;


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

    @PostMapping("/register")
    @CrossOrigin("*")
    public void register(@RequestBody XcUser dto){
        //验证username唯一性
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, dto.getUsername()));
        if (xcUser != null){
            throw new RuntimeException("存在相同账户名，请更换");
        }

        dto.setId(UUID.randomUUID().toString());
        dto.setWxUnionid("test");
        dto.setName(dto.getUsername());
        dto.setUtype("101001");
        dto.setStatus("1");
        dto.setPassword(passwordEncoder.encode(dto.getPassword()));
        System.out.println(dto);
        int insert = xcUserMapper.insert(dto);
        if (insert > 0){
            return;
        }else {
            throw new RuntimeException("添加用户失败");
        }
    }

}
