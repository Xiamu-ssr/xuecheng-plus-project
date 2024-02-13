package com.xuecheng.ucenter.service.impl;


import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserServiceImpl implements UserDetailsService {

    @Autowired
    @Qualifier("password_authservice")
    private AuthService passwordAuthService; // 这将注入PasswordAuthServiceImpl的实例

    @Autowired
    @Qualifier("wx_authservice")
    private AuthService wxAuthService; // 这将注入PasswordAuthServiceImpl的实例


    /**
     * 用户统一认证
     *
     * @param s 用户信息Json字符串
     * @return {@link UserDetails}
     * @throws UsernameNotFoundException 找不到用户名异常
     */
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        //加载s到dto
        AuthParamsDto authParamsDto = new AuthParamsDto();
        try {
//            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
            authParamsDto.setUsername(s);
            authParamsDto.setAuthType("password");
        } catch (Exception e) {
            throw new RuntimeException("用户信息异常,认证请求数据格式不对");
        }
        //统一认证接口
        XcUserExt xcUserExt = new XcUserExt();
        String authType = authParamsDto.getAuthType();
        if (authType.equals("password")){
            xcUserExt = passwordAuthService.execute(authParamsDto);
        }else if (authType.equals("wx")){
            xcUserExt = wxAuthService.execute(authParamsDto);
        }else {
            xcUserExt = passwordAuthService.execute(authParamsDto);
//            throw new RuntimeException("不支持的验证类型");
        }
        return getUserPrincipal(xcUserExt);
    }

    public UserDetails getUserPrincipal(XcUserExt user){
        //用户权限,如果不加报Cannot pass a null GrantedAuthority collection
        String[] authorities = {"read"};
        String password = user.getPassword();
        //为了安全在令牌中不放密码
        user.setPassword(null);
        //将user对象转json
        String userString = JSON.toJSONString(user);
        //创建UserDetails对象
        return User.withUsername(userString).password(password).authorities(authorities).build();
    }
}
