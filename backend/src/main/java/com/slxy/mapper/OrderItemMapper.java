package com.slxy.mapper;

import com.slxy.entity.OrderItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderItemMapper {

    @Select("SELECT * FROM order_item WHERE order_id = #{orderId}")
    List<OrderItem> selectItemsByOrderId(Long orderId);

    @Select("SELECT * FROM order_item")
    List<OrderItem> selectAll();

    @Insert("INSERT INTO order_item (order_id, product_id, product_name, price, quantity, amount) " +
            "VALUES (#{orderId}, #{productId}, #{productName}, #{price}, #{quantity}, #{amount})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OrderItem item);

    @Delete("DELETE FROM order_item WHERE order_id = #{orderId}")
    int deleteByOrderId(Long orderId);

    @Select("SELECT COUNT(*) FROM order_item WHERE product_id = #{productId}")
    long countByProductId(@Param("productId") Long productId);
}
