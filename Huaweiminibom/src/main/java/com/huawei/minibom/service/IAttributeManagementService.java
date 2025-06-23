package com.huawei.minibom.service;

import com.huawei.minibom.result.ReturnResult;
import com.huawei.minibom.vo.AttributeCreateFormVO;
import com.huawei.minibom.vo.AttributeQueryViewVO;
import com.huawei.minibom.vo.AttributeUpdateFormVO;

import java.util.List;

/**
 * 属性管理服务接口
 *
 * @author huawei
 * @since 2025-06-17
 */
public interface IAttributeManagementService {
    /**
     * 根据中/英文名称模糊查询属性
     *
     * @param name 查询名称
     * @return 属性列表
     */
    List<AttributeQueryViewVO> queryAttributeByName(String name);

    /**
     * 创建属性
     *
     * @param createForm 创建表单
     * @return 创建结果
     */
    ReturnResult create(AttributeCreateFormVO createForm);

    /**
     * 更新属性
     *
     * @param formVO 更新表单
     * @return 更新结果
     */
    ReturnResult update(AttributeUpdateFormVO formVO);

    /**
     * 根据ID批量删除属性
     *
     * @param ids 属性ID列表
     * @return 删除结果
     */
    ReturnResult batchDeleteByIds(List<Long> ids);
} 