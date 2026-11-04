package com.slxy.controller;

import com.slxy.common.Result;
import com.slxy.entity.Role;
import com.slxy.entity.User;
import com.slxy.service.FileService;
import com.slxy.service.RoleService;
import com.slxy.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private FileService fileService;

    @Autowired
    private RoleService roleService;

    /**
     * 修改用户密码（MD5 校验）
     */
    @PutMapping("/password")
    public Result<Boolean> updatePassword(@RequestBody Map<String, String> params) {
        try {
            Long userId = Long.valueOf(params.get("id"));
            String oldPassword = params.get("oldPassword");
            String newPassword = params.get("newPassword");

            if (userId == null || oldPassword == null || newPassword == null) {
                return Result.error("参数不完整");
            }

            User user = userService.getById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }

            // 验证旧密码（MD5 比对）
            String md5Old = DigestUtils.md5DigestAsHex(oldPassword.getBytes(StandardCharsets.UTF_8));
            if (!md5Old.equalsIgnoreCase(user.getPassword())) {
                return Result.error("原密码错误");
            }

            // 更新密码（MD5 入库）
            user.setPassword(DigestUtils.md5DigestAsHex(newPassword.getBytes(StandardCharsets.UTF_8)));
            boolean result = userService.updateById(user);

            return result ? Result.success(true) : Result.error("密码更新失败");
        } catch (Exception e) {
            log.error("修改密码失败", e);
            return Result.error("操作失败，请稍后重试");
        }
    }

    /**
     * 上传用户头像：返回 { url }（对齐前端 uploadAvatar 期望结构）
     */
    @PostMapping("/avatar")
    public Result<Map<String, Object>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("请选择要上传的头像文件");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("请上传图片类型的文件");
        }

        try {
            String avatarUrl = fileService.uploadFile(file);

            // 尝试从 SecurityContext 获取当前用户并更新头像
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && authentication.getPrincipal() instanceof User currentUser) {
                User user = userService.getByUsername(currentUser.getUsername());
                if (user != null) {
                    user.setAvatar(avatarUrl);
                    userService.updateById(user);
                }
            }

            Map<String, Object> data = new HashMap<>();
            data.put("url", avatarUrl);
            return Result.success(data);
        } catch (Exception e) {
            log.error("头像上传失败", e);
            return Result.error("头像上传失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/info/{username}")
    public Result<Map<String, Object>> getUserInfo(@PathVariable String username) {
        try {
            User user = userService.getByUsername(username);
            if (user == null) {
                return Result.error("用户不存在");
            }

            Map<String, Object> data = new HashMap<>();
            data.put("id", user.getId());
            data.put("username", user.getUsername());
            data.put("realName", user.getRealName());
            data.put("phone", user.getPhone());
            data.put("email", user.getEmail());
            data.put("avatar", user.getAvatar());
            data.put("roleId", user.getRoleId());
            data.put("status", user.getStatus());

            // 补充角色名称
            if (user.getRoleId() != null) {
                Role role = roleService.getById(user.getRoleId());
                data.put("roleName", role != null ? role.getRoleName() : "");
            }

            return Result.success(data);
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return Result.error("获取用户信息失败：" + e.getMessage());
        }
    }

    /**
     * 更新用户资料
     */
    @PutMapping("/profile")
    public Result<Boolean> updateProfile(@RequestBody User user) {
        try {
            return Result.success(userService.updateById(user));
        } catch (Exception e) {
            log.error("更新用户资料失败", e);
            return Result.error("更新用户资料失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户权限 / 菜单（当前简化模型：返回通配权限 + 空菜单，对齐前端 getUserPermissions）
     */
    @GetMapping("/permissions/{username}")
    public Result<Map<String, Object>> getUserPermissions(@PathVariable String username) {
        Map<String, Object> data = new HashMap<>();
        data.put("permissions", Collections.singletonList("*"));
        data.put("menus", Collections.emptyList());
        return Result.success(data);
    }
}
