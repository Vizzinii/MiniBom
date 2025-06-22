/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.huawei.minibom.service;

import com.huawei.minibom.vo.UserRegisterInfoVO;
import com.huawei.innovation.rdm.minibomdatamodel.dto.entity.UserViewDTO;

import java.util.List;

/**
 * 用户Service接口
 * 
 * @author huawei
 * @since 2025-06-16
 */
public interface IUserService {
    
    /**
     * 根据用户名查找用户
     * 
     * @param name 用户名
     * @return 用户列表
     */
    List<UserViewDTO> find(String name);
    
    /**
     * 保存用户注册信息
     * 
     * @param userRegisterInfoVO 用户注册信息
     * @return 是否保存成功
     */
    boolean save(UserRegisterInfoVO userRegisterInfoVO);

    /**
     * 获取所有用户
     * 
     * @return 所有用户的列表
     */
    List<UserViewDTO> findAll();

    /**
     * 验证用户名格式
     * 
     * @param name 用户名
     * @return 是否合法
     */
    boolean validateName(String name);

    /**
     * 验证密码格式
     * 
     * @param password 密码
     * @return 是否合法
     */
    boolean validatePassword(String password);

    /**
     * 验证邮箱格式
     * 
     * @param email 邮箱
     * @return 是否合法
     */
    boolean validateEmail(String email);
    
    /**
     * 验证电话号码格式
     * 
     * @param telephone 电话号码
     * @return 是否合法
     */
    boolean validateTelephone(String telephone);
} 