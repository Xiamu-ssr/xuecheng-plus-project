package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.po.XcUser;

import java.util.Map;

public interface WxAuthService {

    /**
     * 微信身份验证
     *
     * @param code 授权码
     * @return {@link XcUser}
     */
    public String wxAuth(String code);
}
