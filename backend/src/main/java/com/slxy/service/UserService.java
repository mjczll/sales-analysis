package com.slxy.service;

import com.slxy.common.PageResult;
import com.slxy.entity.User;

import java.time.LocalDateTime;

public interface UserService {

    User getByUsername(String username);

    User getById(Long id);

    PageResult<User> getUserList(int page, int size, String username, String realName, Long roleId, Integer status);

    PageResult<User> getEmployeeList(int page, int size, String keyword, Long roleId, Integer status);

    boolean save(User user);

    boolean updateById(User user);

    boolean removeById(Long id);

    Long getIncrease(LocalDateTime since);
}
