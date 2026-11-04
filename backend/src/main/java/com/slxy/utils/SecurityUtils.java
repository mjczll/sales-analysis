package com.slxy.utils;

import com.slxy.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全上下文工具类：从 SecurityContext 获取当前登录用户。
 */
public class SecurityUtils {

    /**
     * 获取当前登录用户，未登录时返回 null。
     */
    public static User getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User u) {
                return u;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * 获取当前登录用户 ID，未登录时返回 null。
     */
    public static Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }
}
