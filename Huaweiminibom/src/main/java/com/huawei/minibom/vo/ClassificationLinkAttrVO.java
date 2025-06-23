package com.huawei.minibom.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Set;

@Data
public class ClassificationLinkAttrVO {
    @NotEmpty(message = "分类ID不能为空")
    private String classificationId;

    @NotEmpty(message = "属性ID列表不能为空")
    private Set<String> attributeIds;
} 