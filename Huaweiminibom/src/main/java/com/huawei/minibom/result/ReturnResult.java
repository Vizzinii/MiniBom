/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.huawei.minibom.result;

/**
 * 返回结果封装类
 * 
 * @author huawei
 * @since 2025-06-16
 */
public class ReturnResult {
    private Integer code;
    private String message;
    private Object data;

    public ReturnResult() {
    }

    public ReturnResult(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public ReturnResult(Integer code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ReturnResult{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
} 