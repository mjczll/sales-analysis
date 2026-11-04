package com.slxy.mapper;

import com.slxy.entity.User;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM sys_user WHERE id = #{id}")
    User selectById(Long id);

    @Select({"<script>",
            "SELECT * FROM sys_user WHERE id IN",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
            "</script>"})
    List<User> selectByIds(@Param("ids") List<Long> ids);

    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    User selectByUsername(String username);

    @Select({"<script>",
            "SELECT * FROM sys_user",
            "<where>",
            "  <if test='username != null and username != \"\"'>AND username LIKE CONCAT('%', #{username}, '%')</if>",
            "  <if test='realName != null and realName != \"\"'>AND real_name LIKE CONCAT('%', #{realName}, '%')</if>",
            "  <if test='roleId != null'>AND role_id = #{roleId}</if>",
            "  <if test='status != null'>AND status = #{status}</if>",
            "</where>",
            "ORDER BY create_time DESC",
            "</script>"})
    List<User> selectList(@Param("username") String username, @Param("realName") String realName,
                          @Param("roleId") Long roleId, @Param("status") Integer status);

    @Select({"<script>",
            "SELECT * FROM sys_user",
            "<where>",
            "  <if test='username != null and username != \"\"'>AND username LIKE CONCAT('%', #{username}, '%')</if>",
            "  <if test='realName != null and realName != \"\"'>AND real_name LIKE CONCAT('%', #{realName}, '%')</if>",
            "  <if test='roleId != null'>AND role_id = #{roleId}</if>",
            "  <if test='status != null'>AND status = #{status}</if>",
            "</where>",
            "ORDER BY create_time DESC",
            "LIMIT #{offset}, #{size}",
            "</script>"})
    List<User> selectPage(@Param("username") String username, @Param("realName") String realName,
                          @Param("roleId") Long roleId, @Param("status") Integer status,
                          @Param("offset") int offset, @Param("size") int size);

    @Select({"<script>",
            "SELECT COUNT(*) FROM sys_user",
            "<where>",
            "  <if test='username != null and username != \"\"'>AND username LIKE CONCAT('%', #{username}, '%')</if>",
            "  <if test='realName != null and realName != \"\"'>AND real_name LIKE CONCAT('%', #{realName}, '%')</if>",
            "  <if test='roleId != null'>AND role_id = #{roleId}</if>",
            "  <if test='status != null'>AND status = #{status}</if>",
            "</where>",
            "</script>"})
    long countPage(@Param("username") String username, @Param("realName") String realName,
                   @Param("roleId") Long roleId, @Param("status") Integer status);

    /** 员工列表（keyword 模糊匹配 username/realName/phone） */
    @Select({"<script>",
            "SELECT * FROM sys_user",
            "<where>",
            "  <if test='keyword != null and keyword != \"\"'>",
            "    AND (username LIKE CONCAT('%', #{keyword}, '%') OR real_name LIKE CONCAT('%', #{keyword}, '%') OR phone LIKE CONCAT('%', #{keyword}, '%'))",
            "  </if>",
            "  <if test='roleId != null'>AND role_id = #{roleId}</if>",
            "  <if test='status != null'>AND status = #{status}</if>",
            "</where>",
            "ORDER BY create_time DESC",
            "LIMIT #{offset}, #{size}",
            "</script>"})
    List<User> selectEmployeePage(@Param("keyword") String keyword, @Param("roleId") Long roleId,
                                  @Param("status") Integer status, @Param("offset") int offset, @Param("size") int size);

    @Select({"<script>",
            "SELECT COUNT(*) FROM sys_user",
            "<where>",
            "  <if test='keyword != null and keyword != \"\"'>",
            "    AND (username LIKE CONCAT('%', #{keyword}, '%') OR real_name LIKE CONCAT('%', #{keyword}, '%') OR phone LIKE CONCAT('%', #{keyword}, '%'))",
            "  </if>",
            "  <if test='roleId != null'>AND role_id = #{roleId}</if>",
            "  <if test='status != null'>AND status = #{status}</if>",
            "</where>",
            "</script>"})
    long countEmployee(@Param("keyword") String keyword, @Param("roleId") Long roleId, @Param("status") Integer status);

    @Insert("INSERT INTO sys_user (username, password, real_name, phone, email, avatar, status, role_id, create_time, update_time) " +
            "VALUES (#{username}, #{password}, #{realName}, #{phone}, #{email}, #{avatar}, #{status}, #{roleId}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update({"<script>",
            "UPDATE sys_user",
            "<set>",
            "  <if test='username != null'>username = #{username},</if>",
            "  <if test='password != null'>password = #{password},</if>",
            "  <if test='realName != null'>real_name = #{realName},</if>",
            "  <if test='phone != null'>phone = #{phone},</if>",
            "  <if test='email != null'>email = #{email},</if>",
            "  <if test='avatar != null'>avatar = #{avatar},</if>",
            "  <if test='status != null'>status = #{status},</if>",
            "  <if test='roleId != null'>role_id = #{roleId},</if>",
            "  update_time = NOW()",
            "</set>",
            "WHERE id = #{id}",
            "</script>"})
    int updateById(User user);

    @Delete("DELETE FROM sys_user WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT COUNT(*) FROM sys_user WHERE role_id = #{roleId}")
    long countByRoleId(Long roleId);

    @Select("SELECT COUNT(*) FROM sys_user WHERE create_time >= #{since}")
    long countSince(LocalDateTime since);
}
