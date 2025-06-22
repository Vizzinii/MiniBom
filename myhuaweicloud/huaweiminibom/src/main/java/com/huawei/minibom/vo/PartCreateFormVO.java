package com.huawei.minibom.vo;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Part创建表单VO
 */
@Data
public class PartCreateFormVO {
    
    @Valid
    @NotNull(message = "master对象不能为空")
    private MasterFormVO master;
    
    @Valid
    @NotNull(message = "version对象不能为空")
    private VersionFormVO version;
    
    @Data
    public static class MasterFormVO {
        @NotBlank(message = "零件名称不能为空")
        private String name;
        private Boolean endPart = false;
        private Boolean phantomPart = false;
    }
    
    @Data
    public static class VersionFormVO {
        private String name;
        private String description;
        @NotNull(message = "零件来源不能为空")
        private String source;
        @NotNull(message = "装配模式不能为空")
        private String partType;
        private JSONObject clsAttrs;
        private String iterationNote;
        private String classificationId; // 新增：分类ID

        public String getClassificationId() {
            return classificationId;
        }

        public void setClassificationId(String classificationId) {
            this.classificationId = classificationId;
        }
    }
} 