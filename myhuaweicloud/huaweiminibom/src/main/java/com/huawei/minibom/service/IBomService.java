package com.huawei.minibom.service;

import com.huawei.minibom.result.ReturnResult;
import com.huawei.minibom.vo.bom.BomLinkCreateVO;
import com.huawei.minibom.vo.bom.BomTreeQueryVO;
import com.huawei.minibom.vo.bom.BomListQueryVO;
import com.huawei.minibom.vo.bom.BomLinkUpdateVO;
import com.huawei.minibom.vo.bom.BomBatchCreateVO;
import com.huawei.minibom.vo.bom.WhereUsedQueryVO;

/**
 * BOM服务接口
 */
public interface IBomService {

    /**
     * 创建BOM关系
     */
    ReturnResult createBomLink(BomLinkCreateVO bomLinkCreateVO);

    /**
     * 查询BOM结构树
     */
    ReturnResult getBomTree(BomTreeQueryVO queryVO);

    /**
     * 查询BOM清单
     */
    ReturnResult getBomList(BomListQueryVO queryVO);

    /**
     * 更新BOM关系
     */
    ReturnResult updateBomLink(Long bomLinkId, BomLinkUpdateVO updateVO);

    /**
     * 删除BOM关系
     */
    ReturnResult deleteBomLink(Long bomLinkId);

    /**
     * 批量创建BOM关系
     */
    ReturnResult batchCreateBomLinks(BomBatchCreateVO batchCreateVO);

    /**
     * 查询Part的父级使用情况
     */
    ReturnResult getWhereUsed(WhereUsedQueryVO queryVO);
}
