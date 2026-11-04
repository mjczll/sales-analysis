package com.slxy.controller;

import com.slxy.common.PageResult;
import com.slxy.common.Result;
import com.slxy.entity.Category;
import com.slxy.entity.Product;
import com.slxy.entity.StockRecord;
import com.slxy.entity.User;
import com.slxy.mapper.CategoryMapper;
import com.slxy.mapper.ProductMapper;
import com.slxy.mapper.StockRecordMapper;
import com.slxy.mapper.UserMapper;
import com.slxy.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 库存管理，对齐前端 api/stock.js
 */
@Slf4j
@RestController
@RequestMapping("/api/stock")
public class InventoryController {

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private StockRecordMapper stockRecordMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 库存总览：商品库存列表，附带分类名与库存状态
     */
    @GetMapping("/overview")
    public Result<PageResult<Map<String, Object>>> overview(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code) {
        int offset = (page - 1) * size;
        List<Product> products = productMapper.selectPage(name, code, null, null, offset, size);
        long total = productMapper.countPage(name, code, null, null);

        // 批量查询分类名，避免 N+1
        Map<Long, String> categoryNameMap = batchCategoryNames(products);

        List<Map<String, Object>> records = new ArrayList<>();
        for (Product prod : products) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", prod.getId());
            row.put("code", prod.getCode());
            row.put("name", prod.getName());
            row.put("categoryId", prod.getCategoryId());
            row.put("categoryName", categoryNameMap.getOrDefault(prod.getCategoryId(), ""));
            row.put("stock", prod.getStock());
            // 库存状态：缺货 / 预警(<50) / 正常
            if (prod.getStock() == null || prod.getStock() <= 0) {
                row.put("status", "缺货");
                row.put("statusType", "danger");
            } else if (prod.getStock() < 50) {
                row.put("status", "预警");
                row.put("statusType", "warning");
            } else {
                row.put("status", "正常");
                row.put("statusType", "success");
            }
            records.add(row);
        }
        return Result.success(PageResult.of(records, total, page, size));
    }

    /**
     * 库存流水：附带商品名与操作人名
     */
    @GetMapping("/records")
    public Result<PageResult<Map<String, Object>>> records(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) Integer type) {

        // 若按商品名筛选，先反查 productId
        List<Long> ids = null;
        if (StringUtils.hasText(productName)) {
            List<Product> ps = productMapper.selectList(productName, null, null, null);
            ids = new ArrayList<>();
            for (Product prod : ps) {
                ids.add(prod.getId());
            }
            if (ids.isEmpty()) {
                return Result.success(PageResult.empty(page, size));
            }
        }

        int offset = (page - 1) * size;
        List<StockRecord> recordList = stockRecordMapper.selectPage(type, ids, offset, size);
        long total = stockRecordMapper.countPage(type, ids);

        // 批量查询商品名、操作人名，避免 N+1
        Map<Long, String> productNameMap = batchProductNames(recordList);
        Map<Long, String> operatorNameMap = batchOperatorNames(recordList);

        List<Map<String, Object>> records = new ArrayList<>();
        for (StockRecord r : recordList) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", r.getId());
            row.put("productId", r.getProductId());
            row.put("productName", productNameMap.getOrDefault(r.getProductId(), ""));
            row.put("type", r.getType());
            row.put("quantity", r.getQuantity());
            row.put("remark", r.getRemark());
            row.put("userId", r.getUserId());
            row.put("operator", operatorNameMap.getOrDefault(r.getUserId(), ""));
            row.put("createTime", r.getCreateTime());
            records.add(row);
        }
        return Result.success(PageResult.of(records, total, page, size));
    }

    /**
     * 入库：增加库存 + 写流水
     */
    @PostMapping("/in")
    @PreAuthorize("hasAnyRole('ADMIN', 'STOCK')")
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> stockIn(@RequestBody Map<String, Object> body) {
        Long productId = toLong(body.get("productId"));
        Integer quantity = toInt(body.get("quantity"));
        if (productId == null || quantity == null || quantity <= 0) {
            return Result.error("参数不完整或数量非法");
        }
        Product prod = productMapper.selectById(productId);
        if (prod == null) {
            return Result.error("商品不存在");
        }
        // 增加库存
        int newStock = (prod.getStock() == null ? 0 : prod.getStock()) + quantity;
        productMapper.updateStock(productId, newStock);

        // 写流水
        StockRecord r = new StockRecord();
        r.setProductId(productId);
        r.setType(1);
        r.setQuantity(quantity);
        r.setRemark(body.get("remark") != null ? body.get("remark").toString() : "采购入库");
        // 操作人：优先取当前登录用户，避免硬编码兜底
        Long operatorId = toLong(body.get("userId"));
        if (operatorId == null) {
            operatorId = SecurityUtils.getCurrentUserId();
        }
        r.setUserId(operatorId);
        stockRecordMapper.insert(r);
        return Result.success(true);
    }

    /**
     * 出库：校验库存充足后扣减 + 写流水
     */
    @PostMapping("/out")
    @PreAuthorize("hasAnyRole('ADMIN', 'STOCK')")
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> stockOut(@RequestBody Map<String, Object> body) {
        Long productId = toLong(body.get("productId"));
        Integer quantity = toInt(body.get("quantity"));
        if (productId == null || quantity == null || quantity <= 0) {
            return Result.error("参数不完整或数量非法");
        }
        Product prod = productMapper.selectById(productId);
        if (prod == null) {
            return Result.error("商品不存在");
        }
        int current = prod.getStock() == null ? 0 : prod.getStock();
        if (current < quantity) {
            return Result.error("库存不足，禁止出库");
        }
        productMapper.updateStock(productId, current - quantity);

        StockRecord r = new StockRecord();
        r.setProductId(productId);
        r.setType(2);
        r.setQuantity(quantity);
        r.setRemark(body.get("remark") != null ? body.get("remark").toString() : "销售出库");
        // 操作人：优先取当前登录用户，避免硬编码兜底
        Long operatorId = toLong(body.get("userId"));
        if (operatorId == null) {
            operatorId = SecurityUtils.getCurrentUserId();
        }
        r.setUserId(operatorId);
        stockRecordMapper.insert(r);
        return Result.success(true);
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

    /**
     * 批量查询商品名（一次查询，避免 N+1）
     */
    private Map<Long, String> batchProductNames(List<StockRecord> recordList) {
        Map<Long, String> map = new HashMap<>();
        List<Long> ids = new ArrayList<>();
        for (StockRecord r : recordList) {
            if (r.getProductId() != null) {
                ids.add(r.getProductId());
            }
        }
        if (ids.isEmpty()) {
            return map;
        }
        List<Product> products = productMapper.selectByIds(ids);
        for (Product p : products) {
            map.put(p.getId(), p.getName());
        }
        return map;
    }

    /**
     * 批量查询操作人名（一次查询，避免 N+1）
     */
    private Map<Long, String> batchOperatorNames(List<StockRecord> recordList) {
        Map<Long, String> map = new HashMap<>();
        List<Long> ids = new ArrayList<>();
        for (StockRecord r : recordList) {
            if (r.getUserId() != null) {
                ids.add(r.getUserId());
            }
        }
        if (ids.isEmpty()) {
            return map;
        }
        List<User> users = userMapper.selectByIds(ids);
        for (User u : users) {
            map.put(u.getId(), u.getRealName());
        }
        return map;
    }

    private Long toLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).longValue();
        try {
            return Long.valueOf(o.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private Integer toInt(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).intValue();
        try {
            return Integer.valueOf(o.toString());
        } catch (Exception e) {
            return null;
        }
    }
}
