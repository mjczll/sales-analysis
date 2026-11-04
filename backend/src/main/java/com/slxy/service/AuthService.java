package com.slxy.service;

import com.slxy.dto.RegisterDTO;

public interface AuthService {
    /**
     * 登录：校验用户名 + MD5 密码，返回 JWT token
     */
    String login(String username, String password);

    /**
     * 注册：默认角色为销售员（roleId=2）
     */
    boolean register(RegisterDTO registerDTO);
}
