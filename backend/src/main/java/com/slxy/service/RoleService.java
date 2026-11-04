package com.slxy.service;

import com.slxy.common.PageResult;
import com.slxy.entity.Role;

import java.util.List;

public interface RoleService {

    PageResult<Role> getRoleList(int page, int size, String keyword);

    Role getById(Long id);

    boolean save(Role role);

    boolean updateById(Role role);

    boolean removeById(Long id);

    /**
     * 检查角色是否被用户使用
     */
    boolean checkRoleUsed(Long roleId);

    /**
     * 获取所有角色
     */
    List<Role> getAllActiveRoles();
}
