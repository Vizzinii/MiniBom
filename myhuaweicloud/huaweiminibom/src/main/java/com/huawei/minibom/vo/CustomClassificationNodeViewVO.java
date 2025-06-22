package com.huawei.minibom.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomClassificationNodeViewVO {
    private String id;
    private String code;
    private String name;
    private String englishName;
    private String description;
    private String parentId;
    private List<CustomClassificationNodeViewVO> children;
} 