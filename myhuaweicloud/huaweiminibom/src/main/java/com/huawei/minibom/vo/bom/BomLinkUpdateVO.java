package com.huawei.minibom.vo.bom;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * BOM关系更新请求VO
 */
@Data
public class BomLinkUpdateVO {
    
    @Valid
    @NotNull(message = "bomLink对象不能为空")
    private BomLink bomLink;
    
    private BomUsesOccurrence bomUsesOccurrence;
    
    @Data
    public static class BomLink {
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
    }
    
    @Data
    public static class BomUsesOccurrence {
        /**
         * 位号名称，文本类型，最大长度500（可选）
         */
        private String referenceDesignator;
    }
}
