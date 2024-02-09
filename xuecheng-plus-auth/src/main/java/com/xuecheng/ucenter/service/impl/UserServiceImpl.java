package com.xuecheng.ucenter.service.impl;


import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.po.XcUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserServiceImpl implements UserDetailsService {
    @Autowired
    XcUserMapper xcUserMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //根据username查询数据库
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>()
                .eq(XcUser::getUsername, username));
        //用户不存在，返回null
        if (xcUser == null){
            return null;
        }
        //用户存在，拿到密码，封装成UserDetails,密码对比由框架进行
        String password = xcUser.getPassword();
        //扩展用户信息
        xcUser.setPassword(null);
        String userInfo = JSON.toJSONString(xcUser);
        UserDetails userDetails = User.withUsername(userInfo).password(password).authorities("read").build();
        return userDetails;
    }
}
