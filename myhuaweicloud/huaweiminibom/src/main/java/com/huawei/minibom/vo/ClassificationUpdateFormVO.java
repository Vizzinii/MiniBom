package com.huawei.minibom.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class ClassificationUpdateFormVO {

    @NotEmpty(message = "ID不能为空")
    private String id;

    @NotEmpty(message = "名称不能为空")
    private String name;

    private String englishName;

    private String description;
} 