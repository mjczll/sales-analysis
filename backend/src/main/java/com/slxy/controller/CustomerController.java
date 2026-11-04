package com.slxy.controller;

import com.slxy.common.PageResult;
import com.slxy.common.Result;
import com.slxy.entity.Customer;
import com.slxy.mapper.CustomerMapper;
import com.slxy.mapper.OrderMapper;
import com.slxy.utils.ValidatorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客户管理，对齐前端 api/customer.js
 */
@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 分页列表：前端传 page/size、name/contactPerson/level/status
     */
    @GetMapping("/list")
    public Result<PageResult<Customer>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String contactPerson,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) Integer status) {
        int offset = (page - 1) * size;
        List<Customer> records = customerMapper.selectPage(name, contactPerson, level, status, offset, size);
        long total = customerMapper.countPage(name, contactPerson, level, status);
        return Result.success(PageResult.of(records, total, page, size));
    }

    @GetMapping("/{id}")
    public Result<Customer> getById(@PathVariable Long id) {
        return Result.success(customerMapper.selectById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public Result<Boolean> add(@RequestBody Customer customer) {
        if (!ValidatorUtils.isPhoneValid(customer.getPhone())) {
            return Result.error("联系电话格式不正确");
        }
        if (!ValidatorUtils.isEmailValid(customer.getEmail())) {
            return Result.error("邮箱格式不正确");
        }
        if (customer.getStatus() == null) {
            customer.setStatus(1);
        }
        return Result.success(customerMapper.insert(customer) > 0);
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public Result<Boolean> update(@RequestBody Customer customer) {
        if (!ValidatorUtils.isPhoneValid(customer.getPhone())) {
            return Result.error("联系电话格式不正确");
        }
        if (!ValidatorUtils.isEmailValid(customer.getEmail())) {
            return Result.error("邮箱格式不正确");
        }
        return Result.success(customerMapper.updateById(customer) > 0);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALES')")
    public Result<Boolean> delete(@PathVariable Long id) {
        // 删除前校验：该客户下已有订单时禁止删除，避免 sales_order.customer_id 悬空引用
        long orderCount = orderMapper.countByCustomerId(id);
        if (orderCount > 0) {
            return Result.error("该客户下还有 " + orderCount + " 个订单，无法删除，请先处理相关订单");
        }
        return Result.success(customerMapper.deleteById(id) > 0);
    }
}
