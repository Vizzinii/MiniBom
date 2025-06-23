package com.huawei.minibom.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class ClassificationCreateFormVO {

    @NotEmpty(message = "编码不能为空")
    private String code;

    @NotEmpty(message = "名称不能为空")
    private String name;

    private String englishName;

    private String description;

    private String parentId;
} 