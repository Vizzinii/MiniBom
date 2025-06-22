package com.huawei.minibom.controller;

import com.huawei.minibom.result.ReturnResult;
import com.huawei.minibom.service.IBomService;
import com.huawei.minibom.vo.bom.BomLinkCreateVO;
import com.huawei.minibom.vo.bom.BomTreeQueryVO;
import com.huawei.minibom.vo.bom.BomListQueryVO;
import com.huawei.minibom.vo.bom.BomLinkUpdateVO;
import com.huawei.minibom.vo.bom.BomBatchCreateVO;
import com.huawei.minibom.vo.bom.WhereUsedQueryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * BOM管理控制器
 */
@RestController
@RequestMapping
public class BomController {

    @Autowired
    private IBomService bomService;

    /**
     * 创建BOM关系
     */
    @PostMapping("/bom-links")
    public ReturnResult createBomLink(@Valid @RequestBody BomLinkCreateVO bomLinkCreateVO) {
        return bomService.createBomLink(bomLinkCreateVO);
    }

    /**
     * 查询BOM结构树
     */
    @GetMapping("/parts/{versionId}/bom-tree")
    public ReturnResult getBomTree(@PathVariable Long versionId,
                                   @RequestParam(required = false) Integer levels,
                                   @RequestParam(required = false, defaultValue = "true") Boolean includeQty) {
        BomTreeQueryVO queryVO = new BomTreeQueryVO();
        queryVO.setVersionId(versionId);
        queryVO.setLevels(levels);
        queryVO.setIncludeQty(includeQty);
        return bomService.getBomTree(queryVO);
    }

    /**
     * 查询BOM清单
     */
    @GetMapping("/parts/{versionId}/bom-list")
    public ReturnResult getBomList(@PathVariable Long versionId,
                                   @RequestParam(required = false, defaultValue = "true") Boolean recursive,
                                   @RequestParam(required = false, defaultValue = "json") String format) {
        BomListQueryVO queryVO = new BomListQueryVO();
        queryVO.setVersionId(versionId);
        queryVO.setRecursive(recursive);
        queryVO.setFormat(format);
        return bomService.getBomList(queryVO);
    }

    /**
     * 更新BOM关系
     */
    @PutMapping("/bom-links/{bomLinkId}")
    public ReturnResult updateBomLink(@PathVariable Long bomLinkId,
                                      @Valid @RequestBody BomLinkUpdateVO updateVO) {
        return bomService.updateBomLink(bomLinkId, updateVO);
    }

    /**
     * 删除BOM关系
     */
    @DeleteMapping("/bom-links/{bomLinkId}")
    public ReturnResult deleteBomLink(@PathVariable Long bomLinkId) {
        return bomService.deleteBomLink(bomLinkId);
    }

    /**
     * 批量创建BOM关系
     */
    @PostMapping("/bom-links/batch")
    public ReturnResult batchCreateBomLinks(@Valid @RequestBody BomBatchCreateVO batchCreateVO) {
        return bomService.batchCreateBomLinks(batchCreateVO);
    }

    /**
     * 查询Part的父级使用情况
     */
    @GetMapping("/parts/{masterId}/where-used")
    public ReturnResult getWhereUsed(@PathVariable Long masterId,
                                     @RequestParam(required = false, defaultValue = "false") Boolean recursive) {
        WhereUsedQueryVO queryVO = new WhereUsedQueryVO();
        queryVO.setMasterId(masterId);
        queryVO.setRecursive(recursive);
        return bomService.getWhereUsed(queryVO);
    }
}
