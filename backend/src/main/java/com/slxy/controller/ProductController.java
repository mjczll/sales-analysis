package com.slxy.controller;

import com.slxy.common.PageResult;
import com.slxy.common.Result;
import com.slxy.entity.Category;
import com.slxy.entity.Product;
import com.slxy.mapper.CategoryMapper;
import com.slxy.mapper.OrderItemMapper;
import com.slxy.mapper.StockRecordMapper;
import com.slxy.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品管理，对齐前端 api/product.js
 */
@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private StockRecordMapper stockRecordMapper;

    /**
     * 分页列表：前端传 page/size、name/code/categoryId/status，附带 categoryName
     */
    @GetMapping("/list")
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status) {
        PageResult<Product> productPage = productService.getProductList(page, size, name, code, categoryId, status);

        // 批量查询分类名，避免 N+1
        Map<Long, String> categoryNameMap = batchCategoryNames(productPage.getRecords());

        List<Map<String, Object>> records = new ArrayList<>();
        for (Product prod : productPage.getRecords()) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", prod.getId());
            m.put("code", prod.getCode());
            m.put("name", prod.getName());
            m.put("categoryId", prod.getCategoryId());
            m.put("categoryName", categoryNameMap.getOrDefault(prod.getCategoryId(), ""));
            m.put("spec", prod.getSpec());
            m.put("unit", prod.getUnit());
            m.put("price", prod.getPrice());
            m.put("costPrice", prod.getCostPrice());
            m.put("stock", prod.getStock());
            m.put("status", prod.getStatus());
            m.put("remark", prod.getRemark());
            m.put("createTime", prod.getCreateTime());
            m.put("updateTime", prod.getUpdateTime());
            records.add(m);
        }
        PageResult<Map<String, Object>> result = PageResult.of(records, productPage.getTotal(), page, size);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        Product prod = productService.getById(id);
        if (prod == null) {
            return Result.success(null);
        }
        Map<String, Object> m = new HashMap<>();
        m.put("id", prod.getId());
        m.put("code", prod.getCode());
        m.put("name", prod.getName());
        m.put("categoryId", prod.getCategoryId());
        m.put("categoryName", categoryName(prod.getCategoryId()));
        m.put("spec", prod.getSpec());
        m.put("unit", prod.getUnit());
        m.put("price", prod.getPrice());
        m.put("costPrice", prod.getCostPrice());
        m.put("stock", prod.getStock());
        m.put("status", prod.getStatus());
        m.put("remark", prod.getRemark());
        m.put("createTime", prod.getCreateTime());
        m.put("updateTime", prod.getUpdateTime());
        return Result.success(m);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> add(@RequestBody Product product) {
        if (product.getStatus() == null) {
            product.setStatus(1);
        }
        return Result.success(productService.save(product));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> update(@RequestBody Product product) {
        return Result.success(productService.updateById(product));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> delete(@PathVariable Long id) {
        // 删除前校验：商品已被订单明细或库存流水引用时禁止删除，避免悬空引用、保证账目可追溯
        long itemCount = orderItemMapper.countByProductId(id);
        if (itemCount > 0) {
            return Result.error("该商品已被 " + itemCount + " 条订单明细引用，无法删除，请改为下架（停用）");
        }
        long recordCount = stockRecordMapper.countByProductId(id);
        if (recordCount > 0) {
            return Result.error("该商品存在 " + recordCount + " 条库存流水，无法删除，请改为下架（停用）");
        }
        return Result.success(productService.removeById(id));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        productService.updateStatus(id, status);
        return Result.success(true);
    }

    /**
     * 商品分类下拉：返回分类对象列表（含 id/name）
     */
    @GetMapping("/category/list")
    public Result<List<Category>> getProductCategories() {
        return Result.success(categoryMapper.selectList(null));
    }

    /**
     * 批量查询分类名（一次查询，避免 N+1）
     */
    private Map<Long, String> batchCategoryNames(List<Product> products) {
        Map<Long, String> map = new HashMap<>();
        List<Long> ids = new ArrayList<>();
        for (Product p : products) {
            if (p.getCategoryId() != null) {
                ids.add(p.getCategoryId());
            }
        }
        if (ids.isEmpty()) {
            return map;
        }
        List<Category> categories = categoryMapper.selectByIds(ids);
        for (Category c : categories) {
            map.put(c.getId(), c.getName());
        }
        return map;
    }

    private String categoryName(Long categoryId) {
        if (categoryId == null) return "";
        Category c = categoryMapper.selectById(categoryId);
        return c != null ? c.getName() : "";
    }
}
