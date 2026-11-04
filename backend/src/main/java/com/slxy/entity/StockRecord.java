package com.slxy.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 库存流水表 stock_record
 */
@Data
public class StockRecord {
    private Long id;

    /** 商品 ID（逻辑外键 → product.id） */
    private Long productId;

    /** 类型：1 入库 / 2 出库 */
    private Integer type;

    /** 变动数量（正数） */
    private Integer quantity;

    /** 备注（如「销售出库：SOxxx」） */
    private String remark;

    /** 操作人 ID（逻辑外键 → sys_user.id） */
    private Long userId;

    private LocalDateTime createTime;
}
