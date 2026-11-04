package com.slxy.service.impl;

import com.slxy.dto.RegisterDTO;
import com.slxy.entity.User;
import com.slxy.service.AuthService;
import com.slxy.service.UserService;
import com.slxy.utils.JwtUtils;
import com.slxy.utils.ValidatorUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserService userService;

    @Override
    public String login(String username, String password) {
        User user = userService.getByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查用户状态
        if (user.getStatus() != null && user.getStatus() != 1) {
            throw new RuntimeException("用户账号已禁用");
        }

        // 密码校验：数据库存标准 32 位小写 MD5，这里对输入密码做 MD5 后比对
        String md5Input = md5(password);
        if (!md5Input.equalsIgnoreCase(user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 生成 JWT token，包含用户 ID、用户名和角色 ID
        String token = JwtUtils.generateToken(user.getId(), user.getUsername(), String.valueOf(user.getRoleId()));
        log.info("Login successful for user: {}, token generated", username);
        return token;
    }

    @Override
    public boolean register(RegisterDTO registerDTO) {
        log.info("尝试注册用户: {}", registerDTO.getUsername());

        // 检查用户名是否已存在
        User existingUser = userService.getByUsername(registerDTO.getUsername());
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 手机号/邮箱格式校验（后端兜底，防止绕过前端入库脏数据）
        if (!ValidatorUtils.isPhoneValid(registerDTO.getPhone())) {
            throw new RuntimeException("手机号格式不正确");
        }
        if (!ValidatorUtils.isEmailValid(registerDTO.getEmail())) {
            throw new RuntimeException("邮箱格式不正确");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        // 密码 MD5 加密后入库
        user.setPassword(md5(registerDTO.getPassword()));
        user.setRealName(StringUtils.isNotBlank(registerDTO.getRealName())
                ? registerDTO.getRealName() : registerDTO.getUsername());
        user.setPhone(registerDTO.getPhone());
        user.setEmail(registerDTO.getEmail());
        user.setAvatar(registerDTO.getAvatar() != null ? registerDTO.getAvatar() : "");
        // 角色 ID：注册用户一律为销售员（roleId=2），不信任前端传入的 roleId
        user.setRoleId(2L);
        // 用户状态默认启用（1）
        user.setStatus(1);

        boolean saved = userService.save(user);
        log.info("用户注册成功: {}, 保存结果: {}, 角色ID: {}, 状态: {}",
                registerDTO.getUsername(), saved, user.getRoleId(), user.getStatus());
        return saved;
    }

    /**
     * 标准 32 位小写 MD5
     */
    private String md5(String raw) {
        if (raw == null) {
            return "";
        }
        return DigestUtils.md5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8));
    }
}
