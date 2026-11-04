package com.slxy.mapper;

import com.slxy.entity.StockRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface StockRecordMapper {

    @Select({"<script>",
            "SELECT * FROM stock_record",
            "<where>",
            "  <if test='type != null'>AND type = #{type}</if>",
            "  <if test='productIds != null and productIds.size() > 0'>",
            "    AND product_id IN",
            "    <foreach collection='productIds' item='pid' open='(' separator=',' close=')'>#{pid}</foreach>",
            "  </if>",
            "</where>",
            "ORDER BY create_time DESC",
            "LIMIT #{offset}, #{size}",
            "</script>"})
    List<StockRecord> selectPage(@Param("type") Integer type, @Param("productIds") List<Long> productIds,
                                 @Param("offset") int offset, @Param("size") int size);

    @Select({"<script>",
            "SELECT COUNT(*) FROM stock_record",
            "<where>",
            "  <if test='type != null'>AND type = #{type}</if>",
            "  <if test='productIds != null and productIds.size() > 0'>",
            "    AND product_id IN",
            "    <foreach collection='productIds' item='pid' open='(' separator=',' close=')'>#{pid}</foreach>",
            "  </if>",
            "</where>",
            "</script>"})
    long countPage(@Param("type") Integer type, @Param("productIds") List<Long> productIds);

    @Insert("INSERT INTO stock_record (product_id, type, quantity, remark, user_id, create_time) " +
            "VALUES (#{productId}, #{type}, #{quantity}, #{remark}, #{userId}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(StockRecord record);

    @Select("SELECT COUNT(*) FROM stock_record WHERE product_id = #{productId}")
    long countByProductId(@Param("productId") Long productId);
}
