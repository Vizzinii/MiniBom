/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.huawei.minibom.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录参数VO
 * 
 * @author huawei
 * @since 2025-06-16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginParamVO {
    /**
     * 用户名
     */
    private String name;
    
    /**
     * 密码
     */
    private String password;
} 