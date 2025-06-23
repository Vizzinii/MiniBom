/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.huawei.minibom.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户注册信息VO
 * 
 * @author huawei
 * @since 2025-06-16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterInfoVO {
    /**
     * 用户名
     */
    private String name;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 电话号码
     */
    private String telephone;
} 