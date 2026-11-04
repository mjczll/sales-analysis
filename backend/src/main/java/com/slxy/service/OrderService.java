package com.slxy.service;

import com.slxy.common.PageResult;
import com.slxy.entity.Order;
import com.slxy.dto.CreateOrderDTO;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.Map;

public interface OrderService {

    PageResult<Order> getOrderList(int page, int size, String orderNo, Integer status, String startTime, String endTime, Long userId);

    /**
     * 分页查询订单（带客户名/销售员名，JOIN 解决 N+1，customerId 条件下推修复假分页）
     */
    PageResult<Map<String, Object>> getOrderListWithJoin(int page, int size, String orderNo, Integer status,
                                                          String startTime, String endTime, Long userId, Long customerId);

    Order getOrderDetail(Long id);

    Order getById(Long id);

    void payOrder(Long id);

    void shipOrder(Long id);

    void cancelOrder(Long id);

    void completeOrder(Long id);

    /**
     * 统一状态流转入口：根据目标状态分发到支付/发货/取消/完成的完整业务逻辑
     * （含库存扣减/回补、流水写入），供 updateStatus 端点调用，避免绕过状态机。
     */
    void changeStatus(Long id, Integer targetStatus);

    Long getIncrease(LocalDateTime since);

    void createOrder(CreateOrderDTO createOrderDTO);

    /**
     * 创建订单并返回服务端计算出的真实总金额（以商品表价格为准，不信任前端 price）。
     */
    BigDecimal createOrderWithAmount(CreateOrderDTO createOrderDTO);

    /**
     * 删除订单：仅允许删除待审核（0）或已取消（4）的订单；
     * 已审核（1）订单删除前需回补库存并写入库流水。
     */
    void deleteOrder(Long id);

    boolean removeById(Long id);

    boolean checkOrderNoExists(String orderNo);
}
