package com.slxy.service.impl;

import com.slxy.common.PageResult;
import com.slxy.entity.User;
import com.slxy.mapper.UserMapper;
import com.slxy.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User getByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public PageResult<User> getUserList(int page, int size, String username, String realName, Long roleId, Integer status) {
        int offset = (page - 1) * size;
        List<User> records = userMapper.selectPage(username, realName, roleId, status, offset, size);
        long total = userMapper.countPage(username, realName, roleId, status);
        return PageResult.of(records, total, page, size);
    }

    @Override
    public PageResult<User> getEmployeeList(int page, int size, String keyword, Long roleId, Integer status) {
        int offset = (page - 1) * size;
        List<User> records = userMapper.selectEmployeePage(keyword, roleId, status, offset, size);
        long total = userMapper.countEmployee(keyword, roleId, status);
        return PageResult.of(records, total, page, size);
    }

    @Override
    public boolean save(User user) {
        return userMapper.insert(user) > 0;
    }

    @Override
    public boolean updateById(User user) {
        return userMapper.updateById(user) > 0;
    }

    @Override
    public boolean removeById(Long id) {
        return userMapper.deleteById(id) > 0;
    }

    @Override
    public Long getIncrease(LocalDateTime since) {
        return userMapper.countSince(since);
    }

    /**
     * 标准 32 位小写 MD5
     */
    public static String md5(String raw) {
        if (raw == null) {
            return "";
        }
        return DigestUtils.md5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8));
    }
}
