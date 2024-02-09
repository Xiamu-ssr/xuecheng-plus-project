package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;

public interface AuthService {

    /**
     * 认证方法
     *
     * @param authParamsDto auth params dto
     * @return {@link XcUserExt}
     */
    XcUserExt execute(AuthParamsDto authParamsDto);
}
