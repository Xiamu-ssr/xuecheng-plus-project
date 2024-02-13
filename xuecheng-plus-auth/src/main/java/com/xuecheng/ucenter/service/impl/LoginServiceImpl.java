package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.mapper.XcUserRoleMapper;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.model.po.XcUserRole;
import com.xuecheng.ucenter.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    XcUserMapper xcUserMapper;
    @Autowired
    XcUserRoleMapper xcUserRoleMapper;
    @Override
    @Transactional
    public void register(XcUser dto) {
        //验证username唯一性
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, dto.getUsername()));
        if (xcUser != null){
            throw new RuntimeException("存在相同账户名，请更换");
        }
        //插入用户表
        dto.setId(UUID.randomUUID().toString());
        dto.setWxUnionid("test");
        dto.setName(dto.getUsername());
        dto.setUtype("101001");
        dto.setStatus("1");
        dto.setPassword(passwordEncoder.encode(dto.getPassword()));
        int insert = xcUserMapper.insert(dto);
        if (insert <= 0){
            throw new RuntimeException("添加用户失败");
        }
        //插入用户-角色关联表
        XcUserRole xcUserRole = new XcUserRole();
        xcUserRole.setId(UUID.randomUUID().toString());
        xcUserRole.setUserId(dto.getId());
        xcUserRole.setRoleId("17");
        xcUserRole.setCreateTime(LocalDateTime.now());
        int insert1 = xcUserRoleMapper.insert(xcUserRole);
        if (insert1 <= 0){
            throw new RuntimeException("添加用户失败");
        }
    }
}
