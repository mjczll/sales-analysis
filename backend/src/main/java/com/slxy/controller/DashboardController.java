package com.slxy.controller;

import com.slxy.common.Result;
import com.slxy.entity.Customer;
import com.slxy.entity.Order;
import com.slxy.entity.OrderItem;
import com.slxy.entity.Product;
import com.slxy.entity.User;
import com.slxy.mapper.CustomerMapper;
import com.slxy.mapper.OrderItemMapper;
import com.slxy.mapper.OrderMapper;
import com.slxy.mapper.ProductMapper;
import com.slxy.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据概览 / 销售分析，对齐前端 api/dashboard.js
 *
 * 订单口径（与前端 mock 一致）：
 *   - 业绩订单（计入销售额/趋势/排行）：status ∈ {1已审核, 2已发货, 3已完成}
 *   - 有效订单（计入订单总数）：status ≠ 4（排除已取消）
 */
@Slf4j
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private static final Set<Integer> SALE_STATUS = new HashSet<>(Arrays.asList(1, 2, 3));
    private static final List<String> STATUS_LABELS = Arrays.asList("待审核", "已审核", "已发货", "已完成", "已取消");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private CustomerMapper customerMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 顶部四项统计：{ totalSales, orderCount, productCount, customerCount }
     */
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        List<Order> allOrders = orderMapper.selectAll();

        BigDecimal totalSales = allOrders.stream()
                .filter(o -> SALE_STATUS.contains(o.getStatus()))
                .map(Order::getTotalAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long orderCount = allOrders.stream()
                .filter(o -> o.getStatus() == null || o.getStatus() != 4)
                .count();

        long productCount = productMapper.selectList(null, null, null, 1).size();

        long customerCount = customerMapper.selectList(null, null, null, 1).size();

        Map<String, Object> data = new HashMap<>();
        data.put("totalSales", round2(totalSales));
        data.put("orderCount", orderCount);
        data.put("productCount", productCount);
        data.put("customerCount", customerCount);
        return Result.success(data);
    }

    /**
     * 近 days 天销售趋势：[{ date, amount, orderCount }]
     * 支持 start/end 自定义区间（yyyy-MM-dd），有则优先，否则按 days 取近 N 天。
     */
    @GetMapping("/sales-trend")
    public Result<List<Map<String, Object>>> salesTrend(
            @RequestParam(defaultValue = "30") Integer days,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {
        if (days == null || days <= 0) {
            days = 30;
        }

        // 确定统计的起止日期：有 start/end 用自定义区间，否则按近 days 天
        LocalDate startDate;
        LocalDate endDate;
        if (start != null && !start.isBlank() && end != null && !end.isBlank()) {
            startDate = LocalDate.parse(start, DATE_FMT);
            endDate = LocalDate.parse(end, DATE_FMT);
        } else {
            endDate = LocalDate.now();
            startDate = endDate.minusDays(days - 1);
        }

        List<Order> saleOrders = orderMapper.selectAll().stream()
                .filter(o -> SALE_STATUS.contains(o.getStatus()))
                .collect(Collectors.toList());

        // 初始化连续日期桶
        Map<String, Map<String, Object>> map = new java.util.LinkedHashMap<>();
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            String key = d.format(DATE_FMT);
            Map<String, Object> v = new HashMap<>();
            v.put("date", key);
            v.put("amount", BigDecimal.ZERO);
            v.put("orderCount", 0);
            map.put(key, v);
        }

        for (Order o : saleOrders) {
            if (o.getCreateTime() == null) continue;
            String key = o.getCreateTime().toLocalDate().format(DATE_FMT);
            Map<String, Object> v = map.get(key);
            if (v != null) {
                BigDecimal amount = (BigDecimal) v.get("amount");
                BigDecimal add = o.getTotalAmount() == null ? BigDecimal.ZERO : o.getTotalAmount();
                v.put("amount", amount.add(add));
                v.put("orderCount", ((Number) v.get("orderCount")).intValue() + 1);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>(map.values());
        result.forEach(v -> {
            BigDecimal amount = (BigDecimal) v.get("amount");
            v.put("amount", round2(amount).doubleValue());
        });
        return Result.success(result);
    }

    /**
     * 订单状态分布：[{ name, value }]
     */
    @GetMapping("/order-status")
    public Result<List<Map<String, Object>>> orderStatus() {
        List<Order> allOrders = orderMapper.selectAll();
        int[] counts = new int[5];
        for (Order o : allOrders) {
            int s = o.getStatus() == null ? 0 : o.getStatus();
            if (s >= 0 && s < 5) {
                counts[s]++;
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Map<String, Object> m = new HashMap<>();
            m.put("name", STATUS_LABELS.get(i));
            m.put("value", counts[i]);
            result.add(m);
        }
        return Result.success(result);
    }

    /**
     * 商品销售排行 Top N：[{ name, quantity, amount }]
     */
    @GetMapping("/product-rank")
    public Result<List<Map<String, Object>>> productRank(@RequestParam(defaultValue = "10") Integer topN) {
        List<Order> saleOrders = orderMapper.selectAll().stream()
                .filter(o -> SALE_STATUS.contains(o.getStatus()))
                .collect(Collectors.toList());
        Set<Long> saleOrderIds = saleOrders.stream().map(Order::getId).collect(Collectors.toSet());

        List<OrderItem> items = orderItemMapper.selectAll().stream()
                .filter(it -> saleOrderIds.contains(it.getOrderId()))
                .collect(Collectors.toList());

        Map<String, Map<String, Object>> map = new HashMap<>();
        for (OrderItem it : items) {
            String name = it.getProductName();
            if (name == null) name = "未知商品";
            Map<String, Object> m = map.computeIfAbsent(name, k -> {
                Map<String, Object> x = new HashMap<>();
                x.put("name", k);
                x.put("quantity", 0);
                x.put("amount", BigDecimal.ZERO);
                return x;
            });
            m.put("quantity", ((Number) m.get("quantity")).intValue() + (it.getQuantity() == null ? 0 : it.getQuantity()));
            BigDecimal amt = (BigDecimal) m.get("amount");
            BigDecimal add = it.getAmount() == null ? BigDecimal.ZERO : it.getAmount();
            m.put("amount", amt.add(add));
        }

        List<Map<String, Object>> result = new ArrayList<>(map.values());
        result.forEach(v -> v.put("amount", round2((BigDecimal) v.get("amount")).doubleValue()));
        result.sort((a, b) -> Integer.compare(
                (Integer) b.get("quantity"), (Integer) a.get("quantity")));
        return Result.success(result.subList(0, Math.min(topN, result.size())));
    }

    /**
     * 员工业绩排行：[{ name, orderCount, totalAmount }]
     */
    @GetMapping("/employee-rank")
    public Result<List<Map<String, Object>>> employeeRank() {
        List<Order> saleOrders = orderMapper.selectAll().stream()
                .filter(o -> SALE_STATUS.contains(o.getStatus()))
                .collect(Collectors.toList());

        Map<Long, Map<String, Object>> map = new HashMap<>();
        for (Order o : saleOrders) {
            Long uid = o.getUserId();
            if (uid == null) continue;
            Map<String, Object> m = map.computeIfAbsent(uid, k -> {
                Map<String, Object> x = new HashMap<>();
                x.put("userId", k);
                x.put("name", userName(k));
                x.put("orderCount", 0);
                x.put("totalAmount", BigDecimal.ZERO);
                return x;
            });
            m.put("orderCount", ((Number) m.get("orderCount")).intValue() + 1);
            BigDecimal amt = (BigDecimal) m.get("totalAmount");
            BigDecimal add = o.getTotalAmount() == null ? BigDecimal.ZERO : o.getTotalAmount();
            m.put("totalAmount", amt.add(add));
        }

        List<Map<String, Object>> result = new ArrayList<>(map.values());
        result.forEach(v -> v.put("totalAmount", round2((BigDecimal) v.get("totalAmount")).doubleValue()));
        result.sort((a, b) -> Double.compare(
                ((Number) b.get("totalAmount")).doubleValue(),
                ((Number) a.get("totalAmount")).doubleValue()));
        return Result.success(result);
    }

    /**
     * 最近订单：[{ id, orderNo, customerId, customerName, userId, salesman, totalAmount, status, createTime }]
     */
    @GetMapping("/recent-orders")
    public Result<List<Map<String, Object>>> recentOrders(@RequestParam(defaultValue = "6") Integer limit) {
        List<Order> orders = orderMapper.selectAll().stream()
                .sorted((a, b) -> {
                    if (a.getCreateTime() == null) return 1;
                    if (b.getCreateTime() == null) return -1;
                    return b.getCreateTime().compareTo(a.getCreateTime());
                })
                .collect(Collectors.toList());

        List<Map<String, Object>> result = new ArrayList<>();
        for (Order o : orders) {
            if (result.size() >= limit) break;
            Map<String, Object> m = new HashMap<>();
            m.put("id", o.getId());
            m.put("orderNo", o.getOrderNo());
            m.put("customerId", o.getCustomerId());
            m.put("customerName", customerName(o.getCustomerId()));
            m.put("userId", o.getUserId());
            m.put("salesman", userName(o.getUserId()));
            m.put("totalAmount", o.getTotalAmount());
            m.put("status", o.getStatus());
            m.put("createTime", o.getCreateTime());
            result.add(m);
        }
        return Result.success(result);
    }

    /**
     * 分析页汇总：{ totalSales, orderCount, avgOrderAmount, trend }
     * 支持 start/end 自定义区间（yyyy-MM-dd），有则优先，否则按 days 取近 N 天。
     */
    @GetMapping("/analysis")
    public Result<Map<String, Object>> analysis(
            @RequestParam(defaultValue = "30") Integer days,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {
        if (days == null || days <= 0) {
            days = 30;
        }
        List<Map<String, Object>> trend = salesTrend(days, start, end).getData();

        BigDecimal totalSales = BigDecimal.ZERO;
        int orderCount = 0;
        for (Map<String, Object> t : trend) {
            totalSales = totalSales.add(BigDecimal.valueOf(((Number) t.get("amount")).doubleValue()));
            orderCount += ((Number) t.get("orderCount")).intValue();
        }
        double avgOrderAmount = orderCount > 0
                ? round2(totalSales.divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP)).doubleValue()
                : 0;

        Map<String, Object> data = new HashMap<>();
        data.put("totalSales", round2(totalSales).doubleValue());
        data.put("orderCount", orderCount);
        data.put("avgOrderAmount", avgOrderAmount);
        data.put("trend", trend);
        return Result.success(data);
    }

    private String customerName(Long customerId) {
        if (customerId == null) return "";
        Customer c = customerMapper.selectById(customerId);
        return c != null ? c.getName() : "";
    }

    private String userName(Long userId) {
        if (userId == null) return "";
        User u = userMapper.selectById(userId);
        return u != null ? u.getRealName() : "";
    }

    private BigDecimal round2(BigDecimal v) {
        if (v == null) return BigDecimal.ZERO;
        return v.setScale(2, RoundingMode.HALF_UP);
    }
}
