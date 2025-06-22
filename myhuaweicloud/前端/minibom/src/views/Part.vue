<template>
  <div class="page-wrapper">
    <!-- 顶部操作区 -->    <div class="top-action-bar">
      <h2 class="page-title">Part&BOM管理</h2>      <div class="actions">
        <el-button class="add-button" @click="showBatchAddDialog">
          <el-icon><Plus /></el-icon><span>批量添加BOM</span>
        </el-button>
        <el-button class="add-button" @click="showAddDialog">
          <el-icon><Plus /></el-icon><span>添加Part</span>
        </el-button>
        <el-button type="info" @click="testBomAPI">
          <el-icon><Setting /></el-icon><span>测试BOM接口</span>
        </el-button>
      </div>
    </div>    <!-- 查找功能表单 -->
    <el-form :inline="true" class="search-form" style="margin-bottom: 16px;">
      <el-form-item label="名称">
        <el-input v-model="searchParams.name" placeholder="请输入名称"></el-input>
      </el-form-item>
      <el-form-item label="编码">
        <el-input v-model="searchParams.number" placeholder="请输入编码"></el-input>
      </el-form-item>
      <el-form-item label="来源">
        <el-select v-model="searchParams.source" placeholder="请选择来源" clearable style="width: 180px;">
          <el-option v-for="item in sourceEnum" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="装配模式">
        <el-select v-model="searchParams.partType" placeholder="请选择装配模式" clearable style="width: 180px;">
          <el-option v-for="item in partTypeEnum" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>      <el-form-item>
        <el-button type="primary" @click="onSearch">搜索</el-button>
        <el-button @click="onReset">重置</el-button>        <el-button @click="handleRetry" :loading="loading">重新加载</el-button>
        <el-button @click="preloadPartNames" :loading="loading" type="info">预加载Part缓存</el-button>
      </el-form-item></el-form>    <!-- Part列表 -->
    <el-table :data="partList" style="width: 100%" v-loading="loading" class="tech-table">
      <el-table-column prop="name" label="Part Name" sortable></el-table-column>
      <el-table-column 
        prop="masterId" 
        label="masterId" 
        sortable
        :formatter="row => String(row.masterId ?? '')"
        show-overflow-tooltip
        width="240"
        align="left"
      ></el-table-column>
      <el-table-column prop="description" label="description" width="260"></el-table-column>
      <el-table-column label="操作" width="400" align="center">
        <template #default="{ row }">
          <div class="action-buttons">
            <el-tooltip content="查看" placement="top">
              <el-button link :icon="View" class="action-button view" @click="showDetailDialog(row)"></el-button>
            </el-tooltip>
            <el-tooltip content="检出" placement="top">
              <el-button link :icon="Lock" class="action-button checkout" @click="onCheckout(row)"></el-button>
            </el-tooltip>
            <el-tooltip content="撤销检出" placement="top">
              <el-button link :icon="Unlock" class="action-button undo-checkout" @click="onUndoCheckout(row)"></el-button>
            </el-tooltip>
            <el-tooltip content="编辑" placement="top">
              <el-button link :icon="Edit" class="action-button edit" @click="showEditDialog(row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button link :icon="Delete" class="action-button delete" @click="onDelete(row)"></el-button>
            </el-tooltip>
            <el-tooltip content="BOM结构树" placement="top">
              <el-button link :icon="Share" class="action-button tree" @click="showBomTree(row)">结构树</el-button>
            </el-tooltip>
            <el-tooltip content="BOM清单" placement="top">
              <el-button link :icon="List" class="action-button list" @click="showBomList(row)">清单</el-button>
            </el-tooltip>
            <el-tooltip content="父级使用" placement="top">
              <el-button link :icon="TopRight" class="action-button usage" @click="showWhereUsed(row)">父级使用</el-button>
            </el-tooltip>
            <el-tooltip content="添加BOM关系" placement="top">
              <el-button link :icon="Plus" class="action-button add" @click="showAddBomDialog(row)">添加BOM</el-button>
            </el-tooltip>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      layout="total, prev, pager, next, jumper"
      @current-change="handlePageChange"
      @size-change="handleSizeChange"
      style="margin-top: 16px; text-align: right;"
    />    <!-- 添加/编辑Part的弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="40%" class="tech-dialog">
      <el-form :model="partModel" :rules="formRules" ref="formRef" label-width="100px" label-position="top">
        <el-form-item label="Part Name" prop="name" required>
          <el-input v-model="partModel.name"></el-input>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="partModel.description" type="textarea" placeholder="请输入描述"></el-input>
        </el-form-item>
        <el-form-item label="来源" prop="source" required>
          <el-select v-model="partModel.source" placeholder="请选择来源">
            <el-option v-for="item in sourceEnum" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="装配模式" prop="partType" required>
          <el-select v-model="partModel.partType" placeholder="请选择装配模式">
            <el-option v-for="item in partTypeEnum" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="迭代备注" prop="iterationNote" required>
          <el-input v-model="partModel.iterationNote"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button class="reset-button" @click="dialogVisible = false">取 消</el-button>
          <el-button class="add-button" :disabled="!formValid" @click="onSubmit">确 认</el-button>
        </span>
      </template>
    </el-dialog><el-dialog v-model="detailDialogVisible" title="Part详情" width="40%">
      <div v-if="partDetail">
        <p><b>masterId:</b> {{ String(partDetail.masterId) }}</p>
        <p><b>name:</b> {{ partDetail.name }}</p>
        <p><b>description:</b> {{ partDetail.description }}</p>
        <p><b>creator:</b> {{ partDetail.creator }}</p>
        <p><b>createTime:</b> {{ partDetail.createTime }}</p>
        <p><b>endPart:</b> {{ partDetail.endPart ? '是' : '否' }}</p>
        <p><b>phantomPart:</b> {{ partDetail.phantomPart ? '是' : '否' }}</p>
        <p><b>当前版本:</b></p>
        <ul>
          <li><b>versionId:</b> {{ String(partDetail.currentVersion?.versionId) }}</li>
          <li><b>version:</b> {{ partDetail.currentVersion?.version }}</li>
          <li><b>iteration:</b> {{ partDetail.currentVersion?.iteration }}</li>
          <li><b>source:</b> {{ partDetail.currentVersion?.source }}</li>
          <li><b>partType:</b> {{ partDetail.currentVersion?.partType }}</li>
          <li><b>workingState:</b> {{ partDetail.currentVersion?.workingState }}</li>
          <li><b>iterationNote:</b> {{ partDetail.currentVersion?.iterationNote }}</li>
        </ul>
      </div>
    </el-dialog>

    <!-- BOM结构树弹窗 -->
    <el-dialog v-model="bomTreeDialogVisible" title="BOM结构树" width="80%" class="tech-dialog">
      <div v-if="bomTreeData">        <div class="tree-header">
          <h3>根节点: {{ bomTreeData.rootPart?.name }}</h3>
          <div class="tree-controls">
            <el-button @click="expandAll">全部展开</el-button>
            <el-button @click="collapseAll">全部收起</el-button>
          </div>
        </div><el-tree
          ref="bomTreeRef"
          :data="bomTreeNodes"
          :props="{ children: 'children', label: 'label' }"
          node-key="id"
          show-checkbox
          default-expand-all
          class="bom-tree"
        >          <template v-slot="{ node, data }">
            <div class="bom-tree-node">
              <div class="node-info">
                <!-- 优先使用后端返回的名称，如果为空则用masterId从Part列表查找 -->
                <span class="node-name">{{ 
                  data.targetPart?.name || 
                  data.rootPart?.name || 
                  getPartNameByMasterId(data.targetPart?.masterId) ||
                  `Part-${data.targetPart?.masterId || 'Unknown'}` 
                }}</span>
                <span class="node-quantity" v-if="data.quantity">数量: {{ data.quantity }}</span>
                <span class="node-designator" v-if="data.referenceDesignator">位号: {{ data.referenceDesignator }}</span>
                <span class="node-id" v-if="data.targetPart?.masterId" style="color: #999; font-size: 12px; margin-left: 8px;">(ID: {{ data.targetPart.masterId }})</span>
              </div>
              <div class="node-actions" v-if="data.bomLinkId">
                <el-button link size="small" @click="editBomLink(data)">编辑</el-button>
                <el-button link size="small" type="danger" @click="deleteBomLink(data)">删除</el-button>
              </div>
            </div>
          </template>
        </el-tree>
      </div>
      <div v-else class="empty-state">
        <el-empty description="暂无BOM结构数据"></el-empty>
      </div>
    </el-dialog>

    <!-- BOM清单弹窗 -->
    <el-dialog v-model="bomListDialogVisible" title="BOM清单" width="90%" class="tech-dialog">
      <div v-if="bomListData">
        <div class="list-summary">
          <el-tag type="info">总计: {{ bomListData.summary?.totalItems }} 项</el-tag>
          <el-tag type="success">总数量: {{ bomListData.summary?.totalQuantity }}</el-tag>
          <el-tag type="warning">最大层级: {{ bomListData.summary?.maxLevel }}</el-tag>
        </div>        <el-table :data="bomListData.items" style="width: 100%" class="tech-table">
          <el-table-column prop="level" label="层级" width="80" align="center"></el-table-column>
          <el-table-column prop="sequenceNumber" label="序号" width="80" align="center"></el-table-column>
          <el-table-column prop="partName" label="零件名称"></el-table-column>
          <el-table-column prop="quantity" label="数量" width="100" align="center"></el-table-column>
          <el-table-column prop="referenceDesignator" label="位号" width="150"></el-table-column>
          <el-table-column prop="source" label="来源" width="100" align="center"></el-table-column>
          <el-table-column prop="description" label="描述" show-overflow-tooltip></el-table-column>
        </el-table>
      </div>
      <div v-else class="empty-state">
        <el-empty description="暂无BOM清单数据"></el-empty>
      </div>
    </el-dialog>    <!-- 父级使用情况弹窗 -->
    <el-dialog v-model="whereUsedDialogVisible" title="父级使用情况" width="70%" class="tech-dialog">
      <div v-if="whereUsedData && whereUsedData.length > 0">
        <el-table :data="whereUsedData" style="width: 100%" class="tech-table">
          <el-table-column label="父级零件名称">
            <template #default="{ row }">
              {{ getPartNameByMasterId(row.parentPart?.masterId) || row.parentPart?.name || `Part-${row.parentPart?.masterId || 'Unknown'}` }}
            </template>
          </el-table-column>
          <el-table-column label="版本" width="80" align="center">
            <template #default="{ row }">
              {{ row.parentPart?.currentVersion }}
            </template>
          </el-table-column>
          <el-table-column prop="quantity" label="使用数量" width="100" align="center"></el-table-column>
          <el-table-column prop="referenceDesignator" label="位号" width="150"></el-table-column>
          <el-table-column prop="level" label="层级" width="80" align="center"></el-table-column>
        </el-table>
      </div>
      <div v-else class="empty-state">
        <el-empty description="暂无父级使用数据"></el-empty>
      </div>
    </el-dialog>

    <!-- 添加BOM关系弹窗 -->
    <el-dialog v-model="addBomDialogVisible" title="添加BOM关系" width="50%" class="tech-dialog">
      <el-form :model="bomLinkModel" :rules="bomLinkRules" ref="bomLinkFormRef" label-width="120px">        <el-form-item label="父级Part">
          <el-input :value="currentSourcePart?.name + ' (' + (currentSourcePart?.number || currentSourcePart?.masterId) + ')'" disabled></el-input>
        </el-form-item><el-form-item label="子级Part" prop="target" required>
          <el-select v-model="bomLinkModel.target" placeholder="请选择子级Part" filterable>
            <el-option 
              v-for="part in availablePartList" 
              :key="part.masterId" 
              :label="`${part.name} (${part.number || part.masterId})`" 
              :value="part.masterId">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="使用数量" prop="quantity" required>
          <el-input-number v-model="bomLinkModel.quantity" :min="0.1" :step="0.1" :precision="2"></el-input-number>
        </el-form-item>
        <el-form-item label="序号" prop="sequenceNumber">
          <el-input-number v-model="bomLinkModel.sequenceNumber" :min="1"></el-input-number>
        </el-form-item>
        <el-form-item label="位号">
          <el-input v-model="bomLinkModel.referenceDesignator" placeholder="例: R1,R2,R3"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addBomDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="onSubmitBomLink">确 认</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 批量添加BOM关系弹窗 -->
    <el-dialog v-model="batchAddDialogVisible" title="批量添加BOM关系" width="70%" class="tech-dialog">
      <el-form :model="batchBomModel" ref="batchBomFormRef" label-width="120px">
        <el-form-item label="源Part" prop="sourceVersionId" required>
          <el-select v-model="batchBomModel.sourceVersionId" placeholder="请选择源Part" filterable>
            <el-option 
              v-for="part in partList" 
              :key="part.versionId" 
              :label="`${part.name} (${part.number}) - V${part.version}.${part.iteration}`" 
              :value="part.versionId">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      
      <div class="batch-items">
        <div class="items-header">
          <h4>BOM项目列表</h4>
          <el-button @click="addBatchItem">添加项目</el-button>
        </div>
        
        <div v-for="(item, index) in batchBomModel.bomItems" :key="index" class="batch-item">
          <el-form :model="item" :inline="true">
            <el-form-item label="目标Part">
              <el-select 
                v-model="item.targetMasterId" 
                placeholder="请选择零件" 
                filterable
                style="width: 100%"
              >
                <el-option
                  v-for="part in availablePartList"
                  :key="part.masterId"
                  :label="`${part.name || '未命名'} (ID: ${part.masterId})`"
                  :value="part.masterId"
                >
                  <span>{{ part.name || '未命名' }}</span>
                  <span style="float: right; color: #8492a6; font-size: 13px">ID: {{ part.masterId }}</span>
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="数量">
              <el-input-number v-model="item.quantity" :min="0.1" :step="0.1" :precision="2" style="width: 120px;"></el-input-number>
            </el-form-item>
            <el-form-item label="序号">
              <el-input-number v-model="item.sequenceNumber" :min="1" style="width: 100px;"></el-input-number>
            </el-form-item>
            <el-form-item label="位号">
              <el-input v-model="item.referenceDesignator" placeholder="例: R1,R2" style="width: 150px;"></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="danger" @click="removeBatchItem(index)">删除</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="batchAddDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="onSubmitBatchBom">确 认</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 编辑BOM关系弹窗 -->
    <el-dialog v-model="editBomDialogVisible" title="编辑BOM关系" width="50%" class="tech-dialog">
      <el-form :model="editBomModel" ref="editBomFormRef" label-width="120px">
        <el-form-item label="使用数量" required>
          <el-input-number v-model="editBomModel.quantity" :min="0.1" :step="0.1" :precision="2"></el-input-number>
        </el-form-item>
        <el-form-item label="序号">
          <el-input-number v-model="editBomModel.sequenceNumber" :min="1"></el-input-number>
        </el-form-item>
        <el-form-item label="位号">
          <el-input v-model="editBomModel.referenceDesignator" placeholder="例: R1,R2,R3"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editBomDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="onSubmitEditBom">确 认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, onMounted, nextTick, watch, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Edit, Delete, Plus, View, Lock, Unlock, Share, List, TopRight, Setting } from '@element-plus/icons-vue';
import {
  partQueryService,
  partCreateService,
  partUpdateService,
  partDeleteService,
  partEnumService,
  partDetailService,
  partCheckoutService,
  partUndoCheckoutService
} from '@/api/partAPI.js';
import {
  bomTreeQueryService,
  bomListQueryService,
  partWhereUsedService,
  bomLinkCreateService,
  bomLinkBatchCreateService,
  bomLinkUpdateService,
  bomLinkDeleteService
} from '@/api/bomAPI.js';
import { classificationListService } from '@/api/classificationAPI.js';

const partList = ref([]);
const loading = ref(false);
const dialogVisible = ref(false);
const dialogTitle = ref('');
const partModel = ref({
  name: '',
  description: '',
  source: '',
  partType: '',
  iterationNote: ''
});

// 新增：枚举和分类下拉数据
const sourceEnum = ref([])
const partTypeEnum = ref([])
const classificationEnum = ref([])

// 新增：表单校验规则
const formRef = ref(null)
const formRules = {
  name: [ { required: true, message: '请输入Part Name', trigger: 'blur' } ],
  source: [ { required: true, message: '请选择来源', trigger: 'change' } ],
  partType: [ { required: true, message: '请选择装配模式', trigger: 'change' } ],
  iterationNote: [ { required: true, message: '请输入迭代备注', trigger: 'blur' } ]
}
const formValid = ref(false)

const validateForm = () => {
  if (!formRef.value) return false
  return new Promise(resolve => {
    formRef.value.validate(valid => {
      formValid.value = valid
      resolve(valid)
    })
  })
}

const fetchEnums = async () => {
  // 来源
  const sourceRes = await partEnumService('PartSource2')
  sourceEnum.value = (sourceRes || []).map(item => ({ label: item.label, value: item.value }))
  // 装配模式
  const partTypeRes = await partEnumService('AssemblyMode')
  partTypeEnum.value = (partTypeRes || []).map(item => ({ label: item.label, value: item.value }))
  // 分类
  const classRes = await classificationListService()
  // 拍平成一维
  const flatten = (tree) => tree.reduce((acc, node) => {
    acc.push({ label: node.name, value: node.id })
    if (node.children) acc.push(...flatten(node.children))
    return acc
  }, [])
  classificationEnum.value = flatten(classRes || [])
}

const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const searchParams = ref({
  name: '',
  number: '',
  source: '',
  partType: '',
});

const getPartList = async () => {
  loading.value = true;
  try {
    const params = {
      ...searchParams.value,
      page: currentPage.value,
      size: pageSize.value
    };    console.log('查询Part列表参数:', params);
      const res = await partQueryService(params);
    
    // 根据masterId去除重复项，保留最新的版本
    const uniqueParts = deduplicatePartsByMasterId(res.records || []);
    
    partList.value = uniqueParts;
    total.value = res.total || 0;
    
    // 更新Part名称缓存
    updatePartNameCache(partList.value);
    
    console.log(`获取Part列表成功: 原始${(res.records || []).length}个，去重后${partList.value.length}个`);
  } catch (error) {
    console.error('获取Part列表失败:', error);
    
    // 根据错误类型给出不同提示
    if (error.code === 'ECONNRESET' || error.message?.includes('Connection reset')) {
      ElMessage.error('网络连接不稳定，请稍后重试');
    } else if (error.code === 'ETIMEDOUT' || error.message?.includes('timeout')) {
      ElMessage.error('请求超时，请检查网络连接后重试');
    } else {
      ElMessage.error(`获取Part列表失败: ${error.response?.data?.message || error.message || '未知错误'}`);
    }
  } finally {
    loading.value = false;
  }
};

const handlePageChange = (page) => {
  currentPage.value = page;
  getPartList();
};
const handleSizeChange = (size) => {
  pageSize.value = size;
  currentPage.value = 1;
  getPartList();
};

onMounted(() => {
  fetchEnums();
  getPartList();
  // 预加载Part名称缓存（可选）
  // preloadPartNames();
});

const showAddDialog = () => {
  dialogTitle.value = '添加Part';
  partModel.value = {
    name: '',
    description: '',
    source: '',
    partType: '',
    iterationNote: ''
  };
  dialogVisible.value = true;
  nextTick(() => validateForm())
};
const showEditDialog = async (row) => {
  try {
    console.log('编辑Part，先执行检出操作:', row);
    
    // 先检出Part，确保可以编辑
    await partCheckoutService(row.masterId);
    console.log('Part检出成功，可以编辑');
    ElMessage.success('Part已检出，可以编辑');
    
    // 检出成功后设置编辑对话框
    dialogTitle.value = '编辑Part';
    currentEditRow.value = row;
    partModel.value = {
      name: row.name || '',
      description: row.description || '',
      source: row.source || '',
      partType: row.partType || '',
      iterationNote: row.iterationNote || ''
    };
    dialogVisible.value = true;
    nextTick(() => validateForm());
    
    // 刷新Part列表以更新工作状态
    await getPartList();
    
  } catch (error) {
    console.error('Part检出失败:', error);
    
    // 根据错误信息提供更详细的提示
    let errorMessage = 'Part检出失败，无法编辑';
    if (error.response?.data?.message) {
      errorMessage += `: ${error.response.data.message}`;
    } else if (error.message) {
      errorMessage += `: ${error.message}`;
    }
    
    // 检查是否是因为已经检出导致的错误
    if (error.response?.data?.message?.includes('already checked out') || 
        error.response?.data?.message?.includes('已检出')) {
      console.log('Part可能已经检出，尝试直接编辑');
      ElMessage.warning('Part可能已经检出，尝试直接编辑');
      
      // 直接显示编辑对话框
      dialogTitle.value = '编辑Part';
      currentEditRow.value = row;
      partModel.value = {
        name: row.name || '',
        description: row.description || '',
        source: row.source || '',
        partType: row.partType || '',
        iterationNote: row.iterationNote || ''
      };
      dialogVisible.value = true;
      nextTick(() => validateForm());
    } else {
      ElMessage.error(errorMessage);
    }
  }
};
const onSubmit = async () => {
  const valid = await validateForm();
  if (!valid) return;
    try {
    if (dialogTitle.value.includes('编辑')) {
      // 编辑Part：不需要再次检出，因为在showEditDialog中已经检出
      console.log('更新Part:', currentEditRow.value.masterId);
      
      // 直接更新Part
      await partUpdateService(currentEditRow.value.masterId, {
        name: partModel.value.name,
        description: partModel.value.description,
        source: partModel.value.source,
        partType: partModel.value.partType,
        iterationNote: partModel.value.iterationNote
      });
      
      ElMessage.success('更新成功');
    } else {
      // 构建正确的Part创建数据结构
      const createData = {
        master: {
          name: partModel.value.name
        },
        version: {
          source: partModel.value.source,
          partType: partModel.value.partType,
          description: partModel.value.description,
          iterationNote: partModel.value.iterationNote
        }
      };
      await partCreateService(createData);
      ElMessage.success('添加成功');
    }
    dialogVisible.value = false;
    await getPartList();
  } catch (error) {
    console.error('操作失败:', error);
    
    // 提供更详细的错误信息
    let errorMessage = '操作失败';
    if (error.response?.data?.message) {
      errorMessage += `: ${error.response.data.message}`;
    } else if (error.message) {
      errorMessage += `: ${error.message}`;
    }
    
    // 特别处理检出相关错误
    if (error.message?.includes('not checked out') || error.message?.includes('cannot be checked in')) {
      errorMessage = 'Part编辑失败：需要先检出Part才能编辑，请先点击"检出"按钮';
      ElMessage.warning('请先检出Part再进行编辑操作');
    } else {
      ElMessage.error(errorMessage);
    }
  }
};
const onDelete = (row) => {
  ElMessageBox.confirm(`你确定要删除Part【${row.name}】吗?`, '温馨提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning',
    customClass: 'tech-messagebox'
  })
    .then(async () => {
      await partDeleteService(row.masterId);
      ElMessage.success('删除成功');
      getPartList();
    })
    .catch(() => {
      ElMessage.info('已取消删除');
    });
};

const detailDialogVisible = ref(false)
const partDetail = ref(null)
const showDetailDialog = async (row) => {
  const res = await partDetailService(row.masterId)
  partDetail.value = res
  detailDialogVisible.value = true
}

const onCheckout = async (row) => {
  try {
    await partCheckoutService(row.masterId)
    ElMessage.success('检出成功')
    getPartList()
  } catch (e) {
    ElMessage.error('检出失败')
  }
}
const onUndoCheckout = async (row) => {
  try {
    await partUndoCheckoutService(row.masterId)
    ElMessage.success('撤销检出成功')
    getPartList()
  } catch (e) {
    ElMessage.error('撤销检出失败')
  }
}

// 新增：记录当前编辑行的row
const currentEditRow = ref({});

// BOM结构树相关
const bomTreeDialogVisible = ref(false);
const bomTreeData = ref(null);
const bomTreeRef = ref(null);

// BOM清单相关
const bomListDialogVisible = ref(false);
const bomListData = ref(null);

// 父级使用情况相关
const whereUsedDialogVisible = ref(false);
const whereUsedData = ref([]);

// 添加BOM关系相关
const addBomDialogVisible = ref(false);
const currentSourcePart = ref(null);
const availablePartList = ref([]); // 新增：可用零件列表
const bomLinkModel = ref({
  target: '',
  quantity: 1.0,
  sequenceNumber: 10,
  referenceDesignator: ''
});
const bomLinkFormRef = ref(null);
const bomLinkRules = {
  target: [{ required: true, message: '请选择子级Part', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入使用数量', trigger: 'blur' }]
};

// 批量添加BOM关系相关
const batchAddDialogVisible = ref(false);
const batchBomModel = ref({
  sourceVersionId: '',
  bomItems: []
});
const batchBomFormRef = ref(null);

// 编辑BOM关系相关
const editBomDialogVisible = ref(false);
const editBomModel = ref({
  bomLinkId: '',
  quantity: 1.0,
  sequenceNumber: 10,
  referenceDesignator: ''
});
const editBomFormRef = ref(null);

// 计算BOM树节点
const bomTreeNodes = computed(() => {
  if (!bomTreeData.value || !bomTreeData.value.bomTree) return [];
  
  const convertToTreeNodes = (items, level = 1) => {
    return items.map(item => {
      // 优先使用后端返回的名称，然后从缓存中查找，最后使用兜底显示
      let displayName = item.targetPart?.name || item.rootPart?.name;
      
      if (!displayName && item.targetPart?.masterId) {
        // 访问partNameCache.value来确保响应式更新
        displayName = partNameCache.value.get(item.targetPart.masterId) || getPartNameByMasterId(item.targetPart.masterId);
      }
      
      if (!displayName) {
        displayName = `Part-${item.targetPart?.masterId || 'Unknown'}`;
      }
      
      return {
        id: `${item.bomLinkId}_${level}`,
        label: displayName,
        ...item,
        children: item.children ? convertToTreeNodes(item.children, level + 1) : []
      };
    });
  };
  
  return convertToTreeNodes(bomTreeData.value.bomTree);
});

watch(partModel, () => {
  validateForm()
}, { deep: true })

const onSearch = async () => {
  loading.value = true;
  try {
    const params = {
      ...searchParams.value,
      page: currentPage.value,
      size: pageSize.value
    };    console.log('搜索Part参数:', params);
      const res = await partQueryService(params);
    
    // 根据masterId去除重复项，保留最新的版本
    const uniqueParts = deduplicatePartsByMasterId(res.records || []);
    
    partList.value = uniqueParts;
    total.value = res.total || 0;
    
    // 更新Part名称缓存
    updatePartNameCache(partList.value);
    
    console.log(`搜索Part成功: 原始${(res.records || []).length}个，去重后${partList.value.length}个`);
  } catch (error) {
    console.error('搜索Part失败:', error);
    
    // 根据错误类型给出不同提示
    if (error.code === 'ECONNRESET' || error.message?.includes('Connection reset')) {
      ElMessage.error('网络连接不稳定，请稍后重试');
    } else if (error.code === 'ETIMEDOUT' || error.message?.includes('timeout')) {
      ElMessage.error('搜索超时，请检查网络连接后重试');
    } else {
      ElMessage.error(`搜索失败: ${error.response?.data?.message || error.message || '未知错误'}`);
    }
  } finally {
    loading.value = false;
  }
};

const onReset = () => {
  searchParams.value = {
    name: '',
    number: '',
    source: '',
    partType: '',
  };
  currentPage.value = 1;
  onSearch();
};

// 手动重试函数
const handleRetry = async () => {
  ElMessage.info('正在重新加载...');
  await getPartList();
};

// Part去重函数：根据masterId去除重复项，保留最新版本
const deduplicatePartsByMasterId = (parts) => {
  const uniqueParts = [];
  const masterIdMap = new Map();
  
  parts.forEach(part => {
    if (part.masterId) {
      if (!masterIdMap.has(part.masterId)) {
        masterIdMap.set(part.masterId, part);
        uniqueParts.push(part);
      } else {
        // 如果已存在相同masterId，比较版本或更新时间，保留最新的
        const existingPart = masterIdMap.get(part.masterId);
        if (part.updateTime > existingPart.updateTime || 
            part.iteration > existingPart.iteration ||
            part.version > existingPart.version) {
          // 替换为更新的Part
          const index = uniqueParts.findIndex(p => p.masterId === part.masterId);
          if (index !== -1) {
            uniqueParts[index] = part;
            masterIdMap.set(part.masterId, part);
          }
        }
      }
    } else {
      // 没有masterId的Part直接添加
      uniqueParts.push(part);
    }
  });
  
  return uniqueParts;
};

// 全量Part名称缓存（响应式）
const partNameCache = ref(new Map());

// 初始化或更新Part名称缓存
const updatePartNameCache = (parts) => {
  if (parts && Array.isArray(parts)) {
    parts.forEach(part => {
      if (part.masterId && part.name) {
        partNameCache.value.set(part.masterId, part.name);
      }
    });
    console.log(`Part名称缓存已更新，当前缓存数量: ${partNameCache.value.size}`);
  }
};

// 通过API获取指定masterId的Part详情
const fetchPartDetailByMasterId = async (masterId) => {
  try {
    console.log(`尝试通过API获取masterId ${masterId} 的Part详情`);
    const response = await partDetailService(masterId);
    if (response && response.name) {
      const partName = response.name;
      // 更新缓存（触发响应式更新）
      const newCache = new Map(partNameCache.value);
      newCache.set(masterId, partName);
      partNameCache.value = newCache;
      console.log(`通过API获取到Part名称: ${partName}`);
      return partName;
    } else {
      console.warn(`API返回数据格式异常:`, response);
      return null;
    }
  } catch (error) {
    console.error(`获取masterId ${masterId} 的Part详情失败:`, error);
    return null;
  }
};

// 预加载BOM结构树中缺失的Part名称
const preloadMissingPartNames = async (bomTreeData) => {
  if (!bomTreeData || !bomTreeData.bomTree) return;
  
  const collectMasterIds = (items) => {
    const masterIds = [];
    items.forEach(item => {
      if (item.targetPart?.masterId) {
        masterIds.push(item.targetPart.masterId);
      }
      if (item.children) {
        masterIds.push(...collectMasterIds(item.children));
      }
    });
    return masterIds;
  };
  
  // 收集所有masterId
  const allMasterIds = collectMasterIds(bomTreeData.bomTree);
  console.log('BOM结构树中的所有masterId:', allMasterIds);
  
  // 找出缺失名称的masterId
  const missingIds = allMasterIds.filter(id => {
    if (!id) return false;
    // 检查是否在当前partList中
    const inPartList = partList.value.some(p => p.masterId === id && p.name);
    // 检查是否在缓存中
    const inCache = partNameCache.value.has(id);
    return !inPartList && !inCache;
  });
  
  console.log('需要预加载的masterId:', missingIds);
  
  if (missingIds.length === 0) {
    console.log('所有Part名称都已可用，无需预加载');
    return;
  }
  
  // 批量获取缺失的Part详情
  console.log(`开始批量获取 ${missingIds.length} 个缺失的Part名称...`);
  const fetchPromises = missingIds.map(masterId => fetchPartDetailByMasterId(masterId));
  
  try {
    await Promise.allSettled(fetchPromises);
    console.log('批量获取Part名称完成');
  } catch (error) {
    console.error('批量获取Part名称失败:', error);
  }
};

// 获取大量Part数据以建立缓存（可选的优化方法）
const preloadPartNames = async () => {
  try {
    console.log('开始预加载Part名称缓存...');
    ElMessage.info('正在预加载Part名称缓存...');
    
    // 使用大的页面大小获取更多数据
    const response = await partQueryService({ 
      page: 1, 
      size: 1000,  // 获取更多数据
      // 可以添加其他查询条件
    });
    
    if (response && response.records) {
      updatePartNameCache(response.records);
      console.log(`预加载完成，缓存了 ${response.records.length} 个Part名称`);
      ElMessage.success(`预加载完成，缓存了 ${response.records.length} 个Part名称`);
    } else {
      console.warn('预加载响应格式异常:', response);
      ElMessage.warning('预加载响应格式异常');
    }
  } catch (error) {
    console.error('预加载Part名称缓存失败:', error);
    ElMessage.error(`预加载Part名称缓存失败: ${error.response?.data?.message || error.message || '未知错误'}`);
  }
};

// 根据masterId查找Part名称的方法（同步版本，支持异步获取）
const getPartNameByMasterId = (masterId) => {
  if (!masterId) return null;
  
  // 1. 先在当前分页数据中查找
  if (partList.value) {
    const part = partList.value.find(p => p.masterId === masterId);
    if (part && part.name) {
      console.log(`在当前分页中找到masterId ${masterId} 的Part名称: ${part.name}`);
      // 同时更新缓存
      const newCache = new Map(partNameCache.value);
      newCache.set(masterId, part.name);
      partNameCache.value = newCache;
      return part.name;
    }
  }
  
  // 2. 在缓存中查找
  if (partNameCache.value.has(masterId)) {
    const cachedName = partNameCache.value.get(masterId);
    console.log(`在缓存中找到masterId ${masterId} 的Part名称: ${cachedName}`);
    return cachedName;
  }
  
  // 3. 异步获取（不阻塞当前渲染）
  console.log(`在当前数据和缓存中都未找到masterId ${masterId}，异步获取中...`);
  fetchPartDetailByMasterId(masterId);  // 异步获取，会更新缓存
  
  return null;  // 暂时返回null，等缓存更新后会重新渲染
};

// BOM结构树相关方法
const showBomTree = async (row) => {
  try {
    console.log('查看BOM结构树，Part信息:', row);
    
    if (!row.versionId) {
      ElMessage.error('该Part没有版本ID，无法查看BOM结构树');
      return;
    }
    
    const res = await bomTreeQueryService(row.versionId, { includeQty: true });
    console.log('BOM结构树数据:', res);
    
    bomTreeData.value = res;
    bomTreeDialogVisible.value = true;
    
    // 在显示BOM结构树后，尝试预加载缺失的Part名称
    console.log('正在检查并预加载BOM结构树中缺失的Part名称...');
    await preloadMissingPartNames(res);
    
  } catch (error) {
    console.error('获取BOM结构树失败:', error);
    ElMessage.error(`获取BOM结构树失败: ${error.response?.data?.message || error.message || '未知错误'}`);
    bomTreeData.value = null;
    bomTreeDialogVisible.value = true;
  }
};

const expandAll = () => {
  if (bomTreeRef.value) {
    bomTreeNodes.value.forEach(node => {
      bomTreeRef.value.setExpanded(node.id, true);
    });
  }
};

const collapseAll = () => {
  if (bomTreeRef.value) {
    bomTreeNodes.value.forEach(node => {
      bomTreeRef.value.setExpanded(node.id, false);
    });
  }
};

// BOM清单相关方法
const showBomList = async (row) => {
  try {
    if (!row.versionId) {
      ElMessage.error('该Part没有版本ID，无法查看BOM清单');
      return;
    }
    const res = await bomListQueryService(row.versionId, { recursive: true });
    bomListData.value = res;
    bomListDialogVisible.value = true;
  } catch (error) {
    ElMessage.error('获取BOM清单失败');
    bomListData.value = null;
    bomListDialogVisible.value = true;
  }
};

// 父级使用情况相关方法
const showWhereUsed = async (row) => {
  try {
    console.log('查看父级使用情况，Part信息:', row);
    
    const res = await partWhereUsedService(row.masterId, { recursive: true });
    console.log('父级使用情况数据:', res);
    
    whereUsedData.value = res || [];
    whereUsedDialogVisible.value = true;
    
    // 预加载父级Part名称
    if (whereUsedData.value.length > 0) {
      console.log('正在预加载父级使用情况中缺失的Part名称...');
      const parentMasterIds = whereUsedData.value
        .map(item => item.parentPart?.masterId)
        .filter(id => {
          if (!id) return false;
          // 检查是否在当前partList中
          const inPartList = partList.value.some(p => p.masterId === id && p.name);
          // 检查是否在缓存中
          const inCache = partNameCache.value.has(id);
          return !inPartList && !inCache;
        });
      
      console.log('需要预加载的父级Part masterId:', parentMasterIds);
      
      if (parentMasterIds.length > 0) {
        const fetchPromises = parentMasterIds.map(masterId => fetchPartDetailByMasterId(masterId));
        try {
          await Promise.allSettled(fetchPromises);
          console.log('父级Part名称预加载完成');
        } catch (error) {
          console.error('预加载父级Part名称失败:', error);
        }
      }
    }
    
  } catch (error) {
    console.error('获取父级使用情况失败:', error);
    ElMessage.error(`获取父级使用情况失败: ${error.response?.data?.message || error.message || '未知错误'}`);
    whereUsedData.value = [];
    whereUsedDialogVisible.value = true;
  }
};

// 添加BOM关系相关方法
const showAddBomDialog = async (row) => {
  console.log('打开添加BOM对话框，当前Part:', row);
  
  if (!row.versionId) {
    ElMessage.error('无法获取Part版本信息');
    return;
  }
  
  currentSourcePart.value = row; // 修复：设置当前源Part
  addBomDialogVisible.value = true;
  
  // 重置表单
  bomLinkModel.value = {
    target: '',
    quantity: 1.0,
    sequenceNumber: 10,
    referenceDesignator: ''
  };
    // 查询所有可用的Part作为子零件选项
  try {
    const params = {
      page: 1,
      size: 1000, // 获取更多数据，确保显示所有Part
      // 不添加任何过滤条件，获取所有Part
    };
    
    const res = await partQueryService(params);
    availablePartList.value = deduplicatePartsByMasterId(res.records || []);
    
    // 更新Part名称缓存
    updatePartNameCache(availablePartList.value);
    
    console.log('获取可用Part列表:', availablePartList.value.length, '个');
    
    if (availablePartList.value.length === 0) {
      ElMessage.warning('当前没有可用的子零件');
    }
  } catch (error) {
    console.error('获取可用零件列表失败:', error);
    ElMessage.error(`获取可用零件列表失败: ${error.response?.data?.message || error.message || '未知错误'}`);
    availablePartList.value = [];
  }
};

const onSubmitBomLink = async () => {
  if (!bomLinkFormRef.value) return;
  
  try {
    await bomLinkFormRef.value.validate();
    
    // 找到选中的目标Part以获取其masterId
    const targetPart = availablePartList.value.find(part => part.masterId === bomLinkModel.value.target);
    if (!targetPart) {
      ElMessage.error('请选择有效的子级Part');
      return;
    }
      console.log('当前源Part:', currentSourcePart.value);
    console.log('目标Part:', targetPart);
    
    // 根据接口文档，source是versionId，target是masterId
    // 同时包含Part的详细信息，确保请求体包含名称等必要信息
    const data = {
      bomLink: {
        source: currentSourcePart.value.versionId, // Part版本ID
        target: targetPart.masterId, // PartMaster主对象ID
        quantity: Number(bomLinkModel.value.quantity),
        sequenceNumber: Number(bomLinkModel.value.sequenceNumber),
        // 包含源Part信息
        sourcePart: {
          name: currentSourcePart.value.name,
          masterId: currentSourcePart.value.masterId,
          versionId: currentSourcePart.value.versionId,
          description: currentSourcePart.value.description || ''
        },
        // 包含目标Part信息
        targetPart: {
          name: targetPart.name,
          masterId: targetPart.masterId,
          description: targetPart.description || ''
        }
      },
      bomUsesOccurrence: {
        referenceDesignator: bomLinkModel.value.referenceDesignator || ''
      }
    };
    
    console.log('提交BOM关系数据:', JSON.stringify(data, null, 2));
      // 确保数据结构严格符合接口要求，防止意外字段污染
    const cleanData = {
      bomLink: {
        source: data.bomLink.source,
        target: data.bomLink.target,
        quantity: data.bomLink.quantity,
        sequenceNumber: data.bomLink.sequenceNumber,
        sourcePart: {
          name: data.bomLink.sourcePart.name,
          masterId: data.bomLink.sourcePart.masterId,
          versionId: data.bomLink.sourcePart.versionId,
          description: data.bomLink.sourcePart.description
        },
        targetPart: {
          name: data.bomLink.targetPart.name,
          masterId: data.bomLink.targetPart.masterId,
          description: data.bomLink.targetPart.description
        }
      },
      bomUsesOccurrence: {
        referenceDesignator: data.bomUsesOccurrence.referenceDesignator
      }
    };
    
    console.log('清理后的BOM关系数据:', JSON.stringify(cleanData, null, 2));
      await bomLinkCreateService(cleanData);
    console.log('BOM关系创建成功!');
    ElMessage.success('BOM关系创建成功');
    addBomDialogVisible.value = false;
    
    // 刷新当前Part列表以获取最新的versionId信息
    await getPartList();
    
    // 如果BOM结构树正在显示，也刷新BOM结构树数据
    if (bomTreeDialogVisible.value && bomTreeData.value?.rootPart?.versionId) {
      console.log('自动刷新BOM结构树数据...');
      try {
        const res = await bomTreeQueryService(bomTreeData.value.rootPart.versionId, { includeQty: true });
        bomTreeData.value = res;
        console.log('BOM结构树数据已刷新');
      } catch (error) {
        console.error('刷新BOM结构树失败:', error);
      }
    }
      } catch (error) {
    console.error('创建BOM关系失败:', error);
    console.error('错误详情:', error.response?.data);
    console.error('请求配置:', error.config);
    
    // 输出实际发送的数据，帮助调试
    if (error.config?.data) {
      console.error('实际发送的数据:', typeof error.config.data === 'string' ? error.config.data : JSON.stringify(error.config.data, null, 2));
    }
    
    // 提供更详细的错误信息
    let errorMessage = '创建BOM关系失败';
    if (error.response?.status === 500) {
      errorMessage += ': 服务器内部错误';
      if (error.response?.data?.message) {
        errorMessage += ` - ${error.response.data.message}`;
      }
    } else if (error.response?.status === 400) {
      errorMessage += ': 请求参数错误';
      if (error.response?.data?.message) {
        errorMessage += ` - ${error.response.data.message}`;
      }
    } else if (error.response?.data?.message) {
      errorMessage += `: ${error.response.data.message}`;
    } else if (error.message) {
      errorMessage += `: ${error.message}`;
    }
    
    ElMessage.error(errorMessage);
  }
};

// 批量添加BOM关系相关方法
const showBatchAddDialog = async () => {
  batchBomModel.value = {
    sourceVersionId: '',
    bomItems: [
      {
        targetMasterId: '',
        quantity: 1.0,
        sequenceNumber: 10,
        referenceDesignator: ''
      }
    ]
  };
  batchAddDialogVisible.value = true;
    // 查询所有可用的Part作为子零件选项
  try {
    const params = {
      page: 1,
      size: 1000, // 获取更多数据，确保显示所有Part
    };
    
    const res = await partQueryService(params);
    availablePartList.value = deduplicatePartsByMasterId(res.records || []);
    
    // 更新Part名称缓存
    updatePartNameCache(availablePartList.value);
    
    if (availablePartList.value.length === 0) {
      ElMessage.warning('当前没有可用的子零件');
    }
  } catch (error) {
    ElMessage.error('获取可用零件列表失败');
    availablePartList.value = [];
  }
};

const addBatchItem = () => {
  batchBomModel.value.bomItems.push({
    targetMasterId: '',
    quantity: 1.0,
    sequenceNumber: (batchBomModel.value.bomItems.length + 1) * 10,
    referenceDesignator: ''
  });
};

const removeBatchItem = (index) => {
  batchBomModel.value.bomItems.splice(index, 1);
};

const onSubmitBatchBom = async () => {
  if (!batchBomModel.value.sourceVersionId) {
    ElMessage.error('请选择源Part');
    return;
  }
  
  if (batchBomModel.value.bomItems.length === 0) {
    ElMessage.error('请至少添加一个BOM项目');
    return;
  }
  try {
    console.log('批量创建BOM关系数据:', batchBomModel.value);
    
    // 找到源Part信息
    const sourcePart = partList.value.find(part => part.versionId === batchBomModel.value.sourceVersionId);
    if (!sourcePart) {
      ElMessage.error('无法找到源Part信息');
      return;
    }
    
    // 确保批量数据结构严格符合接口要求，防止意外字段污染
    const cleanBatchData = {
      sourceVersionId: batchBomModel.value.sourceVersionId,
      // 包含源Part信息
      sourcePart: {
        name: sourcePart.name,
        masterId: sourcePart.masterId,
        versionId: sourcePart.versionId,
        description: sourcePart.description || ''
      },
      bomItems: batchBomModel.value.bomItems.map(item => {
        // 找到每个目标Part的信息
        const targetPart = availablePartList.value.find(part => part.masterId === item.targetMasterId);
        return {
          targetMasterId: item.targetMasterId,
          quantity: item.quantity,
          sequenceNumber: item.sequenceNumber,
          referenceDesignator: item.referenceDesignator,
          // 包含目标Part信息
          targetPart: targetPart ? {
            name: targetPart.name,
            masterId: targetPart.masterId,
            description: targetPart.description || ''
          } : null
        };
      })
    };
    
    console.log('清理后的批量BOM关系数据:', JSON.stringify(cleanBatchData, null, 2));
    
    await bomLinkBatchCreateService(cleanBatchData);
    ElMessage.success('批量创建BOM关系成功');
    batchAddDialogVisible.value = false;
    
    // 刷新Part列表以获取最新数据
    await getPartList();
  } catch (error) {
    console.error('批量创建BOM关系失败:', error);
    ElMessage.error(`批量创建BOM关系失败: ${error.response?.data?.message || error.message || '未知错误'}`);
  }
};

// 编辑BOM关系相关方法
const editBomLink = (bomItem) => {
  editBomModel.value = {
    bomLinkId: bomItem.bomLinkId,
    quantity: bomItem.quantity,
    sequenceNumber: bomItem.sequenceNumber,
    referenceDesignator: bomItem.referenceDesignator
  };
  editBomDialogVisible.value = true;
};

const onSubmitEditBom = async () => {
  try {
    const data = {
      bomLink: {
        quantity: editBomModel.value.quantity,
        sequenceNumber: editBomModel.value.sequenceNumber
      },
      bomUsesOccurrence: {
        referenceDesignator: editBomModel.value.referenceDesignator
      }
    };
    
    console.log('更新BOM关系数据:', data);
    
    // 确保编辑数据结构严格符合接口要求，防止意外字段污染
    const cleanEditData = {
      bomLink: {
        quantity: data.bomLink.quantity,
        sequenceNumber: data.bomLink.sequenceNumber
      },
      bomUsesOccurrence: {
        referenceDesignator: data.bomUsesOccurrence.referenceDesignator
      }
    };
    
    console.log('清理后的编辑BOM关系数据:', JSON.stringify(cleanEditData, null, 2));
    
    await bomLinkUpdateService(editBomModel.value.bomLinkId, cleanEditData);
    ElMessage.success('BOM关系更新成功');
    editBomDialogVisible.value = false;
    
    // 刷新BOM树数据
    if (bomTreeDialogVisible.value && bomTreeData.value?.rootPart?.versionId) {
      const res = await bomTreeQueryService(bomTreeData.value.rootPart.versionId, { includeQty: true });
      bomTreeData.value = res;
    }
  } catch (error) {
    console.error('更新BOM关系失败:', error);
    ElMessage.error(`更新BOM关系失败: ${error.response?.data?.message || error.message || '未知错误'}`);
  }
};

// 删除BOM关系
const deleteBomLink = (bomItem) => {
  ElMessageBox.confirm(`确定要删除这个BOM关系吗？`, '温馨提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        console.log('删除BOM关系:', bomItem.bomLinkId);
        
        await bomLinkDeleteService(bomItem.bomLinkId);
        ElMessage.success('BOM关系删除成功');
        
        // 刷新BOM树数据
        if (bomTreeDialogVisible.value && bomTreeData.value?.rootPart?.versionId) {
          const res = await bomTreeQueryService(bomTreeData.value.rootPart.versionId, { includeQty: true });
          bomTreeData.value = res;
        }
      } catch (error) {
        console.error('删除BOM关系失败:', error);
        ElMessage.error(`删除BOM关系失败: ${error.response?.data?.message || error.message || '未知错误'}`);
      }
    })
    .catch(() => {
      ElMessage.info('已取消删除');
    });
};

// 测试BOM接口连通性
const testBomAPI = async () => {
  try {
    ElMessage.info('正在测试BOM接口连通性...');
    
    // 检查是否有可用的Part数据
    if (partList.value.length < 2) {
      ElMessage.warning('需要至少2个Part才能测试BOM关系创建，请先添加Part');
      return;
    }
    
    // 找到两个有versionId的Part
    const partsWithVersion = partList.value.filter(part => part.versionId);
    if (partsWithVersion.length < 2) {
      ElMessage.warning('需要至少2个有版本ID的Part才能测试，请检查Part数据');
      return;
    }
    
    const sourcePart = partsWithVersion[0];
    const targetPart = partsWithVersion[1];
    
    console.log('测试用源Part:', sourcePart);
    console.log('测试用目标Part:', targetPart);    // 根据接口文档创建正确的测试数据：source是versionId，target是masterId
    // 同时包含Part的详细信息，确保请求体包含名称等必要信息
    const testData = {
      bomLink: {
        source: sourcePart.versionId, // Part版本ID
        target: targetPart.masterId, // PartMaster主对象ID
        quantity: 1.0,
        sequenceNumber: 999,
        // 包含源Part信息
        sourcePart: {
          name: sourcePart.name,
          masterId: sourcePart.masterId,
          versionId: sourcePart.versionId,
          description: sourcePart.description || ''
        },
        // 包含目标Part信息
        targetPart: {
          name: targetPart.name,
          masterId: targetPart.masterId,
          description: targetPart.description || ''
        }
      },
      bomUsesOccurrence: {
        referenceDesignator: 'TEST_REF'
      }
    };
    
    console.log('测试BOM关系数据:', JSON.stringify(testData, null, 2));
      // 确保测试数据结构严格符合接口要求，防止意外字段污染
    const cleanTestData = {
      bomLink: {
        source: testData.bomLink.source,
        target: testData.bomLink.target,
        quantity: testData.bomLink.quantity,
        sequenceNumber: testData.bomLink.sequenceNumber,
        sourcePart: {
          name: testData.bomLink.sourcePart.name,
          masterId: testData.bomLink.sourcePart.masterId,
          versionId: testData.bomLink.sourcePart.versionId,
          description: testData.bomLink.sourcePart.description
        },
        targetPart: {
          name: testData.bomLink.targetPart.name,
          masterId: testData.bomLink.targetPart.masterId,
          description: testData.bomLink.targetPart.description
        }
      },
      bomUsesOccurrence: {
        referenceDesignator: testData.bomUsesOccurrence.referenceDesignator
      }
    };
    
    console.log('清理后的测试BOM关系数据:', JSON.stringify(cleanTestData, null, 2));
    
    // 尝试创建BOM关系
    await bomLinkCreateService(cleanTestData);
    
    ElMessage.success('BOM接口测试成功！接口工作正常');
    
    // 刷新Part列表
    await getPartList();
      } catch (error) {
    console.error('BOM接口测试失败:', error);
    console.error('错误详情:', error.response?.data);
    console.error('请求配置:', error.config);
    
    // 输出实际发送的数据，帮助调试
    if (error.config?.data) {
      console.error('实际发送的测试数据:', typeof error.config.data === 'string' ? error.config.data : JSON.stringify(error.config.data, null, 2));
    }
    
    let errorMessage = 'BOM接口测试失败';
    if (error.response?.status === 500) {
      errorMessage += ': 服务器内部错误';
      if (error.response?.data?.message) {
        errorMessage += ` - ${error.response.data.message}`;
      }
    } else if (error.response?.status === 400) {
      errorMessage += ': 请求参数错误';
      if (error.response?.data?.message) {
        errorMessage += ` - ${error.response.data.message}`;
      }
    } else if (error.response?.data?.message) {
      errorMessage += `: ${error.response.data.message}`;
    } else if (error.message) {
      errorMessage += `: ${error.message}`;
    }
    
    ElMessage.error(errorMessage);
  }
};
</script>
<style scoped>
.page-wrapper { position: relative; z-index: 1; }
.top-action-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.page-title { font-size: 28px; font-weight: 600; color: #333; }
.actions { display: flex; align-items: center; }
.add-button { margin-left: 16px; }
.action-buttons .el-button { margin: 0 2px; }
.action-button.disabled {
  filter: grayscale(1);
  opacity: 0.5;
  cursor: not-allowed !important;
}
.action-button.active {
  filter: none;
  opacity: 1;
}
.action-button.tree {
  color: #409eff;
}
.action-button.list {
  color: #67c23a;
}
.action-button.usage {
  color: #e6a23c;
}
.action-button.add {
  color: #909399;
}
.tech-table {
  font-size: 18px;
}
.tech-dialog .el-dialog__body {
  padding: 20px;
}
.tree-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}
.tree-header h3 {
  margin: 0;
  color: #333;
}
.tree-controls {
  display: flex;
  gap: 8px;
}
.bom-tree {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  max-height: 500px;
  overflow-y: auto;
}
.bom-tree-node {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 4px 8px;
  min-height: 32px;
}
.tree-node {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 4px 0;
}
.node-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}
.node-name {
  font-weight: 500;
}
.node-number {
  color: #666;
}
.node-quantity {
  color: #409eff;
  font-weight: 500;
}
.node-designator {
  color: #67c23a;
  font-size: 12px;
}
.node-actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
  margin-left: auto;
}
.list-summary {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
}
.empty-state {
  text-align: center;
  padding: 40px 0;
}
.batch-items {
  margin-top: 20px;
}
.items-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e4e7ed;
}
.batch-item {
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  margin-bottom: 12px;
  background-color: #fafafa;
}
</style>