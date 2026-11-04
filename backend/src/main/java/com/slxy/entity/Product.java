package com.slxy.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品表 product
 */
@Data
public class Product {
    private Long id;

    /** 商品编码 */
    private String code;

    /** 商品名称 */
    private String name;

    /** 分类 ID（逻辑外键 → category.id） */
    private Long categoryId;

    /** 规格型号 */
    private String spec;

    /** 单位 */
    private String unit;

    /** 销售单价 */
    private BigDecimal price;

    /** 成本价 */
    private BigDecimal costPrice;

    /** 当前库存 */
    private Integer stock;

    /** 状态：1 上架 / 0 下架 */
    private Integer status;

    /** 备注 */
    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
