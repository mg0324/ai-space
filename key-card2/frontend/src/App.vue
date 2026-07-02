<template>
  <a-config-provider :locale="zhCN">
    <a-layout v-if="isLoggedIn" style="height: 100vh; overflow: hidden">
      <a-layout-sider theme="dark" :width="180" style="overflow-y: auto">
        <div style="color: #fff; font-size: 16px; font-weight: bold; padding: 20px 16px 8px; text-align: center">
          关键卡片
        </div>
        <a-menu
          v-model:selectedKeys="selectedKeys"
          theme="dark"
          mode="inline"
        >
          <a-menu-item key="cards" @click="$router.push('/')">
            <template #icon><CalendarOutlined /></template>
            卡片管理
          </a-menu-item>
          <a-menu-item key="tags" @click="$router.push('/tags')">
            <template #icon><TagsOutlined /></template>
            标签管理
          </a-menu-item>
          <a-menu-item key="generate" @click="$router.push('/generate')">
            <template #icon><ThunderboltOutlined /></template>
            生成网页
          </a-menu-item>
        </a-menu>
      </a-layout-sider>
      <a-layout>
        <a-layout-header style="background: #fff; padding: 0 24px; display: flex; justify-content: space-between; align-items: center; height: 48px; line-height: 48px; box-shadow: 0 1px 4px rgba(0,0,0,0.08); flex-shrink: 0">
          <a-breadcrumb>
            <a-breadcrumb-item><HomeOutlined /></a-breadcrumb-item>
            <a-breadcrumb-item>{{ breadcrumbTitle }}</a-breadcrumb-item>
          </a-breadcrumb>
          <a-space>
            <span style="color: #333; font-size: 14px">
              <UserOutlined style="margin-right: 6px" />
              {{ username }}
            </span>
            <a-button type="text" danger size="small" @click="handleLogout">
              <template #icon><LogoutOutlined /></template>
              退出
            </a-button>
          </a-space>
        </a-layout-header>
        <a-layout-content style="padding: 24px; background: #f0f2f5; overflow: hidden; flex: 1">
          <router-view />
        </a-layout-content>
      </a-layout>
    </a-layout>
    <router-view v-else />
  </a-config-provider>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import zhCN from 'ant-design-vue/es/locale/zh_CN'
import { CalendarOutlined, TagsOutlined, ThunderboltOutlined, LogoutOutlined, UserOutlined, HomeOutlined } from '@ant-design/icons-vue'
import { authApi } from './api/index.js'

const route = useRoute()
const router = useRouter()
const selectedKeys = ref(['cards'])

const isLoggedIn = ref(!!localStorage.getItem('token'))
const username = ref(localStorage.getItem('username') || '')

function updateAuth() {
  isLoggedIn.value = !!localStorage.getItem('token')
  username.value = localStorage.getItem('username') || ''
}

window.addEventListener('storage', updateAuth)

const breadcrumbMap = {
  '/': '卡片管理',
  '/tags': '标签管理',
  '/generate': '生成网页',
}

const breadcrumbTitle = computed(() => {
  const path = route.path
  if (breadcrumbMap[path]) return breadcrumbMap[path]
  if (path === '/cards/new') return '新建卡片'
  if (path.startsWith('/cards/') && path.endsWith('/edit')) return '编辑卡片'
  return '卡片管理'
})

watch(() => route.path, (path) => {
  updateAuth()
  if (path === '/' || path.startsWith('/cards')) selectedKeys.value = ['cards']
  else if (path.startsWith('/tags')) selectedKeys.value = ['tags']
  else if (path.startsWith('/generate')) selectedKeys.value = ['generate']
}, { immediate: true })

const handleLogout = async () => {
  try {
    await authApi.logout()
  } catch {
    // Even if API call fails, proceed with local logout
  }
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  router.push('/login')
}
</script>
