package com.slxy.entity;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 订单明细表 order_item
 */
@Data
public class OrderItem {
    private Long id;

    /** 订单 ID（逻辑外键 → sales_order.id） */
    private Long orderId;

    /** 商品 ID（逻辑外键 → product.id） */
    private Long productId;

    /** 商品名称（快照，防改名影响历史订单） */
    private String productName;

    /** 成交单价（快照） */
    private BigDecimal price;

    /** 数量 */
    private Integer quantity;

    /** 小计金额 = price × quantity */
    private BigDecimal amount;
}
