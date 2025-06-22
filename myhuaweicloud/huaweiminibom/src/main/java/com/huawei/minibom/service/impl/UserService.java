/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.huawei.minibom.service.impl;

import com.huawei.minibom.service.IUserService;
import com.huawei.minibom.vo.UserRegisterInfoVO;
import com.huawei.innovation.rdm.coresdk.basic.enums.ConditionType;
import com.huawei.innovation.rdm.coresdk.basic.vo.QueryRequestVo;
import com.huawei.innovation.rdm.coresdk.basic.vo.RDMPageVO;
import com.huawei.innovation.rdm.minibomdatamodel.delegator.UserDelegator;
import com.huawei.innovation.rdm.minibomdatamodel.dto.entity.UserCreateDTO;
import com.huawei.innovation.rdm.minibomdatamodel.dto.entity.UserViewDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 用户Service实现类
 * 
 * @author huawei
 * @since 2025-06-16
 */
@Service
public class UserService implements IUserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserDelegator userDelegator;

    // 用户名正则表达式：6-32位字母数字组合
    private static final String NAME_REGEX = "^[a-zA-Z0-9]{6,32}$";
    private static final Pattern NAME_PATTERN = Pattern.compile(NAME_REGEX);

    // 密码正则表达式：8-32位，包含字母、数字、特殊字符
    private static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,32}$";
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);

    // 邮箱正则表达式
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    // 手机号正则表达式：中国大陆手机号
    private static final String TELEPHONE_REGEX = "^1[3-9]\\d{9}$";
    private static final Pattern TELEPHONE_PATTERN = Pattern.compile(TELEPHONE_REGEX);

    @Override
    public List<UserViewDTO> find(String name) {
        logger.info("UserService.find 根据姓名查找用户信息: {}", name);
        QueryRequestVo queryRequestVo = new QueryRequestVo();
        queryRequestVo.addCondition("name", ConditionType.EQUAL, name);
        return userDelegator.find(queryRequestVo, new RDMPageVO(1, 1));
    }

    @Override
    public List<UserViewDTO> findAll() {
        logger.info("UserService.findAll 获取所有用户信息");
        QueryRequestVo queryRequestVo = new QueryRequestVo();
        // 不设置任何查询条件，返回所有用户
        // RDMPageVO(1, 100) 表示获取第一页，每页最多100个用户
        return userDelegator.find(queryRequestVo, new RDMPageVO(1, 100));
    }

    @Override
    public boolean save(UserRegisterInfoVO userRegisterInfoVO) {
        logger.info("UserService.save 保存用户注册信息: {}", userRegisterInfoVO.getName());
        try {
            UserCreateDTO userCreateDTO = new UserCreateDTO();
            userCreateDTO.setName(userRegisterInfoVO.getName());
            // 注意：这里使用setPasswords而不是setPassword，因为我们的设计态User实体密码属性是Passwords
            userCreateDTO.setPasswords(userRegisterInfoVO.getPassword());
            userCreateDTO.setEmail(userRegisterInfoVO.getEmail());
            userCreateDTO.setTelephone(userRegisterInfoVO.getTelephone());
            
            UserViewDTO userViewDTO = userDelegator.create(userCreateDTO);
            return userViewDTO != null;
        } catch (Exception e) {
            logger.error("保存用户信息失败", e);
            return false;
        }
    }

    @Override
    public boolean validateName(String name) {
        if (name == null) {
            return false;
        }
        Matcher matcher = NAME_PATTERN.matcher(name);
        return matcher.matches();
    }

    @Override
    public boolean validatePassword(String password) {
        if (password == null) {
            return false;
        }
        Matcher matcher = PASSWORD_PATTERN.matcher(password);
        return matcher.matches();
    }

    @Override
    public boolean validateEmail(String email) {
        if (email == null) {
            return false;
        }
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        return matcher.matches();
    }

    @Override
    public boolean validateTelephone(String telephone) {
        if (telephone == null) {
            return false;
        }
        Matcher matcher = TELEPHONE_PATTERN.matcher(telephone);
        return matcher.matches();
    }
} 