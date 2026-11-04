package com.slxy.mapper;

import com.slxy.entity.Role;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RoleMapper {

    @Select("SELECT * FROM sys_role WHERE id = #{id}")
    Role selectById(Long id);

    @Select("SELECT * FROM sys_role ORDER BY id ASC")
    List<Role> selectAll();

    @Select({"<script>",
            "SELECT * FROM sys_role",
            "<where>",
            "  <if test='keyword != null and keyword != \"\"'>",
            "    AND (role_name LIKE CONCAT('%', #{keyword}, '%') OR role_code LIKE CONCAT('%', #{keyword}, '%'))",
            "  </if>",
            "</where>",
            "ORDER BY id ASC",
            "</script>"})
    List<Role> selectList(@Param("keyword") String keyword);

    @Select({"<script>",
            "SELECT * FROM sys_role",
            "<where>",
            "  <if test='keyword != null and keyword != \"\"'>",
            "    AND (role_name LIKE CONCAT('%', #{keyword}, '%') OR role_code LIKE CONCAT('%', #{keyword}, '%'))",
            "  </if>",
            "</where>",
            "ORDER BY id ASC",
            "LIMIT #{offset}, #{size}",
            "</script>"})
    List<Role> selectPage(@Param("keyword") String keyword, @Param("offset") int offset, @Param("size") int size);

    @Select({"<script>",
            "SELECT COUNT(*) FROM sys_role",
            "<where>",
            "  <if test='keyword != null and keyword != \"\"'>",
            "    AND (role_name LIKE CONCAT('%', #{keyword}, '%') OR role_code LIKE CONCAT('%', #{keyword}, '%'))",
            "  </if>",
            "</where>",
            "</script>"})
    long countPage(@Param("keyword") String keyword);

    @Insert("INSERT INTO sys_role (role_name, role_code, description, create_time) " +
            "VALUES (#{roleName}, #{roleCode}, #{description}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Role role);

    @Update("UPDATE sys_role SET role_name = #{roleName}, role_code = #{roleCode}, description = #{description} WHERE id = #{id}")
    int updateById(Role role);

    @Delete("DELETE FROM sys_role WHERE id = #{id}")
    int deleteById(Long id);
}
