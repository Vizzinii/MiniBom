package com.huawei.minibom.vo;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Part更新表单VO
 */
@Data
public class PartUpdateFormVO {
    
    @NotNull(message = "masterId不能为空")
    private Long masterId;
    
    private String name;
    
    private String description;
    
    private String source;
    
    private String partType;
    
    /**
     * 分类属性，JSON格式
     */
    private JSONObject clsAttrs;
    
    /**
     * 迭代备注
     */
    private String iterationNote;
} 