package com.huawei.minibom.vo.bom;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * BOM清单查询请求VO
 */
@Data
public class BomListQueryVO {
    /**
     * Part版本ID
     */
    @NotNull(message = "版本ID不能为空")
    private Long versionId;
    
    /**
     * 是否递归查询所有层级，默认true
     */
    private Boolean recursive = true;
    
    /**
     * 输出格式，csv/json，默认json
     */
    private String format = "json";
}
