package com.slxy.controller;

import com.slxy.common.PageResult;
import com.slxy.common.Result;
import com.slxy.entity.Category;
import com.slxy.mapper.CategoryMapper;
import com.slxy.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类管理，对齐前端 api/category.js
 */
@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ProductMapper productMapper;

    /**
     * 分页列表：前端传 page/size（与 mockPage 约定一致）
     */
    @GetMapping("/list")
    public Result<PageResult<Category>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name) {
        int offset = (page - 1) * size;
        List<Category> records = categoryMapper.selectPage(name, offset, size);
        long total = categoryMapper.countPage(name);
        return Result.success(PageResult.of(records, total, page, size));
    }

    /**
     * 获取所有分类（下拉用）
     */
    @GetMapping("/all")
    public Result<List<Category>> all() {
        return Result.success(categoryMapper.selectList(null));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> add(@RequestBody Category category) {
        return Result.success(categoryMapper.insert(category) > 0);
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> update(@RequestBody Category category) {
        return Result.success(categoryMapper.updateById(category) > 0);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> delete(@PathVariable Long id) {
        // 删除前校验：该分类下仍有商品时禁止删除，避免 product.category_id 悬空引用
        long productCount = productMapper.countByCategoryId(id);
        if (productCount > 0) {
            return Result.error("该分类下还有 " + productCount + " 个商品，请先移除或调整这些商品的分类后再删除");
        }
        return Result.success(categoryMapper.deleteById(id) > 0);
    }
}
