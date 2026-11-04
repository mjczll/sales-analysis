package com.slxy.utils;

import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * 通用字段格式校验工具（后端兜底校验，防止绕过前端直接入库脏数据）
 */
public final class ValidatorUtils {

    /** 中国大陆手机号：1 开头，第二位 3-9，共 11 位 */
    private static final Pattern PHONE = Pattern.compile("^1[3-9]\\d{9}$");

    /** 标准邮箱格式 */
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private ValidatorUtils() {
    }

    /**
     * 校验手机号格式（空值不校验，视为合法，由必填校验另行控制）
     */
    public static boolean isPhoneValid(String phone) {
        if (!StringUtils.hasText(phone)) {
            return true;
        }
        return PHONE.matcher(phone.trim()).matches();
    }

    /**
     * 校验邮箱格式（空值不校验，视为合法）
     */
    public static boolean isEmailValid(String email) {
        if (!StringUtils.hasText(email)) {
            return true;
        }
        return EMAIL.matcher(email.trim()).matches();
    }
}
