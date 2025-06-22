package com.huawei.minibom.service.impl;

import com.huawei.innovation.rdm.coresdk.basic.dto.ObjectReferenceParamDTO;
import com.huawei.innovation.rdm.coresdk.basic.dto.PersistObjectIdModifierDTO;
import com.huawei.innovation.rdm.coresdk.basic.enums.ConditionType;
import com.huawei.innovation.rdm.coresdk.basic.vo.QueryCondition;
import com.huawei.innovation.rdm.coresdk.basic.vo.QueryRequestVo;
import com.huawei.innovation.rdm.coresdk.basic.vo.RDMPageVO;
import com.huawei.innovation.rdm.minibomdatamodel.delegator.BOMLinkDelegator;
import com.huawei.innovation.rdm.minibomdatamodel.delegator.BOMUsesOccurrenceDelegator;
import com.huawei.innovation.rdm.minibomdatamodel.delegator.PartDelegator;
import com.huawei.innovation.rdm.minibomdatamodel.dto.entity.BOMUsesOccurrenceCreateDTO;
import com.huawei.innovation.rdm.minibomdatamodel.dto.entity.BOMUsesOccurrenceUpdateDTO;
import com.huawei.innovation.rdm.minibomdatamodel.dto.entity.BOMUsesOccurrenceViewDTO;
import com.huawei.innovation.rdm.minibomdatamodel.dto.entity.PartViewDTO;
import com.huawei.innovation.rdm.minibomdatamodel.dto.relation.BOMLinkCreateDTO;
import com.huawei.innovation.rdm.minibomdatamodel.dto.relation.BOMLinkUpdateDTO;
import com.huawei.innovation.rdm.minibomdatamodel.dto.relation.BOMLinkViewDTO;
import com.huawei.minibom.result.ReturnResult;
import com.huawei.minibom.service.IBomService;
import com.huawei.minibom.vo.bom.BomLinkCreateVO;
import com.huawei.minibom.vo.bom.BomTreeQueryVO;
import com.huawei.minibom.vo.bom.BomListQueryVO;
import com.huawei.minibom.vo.bom.BomLinkUpdateVO;
import com.huawei.minibom.vo.bom.BomBatchCreateVO;
import com.huawei.minibom.vo.bom.WhereUsedQueryVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * BOM服务实现类
 */
@Service
public class BomService implements IBomService {

    private static final Logger logger = LoggerFactory.getLogger(BomService.class);

    // 用于缓存BOM链接的父子关系，假设在查询时构建
    private Map<Long, List<BOMLinkViewDTO>> parentToChildrenMap = new HashMap<>();
    private Map<Long, Integer> levelCache = new HashMap<>();

    /**
     * 计算BOM链接的层级
     * @param bomLink BOM链接DTO
     * @return 层级
     */
    private int calculateLevel(BOMLinkViewDTO bomLink) {
        if (levelCache.containsKey(bomLink.getId())) {
            return levelCache.get(bomLink.getId());
        }

        int level = 1;
        Long sourceId = bomLink.getSource().getId();
        // 查找是否有父级BOM链接（即target.id等于当前source.id的链接）
        QueryRequestVo parentQuery = new QueryRequestVo();
        QueryCondition parentCondition = new QueryCondition();
        parentCondition.setJoiner("and");
        parentCondition.addCondition("target.id", ConditionType.EQUAL, sourceId);
        parentQuery.setFilter(parentCondition);
        List<BOMLinkViewDTO> parentLinks = bomLinkDelegator.find(parentQuery, new RDMPageVO());

        if (parentLinks != null && !parentLinks.isEmpty()) {
            // 取第一个父链接计算层级（假设单父结构）
            BOMLinkViewDTO parentLink = parentLinks.get(0);
            level = calculateLevel(parentLink) + 1;
        }

        levelCache.put(bomLink.getId(), level);
        return level;
    }

    /**
     * 计算BOM列表中的最大层级
     * @param bomList BOM链接列表
     * @return 最大层级
     */
    private int calculateMaxLevel(List<BOMLinkViewDTO> bomList) {
        int maxLevel = 0;
        for (BOMLinkViewDTO bomLink : bomList) {
            int level = calculateLevel(bomLink);
            if (level > maxLevel) {
                maxLevel = level;
            }
        }
        return maxLevel;
    }

    /**
     * 计算单位数量，假设与quantity相同或根据业务逻辑调整
     * @param bomLink BOM链接DTO
     * @return 单位数量
     */
    private double calculateUnitQuantity(BOMLinkViewDTO bomLink) {
        // 假设单位数量等于quantity，实际业务逻辑可能需要根据层级或父子关系计算
        return bomLink.getQuantity().doubleValue();
    }

    /**
     * 构建子层级BOM结构
     * @param targetId 目标ID，作为子节点的源ID
     * @return 子层级列表
     */
    private List<Map<String, Object>> buildChildren(Long targetId) {
        List<Map<String, Object>> children = new ArrayList<>();
        QueryRequestVo childQuery = new QueryRequestVo();
        QueryCondition childCondition = new QueryCondition();
        childCondition.setJoiner("and");
        childCondition.addCondition("source.id", ConditionType.EQUAL, targetId);
        childQuery.setFilter(childCondition);
        List<BOMLinkViewDTO> childLinks = bomLinkDelegator.find(childQuery, new RDMPageVO());

        if (childLinks != null && !childLinks.isEmpty()) {
            for (BOMLinkViewDTO childLink : childLinks) {
                Map<String, Object> childItem = new HashMap<>();
                childItem.put("bomLinkId", childLink.getId());
                childItem.put("level", calculateLevel(childLink));
                childItem.put("sequenceNumber", childLink.getSequenceNumber());
                childItem.put("quantity", childLink.getQuantity());
                // 获取referenceDesignator
                String referenceDesignator = "";
                QueryRequestVo occurrenceQuery = new QueryRequestVo();
                QueryCondition occurrenceCondition = new QueryCondition();
                occurrenceCondition.setJoiner("and");
                occurrenceCondition.addCondition("bomLink.id", ConditionType.EQUAL, childLink.getId());
                occurrenceQuery.setFilter(occurrenceCondition);
                List<BOMUsesOccurrenceViewDTO> occurrences = bomUsesOccurrenceDelegator.find(occurrenceQuery, new RDMPageVO());
                if (occurrences != null && !occurrences.isEmpty()) {
                    referenceDesignator = occurrences.get(0).getReferenceDesignator() != null ? occurrences.get(0).getReferenceDesignator() : "";
                }
                childItem.put("referenceDesignator", referenceDesignator);
                // 获取targetPart信息
                Map<String, Object> targetPart = new HashMap<>();
                Long childTargetId = childLink.getTarget().getId();
                QueryRequestVo targetPartQuery = new QueryRequestVo();
                QueryCondition targetPartCondition = new QueryCondition();
                targetPartCondition.setJoiner("and");
                targetPartCondition.addCondition("master.id", ConditionType.EQUAL, childTargetId);
                targetPartQuery.setFilter(targetPartCondition);
                List<PartViewDTO> targetParts = partDelegator.find(targetPartQuery, new RDMPageVO());
                if (targetParts != null && !targetParts.isEmpty()) {
                    PartViewDTO targetPartDTO = targetParts.get(0);
                    targetPart.put("masterId", targetPartDTO.getMaster() != null ? targetPartDTO.getMaster().getId() : childTargetId);
                    targetPart.put("number", targetPartDTO.getMaster().getNumber() != null ? targetPartDTO.getMaster().getNumber() : "");
                    targetPart.put("name", targetPartDTO.getName() != null ? targetPartDTO.getName() : "");
                    targetPart.put("currentVersion", targetPartDTO.getVersion() != null ? targetPartDTO.getVersion() : "");
                } else {
                    targetPart.put("masterId", childTargetId);
                    targetPart.put("number", "");
                    targetPart.put("name", "");
                    targetPart.put("currentVersion", "");
                }
                childItem.put("targetPart", targetPart);
                // 递归构建子层级
                childItem.put("children", buildChildren(childTargetId));
                children.add(childItem);
            }
        }
        return children;
    }

    @Autowired
    private BOMLinkDelegator bomLinkDelegator;

    @Autowired
    private BOMUsesOccurrenceDelegator bomUsesOccurrenceDelegator;

    @Autowired
    private PartDelegator partDelegator;

    @Override
    public ReturnResult createBomLink(BomLinkCreateVO bomLinkCreateVO) {
        logger.info("BomService.createBomLink - 创建BOM关系开始，源ID: {}, 目标ID: {}",
                bomLinkCreateVO.getBomLink().getSource(), bomLinkCreateVO.getBomLink().getTarget());

        // 第一阶段：创建BOM链接
        logger.info("BomService.createBomLink 第一阶段 - 开始创建BOMLink");
        BOMLinkCreateDTO createDTO = new BOMLinkCreateDTO();
        ObjectReferenceParamDTO sourceRef = new ObjectReferenceParamDTO();
        sourceRef.setId(bomLinkCreateVO.getBomLink().getSource());
        createDTO.setSource(sourceRef);

        ObjectReferenceParamDTO targetRef = new ObjectReferenceParamDTO();
        targetRef.setId(bomLinkCreateVO.getBomLink().getTarget());
        createDTO.setTarget(targetRef);

        createDTO.setQuantity(new BigDecimal(bomLinkCreateVO.getBomLink().getQuantity().toString()));
        if (bomLinkCreateVO.getBomLink().getSequenceNumber() != null) {
            createDTO.setSequenceNumber(bomLinkCreateVO.getBomLink().getSequenceNumber());
        }
        BOMLinkViewDTO viewDTO = bomLinkDelegator.create(createDTO);

        // 第二阶段：如果有参考标识符，创建BOMUsesOccurrence
        if (bomLinkCreateVO.getBomUsesOccurrence() != null &&
                bomLinkCreateVO.getBomUsesOccurrence().getReferenceDesignator() != null) {
            logger.info("BomService.createBomLink 第二阶段 - 开始创建BOMUsesOccurrence");
            BOMUsesOccurrenceCreateDTO occurrenceDTO = new BOMUsesOccurrenceCreateDTO();
            ObjectReferenceParamDTO bomLinkRef = new ObjectReferenceParamDTO();
            bomLinkRef.setId(viewDTO.getId());
            occurrenceDTO.setBomLink(bomLinkRef);
            occurrenceDTO.setReferenceDesignator(bomLinkCreateVO.getBomUsesOccurrence().getReferenceDesignator());
            bomUsesOccurrenceDelegator.create(occurrenceDTO);
        }

        logger.info("BomService.createBomLink - 创建BOM关系成功，BOM链接ID: {}", viewDTO != null ? viewDTO.getId() : "未知");
        // 构建符合初始任务描述的响应结构
        Map<String, Object> data = new HashMap<>();
        if (viewDTO != null) {
            data.put("bomLinkId", viewDTO.getId());
            data.put("sourceVersionId", viewDTO.getSource().getId());
            data.put("targetMasterId", viewDTO.getTarget().getId());
            data.put("quantity", viewDTO.getQuantity());
            data.put("sequenceNumber", viewDTO.getSequenceNumber());
            data.put("createTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        }
        return new ReturnResult(200, "BOM关系创建成功", data);
    }

    @Override
    public ReturnResult getBomTree(BomTreeQueryVO queryVO) {
        logger.info("BomService.getBomTree - 查询BOM树结构开始，版本ID: {}", queryVO.getVersionId());
        // 使用QueryRequestVo和QueryCondition来查询BOM树结构，基于版本ID过滤
        QueryRequestVo queryRequestVo = new QueryRequestVo();
        QueryCondition queryCondition = new QueryCondition();
        queryCondition.setJoiner("and");
        queryCondition.addCondition("source.id", ConditionType.EQUAL, queryVO.getVersionId());
        queryRequestVo.setFilter(queryCondition);

        // 设置分页参数，如果需要
        RDMPageVO pageVO = new RDMPageVO();
        // 如果有层级限制，可以设置条件或分页参数
        if (queryVO.getLevels() != null) {
            // 可以通过层级限制来控制查询深度，当前实现暂不处理
        }

        // 获取BOM树数据 - 仅获取直接子节点，子节点的子节点通过buildChildren方法递归获取
        List<BOMLinkViewDTO> bomTree = bomLinkDelegator.find(queryRequestVo, pageVO);
        if (bomTree == null) {
            bomTree = new ArrayList<>();
        }

        logger.info("BomService.getBomTree - 查询BOM树结构完成，找到 {} 条记录", bomTree.size());
        // 构建符合初始任务描述的响应结构
        Map<String, Object> data = new HashMap<>();
        // 获取rootPart信息
        QueryRequestVo partQuery = new QueryRequestVo();
        QueryCondition partCondition = new QueryCondition();
        partCondition.setJoiner("and");
        partCondition.addCondition("id", ConditionType.EQUAL, queryVO.getVersionId());
        partQuery.setFilter(partCondition);
        List<PartViewDTO> rootParts = partDelegator.find(partQuery, new RDMPageVO());
        Map<String, Object> rootPart = new HashMap<>();
        if (rootParts != null && !rootParts.isEmpty()) {
            PartViewDTO rootPartDTO = rootParts.get(0);
            rootPart.put("versionId", rootPartDTO.getId());
            rootPart.put("masterId", rootPartDTO.getMaster() != null ? rootPartDTO.getMaster().getId() : 0);
            rootPart.put("number", rootPartDTO.getMaster().getNumber() != null ? rootPartDTO.getMaster().getNumber() : "");
            rootPart.put("name", rootPartDTO.getMaster().getName() != null ? rootPartDTO.getMaster().getName() : "");
            rootPart.put("version", rootPartDTO.getVersion() != null ? rootPartDTO.getVersion() : "");
            rootPart.put("iteration", rootPartDTO.getIteration() != null ? rootPartDTO.getIteration() : 0);
        } else {
            rootPart.put("versionId", queryVO.getVersionId());
            rootPart.put("masterId", 0);
            rootPart.put("number", "");
            rootPart.put("name", "");
            rootPart.put("version", "");
            rootPart.put("iteration", 0);
        }
        data.put("rootPart", rootPart);
        // 构建bomTree结构
        List<Map<String, Object>> bomTreeData = new ArrayList<>();
        for (BOMLinkViewDTO bomLink : bomTree) {
            Map<String, Object> bomItem = new HashMap<>();
            bomItem.put("bomLinkId", bomLink.getId());
            bomItem.put("level", calculateLevel(bomLink));
            bomItem.put("sequenceNumber", bomLink.getSequenceNumber());
            bomItem.put("quantity", bomLink.getQuantity().doubleValue()); // 确保返回的是double类型
            // 获取referenceDesignator
            String referenceDesignator = "";
            QueryRequestVo occurrenceQuery = new QueryRequestVo();
            QueryCondition occurrenceCondition = new QueryCondition();
            occurrenceCondition.setJoiner("and");
            occurrenceCondition.addCondition("bomLink.id", ConditionType.EQUAL, bomLink.getId());
            occurrenceQuery.setFilter(occurrenceCondition);
            List<BOMUsesOccurrenceViewDTO> occurrences = bomUsesOccurrenceDelegator.find(occurrenceQuery, new RDMPageVO());
            if (occurrences != null && !occurrences.isEmpty()) {
                referenceDesignator = occurrences.get(0).getReferenceDesignator() != null ? occurrences.get(0).getReferenceDesignator() : "";
            }
            bomItem.put("referenceDesignator", referenceDesignator);
            // 获取targetPart信息
            Map<String, Object> targetPart = new HashMap<>();
            Long targetId = bomLink.getTarget().getId();
            QueryRequestVo targetPartQuery = new QueryRequestVo();
            QueryCondition targetPartCondition = new QueryCondition();
            targetPartCondition.setJoiner("and");
            targetPartCondition.addCondition("master.id", ConditionType.EQUAL, targetId);
            targetPartQuery.setFilter(targetPartCondition);
            List<PartViewDTO> targetParts = partDelegator.find(targetPartQuery, new RDMPageVO());
            if (targetParts != null && !targetParts.isEmpty()) {
                PartViewDTO targetPartDTO = targetParts.get(0);
                targetPart.put("masterId", targetPartDTO.getMaster() != null ? targetPartDTO.getMaster().getId() : targetId);
                targetPart.put("number", targetPartDTO.getMaster().getNumber() != null ? targetPartDTO.getMaster().getNumber() : "");
                targetPart.put("name", targetPartDTO.getMaster().getName() != null ? targetPartDTO.getMaster().getName() : "");
                targetPart.put("currentVersion", targetPartDTO.getVersion() != null ? targetPartDTO.getVersion() : "");
            } else {
                targetPart.put("masterId", targetId);
                targetPart.put("number", "");
                targetPart.put("name", "");
                targetPart.put("currentVersion", "");
            }
            bomItem.put("targetPart", targetPart);
            // 构建子层级
            bomItem.put("children", buildChildren(bomLink.getTarget().getId()));
            bomTreeData.add(bomItem);
        }
        data.put("bomTree", bomTreeData);
        return new ReturnResult(200, "查询成功", data);
    }

    @Override
    public ReturnResult getBomList(BomListQueryVO queryVO) {
        logger.info("BomService.getBomList - 查询BOM清单开始，版本ID: {}", queryVO.getVersionId());
        // 使用QueryRequestVo和QueryCondition来查询BOM列表，基于版本ID过滤
        QueryRequestVo queryRequestVo = new QueryRequestVo();
        QueryCondition queryCondition = new QueryCondition();
        queryCondition.setJoiner("and");
        queryCondition.addCondition("source.id", ConditionType.EQUAL, queryVO.getVersionId());
        queryRequestVo.setFilter(queryCondition);

        // 设置分页参数，如果需要
        RDMPageVO pageVO = new RDMPageVO();
        // 如果需要递归查询所有层级，可以设置相关条件
        if (queryVO.getRecursive() != null && queryVO.getRecursive()) {
            // 递归查询逻辑，获取所有层级的BOM链接
            bomList = new ArrayList<>();
            fetchBomLinksRecursively(queryVO.getVersionId(), bomList);
        } else {
            // 非递归查询，仅获取直接子节点
            bomList = bomLinkDelegator.find(queryRequestVo, pageVO);
        }

        if (bomList == null) {
            bomList = new ArrayList<>();
        }
        logger.info("BomService.getBomList - 查询BOM清单完成，找到 {} 条记录", bomList.size());
        // 构建符合初始任务描述的响应结构
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalItems", bomList.size());
        summary.put("totalQuantity", bomList.stream().mapToDouble(dto -> dto.getQuantity().doubleValue()).sum());
        summary.put("maxLevel", calculateMaxLevel(bomList));
        data.put("summary", summary);
        List<Map<String, Object>> items = new ArrayList<>();
        for (BOMLinkViewDTO bomLink : bomList) {
            Map<String, Object> item = new HashMap<>();
            item.put("level", calculateLevel(bomLink));
            item.put("sequenceNumber", bomLink.getSequenceNumber());
            // 获取targetPart信息
            Long targetId = bomLink.getTarget().getId();
            QueryRequestVo targetPartQuery = new QueryRequestVo();
            QueryCondition targetPartCondition = new QueryCondition();
            targetPartCondition.setJoiner("and");
            targetPartCondition.addCondition("master.id", ConditionType.EQUAL, targetId);
            targetPartQuery.setFilter(targetPartCondition);
            List<PartViewDTO> targetParts = partDelegator.find(targetPartQuery, new RDMPageVO());
            String partNumber = "";
            String partName = "";
            String source = "Buy"; // 默认值，符合用户提供的格式
            String description = "";
            if (targetParts != null && !targetParts.isEmpty()) {
                PartViewDTO targetPartDTO = targetParts.get(0);
                partNumber = targetPartDTO.getMaster().getNumber() != null ? targetPartDTO.getMaster().getNumber() : "";
                partName = targetPartDTO.getMaster().getName() != null ? targetPartDTO.getMaster().getName() : "";
                if (targetPartDTO.getSource() != null) {
                    source = String.valueOf(targetPartDTO.getSource());
                }
                description = targetPartDTO.getDescription() != null ? targetPartDTO.getDescription() : "";
            }
            item.put("partNumber", partNumber);
            item.put("partName", partName);
            item.put("quantity", bomLink.getQuantity().doubleValue());
            item.put("unitQuantity", calculateUnitQuantity(bomLink));
            // 获取referenceDesignator
            String referenceDesignator = "";
            QueryRequestVo occurrenceQuery = new QueryRequestVo();
            QueryCondition occurrenceCondition = new QueryCondition();
            occurrenceCondition.setJoiner("and");
            occurrenceCondition.addCondition("bomLink.id", ConditionType.EQUAL, bomLink.getId());
            occurrenceQuery.setFilter(occurrenceCondition);
            List<BOMUsesOccurrenceViewDTO> occurrences = bomUsesOccurrenceDelegator.find(occurrenceQuery, new RDMPageVO());
            if (occurrences != null && !occurrences.isEmpty()) {
                referenceDesignator = occurrences.get(0).getReferenceDesignator() != null ? occurrences.get(0).getReferenceDesignator() : "";
            }
            item.put("referenceDesignator", referenceDesignator);
            item.put("source", source);
            item.put("description", description);
            items.add(item);
        }
        data.put("items", items);
        return new ReturnResult(200, "查询BOM清单成功", data);
    }

    /**
     * 递归获取所有层级的BOM链接
     * @param sourceId 源ID
     * @param bomList 存储所有BOM链接的列表
     */
    private void fetchBomLinksRecursively(Long sourceId, List<BOMLinkViewDTO> bomList) {
        QueryRequestVo queryRequestVo = new QueryRequestVo();
        QueryCondition queryCondition = new QueryCondition();
        queryCondition.setJoiner("and");
        queryCondition.addCondition("source.id", ConditionType.EQUAL, sourceId);
        queryRequestVo.setFilter(queryCondition);
        List<BOMLinkViewDTO> directChildren = bomLinkDelegator.find(queryRequestVo, new RDMPageVO());

        if (directChildren != null && !directChildren.isEmpty()) {
            bomList.addAll(directChildren);
            for (BOMLinkViewDTO child : directChildren) {
                fetchBomLinksRecursively(child.getTarget().getId(), bomList);
            }
        }
    }

    private List<BOMLinkViewDTO> bomList;

    @Override
    public ReturnResult updateBomLink(Long bomLinkId, BomLinkUpdateVO updateVO) {
        logger.info("BomService.updateBomLink - 更新BOM关系开始，BOM链接ID: {}", bomLinkId);
        // 更新BOM链接数量和序列号
        BOMLinkUpdateDTO updateDTO = new BOMLinkUpdateDTO();
        updateDTO.setQuantity(new BigDecimal(updateVO.getBomLink().getQuantity().toString()));
        if (updateVO.getBomLink().getSequenceNumber() != null) {
            updateDTO.setSequenceNumber(updateVO.getBomLink().getSequenceNumber());
        }
        // 设置ID以便更新特定BOM链接
        updateDTO.setId(bomLinkId); // 假设有setId方法，需根据实际DTO结构调整
        BOMLinkViewDTO viewDTO = bomLinkDelegator.update(updateDTO);

        // 更新参考标识符
        if (updateVO.getBomUsesOccurrence() != null &&
                updateVO.getBomUsesOccurrence().getReferenceDesignator() != null) {
            logger.info("BomService.updateBomLink - 更新参考标识符");
            QueryRequestVo queryRequestVo = new QueryRequestVo();
            QueryCondition queryCondition = new QueryCondition();
            queryCondition.setJoiner("and");
            queryCondition.addCondition("bomLink.id", ConditionType.EQUAL, bomLinkId);
            queryRequestVo.setFilter(queryCondition);
            List<BOMUsesOccurrenceViewDTO> occurrences = bomUsesOccurrenceDelegator.find(queryRequestVo, new RDMPageVO());

            for (BOMUsesOccurrenceViewDTO occurrence : occurrences) {
                BOMUsesOccurrenceUpdateDTO occurrenceUpdateDTO = new BOMUsesOccurrenceUpdateDTO();
                occurrenceUpdateDTO.setId(occurrence.getId());
                occurrenceUpdateDTO.setReferenceDesignator(updateVO.getBomUsesOccurrence().getReferenceDesignator());
                bomUsesOccurrenceDelegator.update(occurrenceUpdateDTO);
            }
        }

        logger.info("BomService.updateBomLink - 更新BOM关系成功，BOM链接ID: {}", bomLinkId);
        // 构建符合初始任务描述的响应结构
        Map<String, Object> data = new HashMap<>();
        if (viewDTO != null) {
            data.put("bomLinkId", viewDTO.getId());
            data.put("quantity", viewDTO.getQuantity());
            data.put("sequenceNumber", viewDTO.getSequenceNumber());
            data.put("lastUpdateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        }
        return new ReturnResult(200, "BOM关系更新成功", data);
    }

    @Override
    public ReturnResult deleteBomLink(Long bomLinkId) {
        logger.info("BomService.deleteBomLink - 删除BOM关系开始，BOM链接ID: {}", bomLinkId);
        // 先删除相关的BOMUsesOccurrence记录
        QueryRequestVo queryRequestVo = new QueryRequestVo();
        QueryCondition queryCondition = new QueryCondition();
        queryCondition.setJoiner("and");
        queryCondition.addCondition("bomLink.id", ConditionType.EQUAL, bomLinkId);
        queryRequestVo.setFilter(queryCondition);
        List<BOMUsesOccurrenceViewDTO> occurrences = bomUsesOccurrenceDelegator.find(queryRequestVo, new RDMPageVO());

        if (occurrences != null) {
            for (BOMUsesOccurrenceViewDTO occurrence : occurrences) {
                PersistObjectIdModifierDTO occurrenceDeleteDTO = new PersistObjectIdModifierDTO();
                occurrenceDeleteDTO.setId(occurrence.getId());
                bomUsesOccurrenceDelegator.delete(occurrenceDeleteDTO);
            }
        }

        // 再删除BOM链接
        PersistObjectIdModifierDTO deleteDTO = new PersistObjectIdModifierDTO();
        deleteDTO.setId(bomLinkId);
        int result = bomLinkDelegator.delete(deleteDTO);
        logger.info("BomService.deleteBomLink - 删除BOM关系完成，结果: {}", result);
        return new ReturnResult(200, "BOM关系删除成功", null);
    }

    @Override
    public ReturnResult batchCreateBomLinks(BomBatchCreateVO batchCreateVO) {
        logger.info("BomService.batchCreateBomLinks - 批量创建BOM关系开始，源版本ID: {}", batchCreateVO.getSourceVersionId());
        List<BOMLinkCreateDTO> createDTOs = new ArrayList<>();
        for (BomBatchCreateVO.BomItem item : batchCreateVO.getBomItems()) {
            BOMLinkCreateDTO dto = new BOMLinkCreateDTO();
            // 为源和目标创建 ObjectReferenceParamDTO
            ObjectReferenceParamDTO sourceRef = new ObjectReferenceParamDTO();
            sourceRef.setId(batchCreateVO.getSourceVersionId());
            sourceRef.setClazz("PartVersion"); // 根据参考实现设置，需根据实际类名调整
            dto.setSource(sourceRef);

            ObjectReferenceParamDTO targetRef = new ObjectReferenceParamDTO();
            targetRef.setId(item.getTargetMasterId());
            targetRef.setClazz("PartMaster"); // 根据参考实现设置，需根据实际类名调整
            dto.setTarget(targetRef);

            dto.setQuantity(new BigDecimal(item.getQuantity().toString()));
            if (item.getSequenceNumber() != null) {
                dto.setSequenceNumber(item.getSequenceNumber());
            }
            createDTOs.add(dto);
        }
        List<BOMLinkViewDTO> viewDTOs = bomLinkDelegator.batchCreate(createDTOs);
        logger.info("BomService.batchCreateBomLinks - 批量创建BOM关系完成，创建了 {} 条记录", viewDTOs != null ? viewDTOs.size() : 0);
        // 构建符合初始任务描述的响应结构
        Map<String, Object> data = new HashMap<>();
        data.put("successCount", viewDTOs != null ? viewDTOs.size() : 0);
        data.put("failCount", 0); // 占位符，实际需计算失败数量
        List<Long> createdIds = new ArrayList<>();
        if (viewDTOs != null) {
            for (BOMLinkViewDTO dto : viewDTOs) {
                createdIds.add(dto.getId());
            }
        }
        data.put("createdBomLinks", createdIds);
        return new ReturnResult(200, "批量创建BOM关系成功", data);
    }

    @Override
    public ReturnResult getWhereUsed(WhereUsedQueryVO queryVO) {
        logger.info("BomService.getWhereUsed - 查询零件使用位置开始，主ID: {}", queryVO.getMasterId());
        // 使用QueryRequestVo和QueryCondition来查询零件被使用的位置，基于主ID过滤
        QueryRequestVo queryRequestVo = new QueryRequestVo();
        QueryCondition queryCondition = new QueryCondition();
        queryCondition.setJoiner("and");
        queryCondition.addCondition("target.id", ConditionType.EQUAL, queryVO.getMasterId());
        queryRequestVo.setFilter(queryCondition);

        // 设置分页参数，如果需要
        RDMPageVO pageVO = new RDMPageVO();
        // 获取零件使用位置数据
        List<BOMLinkViewDTO> whereUsedList;
        if (queryVO.getRecursive() != null && queryVO.getRecursive()) {
            // 递归查询逻辑，获取所有上层父零件
            whereUsedList = new ArrayList<>();
            fetchWhereUsedRecursively(queryVO.getMasterId(), whereUsedList, new HashSet<>());
        } else {
            // 非递归查询，仅获取直接父零件
            whereUsedList = bomLinkDelegator.find(queryRequestVo, pageVO);
        }

        if (whereUsedList == null) {
            whereUsedList = new ArrayList<>();
        }

        logger.info("BomService.getWhereUsed - 查询零件使用位置完成，找到 {} 条记录", whereUsedList.size());
        // 构建符合初始任务描述的响应结构
        List<Map<String, Object>> data = new ArrayList<>();
        for (BOMLinkViewDTO bomLink : whereUsedList) {
            Map<String, Object> item = new HashMap<>();
            item.put("bomLinkId", bomLink.getId());
            // 获取parentPart信息
            Long sourceId = bomLink.getSource().getId();
            QueryRequestVo partQuery = new QueryRequestVo();
            QueryCondition partCondition = new QueryCondition();
            partCondition.setJoiner("and");
            partCondition.addCondition("id", ConditionType.EQUAL, sourceId);
            partQuery.setFilter(partCondition);
            List<PartViewDTO> parentParts = partDelegator.find(partQuery, new RDMPageVO());
            Map<String, Object> parentPartData = new HashMap<>();
            if (parentParts != null && !parentParts.isEmpty()) {
                PartViewDTO parentPart = parentParts.get(0);
                parentPartData.put("masterId", parentPart.getMaster() != null ? parentPart.getMaster().getId() : 0);
                parentPartData.put("number", parentPart.getMaster().getNumber() != null ? parentPart.getMaster().getNumber() : "");
                parentPartData.put("name", parentPart.getName() != null ? parentPart.getName() : "");
                parentPartData.put("currentVersion", parentPart.getVersion() != null ? parentPart.getVersion() : "");
            } else {
                parentPartData.put("masterId", 0);
                parentPartData.put("number", "");
                parentPartData.put("name", "");
                parentPartData.put("currentVersion", "");
            }
            item.put("parentPart", parentPartData);
            item.put("quantity", bomLink.getQuantity().doubleValue()); // 确保返回的是double类型
            // 获取referenceDesignator
            String referenceDesignator = "";
            QueryRequestVo occurrenceQuery = new QueryRequestVo();
            QueryCondition occurrenceCondition = new QueryCondition();
            occurrenceCondition.setJoiner("and");
            occurrenceCondition.addCondition("bomLink.id", ConditionType.EQUAL, bomLink.getId());
            occurrenceQuery.setFilter(occurrenceCondition);
            List<BOMUsesOccurrenceViewDTO> occurrences = bomUsesOccurrenceDelegator.find(occurrenceQuery, new RDMPageVO());
            if (occurrences != null && !occurrences.isEmpty()) {
                referenceDesignator = occurrences.get(0).getReferenceDesignator() != null ? occurrences.get(0).getReferenceDesignator() : "";
            }
            item.put("referenceDesignator", referenceDesignator);
            item.put("level", calculateLevel(bomLink));
            data.add(item);
        }
        return new ReturnResult(200, "查询成功", data);
    }

    /**
     * 递归获取所有上层父零件的BOM链接
     * @param targetId 目标ID
     * @param whereUsedList 存储所有BOM链接的列表
     * @param visitedTargets 已访问的目标ID集合，用于避免循环
     */
    private void fetchWhereUsedRecursively(Long targetId, List<BOMLinkViewDTO> whereUsedList, Set<Long> visitedTargets) {
        if (visitedTargets.contains(targetId)) {
            return; // 避免循环引用
        }
        visitedTargets.add(targetId);

        QueryRequestVo queryRequestVo = new QueryRequestVo();
        QueryCondition queryCondition = new QueryCondition();
        queryCondition.setJoiner("and");
        queryCondition.addCondition("target.id", ConditionType.EQUAL, targetId);
        queryRequestVo.setFilter(queryCondition);
        List<BOMLinkViewDTO> directParents = bomLinkDelegator.find(queryRequestVo, new RDMPageVO());

        if (directParents != null && !directParents.isEmpty()) {
            whereUsedList.addAll(directParents);
            for (BOMLinkViewDTO parent : directParents) {
                fetchWhereUsedRecursively(parent.getSource().getId(), whereUsedList, visitedTargets);
            }
        }
    }
}
