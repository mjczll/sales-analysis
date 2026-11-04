package com.slxy.filter;

import com.slxy.entity.User;
import com.slxy.service.UserService;
import com.slxy.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.lang.NonNull;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private UserService userService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
            throws ServletException, IOException {
        String token = getTokenFromRequest(request);
        
        if (StringUtils.hasText(token) && JwtUtils.validateToken(token)) {
            Long userId = JwtUtils.getUserIdFromToken(token);
            User user = userService.getById(userId);
            
            if (user != null && user.getStatus() == 1) {
                // 根据 roleId 生成角色权限，供 @PreAuthorize("hasRole(...)") 使用
                List<SimpleGrantedAuthority> authorities = buildAuthorities(user);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    user, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        
        chain.doFilter(request, response);
    }

    /**
     * 根据用户角色 ID 构造 Spring Security 权限列表。
     * roleId: 1=ADMIN / 2=SALES / 3=STOCK
     */
    private List<SimpleGrantedAuthority> buildAuthorities(User user) {
        String roleCode = roleCodeOf(user.getRoleId());
        return List.of(new SimpleGrantedAuthority("ROLE_" + roleCode));
    }

    private String roleCodeOf(Long roleId) {
        if (roleId == null) {
            return "SALES";
        }
        switch (roleId.intValue()) {
            case 1: return "ADMIN";
            case 3: return "STOCK";
            case 2:
            default: return "SALES";
        }
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}