package com.huawei.minibom.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huawei.innovation.rdm.coresdk.basic.dto.*;
import com.huawei.innovation.rdm.coresdk.basic.enums.ConditionType;
import com.huawei.innovation.rdm.coresdk.basic.vo.QueryCondition;
import com.huawei.innovation.rdm.coresdk.basic.vo.QueryRequestVo;
import com.huawei.innovation.rdm.coresdk.basic.vo.RDMPageVO;
import com.huawei.innovation.rdm.coresdk.extattrmgmt.dto.EXAValueParamDTO;
import com.huawei.innovation.rdm.minibomdatamodel.bean.enumerate.AssemblyMode;
import com.huawei.innovation.rdm.minibomdatamodel.bean.enumerate.PartSource2;
import com.huawei.innovation.rdm.minibomdatamodel.delegator.PartDelegator;
import com.huawei.innovation.rdm.minibomdatamodel.dto.entity.*;
import com.huawei.minibom.result.ReturnCode;
import com.huawei.minibom.result.ReturnResult;
import com.huawei.minibom.service.IPartService;
import com.huawei.minibom.vo.PartCreateFormVO;
import com.huawei.minibom.vo.PartQueryParamVO;
import com.huawei.minibom.vo.PartUpdateFormVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Part管理服务实现类
 */
@Service
public class PartService implements IPartService {
    
    private static final Logger logger = LoggerFactory.getLogger(PartService.class);
    
    @Autowired
    private PartDelegator partDelegator;
    
    @Override
    public ReturnResult createPart(PartCreateFormVO partCreateFormVO) {
        try {
            logger.info("创建Part: {}", partCreateFormVO.getMaster().getName());
            
            // 现在使用正确的枚举值，不需要特殊转换
            
            // 构建Part创建DTO
            PartCreateDTO partCreateDTO = new PartCreateDTO();
            
            // 设置基本属性
            if (partCreateFormVO.getVersion().getIterationNote() != null) {
                partCreateDTO.setIterationNote(partCreateFormVO.getVersion().getIterationNote());
            }
            
            // 设置枚举值 - 根据SDK中的实际枚举值
            if (StringUtils.hasText(partCreateFormVO.getVersion().getSource())) {
                try {
                    partCreateDTO.setSource(parsePartSource(partCreateFormVO.getVersion().getSource()));
                } catch (Exception e) {
                    logger.warn("无法解析source枚举值: {}, 使用默认值PartSource", partCreateFormVO.getVersion().getSource());
                    partCreateDTO.setSource(PartSource2.PartSource);
                }
            }
            
            if (StringUtils.hasText(partCreateFormVO.getVersion().getPartType())) {
                try {
                    partCreateDTO.setPartType(parseAssemblyMode(partCreateFormVO.getVersion().getPartType()));
                } catch (Exception e) {
                    logger.warn("无法解析partType枚举值: {}, 使用默认值AssemblyMode", partCreateFormVO.getVersion().getPartType());
                    partCreateDTO.setPartType(AssemblyMode.AssemblyMode);
                }
            }
            
            // 设置version对象的基本属性
            if (StringUtils.hasText(partCreateFormVO.getVersion().getName())) {
                partCreateDTO.setName(partCreateFormVO.getVersion().getName());
            }
            
            if (StringUtils.hasText(partCreateFormVO.getVersion().getDescription())) {
                partCreateDTO.setDescription(partCreateFormVO.getVersion().getDescription());
            }
            
            // 设置master对象
            PartMasterCreateDTO masterDTO = new PartMasterCreateDTO();
            masterDTO.setName(partCreateFormVO.getMaster().getName());
            if (partCreateFormVO.getMaster().getEndPart() != null) {
                masterDTO.setEndPart(partCreateFormVO.getMaster().getEndPart());
            }
            if (partCreateFormVO.getMaster().getPhantomPart() != null) {
                masterDTO.setPhantomPart(partCreateFormVO.getMaster().getPhantomPart());
            }
            partCreateDTO.setMaster(masterDTO);
            
            // 设置branch对象
            partCreateDTO.setBranch(new PartBranchCreateDTO());
            
            // 设置分类属性
            if (partCreateFormVO.getVersion().getClsAttrs() != null && !partCreateFormVO.getVersion().getClsAttrs().isEmpty()) {
                try {
                    logger.info("🔍 开始设置分类属性，接收到的属性: {}", partCreateFormVO.getVersion().getClsAttrs());
                    
                    // 🎯 参考师兄项目：创建包装对象，把分类属性放在Classification字段下
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.fluentPut("Classification", partCreateFormVO.getVersion().getClsAttrs());
                    JSONArray clsAttrsArray = new JSONArray();
                    clsAttrsArray.add(jsonObject);
                    partCreateDTO.setClsAttrs(clsAttrsArray);
                    
                    logger.info("✅ 成功设置分类属性到PartCreateDTO: 包装结构={}", clsAttrsArray.toJSONString());
                               
                } catch (Exception e) {
                    logger.error("❌ 设置分类属性失败: {}", e.getMessage(), e);
                    logger.warn("继续创建Part，但分类属性可能丢失");
                }
            } else {
                logger.info("ℹ️ 没有提供分类属性，跳过分类属性设置");
            }
            
            // 设置分类ID - 通过扩展属性设置（参考师兄项目实现）
            if (StringUtils.hasText(partCreateFormVO.getVersion().getClassificationId())) {
                try {
                    logger.info("开始设置分类ID，接收到的分类ID: {}", partCreateFormVO.getVersion().getClassificationId());
                    
                    // 验证分类ID是否有效（转换为Long测试）
                    Long classificationIdLong = Long.valueOf(partCreateFormVO.getVersion().getClassificationId());
                    logger.info("分类ID转换为Long成功: {}", classificationIdLong);
                    
                    List<EXAValueParamDTO> extAttrs = new ArrayList<>();
                    EXAValueParamDTO exaValueParamDTO = new EXAValueParamDTO();
                    exaValueParamDTO.setName("Classification");
                    exaValueParamDTO.setValue(classificationIdLong);
                    extAttrs.add(exaValueParamDTO);
                    partCreateDTO.setExtAttrs(extAttrs);
                    
                    logger.info("✅ 成功设置分类ID到PartCreateDTO扩展属性: ID={}, 扩展属性数量={}", 
                               classificationIdLong, extAttrs.size());
                } catch (NumberFormatException e) {
                    logger.error("❌ 分类ID格式错误，无法转换为数字: {}", partCreateFormVO.getVersion().getClassificationId(), e);
                } catch (Exception e) {
                    logger.warn("❌ 设置分类ID失败，继续创建Part: {}", e.getMessage(), e);
                }
            } else {
                logger.warn("⚠️ 没有提供分类ID，跳过分类设置");
            }
            
            // 创建Part
            PartViewDTO partViewDTO = partDelegator.create(partCreateDTO);
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("masterId", partViewDTO.getMaster().getId());
            result.put("versionId", partViewDTO.getId());
            result.put("number", partViewDTO.getMaster().getNumber());
            result.put("name", partViewDTO.getMaster().getName());
            result.put("version", partViewDTO.getVersion());
            result.put("iteration", partViewDTO.getIteration());
            result.put("workingState", partViewDTO.getWorkingState());
            
            return new ReturnResult(ReturnCode.INSERT_OK, "Part创建成功", result);
            
        } catch (Exception e) {
            logger.error("创建Part失败", e);
            return new ReturnResult(ReturnCode.INSERT_ERR, "Part创建失败: " + e.getMessage(), null);
        }
    }
    
    @Override
    public ReturnResult queryParts(PartQueryParamVO partQueryParamVO) {
        try {
            logger.info("查询Part列表: name={}, number={}", partQueryParamVO.getName(), partQueryParamVO.getNumber());
            
            // 构建查询条件
            QueryRequestVo queryRequestVo = new QueryRequestVo();
            QueryCondition queryCondition = new QueryCondition();
            
            List<QueryCondition> conditions = new ArrayList<>();
            
            // 添加查询条件
            if (StringUtils.hasText(partQueryParamVO.getName())) {
                conditions.add(new QueryCondition("master.name", "like", partQueryParamVO.getName()));
            }
            
            if (StringUtils.hasText(partQueryParamVO.getNumber())) {
                conditions.add(new QueryCondition("master.number", "=", partQueryParamVO.getNumber()));
            }
            
            if (StringUtils.hasText(partQueryParamVO.getSource())) {
                conditions.add(new QueryCondition("source", "=", partQueryParamVO.getSource()));
            }
            
            if (StringUtils.hasText(partQueryParamVO.getPartType())) {
                conditions.add(new QueryCondition("partType", "=", partQueryParamVO.getPartType()));
            }
            
            if (StringUtils.hasText(partQueryParamVO.getWorkingState())) {
                conditions.add(new QueryCondition("workingState", "=", partQueryParamVO.getWorkingState()));
            }
            
            if (!conditions.isEmpty()) {
                queryCondition.setJoiner("and");
                queryCondition.setConditions(conditions);
                queryRequestVo.setFilter(queryCondition);
            }
            
            // 设置分页
            RDMPageVO pageVO = new RDMPageVO();
            pageVO.setCurPage(partQueryParamVO.getPage());
            pageVO.setPageSize(partQueryParamVO.getSize());
            
            // 打印调试信息
            System.out.println("【调试】QueryCondition: " + queryCondition);
            logger.info("【调试】QueryCondition: {}", queryCondition);
            
            // 也可以打印整个请求体
            System.out.println("【调试】QueryRequestVo: " + queryRequestVo);
            logger.info("【调试】QueryRequestVo: {}", queryRequestVo);
            
            // 执行分页查询
            List<PartViewDTO> partList = partDelegator.find(queryRequestVo, pageVO);
            
            // 查询总数
            long total = partDelegator.count(queryRequestVo);
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("total", total);
            result.put("page", partQueryParamVO.getPage());
            result.put("size", partQueryParamVO.getSize());
            result.put("records", convertToResultList(partList));
            
            return new ReturnResult(ReturnCode.GET_OK, "查询成功", result);
            
        } catch (Exception e) {
            logger.error("查询Part列表失败", e);
            return new ReturnResult(ReturnCode.GET_ERR, "查询失败: " + e.getMessage(), null);
        }
    }
    
    @Override
    public ReturnResult getPartDetail(Long masterId) {
        try {
            logger.info("获取Part详情: masterId={}", masterId);
            
            // 使用find方法查询指定masterId的所有版本
            QueryRequestVo queryRequestVo = new QueryRequestVo();
            QueryCondition queryCondition = new QueryCondition();
            queryCondition.setJoiner("and");
            queryCondition.addCondition("master.id", ConditionType.EQUAL, masterId);
            queryRequestVo.setFilter(queryCondition);
            
            // 设置分页参数，查询所有版本
            RDMPageVO pageVO = new RDMPageVO();
            pageVO.setCurPage(1);
            pageVO.setPageSize(100); // 设置较大的分页大小以获取所有版本
            
            List<PartViewDTO> versions = partDelegator.find(queryRequestVo, pageVO);
            
            if (versions == null || versions.isEmpty()) {
                return new ReturnResult(ReturnCode.GET_ERR, "Part不存在", null);
            }
            
            // 获取最新迭代
            PartViewDTO latestVersion = getLatestIteration(versions);
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("masterId", latestVersion.getMaster().getId());
            result.put("number", latestVersion.getMaster().getNumber());
            result.put("name", latestVersion.getMaster().getName());
            result.put("description", latestVersion.getDescription());
            result.put("endPart", latestVersion.getMaster().getEndPart());
            result.put("phantomPart", latestVersion.getMaster().getPhantomPart());
            result.put("creator", latestVersion.getCreator());
            result.put("createTime", latestVersion.getCreateTime());
            
            // 当前版本信息
            Map<String, Object> currentVersion = new HashMap<>();
            currentVersion.put("versionId", latestVersion.getId());
            currentVersion.put("version", latestVersion.getVersion());
            currentVersion.put("iteration", latestVersion.getIteration());
            currentVersion.put("source", latestVersion.getSource());
            currentVersion.put("partType", latestVersion.getPartType());
            currentVersion.put("workingState", latestVersion.getWorkingState());
            currentVersion.put("latestIteration", latestVersion.getLatestIteration());
            currentVersion.put("workingCopy", latestVersion.getWorkingCopy());
            currentVersion.put("checkOutUserName", latestVersion.getCheckOutUserName());
            currentVersion.put("iterationNote", latestVersion.getIterationNote());
            currentVersion.put("clsAttrs", latestVersion.getClsAttrs());
            
            result.put("currentVersion", currentVersion);
            
            return new ReturnResult(ReturnCode.GET_OK, "查询成功", result);
            
        } catch (Exception e) {
            logger.error("获取Part详情失败", e);
            return new ReturnResult(ReturnCode.GET_ERR, "查询失败: " + e.getMessage(), null);
        }
    }
    
    @Override
    public ReturnResult updatePart(PartUpdateFormVO partUpdateFormVO) {
        try {
            logger.info("更新Part: masterId={}", partUpdateFormVO.getMasterId());
            
            // 构建更新DTO
            VersionUpdateAndCheckinDTO<PartUpdateByAdminDTO> updateDTO = new VersionUpdateAndCheckinDTO<>();
            updateDTO.setMasterId(partUpdateFormVO.getMasterId());
            
            PartUpdateByAdminDTO partUpdateData = new PartUpdateByAdminDTO();
            
            // 设置更新数据
            if (StringUtils.hasText(partUpdateFormVO.getSource())) {
                partUpdateData.setSource(parsePartSource(partUpdateFormVO.getSource()));
            }
            
            if (StringUtils.hasText(partUpdateFormVO.getPartType())) {
                partUpdateData.setPartType(parseAssemblyMode(partUpdateFormVO.getPartType()));
            }
            
            // 设置迭代备注
            if (StringUtils.hasText(partUpdateFormVO.getIterationNote())) {
                partUpdateData.setIterationNote(partUpdateFormVO.getIterationNote());
            }
            
            // 设置分类属性
            if (partUpdateFormVO.getClsAttrs() != null) {
                JSONArray clsAttrs = new JSONArray();
                clsAttrs.add(partUpdateFormVO.getClsAttrs());
                partUpdateData.setClsAttrs(clsAttrs);
            }
            
            updateDTO.setData(partUpdateData);
            
            // 执行更新并检入
            PartViewDTO updatedPart = partDelegator.updateAndCheckin(updateDTO);
            
            Map<String, Object> result = new HashMap<>();
            result.put("versionId", updatedPart.getId());
            result.put("iteration", updatedPart.getIteration());
            result.put("lastUpdateTime", updatedPart.getLastUpdateTime());
            
            return new ReturnResult(ReturnCode.UPDATE_OK, "Part更新成功", result);
            
        } catch (Exception e) {
            logger.error("更新Part失败", e);
            return new ReturnResult(ReturnCode.UPDATE_ERR, "更新失败: " + e.getMessage(), null);
        }
    }
    
    @Override
    public ReturnResult deletePart(Long masterId) {
        try {
            logger.info("删除Part: masterId={}", masterId);
            
            MasterIdModifierDTO deleteDTO = new MasterIdModifierDTO();
            deleteDTO.setMasterId(masterId);
            
            int result = partDelegator.delete(deleteDTO);
            
            if (result > 0) {
                return new ReturnResult(ReturnCode.DELETE_OK, "Part删除成功", null);
            } else {
                return new ReturnResult(ReturnCode.DELETE_ERR, "Part删除失败", null);
            }
            
        } catch (Exception e) {
            logger.error("删除Part失败", e);
            return new ReturnResult(ReturnCode.DELETE_ERR, "删除失败: " + e.getMessage(), null);
        }
    }
    
    @Override
    public ReturnResult checkoutPart(Long masterId) {
        try {
            logger.info("检出Part: masterId={}", masterId);
            
            VersionCheckOutDTO checkoutDTO = new VersionCheckOutDTO();
            checkoutDTO.setMasterId(masterId);
            
            PartViewDTO checkedOutPart = partDelegator.checkout(checkoutDTO);
            
            Map<String, Object> result = new HashMap<>();
            result.put("versionId", checkedOutPart.getId());
            result.put("workingCopy", checkedOutPart.getWorkingCopy());
            result.put("checkOutUserName", checkedOutPart.getCheckOutUserName());
            result.put("checkOutTime", checkedOutPart.getLastUpdateTime());
            
            return new ReturnResult(ReturnCode.BUSINESS_OK, "Part检出成功", result);
            
        } catch (Exception e) {
            logger.error("检出Part失败", e);
            return new ReturnResult(ReturnCode.BUSINESS_ERR, "检出失败: " + e.getMessage(), null);
        }
    }
    
    @Override
    public ReturnResult undoCheckout(Long masterId) {
        try {
            logger.info("撤销检出Part: masterId={}", masterId);
            
            VersionUndoCheckOutDTO undoDTO = new VersionUndoCheckOutDTO();
            undoDTO.setMasterId(masterId);
            
            partDelegator.undoCheckout(undoDTO);
            
            return new ReturnResult(ReturnCode.BUSINESS_OK, "撤销检出成功", null);
            
        } catch (Exception e) {
            logger.error("撤销检出失败", e);
            return new ReturnResult(ReturnCode.BUSINESS_ERR, "撤销检出失败: " + e.getMessage(), null);
        }
    }
    
    @Override
    public ReturnResult checkinPart(Long masterId, String iterationNote) {
        try {
            logger.info("检入Part: masterId={}", masterId);
            
            VersionCheckInDTO checkinDTO = new VersionCheckInDTO();
            checkinDTO.setMasterId(masterId);
            // VersionCheckInDTO基本功能是检入，迭代备注通过其他方式设置
            // 在华为云SDK中，迭代备注通常在Version对象创建时设置
            
            PartViewDTO checkedInPart = partDelegator.checkin(checkinDTO);
            
            Map<String, Object> result = new HashMap<>();
            result.put("versionId", checkedInPart.getId());
            result.put("iteration", checkedInPart.getIteration());
            result.put("workingState", checkedInPart.getWorkingState());
            result.put("checkinTime", checkedInPart.getLastUpdateTime());
            
            return new ReturnResult(ReturnCode.BUSINESS_OK, "Part检入成功", result);
            
        } catch (Exception e) {
            logger.error("检入Part失败", e);
            return new ReturnResult(ReturnCode.BUSINESS_ERR, "检入失败: " + e.getMessage(), null);
        }
    }
    
    @Override
    public ReturnResult getPartVersions(Long masterId) {
        try {
            logger.info("获取Part版本历史: masterId={}", masterId);
            
            VersionMasterQueryDTO masterQueryDTO = new VersionMasterQueryDTO();
            masterQueryDTO.setMasterId(masterId);
            masterQueryDTO.setVersion("A");
            
            List<PartViewDTO> versions = partDelegator.getVersionByMaster(masterQueryDTO);
            
            logger.info("versions: {}", versions);
            if (versions != null) {
                for (PartViewDTO version : versions) {
                    logger.info("versionId: {}, version: {}", version.getId(), version.getVersion());
                }
            }
            
            if (versions == null || versions.isEmpty()) {
                return new ReturnResult(ReturnCode.GET_ERR, "Part不存在", null);
            }
            
            List<Map<String, Object>> versionList = new ArrayList<>();
            for (PartViewDTO version : versions) {
                Map<String, Object> versionInfo = new HashMap<>();
                versionInfo.put("versionId", version.getId());
                versionInfo.put("version", version.getVersion());
                versionInfo.put("iteration", version.getIteration());
                versionInfo.put("workingState", version.getWorkingState());
                versionInfo.put("latestIteration", version.getLatestIteration());
                versionInfo.put("creator", version.getCreator());
                versionInfo.put("createTime", version.getCreateTime());
                versionInfo.put("iterationNote", version.getIterationNote());
                versionList.add(versionInfo);
            }
            
            return new ReturnResult(ReturnCode.GET_OK, "查询成功", versionList);
            
        } catch (Exception e) {
            logger.error("获取Part版本历史失败", e);
            return new ReturnResult(ReturnCode.GET_ERR, "查询失败: " + e.getMessage(), null);
        }
    }
    
    @Override
    public ReturnResult getVersionDetail(Long versionId) {
        try {
            logger.info("获取版本详情: versionId={}", versionId);
            
            PersistObjectIdDecryptDTO getDTO = new PersistObjectIdDecryptDTO();
            getDTO.setId(versionId);
            
            PartViewDTO version = partDelegator.get(getDTO);
            
            if (version == null) {
                return new ReturnResult(ReturnCode.GET_ERR, "版本不存在", null);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("versionId", version.getId());
            result.put("masterId", version.getMaster().getId());
            result.put("number", version.getMaster().getNumber());
            result.put("name", version.getMaster().getName());
            result.put("version", version.getVersion());
            result.put("iteration", version.getIteration());
            result.put("source", version.getSource());
            result.put("partType", version.getPartType());
            result.put("workingState", version.getWorkingState());
            result.put("iterationNote", version.getIterationNote());
            result.put("clsAttrs", version.getClsAttrs());
            result.put("creator", version.getCreator());
            result.put("createTime", version.getCreateTime());
            
            return new ReturnResult(ReturnCode.GET_OK, "查询成功", result);
            
        } catch (Exception e) {
            logger.error("获取版本详情失败", e);
            return new ReturnResult(ReturnCode.GET_ERR, "查询失败: " + e.getMessage(), null);
        }
    }
    
    @Override
    public ReturnResult getEnumValues(String enumType) {
        try {
            logger.info("获取枚举值: enumType={}", enumType);
            
            List<Map<String, Object>> enumValues = new ArrayList<>();
            
            if ("PartSource".equals(enumType) || "PartSource2".equals(enumType)) {
                // 根据华为云平台配置的PartSource2枚举值
                Map<String, Object> enumValue1 = new HashMap<>();
                enumValue1.put("value", "PartSource");
                enumValue1.put("label", "PartSource");
                enumValue1.put("description", "默认Part来源");
                enumValues.add(enumValue1);
                
                Map<String, Object> enumValue2 = new HashMap<>();
                enumValue2.put("value", "Buy");
                enumValue2.put("label", "购买");
                enumValue2.put("description", "外购零件");
                enumValues.add(enumValue2);
                
                Map<String, Object> enumValue3 = new HashMap<>();
                enumValue3.put("value", "Make");
                enumValue3.put("label", "制造");
                enumValue3.put("description", "自主制造零件");
                enumValues.add(enumValue3);
                
            } else if ("AssemblyMode".equals(enumType)) {
                // 根据华为云平台配置的最新AssemblyMode枚举值
                Map<String, Object> enumValue1 = new HashMap<>();
                enumValue1.put("value", "AssemblyMode");
                enumValue1.put("label", "AssemblyMode");
                enumValue1.put("description", "AssemblyMode");
                enumValues.add(enumValue1);
                
                Map<String, Object> enumValue2 = new HashMap<>();
                enumValue2.put("value", "Part");
                enumValue2.put("label", "零件");
                enumValue2.put("description", "零件");
                enumValues.add(enumValue2);
                
                Map<String, Object> enumValue3 = new HashMap<>();
                enumValue3.put("value", "Separable");
                enumValue3.put("label", "可分离");
                enumValue3.put("description", "可分离");
                enumValues.add(enumValue3);
                
                Map<String, Object> enumValue4 = new HashMap<>();
                enumValue4.put("value", "Inseparable");
                enumValue4.put("label", "不可分离");
                enumValue4.put("description", "不可分离");
                enumValues.add(enumValue4);
                
            } else if ("WorkingState".equals(enumType)) {
                // Part工作状态
                Map<String, Object> enumValue1 = new HashMap<>();
                enumValue1.put("value", "WORKING");
                enumValue1.put("label", "工作中");
                enumValue1.put("description", "正在编辑的状态");
                enumValues.add(enumValue1);
                
                Map<String, Object> enumValue2 = new HashMap<>();
                enumValue2.put("value", "RELEASED");
                enumValue2.put("label", "已发布");
                enumValue2.put("description", "已发布的稳定版本");
                enumValues.add(enumValue2);
                
                Map<String, Object> enumValue3 = new HashMap<>();
                enumValue3.put("value", "SUPERSEDED");
                enumValue3.put("label", "已替代");
                enumValue3.put("description", "被新版本替代");
                enumValues.add(enumValue3);
                
                Map<String, Object> enumValue4 = new HashMap<>();
                enumValue4.put("value", "OBSOLETE");
                enumValue4.put("label", "已废弃");
                enumValue4.put("description", "已废弃不再使用");
                enumValues.add(enumValue4);
                
            } else {
                return new ReturnResult(ReturnCode.GET_ERR, "不支持的枚举类型: " + enumType, null);
            }
            
            return new ReturnResult(ReturnCode.GET_OK, "查询成功", enumValues);
            
        } catch (Exception e) {
            logger.error("获取枚举值失败", e);
            return new ReturnResult(ReturnCode.GET_ERR, "查询失败: " + e.getMessage(), null);
        }
    }
    
    // 辅助方法
    private PartSource2 parsePartSource(String source) {
        return PartSource2.getItem(source);
    }
    
    private AssemblyMode parseAssemblyMode(String partType) {
        return AssemblyMode.getItem(partType);
    }
    
    private PartViewDTO getLatestIteration(List<PartViewDTO> versions) {
        PartViewDTO latest = versions.get(0);
        for (PartViewDTO version : versions) {
            if (version.getIteration() > latest.getIteration()) {
                latest = version;
            }
        }
        return latest;
    }
    
    private List<Map<String, Object>> convertToResultList(List<PartViewDTO> partList) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        if (partList != null) {
            for (PartViewDTO part : partList) {
                Map<String, Object> partInfo = new HashMap<>();
                partInfo.put("masterId", String.valueOf(part.getMaster().getId()));
                partInfo.put("versionId", String.valueOf(part.getId()));
                partInfo.put("number", part.getMaster().getNumber());
                partInfo.put("name", part.getMaster().getName());
                partInfo.put("description", part.getDescription());
                partInfo.put("version", part.getVersion());
                partInfo.put("iteration", part.getIteration());
                partInfo.put("source", part.getSource());
                partInfo.put("partType", part.getPartType());
                partInfo.put("workingState", part.getWorkingState());
                partInfo.put("latestIteration", part.getLatestIteration());
                partInfo.put("latestVersion", part.getLatestVersion());
                partInfo.put("creator", part.getCreator());
                partInfo.put("createTime", part.getCreateTime());
                partInfo.put("modifier", part.getModifier());
                partInfo.put("lastUpdateTime", part.getLastUpdateTime());
                results.add(partInfo);
            }
        }
        
        return results;
    }
} 


