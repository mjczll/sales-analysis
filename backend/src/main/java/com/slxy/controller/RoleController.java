package com.slxy.controller;

import com.slxy.common.PageResult;
import com.slxy.common.Result;
import com.slxy.entity.Role;
import com.slxy.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping("/list")
    public Result<PageResult<Role>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        try {
            PageResult<Role> rolePage = roleService.getRoleList(page, size, keyword);
            return Result.success(rolePage);
        } catch (Exception e) {
            log.error("获取角色列表失败", e);
            return Result.error("获取角色列表失败：" + e.getMessage());
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> add(@RequestBody Role role) {
        try {
            return Result.success(roleService.save(role));
        } catch (Exception e) {
            log.error("添加角色失败", e);
            return Result.error("添加角色失败：" + e.getMessage());
        }
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> update(@RequestBody Role role) {
        try {
            return Result.success(roleService.updateById(role));
        } catch (Exception e) {
            log.error("更新角色失败", e);
            return Result.error("更新角色失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> delete(@PathVariable Long id) {
        try {
            // 检查角色是否被使用
            if (roleService.checkRoleUsed(id)) {
                return Result.error("该角色正在被使用，无法删除");
            }
            return Result.success(roleService.removeById(id));
        } catch (Exception e) {
            log.error("删除角色失败", e);
            return Result.error("删除角色失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有角色
     */
    @GetMapping("/all")
    public Result<List<Role>> getAllActiveRoles() {
        try {
            List<Role> roles = roleService.getAllActiveRoles();
            return Result.success(roles);
        } catch (Exception e) {
            log.error("获取角色列表失败", e);
            return Result.error("获取角色列表失败：" + e.getMessage());
        }
    }
}
