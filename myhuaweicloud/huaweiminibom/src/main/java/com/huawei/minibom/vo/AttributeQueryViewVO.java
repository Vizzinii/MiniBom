package com.huawei.minibom.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;

/**
 * 属性查询视图VO
 *
 * @author huawei
 * @since 2025-06-17
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttributeQueryViewVO {

    private String id;
    private String name;
    private String nameEn;
    private String type;
    private Boolean disableFlag;
    private String description;
    private String descriptionEn;
    private String creator;
    private Date createTime;
    private String modifier;
    private Date lastModifyTime;
    private CustomFolder folder;

    /**
     * 自定义内部类，用于简化Folder信息，避免敏感信息泄露
     */
    @Data
    public static class CustomFolder {
        private String name;
        private String nameEn;
        private String businessCode;
    }
} 