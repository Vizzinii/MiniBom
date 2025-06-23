package com.huawei.minibom.controller;

import com.huawei.minibom.result.ReturnCode;
import com.huawei.minibom.result.ReturnResult;
import com.huawei.minibom.service.IClassificationManagementService;
import com.huawei.minibom.vo.ClassificationCreateFormVO;
import com.huawei.minibom.vo.ClassificationLinkAttrVO;
import com.huawei.minibom.vo.ClassificationUpdateFormVO;
import com.huawei.minibom.vo.CustomClassificationNodeViewVO;
import com.huawei.minibom.vo.ClassificationViewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/classifications")
public class ClassificationManagementController {

    private final IClassificationManagementService classificationManagementService;

    @Autowired
    public ClassificationManagementController(IClassificationManagementService classificationManagementService) {
        this.classificationManagementService = classificationManagementService;
    }

    /**
     * 创建分类
     */
    @PostMapping
    public ReturnResult create(@RequestBody @Validated ClassificationCreateFormVO vo) {
        String id = classificationManagementService.create(vo);
        return new ReturnResult(ReturnCode.INSERT_OK, "创建成功", id);
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    public ReturnResult delete(@PathVariable String id) {
        classificationManagementService.deleteById(id);
        return new ReturnResult(ReturnCode.DELETE_OK, "删除成功");
    }

    /**
     * 更新分类
     */
    @PutMapping("/{id}")
    public ReturnResult update(@PathVariable String id, @RequestBody @Validated ClassificationUpdateFormVO vo) {
        vo.setId(id);
        classificationManagementService.update(vo);
        return new ReturnResult(ReturnCode.UPDATE_OK, "更新成功");
    }

    /**
     * 查询分类
     * @param name 按名称或编码查询（可选）
     * @return 树状或列表结构的分类
     */
    @GetMapping
    public ReturnResult query(@RequestParam(required = false) String name) {
        List<CustomClassificationNodeViewVO> result;
        if (name != null && !name.isEmpty()) {
            result = classificationManagementService.queryByNameOrCode(name);
        } else {
            result = classificationManagementService.queryClassificationTree();
        }
        return new ReturnResult(ReturnCode.GET_OK, "查询成功", result);
    }

    /**
     * 查询分类详情
     * @param id 分类id
     * @return 分类详情
     */
    @GetMapping("/{id}/details")
    public ReturnResult queryClassificationDetail(@PathVariable String id) {
        ClassificationViewVO result = classificationManagementService.queryClassificationDetail(id);
        return new ReturnResult(ReturnCode.GET_OK, "查询成功", result);
    }

    /**
     * 关联属性
     */
    @PostMapping("/link-attributes")
    public ReturnResult linkAttributes(@RequestBody @Validated ClassificationLinkAttrVO vo) {
        classificationManagementService.linkAttributes(vo);
        return new ReturnResult(ReturnCode.BUSINESS_OK, "关联成功");
    }
} 