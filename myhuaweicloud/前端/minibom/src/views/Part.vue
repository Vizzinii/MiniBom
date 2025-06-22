<template>
  <div class="page-wrapper">
    <!-- 顶部操作区 -->
    <div class="top-action-bar">
      <h2 class="page-title">Part</h2>
      <div class="actions">
        <el-button class="add-button" @click="showAddDialog">
          <el-icon><Plus /></el-icon><span>添加Part</span>
        </el-button>
      </div>
    </div>
    <!-- 查找功能表单 -->
    <el-form :inline="true" class="search-form" style="margin-bottom: 16px;">
      <el-form-item label="名称">
        <el-input v-model="searchParams.name" placeholder="请输入名称"></el-input>
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
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSearch">搜索</el-button>
        <el-button @click="onReset">重置</el-button>
      </el-form-item>
    </el-form>
    <!-- Part列表 -->
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
      <el-table-column label="操作" width="180" align="center">
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
    />
    <!-- 添加/编辑Part的弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="40%" class="tech-dialog">
      <el-form :model="partModel" :rules="formRules" ref="formRef" label-width="100px" label-position="top">
        <el-form-item label="Part Name" prop="name" required>
          <el-input v-model="partModel.name"></el-input>
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
    </el-dialog>
    <el-dialog v-model="detailDialogVisible" title="Part详情" width="40%">
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
  </div>
</template>
<script setup>
import { ref, onMounted, nextTick, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Edit, Delete, Plus, View, Lock, Unlock } from '@element-plus/icons-vue';
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
import { classificationListService } from '@/api/classificationAPI.js';

const partList = ref([]);
const loading = ref(false);
const dialogVisible = ref(false);
const dialogTitle = ref('');
const partModel = ref({
  master: { name: '' },
  version: { source: '', partType: '', workingState: '', classificationId: '', description: '', iterationNote: '' }
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
    };
    const res = await partQueryService(params);
    partList.value = res.records || [];
    total.value = res.total || 0;
  } catch (error) {
    ElMessage.error('获取Part列表失败');
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
});

const showAddDialog = () => {
  dialogTitle.value = '添加Part';
  partModel.value = { master: { name: '' }, version: { source: '', partType: '', workingState: '', classificationId: '', description: '', iterationNote: '' } };
  dialogVisible.value = true;
  nextTick(() => validateForm())
};
const showEditDialog = (row) => {
  dialogTitle.value = '编辑Part';
  currentEditRow.value = row;
  partModel.value = {
    name: row.name || '',
    source: row.source || '',
    partType: row.partType || '',
    iterationNote: row.iterationNote || ''
  };
  dialogVisible.value = true;
  nextTick(() => validateForm())
};
const onSubmit = async () => {
  const valid = await validateForm();
  if (!valid) return;
  try {
    if (dialogTitle.value.includes('编辑')) {
      await partUpdateService(currentEditRow.value.masterId, {
        name: partModel.value.name,
        source: partModel.value.source,
        partType: partModel.value.partType,
        iterationNote: partModel.value.iterationNote
      });
      ElMessage.success('更新成功');
    } else {
      await partCreateService(partModel.value);
      ElMessage.success('添加成功');
    }
    dialogVisible.value = false;
    await getPartList();
  } catch (error) {
    ElMessage.error('操作失败');
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
    };
    const res = await partQueryService(params);
    partList.value = res.records || [];
    total.value = res.total || 0;
  } catch (error) {
    ElMessage.error('获取Part列表失败');
  } finally {
    loading.value = false;
  }
};

const onReset = () => {
  searchParams.value = {
    name: '',
    source: '',
    partType: '',
  };
  currentPage.value = 1;
  onSearch();
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
.tech-table {
  font-size: 18px;
}
</style> 