package com.huawei.minibom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 主启动类
 * 
 * @author huawei
 * @since 2025-06-16
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.huawei.innovation", "com.huawei.minibom"})
public class HuaweiminibomApplication {

    public static void main(String[] args) {
        SpringApplication.run(HuaweiminibomApplication.class, args);
    }

} 