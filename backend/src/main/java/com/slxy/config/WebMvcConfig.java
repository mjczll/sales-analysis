package com.slxy.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final FileUploadConfig fileUploadConfig;

    public WebMvcConfig(FileUploadConfig fileUploadConfig) {
        this.fileUploadConfig = fileUploadConfig;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 相对路径解析为绝对路径，保证 file: 资源定位正确（相对路径需相对于后端运行工作目录）
        String uploadPath = new File(fileUploadConfig.getPath()).getAbsolutePath();
        // 配置静态资源处理器，用于访问上传的文件
        registry.addResourceHandler(fileUploadConfig.getPrefix() + "**")
                .addResourceLocations("file:" + uploadPath + File.separator);
        // 兼容前端通过 /api 代理访问上传文件
        registry.addResourceHandler("/api" + fileUploadConfig.getPrefix() + "**")
                .addResourceLocations("file:" + uploadPath + File.separator);
    }
}