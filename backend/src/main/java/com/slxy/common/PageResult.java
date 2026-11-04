package com.slxy.common;

import lombok.Data;

import java.util.List;

/**
 * 通用分页结果封装（替代 MyBatis-Plus 的 IPage / Page）
 * 前端约定字段：records / total / page / size
 */
@Data
public class PageResult<T> {
    /** 当前页数据 */
    private List<T> records;
    /** 总记录数 */
    private long total;
    /** 当前页码（从 1 开始） */
    private long page;
    /** 每页大小 */
    private long size;

    public PageResult() {
    }

    public PageResult(List<T> records, long total, long page, long size) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.size = size;
    }

    public static <T> PageResult<T> of(List<T> records, long total, long page, long size) {
        return new PageResult<>(records, total, page, size);
    }

    public static <T> PageResult<T> empty(long page, long size) {
        return new PageResult<>(List.of(), 0, page, size);
    }
}
