<template>
  <div class="login-container">
    <!-- 背景中的网格线和光晕效果 -->
    <div class="background-grid"></div>

    <div class="login-card">
      <div class="card-header">
        <!-- 标题根据当前是登录还是注册状态动态变化 -->
        <h2>{{ isRegister ? '欢迎注册 MiniBOM' : 'MiniBOM 系统登录' }}</h2>
      </div>

      <!-- 登录表单 -->
      <el-form
        v-if="!isRegister"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @submit.prevent="handleLogin"
        label-position="top"
        ref="loginFormRef"
        autocomplete="off"
      >
        <el-form-item label="用户名" prop="name">
          <el-input
            v-model="loginForm.name"
            placeholder="请输入您的用户名"
            :prefix-icon="User"
            size="large"
            autocomplete="off"
          ></el-input>
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入您的密码"
            :prefix-icon="Lock"
            show-password
            size="large"
            autocomplete="new-password"
          ></el-input>
        </el-form-item>

        <el-form-item>
          <!-- 按钮宽度100% 并在样式中自定义效果 -->
          <el-button
            class="login-button"
            native-type="submit"
            size="large"
            :loading="loading"
          >
            登  录
          </el-button>
        </el-form-item>
        <div class="form-footer">
          <el-link type="primary" @click="isRegister = true; resetForms()">还没有账户？立即注册</el-link>
        </div>
      </el-form>

      <!-- 注册表单 -->
      <el-form
        v-else
        :model="registerForm"
        :rules="registerRules"
        class="login-form"
        @submit.prevent="handleRegister"
        label-position="top"
        ref="registerFormRef"
        autocomplete="off"
      >
        <el-form-item label="用户名" prop="name">
          <el-input
            v-model="registerForm.name"
            placeholder="请输入用户名"
            :prefix-icon="User"
            size="large"
            autocomplete="off"
          ></el-input>
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
            size="large"
            autocomplete="new-password"
          ></el-input>
        </el-form-item>

        <el-form-item label="确认密码" prop="rePassword">
          <el-input
            v-model="registerForm.rePassword"
            type="password"
            placeholder="请再次输入密码"
            :prefix-icon="Lock"
            show-password
            size="large"
            autocomplete="new-password"
          ></el-input>
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入邮箱"
            :prefix-icon="Message"
            size="large"
            autocomplete="off"
          ></el-input>
        </el-form-item>

        <el-form-item label="手机号" prop="telephone">
          <el-input
            v-model="registerForm.telephone"
            placeholder="请输入手机号"
            :prefix-icon="Phone"
            size="large"
            autocomplete="off"
          ></el-input>
        </el-form-item>

        <el-form-item>
          <!-- 按钮宽度100% 并在样式中自定义效果 -->
          <el-button
            class="login-button"
            native-type="submit"
            size="large"
            :loading="loading"
          >
            注  册
          </el-button>
        </el-form-item>
        <div class="form-footer">
          <el-link type="primary" @click="isRegister = false; resetForms()">已有账户？返回登录</el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, nextTick } from 'vue'
import { useRouter } from 'vue-router'
// 引入 Element Plus 图标
import { User, Lock, Message, Phone } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useTokenStore } from '@/stores/token.js'
import { userLoginService, userRegisterService } from '@/api/user.js'

const router = useRouter()
const tokenStore = useTokenStore()

// --- 状态控制 ---
const isRegister = ref(false)
const loading = ref(false)

// --- 登录逻辑 ---
const loginFormRef = ref(null)
const loginForm = reactive({
  name: '',
  password: ''
})
const loginRules = {
  name: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerFormRef = ref(null)
const registerForm = reactive({
  name: '',
  password: '',
  rePassword: '',
  email: '',
  telephone: ''
})

const validateRePassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  name: [{ required: true, message: '请输入6-32位的字母或数字作为用户名', pattern: /^[a-zA-Z0-9]{6,32}$/, trigger: 'blur' }],
  password: [{ required: true, message: '密码需8-32位，且包含字母、数字和特殊字符(@$!%*?&)', pattern: /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,32}$/, trigger: 'blur' }],
  rePassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateRePassword, trigger: 'blur' }
  ],
  email: [{ required: true, type: 'email', message: '请输入有效的邮箱地址，如 example@domain.com', trigger: 'blur' }],
  telephone: [{ required: true, message: '请输入11位中国大陆手机号码', pattern: /^1[3-9]\d{9}$/, trigger: 'blur' }]
}

// 切换时重置表单校验状态
const resetForms = () => {
  nextTick(() => {
    loginFormRef.value?.clearValidate();
    registerFormRef.value?.clearValidate();
  });
}

const handleLogin = async () => {
  await loginFormRef.value.validate()
  loading.value = true
  try {
    const token = await userLoginService(loginForm)
    tokenStore.setToken(token)
    ElMessage.success('登录成功，即将跳转...')
    await router.push('/layout')
  } catch (error) {
    // 错误消息已由拦截器统一处理
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  await registerFormRef.value.validate()
  loading.value = true
  try {
    await userRegisterService(registerForm)
    ElMessage.success('注册成功，请返回登录')
    isRegister.value = false
  } catch (error) {
    // 错误消息已由拦截器统一处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 使用更专业的字体，如果用户本地没有，会回退到 sans-serif */
@import url('https://fonts.googleapis.com/css2?family=Source+Sans+Pro:wght@400;600;700&display=swap');

.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  overflow: hidden; /* 防止背景元素溢出 */
  position: relative; /* 为伪元素定位 */
  
  /* 主色调: 深邃的科技蓝 (#0D1B3E) 到 星云紫 (#2F1E4A) 的渐变 */
  background: linear-gradient(135deg, #0d1b3e 0%, #2f1e4a 100%);
  font-family: 'Source Sans Pro', 'HarmonyOS Sans', 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
}

/* 动态背景的网格线，使用伪元素实现，不干扰布局 */
.background-grid {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.05) 1px, transparent 1px);
  background-size: 30px 30px;
  animation: bg-pan 15s linear infinite;
  z-index: 1;
}

@keyframes bg-pan {
  0% {
    background-position: 0 0;
  }
  100% {
    background-position: 30px 30px;
  }
}

/* 登录卡片 - 核心的毛玻璃效果 */
.login-card {
  width: 420px;
  padding: 40px;
  z-index: 2; /* 确保在背景之上 */

  /* 毛玻璃效果 (Glassmorphism) */
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(15px);
  -webkit-backdrop-filter: blur(15px); /* 兼容 Safari */
  
  /* 边框增加质感 */
  border: 1px solid rgba(255, 255, 255, 0.2);
  
  /* 圆角和阴影 */
  border-radius: 16px;
  box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.37);
}

.card-header {
  text-align: center;
  margin-bottom: 2rem;
}

.card-header h2 {
  color: #ffffff;
  font-size: 28px;
  font-weight: 600;
  margin: 0;
}

/* 深度选择器 :deep() 用于修改 Element Plus 组件的内部样式 */

/* 表单项标签 */
.login-form :deep(.el-form-item__label) {
  color: rgba(255, 255, 255, 0.8);
  font-weight: 600;
}

/* 输入框样式 */
.login-form :deep(.el-input__wrapper) {
  background-color: rgba(0, 0, 0, 0.2);
  box-shadow: none; /* 移除 Element Plus 的默认阴影 */
  border-radius: 8px;
  border: 1px solid transparent;
  transition: border-color 0.3s, box-shadow 0.3s;
}

/* 输入框聚焦时的发光效果 */
.login-form :deep(.el-input__wrapper.is-focus) {
  border-color: #00BFFF; /* 高亮色：赛博蓝 */
  box-shadow: 0 0 10px rgba(0, 191, 255, 0.5); /* 外发光效果 */
}

/* 输入框内的文字颜色 */
.login-form :deep(.el-input__inner) {
  color: #ffffff;
}
/* 输入框占位符颜色 */
.login-form :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.5);
}

/* 输入框前缀图标颜色 */
.login-form :deep(.el-input__prefix-icon) {
  color: rgba(255, 255, 255, 0.8);
}

/* 登录按钮 */
.login-button {
  width: 100%;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  font-size: 16px;
  letter-spacing: 2px; /* 增加文字间距 */

  /* 高亮点缀色: 赛博蓝到青色的渐变 */
  background: linear-gradient(90deg, #00BFFF 0%, #0AFFA7 100%);
  
  transition: all 0.3s ease;
}

.login-button:hover {
  /* 鼠标悬停时提高亮度并增加发光效果 */
  filter: brightness(1.2);
  box-shadow: 0 0 15px rgba(10, 255, 167, 0.6);
}

.form-footer {
  text-align: center;
  margin-top: 1rem;
}
</style>