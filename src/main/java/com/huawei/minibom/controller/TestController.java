/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.huawei.minibom.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试Controller
 * 用于验证Spring Boot基础功能是否正常
 *
 * @author huawei
 * @since 2025-06-16
 */
@RestController
@RequestMapping("/test")
public class TestController {

    /**
     * 简单的健康检查接口
     *
     * @return 状态信息
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Spring Boot 应用运行正常！");
    }

    /**
     * 获取项目信息
     *
     * @return 项目信息
     */
    @GetMapping("/info")
    public ResponseEntity<String> info() {
        return ResponseEntity.ok("华为MiniBOM系统 - 版本 1.0.0");
    }
} 