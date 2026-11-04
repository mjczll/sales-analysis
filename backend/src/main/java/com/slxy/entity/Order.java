package com.slxy.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

/**
 * 销售订单表 sales_order
 */
@Data
public class Order {
    private Long id;

    /** 订单编号（SO + 时间戳 + 随机数） */
    private String orderNo;

    /** 客户 ID（逻辑外键 → customer.id） */
    private Long customerId;

    /** 下单人 / 销售员 ID（逻辑外键 → sys_user.id） */
    private Long userId;

    /** 订单总金额（冗余，下单时汇总） */
    private BigDecimal totalAmount;

    /** 备注 */
    private String remark;

    /** 状态：0 待审核 / 1 已审核 / 2 已发货 / 3 已完成 / 4 已取消 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /** 关联的订单明细列表，不存储在数据库中 */
    private List<OrderItem> items;
}
