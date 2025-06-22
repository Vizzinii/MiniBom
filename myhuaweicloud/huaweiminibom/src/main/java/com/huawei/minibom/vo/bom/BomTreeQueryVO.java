package com.huawei.minibom.vo.bom;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * BOM结构树查询请求VO
 */
@Data
public class BomTreeQueryVO {
    /**
     * Part版本ID
     */
    @NotNull(message = "版本ID不能为空")
    private Long versionId;
    
    /**
     * 展开层级，默认无限制
     */
    private Integer levels;
    
    /**
     * 是否包含数量信息，默认true
     */
    private Boolean includeQty = true;
}
