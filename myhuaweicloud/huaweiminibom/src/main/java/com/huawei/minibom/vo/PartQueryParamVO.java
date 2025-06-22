package com.huawei.minibom.vo;

import lombok.Data;

/**
 * Part查询参数VO
 */
@Data
public class PartQueryParamVO {
    
    /**
     * 零件名称（模糊查询）
     */
    private String name;
    
    /**
     * 零件编码（精确查询）
     */
    private String number;
    
    /**
     * 零件来源
     */
    private String source;
    
    /**
     * 装配模式
     */
    private String partType;
    
    /**
     * 工作状态
     */
    private String workingState;
    
    /**
     * 页码，默认1
     */
    private Integer page = 1;
    
    /**
     * 每页大小，默认20
     */
    private Integer size = 20;
} 