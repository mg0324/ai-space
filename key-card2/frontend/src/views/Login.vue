<template>
  <div style="display: flex; justify-content: center; align-items: center; min-height: 100vh; background: #f0f2f5">
    <a-card title="管理员登录" style="width: 400px" :bordered="true">
      <a-form :model="form" :rules="rules" @finish="handleLogin" layout="vertical">
        <a-form-item label="用户名" name="username">
          <a-input v-model:value="form.username" placeholder="请输入用户名" size="large" />
        </a-form-item>
        <a-form-item label="密码" name="password">
          <a-input-password v-model:value="form.password" placeholder="请输入密码" size="large" @pressEnter="handleLogin" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit" :loading="loading" block size="large">
            登录
          </a-button>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { authApi } from '../api/index.js'

const router = useRouter()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
})

const rules = {
  username: [
    { required: true, message: '请输入用户名' },
  ],
  password: [
    { required: true, message: '请输入密码' },
  ],
}

const handleLogin = async () => {
  loading.value = true
  try {
    const { data } = await authApi.login({
      username: form.username,
      password: form.password,
    })
    localStorage.setItem('token', data.token)
    localStorage.setItem('username', data.username)
    message.success('登录成功')
    router.push('/')
  } catch (err) {
    const msg = err.response?.data?.error || '登录失败'
    message.error(msg)
  } finally {
    loading.value = false
  }
}
</script>
