package com.slxy.service.impl;

import com.slxy.common.PageResult;
import com.slxy.entity.Role;
import com.slxy.mapper.RoleMapper;
import com.slxy.mapper.UserMapper;
import com.slxy.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public PageResult<Role> getRoleList(int page, int size, String keyword) {
        int offset = (page - 1) * size;
        List<Role> records = roleMapper.selectPage(keyword, offset, size);
        long total = roleMapper.countPage(keyword);
        return PageResult.of(records, total, page, size);
    }

    @Override
    public Role getById(Long id) {
        return roleMapper.selectById(id);
    }

    @Override
    public boolean save(Role role) {
        return roleMapper.insert(role) > 0;
    }

    @Override
    public boolean updateById(Role role) {
        return roleMapper.updateById(role) > 0;
    }

    @Override
    public boolean removeById(Long id) {
        return roleMapper.deleteById(id) > 0;
    }

    @Override
    public boolean checkRoleUsed(Long roleId) {
        // 查询 sys_user 表中是否存在该角色的用户
        return userMapper.countByRoleId(roleId) > 0;
    }

    @Override
    public List<Role> getAllActiveRoles() {
        return roleMapper.selectAll();
    }
}
