package com.huawei.minibom.controller;

import com.huawei.minibom.result.ReturnResult;
import com.huawei.minibom.service.IPartService;
import com.huawei.minibom.vo.PartCreateFormVO;
import com.huawei.minibom.vo.PartQueryParamVO;
import com.huawei.minibom.vo.PartUpdateFormVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Part管理控制器
 */
@RestController
@RequestMapping("/parts")
@CrossOrigin
public class PartController {
    
    @Autowired
    private IPartService partService;
    
    /**
     * 创建Part
     */
    @PostMapping
    public ReturnResult createPart(@Valid @RequestBody PartCreateFormVO partCreateFormVO) {
        return partService.createPart(partCreateFormVO);
    }
    
    /**
     * 查询Part列表
     */
    @GetMapping
    public ReturnResult queryParts(PartQueryParamVO partQueryParamVO) {
        return partService.queryParts(partQueryParamVO);
    }
    
    /**
     * 获取Part详情
     */
    @GetMapping("/{masterId}")
    public ReturnResult getPartDetail(@PathVariable Long masterId) {
        return partService.getPartDetail(masterId);
    }
    
    /**
     * 更新Part
     */
    @PutMapping("/{masterId}")
    public ReturnResult updatePart(@PathVariable Long masterId, @Valid @RequestBody PartUpdateFormVO partUpdateFormVO) {
        partUpdateFormVO.setMasterId(masterId);
        return partService.updatePart(partUpdateFormVO);
    }
    
    /**
     * 删除Part
     */
    @DeleteMapping("/{masterId}")
    public ReturnResult deletePart(@PathVariable Long masterId) {
        return partService.deletePart(masterId);
    }
    
    /**
     * 检出Part
     */
    @PostMapping("/{masterId}/checkout")
    public ReturnResult checkoutPart(@PathVariable Long masterId) {
        return partService.checkoutPart(masterId);
    }
    
    /**
     * 撤销检出
     */
    @PostMapping("/{masterId}/undo-checkout")
    public ReturnResult undoCheckout(@PathVariable Long masterId) {
        return partService.undoCheckout(masterId);
    }
    
    /**
     * 检入Part
     */
    @PostMapping("/{masterId}/checkin")
    public ReturnResult checkinPart(@PathVariable Long masterId, @RequestBody(required = false) String iterationNote) {
        return partService.checkinPart(masterId, iterationNote);
    }
    
    /**
     * 获取Part版本历史
     */
    @GetMapping("/{masterId}/versions")
    public ReturnResult getPartVersions(@PathVariable Long masterId) {
        return partService.getPartVersions(masterId);
    }
    
    /**
     * 获取指定版本详情
     */
    @GetMapping("/versions/{versionId}")
    public ReturnResult getVersionDetail(@PathVariable Long versionId) {
        return partService.getVersionDetail(versionId);
    }
    
    /**
     * 获取枚举值
     */
    @GetMapping("/enums/{enumType}")
    public ReturnResult getEnumValues(@PathVariable String enumType) {
        return partService.getEnumValues(enumType);
    }
} 