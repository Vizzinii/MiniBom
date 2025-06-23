package com.huawei.minibom.vo;

import lombok.Data;

/**
 * 属性更新表单VO
 *
 * @author huawei
 * @since 2025-06-17
 */
@Data
public class AttributeUpdateFormVO {
    /**
     * 要更新的属性ID
     */
    private Long id;

    /**
     * 中文描述
     */
    private String description;

    /**
     * 英文描述
     */
    private String descriptionEn;
} 