package com.slxy.service.impl;

import com.slxy.common.PageResult;
import com.slxy.entity.Product;
import com.slxy.mapper.ProductMapper;
import com.slxy.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Override
    public PageResult<Product> getProductList(int page, int size, String name, String code, Long categoryId, Integer status) {
        int offset = (page - 1) * size;
        List<Product> records = productMapper.selectPage(name, code, categoryId, status, offset, size);
        long total = productMapper.countPage(name, code, categoryId, status);
        return PageResult.of(records, total, page, size);
    }

    @Override
    public Product getById(Long id) {
        return productMapper.selectById(id);
    }

    @Override
    public boolean save(Product product) {
        return productMapper.insert(product) > 0;
    }

    @Override
    public boolean updateById(Product product) {
        return productMapper.updateById(product) > 0;
    }

    @Override
    public boolean removeById(Long id) {
        return productMapper.deleteById(id) > 0;
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        productMapper.updateStatus(id, status);
    }

    @Override
    public Long getIncrease(LocalDateTime since) {
        return productMapper.countSince(since);
    }

    @Override
    public List<Map<String, Object>> getTopProduct(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        return productMapper.selectTopProduct(limit);
    }

    @Override
    public List<Map<String, Object>> getCategoryDistribution() {
        return productMapper.selectCategoryDistribution();
    }

    @Override
    public List<String> getProductCategories() {
        // 返回所有分类名称（供前端下拉等使用）
        return productMapper.selectCategoryDistribution().stream()
                .map(m -> m.get("name") != null ? m.get("name").toString() : "")
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.toList());
    }
}
