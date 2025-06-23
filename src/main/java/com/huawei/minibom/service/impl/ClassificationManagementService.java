package com.huawei.minibom.service.impl;

import com.huawei.innovation.rdm.coresdk.basic.dto.ObjectReferenceParamDTO;
import com.huawei.innovation.rdm.coresdk.basic.dto.PersistObjectIdDecryptDTO;
import com.huawei.innovation.rdm.coresdk.basic.dto.PersistObjectIdModifierDTO;
import com.huawei.innovation.rdm.coresdk.basic.dto.PersistObjectIdsModifierDTO;
import com.huawei.innovation.rdm.coresdk.basic.enums.ConditionType;
import com.huawei.innovation.rdm.coresdk.basic.vo.QueryCondition;
import com.huawei.innovation.rdm.coresdk.basic.vo.QueryRequestVo;
import com.huawei.innovation.rdm.coresdk.basic.vo.RDMPageVO;
import com.huawei.innovation.rdm.xdm.delegator.ClassificationNodeDelegator;
import com.huawei.innovation.rdm.xdm.delegator.EXADefinitionDelegator;
import com.huawei.innovation.rdm.xdm.delegator.EXADefinitionLinkDelegator;
import com.huawei.innovation.rdm.xdm.dto.entity.ClassificationNodeCreateDTO;
import com.huawei.innovation.rdm.xdm.dto.entity.ClassificationNodeQueryViewDTO;
import com.huawei.innovation.rdm.xdm.dto.entity.ClassificationNodeUpdateDTO;
import com.huawei.innovation.rdm.xdm.dto.entity.ClassificationNodeViewDTO;
import com.huawei.innovation.rdm.xdm.dto.entity.EXADefinitionQueryViewDTO;
import com.huawei.innovation.rdm.xdm.dto.entity.EXADefinitionViewDTO;
import com.huawei.innovation.rdm.xdm.dto.entity.FolderQueryViewDTO;
import com.huawei.innovation.rdm.xdm.dto.entity.FolderViewDTO;
import com.huawei.innovation.rdm.xdm.dto.relation.EXADefinitionLinkCreateDTO;
import com.huawei.innovation.rdm.xdm.dto.relation.EXADefinitionLinkQueryViewDTO;
import com.huawei.innovation.rdm.xdm.dto.relation.EXADefinitionLinkViewDTO;
import com.huawei.minibom.service.IClassificationManagementService;
import com.huawei.minibom.vo.AttributeQueryViewVO;
import com.huawei.minibom.vo.ClassificationCreateFormVO;
import com.huawei.minibom.vo.ClassificationLinkAttrVO;
import com.huawei.minibom.vo.ClassificationUpdateFormVO;
import com.huawei.minibom.vo.CustomClassificationNodeViewVO;
import com.huawei.minibom.vo.ClassificationViewVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ClassificationManagementService implements IClassificationManagementService {
    private static final Logger logger = LoggerFactory.getLogger(ClassificationManagementService.class);

    @Autowired
    private ClassificationNodeDelegator classificationNodeDelegator;

    @Autowired
    private EXADefinitionLinkDelegator exaDefinitionLinkDelegator;

    @Autowired
    private EXADefinitionDelegator exaDefinitionDelegator;

    @Override
    public String create(ClassificationCreateFormVO vo) {
        ClassificationNodeCreateDTO createDTO = new ClassificationNodeCreateDTO();
        BeanUtils.copyProperties(vo, createDTO);
        createDTO.setBusinessCode(vo.getCode());
        
        // 如果英文名为空，则默认使用中文名
        if (!StringUtils.hasText(vo.getEnglishName())) {
            createDTO.setNameEn(vo.getName());
        } else {
            createDTO.setNameEn(vo.getEnglishName());
        }

        if (StringUtils.hasText(vo.getParentId())) {
            ObjectReferenceParamDTO parentNode = new ObjectReferenceParamDTO();
            parentNode.setId(Long.valueOf(vo.getParentId()));
            parentNode.setClazz("ClassificationNode");
            createDTO.setParentNode(parentNode);
        }

        return classificationNodeDelegator.create(createDTO).getId().toString();
    }

    @Override
    public void deleteById(String id) {
        PersistObjectIdModifierDTO modifierDTO = new PersistObjectIdModifierDTO();
        modifierDTO.setId(Long.valueOf(id));
        classificationNodeDelegator.delete(modifierDTO);
    }

    @Override
    public void update(ClassificationUpdateFormVO vo) {
        ClassificationNodeUpdateDTO updateDTO = new ClassificationNodeUpdateDTO();
        updateDTO.setId(Long.valueOf(vo.getId()));
        updateDTO.setName(vo.getName());
        // 如果英文名为空，则默认使用中文名
        if (!StringUtils.hasText(vo.getEnglishName())) {
            updateDTO.setNameEn(vo.getName());
        } else {
            updateDTO.setNameEn(vo.getEnglishName());
        }
        updateDTO.setDescription(vo.getDescription());
        classificationNodeDelegator.update(updateDTO);
    }

    @Override
    public List<CustomClassificationNodeViewVO> queryClassificationTree() {
        QueryRequestVo queryRequestVo = new QueryRequestVo();
        RDMPageVO page = new RDMPageVO();
        page.setPageSize(1000); // Assume total classifications are less than 1000

        List<ClassificationNodeQueryViewDTO> allNodes;
        try {
            // 1. Global Defense: Catch errors during the main query
            allNodes = classificationNodeDelegator.query(queryRequestVo, page);
        } catch (Exception e) {
            logger.error("Failed to query classifications from SDK, likely due to corrupted data. Returning empty list.", e);
            return Collections.emptyList();
        }

        if (allNodes == null || allNodes.isEmpty()) {
            return Collections.emptyList();
        }

        List<CustomClassificationNodeViewVO> allNodeVOs = new ArrayList<>();
        // 2. Local Defense: Use a for-loop for resilience against individual corrupted nodes
        for (ClassificationNodeQueryViewDTO node : allNodes) {
            try {
                CustomClassificationNodeViewVO vo = new CustomClassificationNodeViewVO();
                BeanUtils.copyProperties(node, vo);
                vo.setId(node.getId().toString());
                vo.setCode(node.getBusinessCode());
                
                // 3. Data Cleansing: Default englishName to name if it's blank
                vo.setEnglishName(StringUtils.hasText(node.getNameEn()) ? node.getNameEn() : node.getName());

                // Try to get parent, but don't fail the whole process if one lookup fails
                try {
                    com.huawei.innovation.rdm.coresdk.basic.dto.QueryParentDTO queryParentDTO = new com.huawei.innovation.rdm.coresdk.basic.dto.QueryParentDTO();
                    queryParentDTO.setChildId(node.getId());
                    ClassificationNodeQueryViewDTO parent = classificationNodeDelegator.getParent(queryParentDTO);
                    if (parent != null && parent.getId() != null) {
                        vo.setParentId(parent.getId().toString());
                    }
                } catch (Exception e) {
                    // This is expected for root nodes, log as warning
                    logger.warn("Could not determine parent for node {}. Treating as root. Error: {}", node.getId(), e.getMessage());
                }
                allNodeVOs.add(vo);
            } catch (Exception e) {
                // Log and skip the single corrupted node
                logger.error("Failed to process a single classification node with ID: {}. Skipping it.", node.getId(), e);
            }
        }

        Map<String, CustomClassificationNodeViewVO> nodeMap = allNodeVOs.stream()
                .collect(Collectors.toMap(CustomClassificationNodeViewVO::getId, Function.identity()));

        List<CustomClassificationNodeViewVO> rootNodes = new ArrayList<>();
        allNodeVOs.forEach(node -> {
            String parentId = node.getParentId();
            if (parentId != null && nodeMap.containsKey(parentId)) {
                CustomClassificationNodeViewVO parentNode = nodeMap.get(parentId);
                if (parentNode.getChildren() == null) {
                    parentNode.setChildren(new ArrayList<>());
                }
                parentNode.getChildren().add(node);
            } else {
                rootNodes.add(node);
            }
        });

        return rootNodes;
    }

    @Override
    public void linkAttributes(ClassificationLinkAttrVO vo) {
        Long classificationId = Long.valueOf(vo.getClassificationId());
        Set<String> newAttrIds = vo.getAttributeIds();

        if (newAttrIds == null || newAttrIds.isEmpty()) {
            logger.info("No attributes to link for classification ID {}", classificationId);
            return;
        }

        List<EXADefinitionLinkCreateDTO> linksToAdd = newAttrIds.stream()
            .filter(attrId -> attrId != null && !attrId.isEmpty())
            .map(attrId -> {
                EXADefinitionLinkCreateDTO linkCreateDTO = new EXADefinitionLinkCreateDTO();
                
                ObjectReferenceParamDTO source = new ObjectReferenceParamDTO();
                source.setClazz("EXADefinition");
                source.setId(Long.valueOf(attrId));
                linkCreateDTO.setSource(source);
                
                ObjectReferenceParamDTO target = new ObjectReferenceParamDTO();
                target.setClazz("ClassificationNode");
                target.setId(classificationId);
                linkCreateDTO.setTarget(target);
                
                return linkCreateDTO;
            }).collect(Collectors.toList());

        if (!linksToAdd.isEmpty()) {
            exaDefinitionLinkDelegator.batchCreate(linksToAdd);
        }
    }

    @Override
    public List<CustomClassificationNodeViewVO> queryByNameOrCode(String name) {
        if (!StringUtils.hasText(name)) {
            return queryClassificationTree();
        }

        QueryRequestVo queryRequestVo = new QueryRequestVo();
        QueryCondition filter = new QueryCondition();
        filter.setJoiner("or");
        filter.addCondition("name", ConditionType.EQUAL, name);
        filter.addCondition("businessCode", ConditionType.EQUAL, name);
        queryRequestVo.setFilter(filter);

        List<ClassificationNodeQueryViewDTO> results = classificationNodeDelegator.query(queryRequestVo, new RDMPageVO());

        return results.stream()
                .map(this::convertDtoToFlatVo)
                .collect(Collectors.toList());
    }

    private CustomClassificationNodeViewVO convertDtoToFlatVo(ClassificationNodeQueryViewDTO dto) {
        CustomClassificationNodeViewVO vo = new CustomClassificationNodeViewVO();
        BeanUtils.copyProperties(dto, vo);
        vo.setId(dto.getId().toString());
        vo.setCode(dto.getBusinessCode());
        vo.setEnglishName(dto.getNameEn());
        return vo;
    }

    @Override
    public ClassificationViewVO queryClassificationDetail(String id) {
        if (!StringUtils.hasText(id)) {
            return null;
        }
        try {
            // 1. 获取分类基本信息
            PersistObjectIdDecryptDTO decryptDTO = new PersistObjectIdDecryptDTO();
            decryptDTO.setId(Long.valueOf(id));
            ClassificationNodeViewDTO dto = classificationNodeDelegator.get(decryptDTO);
            if (dto == null) {
                return null;
            }
            ClassificationViewVO vo = new ClassificationViewVO();
            vo.setId(dto.getId().toString());
            vo.setName(dto.getName());
            vo.setCode(dto.getBusinessCode());

            // 2. 查询关联的属性链接
            // 参考师兄项目的做法，使用 find 方法而不是 query 方法
            List<EXADefinitionLinkQueryViewDTO> links = null;
            
            try {
                logger.info("使用 find 方法查询分类ID: {} 的关联属性链接", id);
                
                QueryRequestVo linkQuery = new QueryRequestVo();
                QueryCondition linkCondition = new QueryCondition();
                linkCondition.addCondition("target.id", ConditionType.EQUAL, Long.valueOf(id));
                linkCondition.setJoiner("or");  // 参考师兄项目的做法
                linkQuery.setFilter(linkCondition);
                
                // 使用 find 方法而不是 query 方法
                List<EXADefinitionLinkViewDTO> findLinks = exaDefinitionLinkDelegator.find(linkQuery, new RDMPageVO());
                
                logger.info("find 方法查询成功，找到 {} 个关联链接", findLinks != null ? findLinks.size() : 0);
                
                // 收集属性ID，避免复杂的类型转换
                List<Long> linkedAttributeIds = new ArrayList<>();
                if (findLinks != null) {
                    for (EXADefinitionLinkViewDTO findLink : findLinks) {
                        logger.info("找到关联链接 - Source ID: {}, Target ID: {}", 
                            findLink.getSource() != null ? findLink.getSource().getId() : "null",
                            findLink.getTarget() != null ? findLink.getTarget().getId() : "null");
                        
                        // 收集属性ID（source是属性，target是分类）
                        if (findLink.getSource() != null && findLink.getSource().getId() != null) {
                            linkedAttributeIds.add(findLink.getSource().getId());
                        }
                    }
                }
                
                logger.info("提取到的已关联属性ID: {}", linkedAttributeIds);
                
                if (linkedAttributeIds.isEmpty()) {
                    logger.info("该分类没有关联任何属性");
                    vo.setAttributes(Collections.emptyList());
                    return vo;
                }
                
                // 只查询已关联的属性详情
                List<AttributeQueryViewVO> attributeVOs = new ArrayList<>();
                for (Long attributeId : linkedAttributeIds) {
                    try {
                        // 逐个查询每个已关联属性的详情
                        PersistObjectIdDecryptDTO attrDecryptDTO = new PersistObjectIdDecryptDTO();
                        attrDecryptDTO.setId(attributeId);
                        com.huawei.innovation.rdm.xdm.dto.entity.EXADefinitionViewDTO attrDto = exaDefinitionDelegator.get(attrDecryptDTO);
                        
                        if (attrDto != null) {
                            AttributeQueryViewVO attrVo = new AttributeQueryViewVO();
                            String[] ignoreProperties = new String[]{"folder", "id"};
                            BeanUtils.copyProperties(attrDto, attrVo, ignoreProperties);
                            attrVo.setId(String.valueOf(attrDto.getId()));
                            attrVo.setNameEn(attrDto.getNameEn());

                            AttributeQueryViewVO.CustomFolder folder = new AttributeQueryViewVO.CustomFolder();
                            com.huawei.innovation.rdm.xdm.dto.entity.FolderViewDTO eFolder = attrDto.getFolder();
                            if (eFolder != null) {
                                folder.setName(eFolder.getName());
                                folder.setNameEn(eFolder.getNameEn());
                                folder.setBusinessCode(eFolder.getBusinessCode());
                                attrVo.setFolder(folder);
                            }
                            
                            attributeVOs.add(attrVo);
                            logger.info("获取已关联属性详情: {} ({})", attrDto.getName(), attrDto.getId());
                        }
                    } catch (Exception e) {
                        logger.warn("获取属性ID {} 的详情失败: {}", attributeId, e.getMessage());
                    }
                }

                vo.setAttributes(attributeVOs);
                logger.info("返回分类详情，包含 {} 个已关联属性", attributeVOs.size());
                return vo;
                
            } catch (Exception e) {
                logger.error("查询分类详情失败: {}", e.getMessage());
                vo.setAttributes(Collections.emptyList());
                return vo;
            }
        } catch (NumberFormatException e) {
            logger.error("Invalid classification ID format: {}", id, e);
            return null;
        } catch (Exception e) {
            logger.error("Failed to query classification detail for ID {}.", id, e);
            return null;
        }
    }
}
 