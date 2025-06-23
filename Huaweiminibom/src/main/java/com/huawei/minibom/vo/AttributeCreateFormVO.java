package com.huawei.minibom.vo;

import lombok.Data;

/**
 * 属性创建表单VO
 *
 * @author huawei
 * @since 2025-06-17
 */
@Data
public class AttributeCreateFormVO {

    /**
     * 中文名称
     */
    private String name;

    /**
     * 英文名称
     */
    private String nameEn;

    /**
     * 类别 (似乎未使用，但保留以作参考)
     */
    private String category;

    /**
     * 数据类型 (STRING, DECIMAL, DATE, BOOLEAN etc.)
     */
    private String type;

    /**
     * 中文描述
     */
    private String description;

    /**
     * 英文描述
     */
    private String descriptionEn;

    /**
     * 禁用标志
     */
    private Boolean disableFlag;
} 