package com.slxy.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 商品分类表 category
 */
@Data
public class Category {
    private Long id;

    /** 分类名称 */
    private String name;

    /** 描述 */
    private String description;

    private LocalDateTime createTime;
}
