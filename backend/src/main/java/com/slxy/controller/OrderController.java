package com.slxy.controller;

import com.slxy.common.PageResult;
import com.slxy.common.Result;
import com.slxy.dto.CreateOrderDTO;
import com.slxy.entity.Customer;
import com.slxy.entity.Order;
import com.slxy.entity.User;
import com.slxy.mapper.CustomerMapper;
import com.slxy.mapper.OrderItemMapper;
import com.slxy.mapper.UserMapper;
import com.slxy.service.OrderService;
import com.slxy.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 销售订单管理，对齐前端 api/order.js
 */
@Slf4j
@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private CustomerMapper customerMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 分页列表：前端传 page/size，附带 customerName/salesman（名称反查）
     */
    @GetMapping("/list")
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String salesmanName) {

        // 客户名称 / 销售人员名称反查 ID（仅一次查询）
        Long customerId = null;
        Long salesmanId = null;
        if (StringUtils.hasText(customerName)) {
            List<Customer> cs = customerMapper.selectList(customerName, null, null, null);
            if (!cs.isEmpty()) {
                customerId = cs.get(0).getId();
            }
        }
        if (StringUtils.hasText(salesmanName)) {
            List<User> us = userMapper.selectList(null, salesmanName, null, null);
            if (!us.isEmpty()) {
                salesmanId = us.get(0).getId();
            }
        }

        final Long filterUserId = userId != null ? userId : salesmanId;

        // 数据越权防护：销售员仅能查看自己的订单，管理员可查看全部
        Long scopedUserId = filterUserId;
        if (isSales()) {
            Long currentId = SecurityUtils.getCurrentUserId();
            // 销售员查询条件中若指定了他人 userId，则忽略并强制限定为自己
            scopedUserId = currentId;
        }

        // JOIN 一次查询，附带 customerName / salesman，customerId 条件下推（修复 N+1 与假分页）
        PageResult<Map<String, Object>> result =
                orderService.getOrderListWithJoin(page, size, orderNo, status, start, end, scopedUserId, customerId);

        // 转换字段名，保持前端契约（customerName / salesman）
        List<Map<String, Object>> records = result.getRecords().stream().map(o -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", o.get("id"));
            m.put("orderNo", o.get("order_no"));
            m.put("customerId", o.get("customer_id"));
            m.put("customerName", o.get("customer_name"));
            m.put("userId", o.get("user_id"));
            m.put("salesman", o.get("salesman_name"));
            m.put("totalAmount", o.get("total_amount"));
            m.put("remark", o.get("remark"));
            m.put("status", o.get("status"));
            m.put("createTime", o.get("create_time"));
            m.put("updateTime", o.get("update_time"));
            return m;
        }).collect(Collectors.toList());

        return Result.success(PageResult.of(records, result.getTotal(), page, size));
    }

    @GetMapping("/check-no")
    public Result<Boolean> checkOrderNoExists(@RequestParam String orderNo) {
        boolean exists = orderService.checkOrderNoExists(orderNo);
        return Result.success(!exists);
    }

    /**
     * 订单详情：附带 items + customerName + salesman
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        // 销售员仅能查看自己的订单详情
        checkOwnership(id);
        Order order = orderService.getOrderDetail(id);
        if (order == null) {
            return Result.success(null);
        }
        Map<String, Object> m = new HashMap<>();
        m.put("id", order.getId());
        m.put("orderNo", order.getOrderNo());
        m.put("customerId", order.getCustomerId());
        m.put("customerName", customerName(order.getCustomerId()));
        m.put("userId", order.getUserId());
        m.put("salesman", userName(order.getUserId()));
        m.put("totalAmount", order.getTotalAmount());
        m.put("remark", order.getRemark());
        m.put("status", order.getStatus());
        m.put("createTime", order.getCreateTime());
        m.put("updateTime", order.getUpdateTime());
        m.put("items", order.getItems());
        return Result.success(m);
    }

    /**
     * 新建订单：返回 { id, totalAmount }
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    @Transactional(rollbackFor = Exception.class)
    public Result<Map<String, Object>> add(@RequestBody CreateOrderDTO createOrderDTO) {
        // 单号由服务端生成
        createOrderDTO.setOrderNo(null);
        // 销售员创建的订单，销售人员强制锁死为本人（防止前端任意指定他人）
        if (isSales()) {
            createOrderDTO.setUserId(SecurityUtils.getCurrentUserId());
        }
        // 通过 OrderService 创建，服务端以商品表价格计算真实总金额（不信任前端 price）
        BigDecimal totalAmount = orderService.createOrderWithAmount(createOrderDTO);

        Map<String, Object> data = new HashMap<>();
        data.put("totalAmount", totalAmount);
        return Result.success(data);
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Deprecated
    public Result<Boolean> update(@RequestBody Order order) {
        // 订单编辑端点已废弃：状态变更走 /{id}/status 状态机，金额由服务端计算，编辑会绕过状态机
        return Result.error("订单不支持直接编辑，请使用改状态/取消功能");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> delete(@PathVariable Long id) {
        checkOwnership(id);
        orderService.deleteOrder(id);
        return Result.success(true);
    }

    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> batchDelete(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error("删除的订单ID列表不能为空");
        }
        for (Long id : ids) {
            checkOwnership(id);
            orderService.deleteOrder(id);
        }
        return Result.success(true);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        checkOwnership(id);
        // 统一走状态机（含库存扣减/回补、流水写入、状态流转校验），不再裸改状态
        orderService.changeStatus(id, status);
        return Result.success(true);
    }

    @PutMapping("/{id}/ship")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> shipOrder(@PathVariable Long id) {
        checkOwnership(id);
        orderService.shipOrder(id);
        return Result.success(true);
    }

    @PutMapping("/{id}/pay")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> payOrder(@PathVariable Long id) {
        checkOwnership(id);
        orderService.payOrder(id);
        return Result.success(true);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> cancelOrder(@PathVariable Long id) {
        checkOwnership(id);
        orderService.cancelOrder(id);
        return Result.success(true);
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> completeOrder(@PathVariable Long id) {
        checkOwnership(id);
        orderService.completeOrder(id);
        return Result.success(true);
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

    /**
     * 当前登录用户是否为销售员（roleId=2）
     */
    private boolean isSales() {
        User current = SecurityUtils.getCurrentUser();
        return current != null && current.getRoleId() != null && current.getRoleId() == 2L;
    }

    /**
     * 越权校验：销售员只能操作归属自己的订单；管理员可操作全部。
     */
    private void checkOwnership(Long orderId) {
        if (!isSales()) {
            return;
        }
        Long currentId = SecurityUtils.getCurrentUserId();
        Order order = orderService.getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getUserId() == null || !order.getUserId().equals(currentId)) {
            throw new RuntimeException("无权操作他人的订单");
        }
    }
}
