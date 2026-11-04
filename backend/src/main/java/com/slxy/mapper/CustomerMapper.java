package com.slxy.mapper;

import com.slxy.entity.Customer;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CustomerMapper {

    @Select("SELECT * FROM customer WHERE id = #{id}")
    Customer selectById(Long id);

    @Select({"<script>",
            "SELECT * FROM customer",
            "<where>",
            "  <if test='name != null and name != \"\"'>AND name LIKE CONCAT('%', #{name}, '%')</if>",
            "  <if test='contactPerson != null and contactPerson != \"\"'>AND contact_person LIKE CONCAT('%', #{contactPerson}, '%')</if>",
            "  <if test='level != null and level != \"\"'>AND level = #{level}</if>",
            "  <if test='status != null'>AND status = #{status}</if>",
            "</where>",
            "ORDER BY create_time DESC",
            "</script>"})
    List<Customer> selectList(@Param("name") String name, @Param("contactPerson") String contactPerson,
                              @Param("level") String level, @Param("status") Integer status);

    @Select({"<script>",
            "SELECT * FROM customer",
            "<where>",
            "  <if test='name != null and name != \"\"'>AND name LIKE CONCAT('%', #{name}, '%')</if>",
            "  <if test='contactPerson != null and contactPerson != \"\"'>AND contact_person LIKE CONCAT('%', #{contactPerson}, '%')</if>",
            "  <if test='level != null and level != \"\"'>AND level = #{level}</if>",
            "  <if test='status != null'>AND status = #{status}</if>",
            "</where>",
            "ORDER BY create_time DESC",
            "LIMIT #{offset}, #{size}",
            "</script>"})
    List<Customer> selectPage(@Param("name") String name, @Param("contactPerson") String contactPerson,
                              @Param("level") String level, @Param("status") Integer status,
                              @Param("offset") int offset, @Param("size") int size);

    @Select({"<script>",
            "SELECT COUNT(*) FROM customer",
            "<where>",
            "  <if test='name != null and name != \"\"'>AND name LIKE CONCAT('%', #{name}, '%')</if>",
            "  <if test='contactPerson != null and contactPerson != \"\"'>AND contact_person LIKE CONCAT('%', #{contactPerson}, '%')</if>",
            "  <if test='level != null and level != \"\"'>AND level = #{level}</if>",
            "  <if test='status != null'>AND status = #{status}</if>",
            "</where>",
            "</script>"})
    long countPage(@Param("name") String name, @Param("contactPerson") String contactPerson,
                   @Param("level") String level, @Param("status") Integer status);

    @Insert("INSERT INTO customer (name, contact_person, phone, email, address, level, source, remark, status, create_time) " +
            "VALUES (#{name}, #{contactPerson}, #{phone}, #{email}, #{address}, #{level}, #{source}, #{remark}, #{status}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Customer customer);

    @Update("UPDATE customer SET name = #{name}, contact_person = #{contactPerson}, phone = #{phone}, " +
            "email = #{email}, address = #{address}, level = #{level}, source = #{source}, remark = #{remark}, status = #{status} " +
            "WHERE id = #{id}")
    int updateById(Customer customer);

    @Delete("DELETE FROM customer WHERE id = #{id}")
    int deleteById(Long id);
}
