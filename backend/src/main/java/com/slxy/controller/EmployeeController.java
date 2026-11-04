package com.slxy.controller;

import com.slxy.common.PageResult;
import com.slxy.common.Result;
import com.slxy.entity.User;
import com.slxy.mapper.OrderMapper;
import com.slxy.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

/**
 * 员工管理（对应 sys_user 表），对齐前端 api/employee.js
 */
@Slf4j
@RestController
@RequestMapping("/api/employee")
public class EmployeeController {

    @Autowired
    private UserService userService;

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 分页列表：前端传 page/size、keyword（模糊匹配 username/realName/phone）、roleId、status
     */
    @GetMapping("/list")
    public Result<PageResult<User>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) Integer status) {
        try {
            PageResult<User> result = userService.getEmployeeList(page, size, keyword, roleId, status);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取员工列表失败", e);
            return Result.error("获取员工列表失败：" + e.getMessage());
        }
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> update(@RequestBody User user) {
        try {
            // 更新时不重置密码（前端未传 password 字段时保留原密码）
            if (!StringUtils.hasText(user.getPassword())) {
                user.setPassword(null);
            } else {
                user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes(StandardCharsets.UTF_8)));
            }
            return Result.success(userService.updateById(user));
        } catch (Exception e) {
            log.error("更新员工失败", e);
            return Result.error("更新员工失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> delete(@PathVariable Long id,
                                  @RequestParam(required = false) String password) {
        try {
            // 获取当前登录用户
            User currentUser = getCurrentUser();

            // 判断是否删除自己：删除自己需校验密码
            if (currentUser != null && currentUser.getId().equals(id)) {
                if (!StringUtils.hasText(password)) {
                    return Result.error("删除自己的账号需要输入密码确认");
                }
                String md5 = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
                if (!md5.equalsIgnoreCase(currentUser.getPassword())) {
                    return Result.error("密码错误");
                }
            }

            // 引用校验：员工名下还有订单时禁止删除，避免订单销售人员信息悬空
            long orderCount = orderMapper.countByUserId(id);
            if (orderCount > 0) {
                return Result.error("该员工名下还有 " + orderCount + " 个订单，无法删除，请先处理订单或改为停用");
            }

            return Result.success(userService.removeById(id));
        } catch (Exception e) {
            log.error("删除员工失败", e);
            return Result.error("删除员工失败：" + e.getMessage());
        }
    }

    /**
     * 从 SecurityContext 获取当前登录用户
     */
    private User getCurrentUser() {
        try {
            org.springframework.security.core.Authentication auth =
                    org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()
                    && auth.getPrincipal() instanceof User u) {
                return u;
            }
        } catch (Exception e) {
            log.warn("获取当前登录用户失败", e);
        }
        return null;
    }
}
