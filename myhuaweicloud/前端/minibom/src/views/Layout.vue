<template>
  <el-container class="layout-container">
    <!-- 1. 左侧导航栏 (Sidebar) -->
    <el-aside width="220px">
      <div class="sidebar-header">
        <img src="@/assets/logo.svg" class="sidebar-logo" />
        <h1>MiniBOM 系统</h1>
      </div>
      <el-scrollbar>
        <el-menu :default-openeds="['1']" router>
          <el-sub-menu index="1">
            <template #title>
              <el-icon><DataAnalysis /></el-icon><span>数据管理</span>
            </template>
            <el-menu-item index="/layout/attribute">
              <el-icon><Tickets /></el-icon><span>属性管理</span>
            </el-menu-item>
            <el-menu-item index="/layout/classification">
              <el-icon><Promotion /></el-icon><span>分类管理</span>
            </el-menu-item>
            <el-menu-item index="/layout/part">
              <el-icon><Document /></el-icon><span>Part&BOM管理</span>
            </el-menu-item>
            <!-- 在这里可以添加更多菜单项 -->
          </el-sub-menu>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <el-container>
      <!-- 2. 顶部栏 (Header) -->
      <el-header>
        <div class="toolbar">
          <el-dropdown>
            <div class="user-info">
              <el-icon class="user-icon"><Setting /></el-icon>
              <span>Admin</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>查看信息</el-dropdown-item>
                <el-dropdown-item divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 3. 主内容区域 (Main Content) -->
      <el-main>
        <router-view></router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script lang="ts" setup>
import { Setting, DataAnalysis, Tickets, Promotion, Document } from '@element-plus/icons-vue'
</script>

<style scoped>
/*
  ================================================================
  ==  全新、稳定、简洁的后台布局样式
  ================================================================
*/

/* ---- 1. 全局与布局 ---- */
.layout-container {
  height: 100vh;
  /* 定义颜色变量，方便维护 */
  --sidebar-bg-color: #2c3e50;
  --sidebar-text-color: #bfcbd9;
  --sidebar-active-text-color: #409EFF; /* Element Plus 默认主题蓝 */
  --main-bg-color: #f4f6f9;
}

/* ---- 2. 左侧导航栏 ---- */
.el-aside {
  background-color: var(--sidebar-bg-color);
  transition: width 0.3s;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 60px; /* 固定高度 */
  color: #ffffff;
}

.sidebar-logo {
  width: 32px;
  height: 32px;
  margin-right: 10px;
}

.sidebar-header h1 {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

/*
  核心：通过官方 CSS 变量配置 el-menu，这是最稳定可靠的方式
*/
.el-menu {
  /* 配置菜单的颜色 */
  --el-menu-bg-color: var(--sidebar-bg-color);
  --el-menu-text-color: var(--sidebar-text-color);
  --el-menu-active-color: var(--sidebar-active-text-color);
  --el-menu-hover-bg-color: #263445; /* 悬停时一个稍亮的深灰色 */
  --el-menu-hover-text-color: #ffffff; /* 悬停时文字变白 */

  /* 移除默认边框 */
  border-right: none;
}

/* 对激活项的背景色做一点细微的强调 */
.el-menu-item.is-active {
  background-color: #263445;
}

/* ---- 3. 顶部和主内容区 ---- */
.el-header {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  background-color: #ffffff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  padding-right: 20px;
}

.toolbar {
  display: flex;
  align-items: center;
}

.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
}

.user-icon {
  margin-right: 8px;
  font-size: 18px;
}

.el-main {
  background-color: var(--main-bg-color);
  padding: 20px;
}
</style>