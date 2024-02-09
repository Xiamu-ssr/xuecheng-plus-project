package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("password_authservice")
public class PasswordAuthServiceImpl implements AuthService {

    @Autowired
    XcUserMapper xcUserMapper;

    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        throw new RuntimeException("不支持密码登录");
//        if (StringUtils.isEmpty( authParamsDto.getUsername())) {
//            throw new RuntimeException("用户信息异常,无用户名");
//        }
//        //根据username查询数据库
//        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>()
//                .eq(XcUser::getUsername, authParamsDto.getUsername()));
//        //用户不存在，返回null
//        if (xcUser == null){
//            throw new RuntimeException("账号不存在");
//        }
//        XcUserExt xcUserExt = new XcUserExt();
//        BeanUtils.copyProperties(xcUser, xcUserExt);
//        return xcUserExt;
    }
}
