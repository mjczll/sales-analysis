package com.slxy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 企业级销售业绩分析系统 —— 应用启动类
 */
@SpringBootApplication
@MapperScan("com.slxy.mapper")
public class SalesAnalysisApplication {

    public static void main(String[] args) {
        SpringApplication.run(SalesAnalysisApplication.class, args);
    }
}
