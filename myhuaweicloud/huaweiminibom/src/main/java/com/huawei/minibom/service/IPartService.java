package com.huawei.minibom.service;

import com.huawei.minibom.result.ReturnResult;
import com.huawei.minibom.vo.PartCreateFormVO;
import com.huawei.minibom.vo.PartQueryParamVO;
import com.huawei.minibom.vo.PartUpdateFormVO;

/**
 * Part管理服务接口
 */
public interface IPartService {
    
    /**
     * 创建Part
     */
    ReturnResult createPart(PartCreateFormVO partCreateFormVO);
    
    /**
     * 查询Part列表
     */
    ReturnResult queryParts(PartQueryParamVO partQueryParamVO);
    
    /**
     * 获取Part详情
     */
    ReturnResult getPartDetail(Long masterId);
    
    /**
     * 更新Part
     */
    ReturnResult updatePart(PartUpdateFormVO partUpdateFormVO);
    
    /**
     * 删除Part
     */
    ReturnResult deletePart(Long masterId);
    
    /**
     * 检出Part
     */
    ReturnResult checkoutPart(Long masterId);
    
    /**
     * 撤销检出
     */
    ReturnResult undoCheckout(Long masterId);
    
    /**
     * 检入Part
     */
    ReturnResult checkinPart(Long masterId, String iterationNote);
    
    /**
     * 获取Part版本历史
     */
    ReturnResult getPartVersions(Long masterId);
    
    /**
     * 获取指定版本详情
     */
    ReturnResult getVersionDetail(Long versionId);
    
    /**
     * 获取枚举值
     */
    ReturnResult getEnumValues(String enumType);
} 