package com.huawei.minibom.controller;

import com.huawei.minibom.result.ReturnCode;
import com.huawei.minibom.result.ReturnResult;
import com.huawei.minibom.service.IAttributeManagementService;
import com.huawei.minibom.vo.AttributeCreateFormVO;
import com.huawei.minibom.vo.AttributeQueryViewVO;
import com.huawei.minibom.vo.AttributeUpdateFormVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 属性管理Controller
 *
 * @author huawei
 * @since 2025-06-17
 */
@RestController
@RequestMapping("/attribute")
@CrossOrigin
public class AttributeManagementController {
    @Autowired
    private IAttributeManagementService attributeManagementService;

    /**
     * 根据名称查询属性
     *
     * @param name 查询关键字 (可选)
     * @return 属性列表
     */
    @GetMapping("/query")
    public ReturnResult queryAttribute(@RequestParam(required = false) String name) {
        List<AttributeQueryViewVO> result = attributeManagementService.queryAttributeByName(name);
        return new ReturnResult(ReturnCode.GET_OK, "查询属性成功", result);
    }

    /**
     * 创建属性
     *
     * @param createForm 创建表单
     * @return 创建结果
     */
    @PostMapping("/create")
    public ReturnResult create(@RequestBody AttributeCreateFormVO createForm) {
        return attributeManagementService.create(createForm);
    }

    /**
     * 更新属性
     *
     * @param form 更新表单
     * @return 更新结果
     */
    @PutMapping("/update")
    public ReturnResult update(@RequestBody AttributeUpdateFormVO form) {
        return attributeManagementService.update(form);
    }

    /**
     * 批量删除属性
     *
     * @param ids ID列表 (RequestBody中接收String列表，然后转换为Long)
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    public ReturnResult delete(@RequestBody List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ReturnResult(ReturnCode.PARAM_IS_INVALID, "ID列表不能为空");
        }
        
        List<Long> longIds = new ArrayList<>();
        try {
            for (String id : ids) {
                longIds.add(Long.parseLong(id));
            }
        } catch (NumberFormatException e) {
            return new ReturnResult(ReturnCode.PARAM_TYPE_ERROR, "ID格式错误，无法转换为数字");
        }
        
        return attributeManagementService.batchDeleteByIds(longIds);
    }
} 