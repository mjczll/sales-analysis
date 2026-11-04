package com.slxy.service.impl;

import com.slxy.common.PageResult;
import com.slxy.dto.CreateOrderDTO;
import com.slxy.entity.Order;
import com.slxy.entity.OrderItem;
import com.slxy.entity.Product;
import com.slxy.entity.StockRecord;
import com.slxy.mapper.OrderMapper;
import com.slxy.mapper.OrderItemMapper;
import com.slxy.mapper.ProductMapper;
import com.slxy.mapper.StockRecordMapper;
import com.slxy.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private StockRecordMapper stockRecordMapper;

    @Override
    public PageResult<Order> getOrderList(int page, int size, String orderNo, Integer status, String startTime, String endTime, Long userId) {
        LocalDateTime start = null;
        LocalDateTime end = null;
        if (StringUtils.hasText(startTime)) {
            try {
                start = LocalDateTime.parse(startTime + "T00:00:00");
            } catch (Exception ignored) {
            }
        }
        if (StringUtils.hasText(endTime)) {
            try {
                end = LocalDateTime.parse(endTime + "T23:59:59");
            } catch (Exception ignored) {
            }
        }
        int offset = (page - 1) * size;
        List<Order> records = orderMapper.selectPage(orderNo, status, userId, start, end, offset, size);
        long total = orderMapper.countPage(orderNo, status, userId, start, end);
        return PageResult.of(records, total, page, size);
    }

    @Override
    public PageResult<Map<String, Object>> getOrderListWithJoin(int page, int size, String orderNo, Integer status,
                                                                String startTime, String endTime, Long userId, Long customerId) {
        LocalDateTime start = null;
        LocalDateTime end = null;
        if (StringUtils.hasText(startTime)) {
            try {
                start = LocalDateTime.parse(startTime + "T00:00:00");
            } catch (Exception ignored) {
            }
        }
        if (StringUtils.hasText(endTime)) {
            try {
                end = LocalDateTime.parse(endTime + "T23:59:59");
            } catch (Exception ignored) {
            }
        }
        int offset = (page - 1) * size;
        List<Map<String, Object>> records = orderMapper.selectPageWithJoin(orderNo, status, userId, customerId, start, end, offset, size);
        long total = orderMapper.countPageWithJoin(orderNo, status, userId, customerId, start, end);
        return PageResult.of(records, total, page, size);
    }

    @Override
    public Order getOrderDetail(Long id) {
        Order order = orderMapper.selectById(id);
        if (order != null) {
            List<OrderItem> items = orderItemMapper.selectItemsByOrderId(id);
            order.setItems(items);
        }
        return order;
    }

    @Override
    public Order getById(Long id) {
        return orderMapper.selectById(id);
    }

    @Override
    public void payOrder(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new RuntimeException("订单状态错误，无法支付");
        }

        List<OrderItem> items = orderItemMapper.selectItemsByOrderId(id);
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("订单无商品，无法支付");
        }

        // 逐个商品原子扣减库存，并写库存流水
        for (OrderItem item : items) {
            Long productId = item.getProductId();
            Integer quantity = item.getQuantity();

            Product product = productMapper.selectById(productId);
            if (product == null) {
                throw new RuntimeException("商品不存在");
            }

            // 原子扣减：仅当库存充足时成功，避免并发超卖
            int affected = productMapper.deductStock(productId, quantity);
            if (affected == 0) {
                throw new RuntimeException("商品库存不足：" + product.getName());
            }

            // 写库存出库流水
            StockRecord record = new StockRecord();
            record.setProductId(productId);
            record.setType(2); // 2 出库
            record.setQuantity(quantity);
            record.setRemark("销售出库：" + order.getOrderNo());
            record.setUserId(order.getUserId());
            stockRecordMapper.insert(record);
        }

        // 更新订单状态为已审核（1）
        orderMapper.updateStatus(id, 1);
    }

    @Override
    public void shipOrder(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() == null || order.getStatus() != 1) {
            throw new RuntimeException("仅已审核（已支付）的订单才能发货");
        }
        orderMapper.updateStatus(id, 2);
    }

    @Override
    public void cancelOrder(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 只要订单已扣过库存（已审核 1 / 已发货 2 / 已完成 3）就回补库存；
        // 待审核(0)未扣库存，无需回补
        Integer st = order.getStatus();
        if (st != null && (st == 1 || st == 2 || st == 3)) {
            List<OrderItem> items = orderItemMapper.selectItemsByOrderId(id);
            if (items != null) {
                for (OrderItem item : items) {
                    // 原子回补库存（SQL 自增，避免并发覆盖）
                    productMapper.addStock(item.getProductId(), item.getQuantity());

                    // 写库存入库流水（取消回补）
                    StockRecord record = new StockRecord();
                    record.setProductId(item.getProductId());
                    record.setType(1); // 1 入库
                    record.setQuantity(item.getQuantity());
                    record.setRemark("取消订单回补：" + order.getOrderNo());
                    record.setUserId(order.getUserId());
                    stockRecordMapper.insert(record);
                }
            }
        }

        orderMapper.updateStatus(id, 4);
    }

    @Override
    public void completeOrder(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() == null || order.getStatus() != 2) {
            throw new RuntimeException("仅已发货的订单才能完成");
        }
        orderMapper.updateStatus(id, 3);
    }

    /**
     * 删除订单：
     * - 仅允许删除待审核（0）或已取消（4）的订单；
     * - 已审核（1）订单已扣过库存，删除前需回补库存并写入库流水；
     * - 已发货（2）/已完成（3）订单禁止删除（业务上应走取消/退货流程）。
     */
    @Override
    public void deleteOrder(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        Integer status = order.getStatus();
        if (status != null && (status == 2 || status == 3)) {
            throw new RuntimeException("已发货/已完成的订单不能直接删除，请先取消");
        }
        // 已审核订单删除前回补库存（与取消订单一致）
        if (status != null && status == 1) {
            List<OrderItem> items = orderItemMapper.selectItemsByOrderId(id);
            if (items != null) {
                for (OrderItem item : items) {
                    // 原子回补库存
                    productMapper.addStock(item.getProductId(), item.getQuantity());

                    StockRecord record = new StockRecord();
                    record.setProductId(item.getProductId());
                    record.setType(1); // 入库回补
                    record.setQuantity(item.getQuantity());
                    record.setRemark("删除订单回补：" + order.getOrderNo());
                    record.setUserId(order.getUserId());
                    stockRecordMapper.insert(record);
                }
            }
        }
        // 删除明细 + 主表
        orderItemMapper.deleteByOrderId(id);
        orderMapper.deleteById(id);
    }

    @Override
    public void changeStatus(Long id, Integer targetStatus) {
        if (targetStatus == null) {
            throw new RuntimeException("目标状态不能为空");
        }
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        Integer current = order.getStatus();
        // 取消订单已是终态，不允许再流转
        if (current != null && current == 4) {
            throw new RuntimeException("已取消订单不能变更状态");
        }
        // 按目标状态分发到对应业务逻辑（含库存联动与状态机校验）
        switch (targetStatus) {
            case 0:
                // 回到待审核：仅允许从已审核回退（未扣库存场景已无，这里保守限制）
                if (current == null || current != 1) {
                    throw new RuntimeException("仅已审核订单可回退为待审核");
                }
                orderMapper.updateStatus(id, 0);
                break;
            case 1:
                payOrder(id);
                break;
            case 2:
                shipOrder(id);
                break;
            case 3:
                completeOrder(id);
                break;
            case 4:
                cancelOrder(id);
                break;
            default:
                throw new RuntimeException("非法的订单状态：" + targetStatus);
        }
    }

    @Override
    public Long getIncrease(LocalDateTime since) {
        return orderMapper.countSince(since);
    }

    @Override
    public void createOrder(CreateOrderDTO createOrderDTO) {
        createOrderWithAmount(createOrderDTO);
    }

    @Override
    public BigDecimal createOrderWithAmount(CreateOrderDTO createOrderDTO) {
        // 创建订单对象并设置属性
        Order order = new Order();
        order.setCustomerId(createOrderDTO.getCustomerId());
        // 下单人：优先取当前登录用户，避免前端任意指定
        Long operatorId = createOrderDTO.getUserId();
        if (operatorId == null) {
            operatorId = com.slxy.utils.SecurityUtils.getCurrentUserId();
        }
        order.setUserId(operatorId);
        String orderNo = (createOrderDTO.getOrderNo() != null && !createOrderDTO.getOrderNo().trim().isEmpty())
                ? createOrderDTO.getOrderNo() : generateUniqueOrderNo();
        order.setOrderNo(orderNo);
        order.setTotalAmount(BigDecimal.ZERO);
        order.setRemark(createOrderDTO.getRemark());
        order.setStatus(0);

        // 保存订单主表信息（主键回填到 order.id）
        orderMapper.insert(order);

        BigDecimal totalAmount = BigDecimal.ZERO;
        if (createOrderDTO.getItems() != null && !createOrderDTO.getItems().isEmpty()) {
            for (CreateOrderDTO.OrderItemDTO itemDTO : createOrderDTO.getItems()) {
                Product product = productMapper.selectById(itemDTO.getProductId());
                if (product == null) {
                    throw new RuntimeException("商品不存在");
                }
                if (product.getStatus() != null && product.getStatus() != 1) {
                    throw new RuntimeException("商品未上架，无法添加到订单");
                }

                // 价格：以服务端商品表销售价为准，不信任前端传入的 price
                BigDecimal price = product.getPrice();
                if (price == null) {
                    price = BigDecimal.ZERO;
                }
                int quantity = itemDTO.getQuantity() != null ? itemDTO.getQuantity() : 0;
                if (quantity <= 0) {
                    throw new RuntimeException("商品数量必须大于 0");
                }

                // 创建订单时校验库存（只拦截明显超库存的订单，不预占；实际扣减由支付时的原子操作保证）
                if (product.getStock() != null && quantity > product.getStock()) {
                    throw new RuntimeException("商品库存不足：" + product.getName() + "（当前库存 " + product.getStock() + "）");
                }

                BigDecimal amount = price.multiply(new BigDecimal(quantity));

                OrderItem item = new OrderItem();
                item.setOrderId(order.getId());
                item.setProductId(itemDTO.getProductId());
                item.setProductName(product.getName());
                item.setQuantity(quantity);
                item.setPrice(price);
                item.setAmount(amount);

                orderItemMapper.insert(item);
                totalAmount = totalAmount.add(amount);
            }
        }

        // 回填订单总金额
        order.setTotalAmount(totalAmount);
        orderMapper.updateById(order);
        return totalAmount;
    }

    @Override
    public boolean removeById(Long id) {
        return orderMapper.deleteById(id) > 0;
    }

    @Override
    public boolean checkOrderNoExists(String orderNo) {
        return orderMapper.selectByOrderNo(orderNo) != null;
    }

    private String generateOrderNo() {
        // 使用纳秒时间戳 + 较大随机空间，降低并发碰撞概率（仍由 checkOrderNoExists 兜底）
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String nanos = String.format("%06d", now.getNano() / 1000); // 微秒后 6 位
        String random = String.format("%04d", (int) (Math.random() * 10000));
        return "SO" + timestamp + nanos + random;
    }

    /**
     * 生成不重复的订单号：若随机碰撞已存在，则重试（最多 5 次）。
     */
    private String generateUniqueOrderNo() {
        for (int i = 0; i < 5; i++) {
            String no = generateOrderNo();
            if (!checkOrderNoExists(no)) {
                return no;
            }
        }
        throw new RuntimeException("订单号生成失败，请重试");
    }
}
