/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.huawei.minibom.controller;

import com.huawei.minibom.result.ReturnCode;
import com.huawei.minibom.result.ReturnResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

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

    /**
     * 测试验证分类ID和属性名称
     */
    @GetMapping("/verify-classification/{classificationId}")
    public ReturnResult verifyClassification(@PathVariable String classificationId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 验证分类ID是否存在
            result.put("classificationId", classificationId);
            
            // 调用分类管理服务查询分类详情
            // 这里需要注入分类管理服务
            result.put("status", "需要注入ClassificationManagementService来验证");
            result.put("message", "请使用 GET /classifications/" + classificationId + "/details 来查看该分类的详细信息和关联属性");
            
            return new ReturnResult(ReturnCode.GET_OK, "验证完成", result);
        } catch (Exception e) {
            result.put("error", e.getMessage());
            return new ReturnResult(ReturnCode.GET_ERR, "验证失败", result);
        }
    }
} 