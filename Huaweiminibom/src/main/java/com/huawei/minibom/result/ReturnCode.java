/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.huawei.minibom.result;

/**
 * 返回状态码常量
 * 
 * @author huawei
 * @since 2025-06-16
 */
public class ReturnCode {
    
    // 注册部分（11X）
    public static final Integer USER_NAME_ALREADY_EXISTED = 111; // 用户名已存在
    public static final Integer USER_NAME_INVALID = 112; // 用户名非法
    public static final Integer PASSWORD_INVALID = 113; // 密码非法
    public static final Integer EMAIL_INVALID = 114; // 邮箱非法
    public static final Integer TELEPHONE_INVALID = 115; // 手机号非法
    public static final Integer USER_REGISTER_SUCCESS = 116; // 注册成功
    public static final Integer USER_REGISTER_FAIL = 117; // 注册失败

    // 登录部分（12X）
    public static final Integer USER_NOT_EXIST = 121; // 用户名不存在
    public static final Integer USER_OR_PASSWORD_NOT_MATCH = 122; // 用户名和密码不匹配
    public static final Integer LOGIN_SUCCESS = 123; // 登录成功
    public static final Integer LOGIN_FAIL = 124; // 登录失败

    // 基础操作结果码
    public static final Integer INSERT_OK = 20011;
    public static final Integer DELETE_OK = 20021;
    public static final Integer UPDATE_OK = 20031;
    public static final Integer GET_OK = 20041;

    public static final Integer INSERT_ERR = 20010;
    public static final Integer DELETE_ERR = 20020;
    public static final Integer UPDATE_ERR = 20030;
    public static final Integer GET_ERR = 20040;

    // 参数错误码 (4XX)
    public static final Integer PARAM_IS_INVALID = 40001;
    public static final Integer PARAM_TYPE_ERROR = 40002;
    public static final Integer TARGET_NOT_EXIST = 40401; // 目标不存在

    // 系统错误码
    public static final Integer SYSTEM_ERR = 50001;
    public static final Integer SYSTEM_TIMEOUT_ERR = 50002;
    public static final Integer SYSTEM_UNKNOWN_ERR = 59999;

    // 业务错误码
    public static final Integer BUSINESS_ERR = 60002;
    public static final Integer BUSINESS_OK = 60003;
} 