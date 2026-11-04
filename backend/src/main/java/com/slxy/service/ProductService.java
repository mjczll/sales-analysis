package com.slxy.service;

import com.slxy.common.PageResult;
import com.slxy.entity.Product;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ProductService {

    PageResult<Product> getProductList(int page, int size, String name, String code, Long categoryId, Integer status);

    Product getById(Long id);

    boolean save(Product product);

    boolean updateById(Product product);

    boolean removeById(Long id);

    void updateStatus(Long id, Integer status);

    Long getIncrease(LocalDateTime since);

    List<Map<String, Object>> getTopProduct(Integer limit);

    List<Map<String, Object>> getCategoryDistribution();

    List<String> getProductCategories();
}
