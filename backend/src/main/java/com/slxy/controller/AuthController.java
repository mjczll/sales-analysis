package com.slxy.controller;

import com.slxy.common.Result;
import com.slxy.dto.LoginDTO;
import com.slxy.dto.RegisterDTO;
import com.slxy.entity.User;
import com.slxy.service.AuthService;
import com.slxy.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    /**
     * 登录：返回 { token, user }，与前端 login() 期望结构一致
     * 密码采用 MD5 校验（数据库存的是标准 32 位小写 MD5）
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO loginDTO) {
        try {
            log.info("Login request received: {}", loginDTO.getUsername());

            // 生成 JWT token（内部完成 MD5 密码校验）
            String token = authService.login(loginDTO.getUsername(), loginDTO.getPassword());

            // 查询用户信息（脱敏：不返回密码）
            User user = userService.getByUsername(loginDTO.getUsername());
            Map<String, Object> userMap = buildUserMap(user);

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", userMap);
            return Result.success(data);
        } catch (Exception e) {
            log.error("Login failed", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 注册：默认注册为销售员（roleId=2），返回 { token, user }
     */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody RegisterDTO registerDTO) {
        try {
            log.info("Register request received: {}", registerDTO.getUsername());

            authService.register(registerDTO);

            // 注册成功后自动登录：生成 token + 返回用户信息
            String token = authService.login(registerDTO.getUsername(), registerDTO.getPassword());
            User user = userService.getByUsername(registerDTO.getUsername());

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", buildUserMap(user));
            return Result.success(data);
        } catch (Exception e) {
            log.error("Register failed", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户信息脱敏，避免把 password 返回给前端
     */
    private Map<String, Object> buildUserMap(User user) {
        Map<String, Object> map = new HashMap<>();
        if (user == null) {
            return map;
        }
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("realName", user.getRealName());
        map.put("phone", user.getPhone());
        map.put("email", user.getEmail());
        map.put("avatar", user.getAvatar());
        map.put("roleId", user.getRoleId());
        map.put("status", user.getStatus());
        map.put("createTime", user.getCreateTime());
        return map;
    }
}
