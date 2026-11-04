package com.slxy.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 角色表 sys_role
 */
@Data
public class Role {
    private Long id;

    /** 角色名称 */
    private String roleName;

    /** 角色编码（如 ADMIN / SALES / STOCK） */
    private String roleCode;

    /** 描述 */
    private String description;

    private LocalDateTime createTime;
}
