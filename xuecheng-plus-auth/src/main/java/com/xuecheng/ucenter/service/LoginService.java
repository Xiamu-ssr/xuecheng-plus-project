package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.po.XcUser;
import org.springframework.web.bind.annotation.RequestBody;

public interface LoginService {
    public void register(XcUser dto);
}
