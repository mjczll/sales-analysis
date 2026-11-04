package com.slxy.mapper;

import com.slxy.entity.Category;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CategoryMapper {

    @Select("SELECT * FROM category WHERE id = #{id}")
    Category selectById(Long id);

    @Select({"<script>",
            "SELECT * FROM category WHERE id IN",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
            "</script>"})
    List<Category> selectByIds(@Param("ids") List<Long> ids);

    @Select({"<script>",
            "SELECT * FROM category",
            "<where>",
            "  <if test='name != null and name != \"\"'>AND name LIKE CONCAT('%', #{name}, '%')</if>",
            "</where>",
            "ORDER BY id ASC",
            "</script>"})
    List<Category> selectList(@Param("name") String name);

    @Select({"<script>",
            "SELECT * FROM category",
            "<where>",
            "  <if test='name != null and name != \"\"'>AND name LIKE CONCAT('%', #{name}, '%')</if>",
            "</where>",
            "ORDER BY id ASC",
            "LIMIT #{offset}, #{size}",
            "</script>"})
    List<Category> selectPage(@Param("name") String name, @Param("offset") int offset, @Param("size") int size);

    @Select({"<script>",
            "SELECT COUNT(*) FROM category",
            "<where>",
            "  <if test='name != null and name != \"\"'>AND name LIKE CONCAT('%', #{name}, '%')</if>",
            "</where>",
            "</script>"})
    long countPage(@Param("name") String name);

    @Insert("INSERT INTO category (name, description, create_time) VALUES (#{name}, #{description}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Category category);

    @Update("UPDATE category SET name = #{name}, description = #{description} WHERE id = #{id}")
    int updateById(Category category);

    @Delete("DELETE FROM category WHERE id = #{id}")
    int deleteById(Long id);
}
