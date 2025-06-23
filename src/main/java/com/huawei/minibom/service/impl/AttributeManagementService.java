package com.huawei.minibom.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.huawei.minibom.result.ReturnCode;
import com.huawei.minibom.result.ReturnResult;
import com.huawei.minibom.service.IAttributeManagementService;
import com.huawei.minibom.vo.AttributeCreateFormVO;
import com.huawei.minibom.vo.AttributeQueryViewVO;
import com.huawei.minibom.vo.AttributeUpdateFormVO;
import com.huawei.innovation.rdm.coresdk.basic.dto.PersistObjectIdDecryptDTO;
import com.huawei.innovation.rdm.coresdk.basic.dto.PersistObjectIdsModifierDTO;
import com.huawei.innovation.rdm.coresdk.basic.enums.ConditionType;
import com.huawei.innovation.rdm.coresdk.basic.vo.QueryCondition;
import com.huawei.innovation.rdm.coresdk.basic.vo.QueryRequestVo;
import com.huawei.innovation.rdm.coresdk.basic.vo.RDMPageVO;
import com.huawei.innovation.rdm.xdm.delegator.EXADefinitionDelegator;
import com.huawei.innovation.rdm.xdm.dto.entity.EXADefinitionCreateDTO;
import com.huawei.innovation.rdm.xdm.dto.entity.EXADefinitionQueryViewDTO;
import com.huawei.innovation.rdm.xdm.dto.entity.EXADefinitionUpdateDTO;
import com.huawei.innovation.rdm.xdm.dto.entity.EXADefinitionViewDTO;
import com.huawei.innovation.rdm.xdm.dto.entity.FolderQueryViewDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 属性管理服务实现类
 *
 * @author huawei
 * @since 2025-06-17
 */
@Service
public class AttributeManagementService implements IAttributeManagementService {
    @Autowired
    private EXADefinitionDelegator exaDefinitionDelegator;

    @Override
    public List<AttributeQueryViewVO> queryAttributeByName(String name) {
        QueryRequestVo queryRequestVo = new QueryRequestVo();
        QueryCondition queryCondition = new QueryCondition();
        if (name != null && !name.trim().isEmpty()) {
            queryCondition.setJoiner("or");
            queryCondition.addCondition("nameEn", ConditionType.LIKE, name);
            queryCondition.addCondition("name", ConditionType.LIKE, name);
        }
        queryRequestVo.setFilter(queryCondition);
        List<EXADefinitionQueryViewDTO> query = exaDefinitionDelegator.query(queryRequestVo, new RDMPageVO());
        List<AttributeQueryViewVO> newlist = new ArrayList<>();
        for (EXADefinitionQueryViewDTO e : query) {
            AttributeQueryViewVO vo = new AttributeQueryViewVO();
            // 忽略folder和id，因为id类型不匹配，folder需要特殊处理
            String[] ignoreProperties = new String[]{"folder", "id"};
            BeanUtils.copyProperties(e, vo, ignoreProperties);
            vo.setId(String.valueOf(e.getId()));

            // 过滤并简化folder的敏感属性
            AttributeQueryViewVO.CustomFolder folder = new AttributeQueryViewVO.CustomFolder();
            FolderQueryViewDTO eFolder = e.getFolder();
            if (eFolder != null) {
                folder.setName(eFolder.getName());
                folder.setNameEn(eFolder.getNameEn());
                folder.setBusinessCode(eFolder.getBusinessCode());
                vo.setFolder(folder);
            }
            newlist.add(vo);
        }
        return newlist;
    }

    @Override
    public ReturnResult create(AttributeCreateFormVO createForm) {
        EXADefinitionCreateDTO dto = new EXADefinitionCreateDTO();
        BeanUtils.copyProperties(createForm, dto);

        // 参照师兄项目，对constraint进行硬编码
        String dtoType = dto.getType();
        JSONObject jsonObject = new JSONObject();
        if ("STRING".equals(dtoType)) {
            jsonObject.put("length", "255");
        } else if ("DECIMAL".equals(dtoType)) {
            jsonObject.put("precision", "9");
        } else {
            // 对于其他类型，设置一个默认值避免报错
            jsonObject.put("notnull", false);
        }
        dto.setConstraint(jsonObject.toJSONString());
        EXADefinitionViewDTO exaDefinitionViewDTO = exaDefinitionDelegator.create(dto);
        return new ReturnResult(ReturnCode.INSERT_OK, "成功创建属性", exaDefinitionViewDTO);
    }

    @Override
    public ReturnResult update(AttributeUpdateFormVO formVO) {
        Long id = formVO.getId();
        // 1. 根据ID查询属性细节，以保留不应被修改的字段
        PersistObjectIdDecryptDTO dto1 = new PersistObjectIdDecryptDTO();
        dto1.setId(id);
        EXADefinitionViewDTO exaDefinitionViewDTO = exaDefinitionDelegator.get(dto1);
        if (exaDefinitionViewDTO == null) {
            return new ReturnResult(ReturnCode.TARGET_NOT_EXIST, "要修改的属性不存在");
        }

        // 2. 准备更新DTO，只设置允许修改的字段
        EXADefinitionUpdateDTO dto2 = new EXADefinitionUpdateDTO();
        dto2.setId(id);
        // 关键：保留原有的类型和约束
        dto2.setType(exaDefinitionViewDTO.getType());
        dto2.setConstraint(exaDefinitionViewDTO.getConstraint());
        // 只更新描述
        dto2.setDescription(formVO.getDescription());
        dto2.setDescriptionEn(formVO.getDescriptionEn());

        // 3. 请求更新
        EXADefinitionViewDTO viewDTO = exaDefinitionDelegator.update(dto2);

        return new ReturnResult(ReturnCode.UPDATE_OK, "修改成功", viewDTO);
    }

    @Override
    public ReturnResult batchDeleteByIds(List<Long> ids) {
        PersistObjectIdsModifierDTO dto = new PersistObjectIdsModifierDTO();
        dto.setIds(ids);
        int count = exaDefinitionDelegator.batchDelete(dto);
        return new ReturnResult(ReturnCode.DELETE_OK, "删除成功，共删除 " + count + " 条数据", count);
    }
} 