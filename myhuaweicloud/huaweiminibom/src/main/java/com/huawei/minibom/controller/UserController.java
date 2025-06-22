/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.huawei.minibom.controller;

import com.huawei.minibom.result.ReturnCode;
import com.huawei.minibom.result.ReturnResult;
import com.huawei.minibom.service.IUserService;
import com.huawei.minibom.utils.JwtUtils;
import com.huawei.minibom.vo.LoginParamVO;
import com.huawei.minibom.vo.UserRegisterInfoVO;
import com.huawei.innovation.rdm.minibomdatamodel.dto.entity.UserViewDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户管理Controller
 * 提供用户登录、注册等功能
 *
 * @author huawei
 * @since 2025-06-16
 */
@RequestMapping("/user")
@RestController
@CrossOrigin
public class UserController {

    @Autowired
    private IUserService userService;

    /**
     * 用户登录
     * 
     * @param loginParamVO 登录参数
     * @return 登录结果
     */
    @PostMapping("/login")
    public ReturnResult login(@RequestBody LoginParamVO loginParamVO) {
        String name = loginParamVO.getName();
        List<UserViewDTO> userViewDTOS = userService.find(name);
        
        if (userViewDTOS == null || userViewDTOS.isEmpty()) {
            return new ReturnResult(ReturnCode.USER_NOT_EXIST, "用户不存在");
        }

        UserViewDTO userViewDTO = userViewDTOS.get(0);
        // 注意：这里使用getPasswords()而不是getPassword()，因为我们的设计态User实体密码属性是Passwords
        if (!loginParamVO.getPassword().equals(userViewDTO.getPasswords())) {
            return new ReturnResult(ReturnCode.USER_OR_PASSWORD_NOT_MATCH, "用户名或密码错误");
        }

        Map<String, Object> map = new HashMap<>();
        map.put("user", userViewDTO);
        String token = JwtUtils.generateJwt(map);

        return new ReturnResult(ReturnCode.LOGIN_SUCCESS, "登录成功", token);
    }

    /**
     * 用户注册
     * 
     * @param userRegisterInfoVO 注册信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public ReturnResult register(@RequestBody UserRegisterInfoVO userRegisterInfoVO) {
        String name = userRegisterInfoVO.getName();
        List<UserViewDTO> userViewDTOS = userService.find(name);
        
        if (userViewDTOS != null && !userViewDTOS.isEmpty()) {
            return new ReturnResult(ReturnCode.USER_NAME_ALREADY_EXISTED, "用户名已存在");
        }

        // 校验用户名、密码、手机号、邮箱是否符合要求
        if (!userService.validateName(userRegisterInfoVO.getName())) {
            return new ReturnResult(ReturnCode.USER_NAME_INVALID, "用户名不符合规范");
        }

        if (!userService.validatePassword(userRegisterInfoVO.getPassword())) {
            return new ReturnResult(ReturnCode.PASSWORD_INVALID, "密码不符合规范");
        }

        if (!userService.validateEmail(userRegisterInfoVO.getEmail())) {
            return new ReturnResult(ReturnCode.EMAIL_INVALID, "邮箱不符合规范");
        }

        if (!userService.validateTelephone(userRegisterInfoVO.getTelephone())) {
            return new ReturnResult(ReturnCode.TELEPHONE_INVALID, "手机号不符合规范");
        }

        boolean res = userService.save(userRegisterInfoVO);

        if (res) {
            return new ReturnResult(ReturnCode.USER_REGISTER_SUCCESS, "注册成功");
        } else {
            return new ReturnResult(ReturnCode.USER_REGISTER_FAIL, "注册失败");
        }
    }

    /**
     * 获取所有用户列表
     *
     * @return 所有用户列表
     */
    @GetMapping("/list")
    public ReturnResult listAllUsers() {
        try {
            List<UserViewDTO> users = userService.findAll();
            return new ReturnResult(ReturnCode.BUSINESS_OK, "获取用户列表成功", users);
        } catch (Exception e) {
            return new ReturnResult(ReturnCode.BUSINESS_ERR, "获取用户列表失败: " + e.getMessage());
        }
    }

    /**
     * 测试华为云iDME连接
     * 
     * @return 测试结果
     */
    @GetMapping("/test")
    public ReturnResult testConnection() {
        try {
            // 测试查询用户列表，用于验证华为云iDME连接是否正常
            List<UserViewDTO> users = userService.find("test");
            return new ReturnResult(ReturnCode.BUSINESS_OK, "华为云iDME连接正常", users);
        } catch (Exception e) {
            return new ReturnResult(ReturnCode.BUSINESS_ERR, "华为云iDME连接失败: " + e.getMessage());
        }
    }
} 