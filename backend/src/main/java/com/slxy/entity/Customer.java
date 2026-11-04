package com.slxy.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 客户表 customer
 */
@Data
public class Customer {
    private Long id;

    /** 客户名称 */
    private String name;

    /** 联系人 */
    private String contactPerson;

    /** 联系电话 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 地址 */
    private String address;

    /** 客户等级 */
    private String level;

    /** 客户来源 */
    private String source;

    /** 备注 */
    private String remark;

    /** 状态：1 正常 / 0 停用 */
    private Integer status;

    private LocalDateTime createTime;
}
