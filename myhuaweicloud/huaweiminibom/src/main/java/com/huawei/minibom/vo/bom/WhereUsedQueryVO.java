package com.huawei.minibom.vo.bom;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Part父级使用情况查询请求VO
 */
@Data
public class WhereUsedQueryVO {
    /**
     * Part主对象ID
     */
    @NotNull(message = "主对象ID不能为空")
    private Long masterId;
    
    /**
     * 是否递归查询，默认false
     */
    private Boolean recursive = false;
}
