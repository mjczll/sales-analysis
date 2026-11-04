package com.slxy.mapper;

import com.slxy.entity.Order;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    @Select("SELECT * FROM sales_order WHERE id = #{id}")
    Order selectById(Long id);

    @Select({"<script>",
            "SELECT * FROM sales_order",
            "<where>",
            "  <if test='orderNo != null and orderNo != \"\"'>AND order_no LIKE CONCAT('%', #{orderNo}, '%')</if>",
            "  <if test='status != null'>AND status = #{status}</if>",
            "  <if test='userId != null'>AND user_id = #{userId}</if>",
            "  <if test='startTime != null'>AND create_time &gt;= #{startTime}</if>",
            "  <if test='endTime != null'>AND create_time &lt;= #{endTime}</if>",
            "</where>",
            "ORDER BY create_time DESC",
            "LIMIT #{offset}, #{size}",
            "</script>"})
    List<Order> selectPage(@Param("orderNo") String orderNo, @Param("status") Integer status,
                           @Param("userId") Long userId, @Param("startTime") LocalDateTime startTime,
                           @Param("endTime") LocalDateTime endTime,
                           @Param("offset") int offset, @Param("size") int size);

    @Select({"<script>",
            "SELECT COUNT(*) FROM sales_order",
            "<where>",
            "  <if test='orderNo != null and orderNo != \"\"'>AND order_no LIKE CONCAT('%', #{orderNo}, '%')</if>",
            "  <if test='status != null'>AND status = #{status}</if>",
            "  <if test='userId != null'>AND user_id = #{userId}</if>",
            "  <if test='startTime != null'>AND create_time &gt;= #{startTime}</if>",
            "  <if test='endTime != null'>AND create_time &lt;= #{endTime}</if>",
            "</where>",
            "</script>"})
    long countPage(@Param("orderNo") String orderNo, @Param("status") Integer status,
                   @Param("userId") Long userId, @Param("startTime") LocalDateTime startTime,
                   @Param("endTime") LocalDateTime endTime);

    @Select("SELECT * FROM sales_order ORDER BY create_time DESC")
    List<Order> selectAll();

    @Select("SELECT * FROM sales_order WHERE order_no = #{orderNo}")
    Order selectByOrderNo(String orderNo);

    /**
     * 分页查询订单（带客户名、销售员名，一次 JOIN 解决 N+1），
     * 支持 customerId 条件下推（修复按客户名过滤的假分页问题）。
     */
    @Select({"<script>",
            "SELECT o.id, o.order_no, o.customer_id, o.user_id, o.total_amount, o.remark, o.status, o.create_time, o.update_time, ",
            "       c.name AS customer_name, u.real_name AS salesman_name ",
            "FROM sales_order o ",
            "LEFT JOIN customer c ON o.customer_id = c.id ",
            "LEFT JOIN sys_user u ON o.user_id = u.id ",
            "<where>",
            "  <if test='orderNo != null and orderNo != \"\"'>AND o.order_no LIKE CONCAT('%', #{orderNo}, '%')</if>",
            "  <if test='status != null'>AND o.status = #{status}</if>",
            "  <if test='userId != null'>AND o.user_id = #{userId}</if>",
            "  <if test='customerId != null'>AND o.customer_id = #{customerId}</if>",
            "  <if test='startTime != null'>AND o.create_time &gt;= #{startTime}</if>",
            "  <if test='endTime != null'>AND o.create_time &lt;= #{endTime}</if>",
            "</where>",
            "ORDER BY o.create_time DESC",
            "LIMIT #{offset}, #{size}",
            "</script>"})
    List<Map<String, Object>> selectPageWithJoin(@Param("orderNo") String orderNo, @Param("status") Integer status,
                                                 @Param("userId") Long userId, @Param("customerId") Long customerId,
                                                 @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime,
                                                 @Param("offset") int offset, @Param("size") int size);

    @Select({"<script>",
            "SELECT COUNT(*) FROM sales_order o ",
            "<where>",
            "  <if test='orderNo != null and orderNo != \"\"'>AND o.order_no LIKE CONCAT('%', #{orderNo}, '%')</if>",
            "  <if test='status != null'>AND o.status = #{status}</if>",
            "  <if test='userId != null'>AND o.user_id = #{userId}</if>",
            "  <if test='customerId != null'>AND o.customer_id = #{customerId}</if>",
            "  <if test='startTime != null'>AND o.create_time &gt;= #{startTime}</if>",
            "  <if test='endTime != null'>AND o.create_time &lt;= #{endTime}</if>",
            "</where>",
            "</script>"})
    long countPageWithJoin(@Param("orderNo") String orderNo, @Param("status") Integer status,
                           @Param("userId") Long userId, @Param("customerId") Long customerId,
                           @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Insert("INSERT INTO sales_order (order_no, customer_id, user_id, total_amount, remark, status, create_time, update_time) " +
            "VALUES (#{orderNo}, #{customerId}, #{userId}, #{totalAmount}, #{remark}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);

    @Update({"<script>",
            "UPDATE sales_order",
            "<set>",
            "  <if test='orderNo != null'>order_no = #{orderNo},</if>",
            "  <if test='customerId != null'>customer_id = #{customerId},</if>",
            "  <if test='userId != null'>user_id = #{userId},</if>",
            "  <if test='totalAmount != null'>total_amount = #{totalAmount},</if>",
            "  <if test='remark != null'>remark = #{remark},</if>",
            "  update_time = NOW()",
            "</set>",
            "WHERE id = #{id}",
            "</script>"})
    int updateById(Order order);

    @Update("UPDATE sales_order SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Delete("DELETE FROM sales_order WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT COUNT(*) FROM sales_order WHERE customer_id = #{customerId}")
    long countByCustomerId(@Param("customerId") Long customerId);

    @Select("SELECT COUNT(*) FROM sales_order WHERE user_id = #{userId}")
    long countByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM sales_order WHERE status != 4 AND create_time >= #{since}")
    long countSince(LocalDateTime since);
}
