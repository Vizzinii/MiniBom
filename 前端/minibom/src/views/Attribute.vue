<template>
    <div class="page-wrapper">
      <!-- 顶部操作区 -->
      <div class="top-action-bar">
        <h2 class="page-title">属性管理</h2>
        <div class="actions">
          <!-- 搜索表单 -->
          <el-form :inline="true" class="search-form">
            <el-form-item label="属性名称">
              <el-input v-model="searchName" placeholder="请输入名称"></el-input>
            </el-form-item>
            <el-form-item>
              <el-button class="search-button" @click="onSearch">搜索</el-button>
              <el-button class="reset-button" @click="onReset">重置</el-button>
            </el-form-item>
          </el-form>
          <el-button class="add-button" @click="showAddDialog">
            <el-icon><Plus /></el-icon><span>添加属性</span>
          </el-button>
        </div>
      </div>
  
      <!-- 属性列表 -->
      <el-table :data="attributeList" style="width: 100%" v-loading="loading" class="tech-table">
        <el-table-column prop="name" label="中文名称" sortable></el-table-column>
        <el-table-column prop="nameEn" label="英文名称" sortable></el-table-column>
        <el-table-column prop="type" label="数据类型"></el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip></el-table-column>
        <el-table-column label="操作" width="150" align="center">
          <template #default="{ row }">
            <div class="action-buttons">
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
  
      <!-- 添加/编辑属性的弹窗 -->
      <el-dialog v-model="dialogVisible" :title="dialogTitle" width="40%" class="tech-dialog">
        <el-form :model="attributeModel" label-width="100px" label-position="top">
          <el-form-item label="中文名称">
            <el-input v-model="attributeModel.name"></el-input>
          </el-form-item>
          <el-form-item label="英文名称">
            <el-input v-model="attributeModel.nameEn"></el-input>
          </el-form-item>
          <el-form-item label="数据类型">
            <el-select v-model="attributeModel.type" placeholder="请选择" :disabled="!!attributeModel.id" style="width: 100%;">
              <el-option label="字符串 (STRING)" value="STRING"></el-option>
              <el-option label="数值型 (DECIMAL)" value="DECIMAL"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="描述">
            <el-input type="textarea" v-model="attributeModel.description"></el-input>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button class="reset-button" @click="dialogVisible = false">取 消</el-button>
            <el-button class="add-button" @click="onSubmit">确 认</el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue';
  import { ElMessage, ElMessageBox } from 'element-plus';
  import { Edit, Delete, Plus } from '@element-plus/icons-vue';
  import { 
      attributeQueryService, 
      attributeCreateService, 
      attributeUpdateService, 
      attributeDeleteService 
  } from '@/api/attributeAPI.js';
  
  const attributeList = ref([]);
  const loading = ref(false);
  const searchName = ref('');
  const dialogVisible = ref(false);
  const dialogTitle = ref('');
  const attributeModel = ref({
      id: null,
      name: '',
      nameEn: '',
      type: 'STRING',
      description: ''
  });
  
  const getAttributeList = async () => {
      loading.value = true;
      try {
          const res = await attributeQueryService(searchName.value);
          attributeList.value = res;
      } catch (error) {
          console.error(error);
      } finally {
          loading.value = false;
      }
  };
  
  onMounted(() => {
      getAttributeList();
  });
  
  const onSearch = () => getAttributeList();
  
  const onReset = () => {
      searchName.value = '';
      getAttributeList();
  };
  
  const showAddDialog = () => {
      dialogTitle.value = '添加属性';
      attributeModel.value = { id: null, name: '', nameEn: '', type: 'STRING', description: '' };
      dialogVisible.value = true;
  };
  
  const showEditDialog = (row) => {
      dialogTitle.value = '编辑属性';
      attributeModel.value = { ...row };
      dialogVisible.value = true;
  };
  
  const onSubmit = async () => {
      try {
          if (attributeModel.value.id) {
              await attributeUpdateService({
                  id: attributeModel.value.id,
                  description: attributeModel.value.description,
                  name: attributeModel.value.name,
                  nameEn: attributeModel.value.nameEn
              });
              ElMessage.success('更新成功');
          } else {
              await attributeCreateService(attributeModel.value);
              ElMessage.success('添加成功');
          }
          dialogVisible.value = false;
          await getAttributeList();
      } catch (error) {
          ElMessage.error('操作失败');
      }
  };
  
  const onDelete = (row) => {
      ElMessageBox.confirm(`你确定要删除属性【${row.name}】吗?`, '温馨提示', {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning',
          customClass: 'tech-messagebox'
      })
      .then(async () => {
          await attributeDeleteService([row.id]);
          ElMessage.success('删除成功');
          await getAttributeList();
      })
      .catch(() => {
         ElMessage.info('已取消删除');
      });
  };
  </script>
  
  <style scoped>
  .page-wrapper {
    position: relative;
    z-index: 1;
  }
  
  :root {
    --accent-cyan: #00bfff;
    --accent-teal: #0affa7;
    --text-primary: #ffffff;
    --text-secondary: rgba(229, 231, 235, 0.9);
    --border-color: rgba(255, 255, 255, 0.3);
    --bg-input: rgba(0, 0, 0, 0.3);
    --danger-color: #ff6b6b;
  }
  
  .top-action-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
  }
  
  .page-title {
    font-size: 28px;
    font-weight: 600;
    color: var(--text-primary);
    text-shadow: 0 0 10px rgba(255, 255, 255, 0.3);
  }
  
  .actions {
    display: flex;
    align-items: center;
    gap: 16px;
  }
  
  .search-form {
    display: flex;
    gap: 10px;
  }
  .search-form :deep(.el-form-item) {
    margin-bottom: 0;
  }
  .search-form :deep(.el-form-item__label) {
    color: var(--text-secondary);
    font-weight: 600;
  }
  
  /*
    ======================================================
    ==  核心修复：统一所有表单控件 (Input, Select, Textarea) 的样式 ==
    ======================================================
  */
  :deep(.el-input__wrapper),
  :deep(.el-select .el-select__wrapper),
  :deep(.el-textarea__inner) {
    background-color: #ffffff !important;
    box-shadow: 0 1px 3px rgba(0,0,0,0.1);
    border: 1px solid #dcdfe6 !important;
    border-radius: 6px;
    color: #606266 !important;
    transition: border-color 0.3s, box-shadow 0.3s;
  }
  
  :deep(.el-textarea__inner) {
      padding: 8px 11px;
      min-height: 90px !important;
  }
  
  :deep(.el-input.is-focus .el-input__wrapper),
  :deep(.el-select.is-focus .el-select__wrapper),
  :deep(.el-textarea.is-focus .el-textarea__inner) {
    border-color: var(--accent-cyan) !important;
    box-shadow: 0 0 10px rgba(0, 191, 255, 0.5) !important;
  }
  
  :deep(.el-input__inner::placeholder),
  :deep(.el-textarea__inner::placeholder) {
    color: #a8abb2;
  }
  
  /* 按钮样式 */
  .tech-button, .search-button, .reset-button, .add-button {
    border-radius: 6px;
    font-weight: 600;
    transition: all 0.3s ease;
    border: none;
    padding: 10px 20px;
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  }
  
  .search-button, .add-button {
    background-color: #409EFF;
    color: white;
  }
  .search-button:hover, .add-button:hover {
    background-color: #66b1ff;
    box-shadow: 0 4px 8px rgba(64, 158, 255, 0.2);
  }
  
  .reset-button {
    background-color: #f5f7fa;
    color: #606266;
    border: 1px solid #dcdfe6;
  }
  
  .reset-button:hover {
    background-color: #e4e7ed;
  }
  
  .add-button {
    display: flex;
    align-items: center;
    gap: 8px;
    border: none;
    font-weight: 600;
    color: #000;
    background: linear-gradient(90deg, var(--accent-cyan) 0%, var(--accent-teal) 100%);
    transition: all 0.3s ease;
  }
  .add-button:hover {
    filter: brightness(1.2);
    box-shadow: 0 0 15px rgba(10, 255, 167, 0.6);
  }
  
  /* 表格样式 */
  .tech-table {
    background: transparent !important;
    border-radius: 8px;
    overflow: hidden;
    border: 1px solid var(--border-color);
  }
  .tech-table :deep(th.el-table__cell) {
    background: rgba(0, 191, 255, 0.15) !important;
    color: var(--text-primary);
    font-weight: 600;
    border-bottom: 1px solid var(--accent-cyan);
  }
  .tech-table :deep(td.el-table__cell) {
    border-bottom: 1px solid var(--border-color);
  }
  .tech-table :deep(tr) {
    background: transparent !important;
    color: var(--text-secondary);
    transition: background-color 0.3s;
  }
  .tech-table :deep(tr:hover > td.el-table__cell) {
    background: rgba(0, 191, 255, 0.1) !important;
  }
  .tech-table :deep(.el-table__inner-wrapper::before) {
    display: none;
  }
  
  /* 表格内操作按钮 */
  .action-buttons {
    display: flex;
    gap: 10px;
    justify-content: center;
  }
  .action-button {
    font-size: 18px;
    transition: all 0.2s;
    padding: 4px;
  }
  .action-button.edit { color: var(--accent-cyan); }
  .action-button.delete { color: var(--danger-color); }
  .action-button:hover {
    transform: scale(1.2);
    filter: drop-shadow(0 0 5px currentColor);
  }
  
  /* 弹窗样式 */
  :deep(.tech-dialog) {
    background: rgba(25, 35, 65, 0.9) !important;
    border-radius: 12px;
    border: 1px solid var(--border-color);
    backdrop-filter: blur(12px);
    -webkit-backdrop-filter: blur(12px);
  }
  :deep(.tech-dialog .el-dialog__title) {
    color: var(--text-primary);
    font-weight: 600;
  }
  :deep(.tech-dialog .el-dialog__headerbtn .el-icon) {
    color: var(--text-primary);
  }
  :deep(.tech-dialog .el-form-item__label) {
    color: var(--text-secondary);
    font-weight: 600;
  }
  </style>
  
  <!-- 全局样式 -->
  <style>
  .el-message-box.tech-messagebox {
    background: rgba(25, 35, 65, 0.9) !important;
    border: 1px solid rgba(255, 255, 255, 0.2) !important;
    backdrop-filter: blur(10px);
  }
  .tech-messagebox .el-message-box__title,
  .tech-messagebox .el-message-box__content {
    color: #fff !important;
  }
  </style>