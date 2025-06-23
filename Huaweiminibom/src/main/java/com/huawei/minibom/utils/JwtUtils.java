/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.huawei.minibom.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.Map;

/**
 * JWT工具类
 * 
 * @author huawei
 * @since 2025-06-16
 */
public class JwtUtils {

    // 签名密钥
    private static final String signKey = "huaweiminibom2025";
    
    // 过期时间：24小时
    private static final Long expire = 24 * 60 * 60 * 1000L;

    /**
     * 生成JWT令牌
     * 
     * @param claims 数据声明
     * @return JWT令牌字符串
     */
    public static String generateJwt(Map<String, Object> claims) {
        return Jwts.builder()
                .addClaims(claims)
                .signWith(SignatureAlgorithm.HS256, signKey)
                .setExpiration(new Date(System.currentTimeMillis() + expire))
                .compact();
    }

    /**
     * 解析JWT令牌
     * 
     * @param jwt JWT令牌字符串
     * @return 数据声明
     */
    public static Claims parseJWT(String jwt) {
        return Jwts.parser()
                .setSigningKey(signKey)
                .parseClaimsJws(jwt)
                .getBody();
    }
} 