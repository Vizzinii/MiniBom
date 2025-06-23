package com.huawei.minibom.vo;

import lombok.Data;

import java.util.List;

@Data
public class ClassificationViewVO {

    private String id;
    private String name;
    private String code;
    private List<AttributeQueryViewVO> attributes;
} 