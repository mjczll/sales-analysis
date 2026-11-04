package com.slxy.mapper;

import com.slxy.entity.Product;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface ProductMapper {

    @Select("SELECT * FROM product WHERE id = #{id}")
    Product selectById(Long id);

    @Select({"<script>",
            "SELECT * FROM product WHERE id IN",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
            "</script>"})
    List<Product> selectByIds(@Param("ids") List<Long> ids);

    @Select({"<script>",
            "SELECT * FROM product",
            "<where>",
            "  <if test='name != null and name != \"\"'>AND name LIKE CONCAT('%', #{name}, '%')</if>",
            "  <if test='code != null and code != \"\"'>AND code LIKE CONCAT('%', #{code}, '%')</if>",
            "  <if test='categoryId != null'>AND category_id = #{categoryId}</if>",
            "  <if test='status != null'>AND status = #{status}</if>",
            "</where>",
            "ORDER BY create_time DESC",
            "</script>"})
    List<Product> selectList(@Param("name") String name, @Param("code") String code,
                             @Param("categoryId") Long categoryId, @Param("status") Integer status);

    @Select({"<script>",
            "SELECT * FROM product",
            "<where>",
            "  <if test='name != null and name != \"\"'>AND name LIKE CONCAT('%', #{name}, '%')</if>",
            "  <if test='code != null and code != \"\"'>AND code LIKE CONCAT('%', #{code}, '%')</if>",
            "  <if test='categoryId != null'>AND category_id = #{categoryId}</if>",
            "  <if test='status != null'>AND status = #{status}</if>",
            "</where>",
            "ORDER BY create_time DESC",
            "LIMIT #{offset}, #{size}",
            "</script>"})
    List<Product> selectPage(@Param("name") String name, @Param("code") String code,
                             @Param("categoryId") Long categoryId, @Param("status") Integer status,
                             @Param("offset") int offset, @Param("size") int size);

    @Select({"<script>",
            "SELECT COUNT(*) FROM product",
            "<where>",
            "  <if test='name != null and name != \"\"'>AND name LIKE CONCAT('%', #{name}, '%')</if>",
            "  <if test='code != null and code != \"\"'>AND code LIKE CONCAT('%', #{code}, '%')</if>",
            "  <if test='categoryId != null'>AND category_id = #{categoryId}</if>",
            "  <if test='status != null'>AND status = #{status}</if>",
            "</where>",
            "</script>"})
    long countPage(@Param("name") String name, @Param("code") String code,
                   @Param("categoryId") Long categoryId, @Param("status") Integer status);

    @Insert("INSERT INTO product (code, name, category_id, spec, unit, price, cost_price, stock, status, remark, create_time, update_time) " +
            "VALUES (#{code}, #{name}, #{categoryId}, #{spec}, #{unit}, #{price}, #{costPrice}, #{stock}, #{status}, #{remark}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Product product);

    @Update({"<script>",
            "UPDATE product",
            "<set>",
            "  <if test='code != null'>code = #{code},</if>",
            "  <if test='name != null'>name = #{name},</if>",
            "  <if test='categoryId != null'>category_id = #{categoryId},</if>",
            "  <if test='spec != null'>spec = #{spec},</if>",
            "  <if test='unit != null'>unit = #{unit},</if>",
            "  <if test='price != null'>price = #{price},</if>",
            "  <if test='costPrice != null'>cost_price = #{costPrice},</if>",
            "  <if test='status != null'>status = #{status},</if>",
            "  <if test='remark != null'>remark = #{remark},</if>",
            "  update_time = NOW()",
            "</set>",
            "WHERE id = #{id}",
            "</script>"})
    int updateById(Product product);

    @Update("UPDATE product SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Update("UPDATE product SET stock = #{stock} WHERE id = #{id}")
    int updateStock(@Param("id") Long id, @Param("stock") Integer stock);

    /**
     * 原子扣减库存：仅当剩余库存足够时才扣减，避免并发超卖。
     * 返回受影响行数（0 表示库存不足）。
     */
    @Update("UPDATE product SET stock = stock - #{quantity} WHERE id = #{id} AND stock >= #{quantity}")
    int deductStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 原子回补库存：库存加回指定数量（取消/删除订单回补、入库等场景）。
     * 直接 SQL 自增，避免 read-modify-write 的并发覆盖问题。
     */
    @Update("UPDATE product SET stock = stock + #{quantity} WHERE id = #{id}")
    int addStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    @Delete("DELETE FROM product WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT COUNT(*) FROM product WHERE category_id = #{categoryId}")
    long countByCategoryId(@Param("categoryId") Long categoryId);

    @Select("SELECT COUNT(*) FROM product WHERE create_time >= #{since}")
    long countSince(java.time.LocalDateTime since);

    /**
     * 热门商品排行（按订单明细销量/销售额，仅统计有效订单）
     */
    @Select("SELECT p.id, p.name, COALESCE(SUM(oi.quantity), 0) AS quantity, " +
            "COALESCE(SUM(oi.amount), 0) AS amount " +
            "FROM product p " +
            "LEFT JOIN order_item oi ON oi.product_id = p.id " +
            "LEFT JOIN sales_order o ON o.id = oi.order_id " +
            "WHERE o.id IS NULL OR o.status != 4 " +
            "GROUP BY p.id, p.name " +
            "ORDER BY quantity DESC, amount DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectTopProduct(@Param("limit") int limit);

    /**
     * 分类商品分布：每个分类下的商品数量
     */
    @Select("SELECT c.id AS categoryId, c.name AS name, COUNT(p.id) AS value " +
            "FROM category c " +
            "LEFT JOIN product p ON p.category_id = c.id " +
            "GROUP BY c.id, c.name " +
            "ORDER BY value DESC")
    List<Map<String, Object>> selectCategoryDistribution();
}
