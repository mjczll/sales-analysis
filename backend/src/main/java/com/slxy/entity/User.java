package com.slxy.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户表 sys_user
 */
@Data
public class User {
    private Long id;

    /** 登录账号 */
    private String username;

    /** 密码（MD5 加密）——序列化时忽略，避免哈希泄露 */
    @JsonIgnore
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 头像地址 */
    private String avatar;

    /** 状态：1 启用 / 0 禁用 */
    private Integer status;

    /** 角色 ID（逻辑外键 → sys_role.id） */
    private Long roleId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
