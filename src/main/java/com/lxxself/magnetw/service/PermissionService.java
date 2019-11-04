package com.lxxself.magnetw.service;

import com.lxxself.magnetw.config.ApplicationConfig;
import com.lxxself.magnetw.handler.PermissionHandler;
import com.lxxself.magnetw.response.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

@Service
public class PermissionService {
    @Autowired
    ApplicationConfig config;

    public BaseResponse runAsPermission(String password, String message, PermissionHandler handler) throws Exception {
        if (StringUtils.isEmpty(config.adminPasswordMD5)) {
            return BaseResponse.error("没有设置管理密码");
        } else {
            if (config.adminPasswordMD5.equals(DigestUtils.md5DigestAsHex(password.getBytes()))) {
                return BaseResponse.success(handler == null ? null : handler.onPermissionGranted(), message);
            } else {
                return BaseResponse.error("没有权限");
            }
        }
    }

}
