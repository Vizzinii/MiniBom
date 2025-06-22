package com.huawei.minibom.vo.bom;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

/**
 * BOM关系批量创建请求VO
 */
@Data
public class BomBatchCreateVO {
    /**
     * 源版本ID
     */
    @NotNull(message = "源版本ID不能为空")
    private Long sourceVersionId;
    
    /**
     * BOM项列表
     */
    @Valid
    @NotNull(message = "BOM项列表不能为空")
    private List<BomItem> bomItems;
    
    @Data
    public static class BomItem {
        /**
         * 目标主对象ID
         */
        @NotNull(message = "目标主对象ID不能为空")
        private Long targetMasterId;
        
        /**
         * 使用数量，浮点型
         */
        @NotNull(message = "使用数量不能为空")
        @Positive(message = "使用数量必须大于0")
        private Double quantity;
        
        /**
         * 行号，长整型（可选）
         */
        private Long sequenceNumber;
        
        /**
         * 位号名称，文本类型，最大长度500（可选）
         */
        private String referenceDesignator;
    }
}
