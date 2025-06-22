import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/views/Login.vue'
import Layout from '@/views/Layout.vue'
import Attribute from '@/views/Attribute.vue'
import Classification from '@/views/Classification.vue'

const routes = [
  { path: '/login', component: Login },
  {
    path: '/layout',
    component: Layout,
    redirect: '/layout/attribute',
    children: [
      { path: 'attribute', component: Attribute },
      { path: 'classification', component: Classification },
      { path: 'part', component: () => import('@/views/Part.vue') }
      // 之后可以添加更多子路由，如 bom, category 等
    ]
  },
  // 将根路径重定向到登录页
  {
    path: '/',
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router 