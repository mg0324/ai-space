<template>
  <div style="display: flex; justify-content: center; align-items: center; min-height: 100vh; background: #f0f2f5">
    <a-card title="管理员注册" style="width: 400px" :bordered="true">
      <a-form :model="form" :rules="rules" @finish="handleRegister" layout="vertical">
        <a-form-item label="用户名" name="username">
          <a-input v-model:value="form.username" placeholder="请输入用户名（2-20字符）" size="large" />
        </a-form-item>
        <a-form-item label="密码" name="password">
          <a-input-password v-model:value="form.password" placeholder="请输入密码（6位以上）" size="large" />
        </a-form-item>
        <a-form-item label="确认密码" name="confirmPassword">
          <a-input-password v-model:value="form.confirmPassword" placeholder="请再次输入密码" size="large" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit" :loading="loading" block size="large">
            注册
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
  confirmPassword: '',
})

const validateConfirmPassword = async (_rule, value) => {
  if (value && value !== form.password) {
    throw new Error('两次输入的密码不一致')
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名' },
    { min: 2, max: 20, message: '用户名长度需在2-20字符之间' },
  ],
  password: [
    { required: true, message: '请输入密码' },
    { min: 6, message: '密码长度不能少于6位' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码' },
    { validator: validateConfirmPassword },
  ],
}

const handleRegister = async () => {
  loading.value = true
  try {
    const { data } = await authApi.register({
      username: form.username,
      password: form.password,
    })
    localStorage.setItem('token', data.token)
    localStorage.setItem('username', data.username)
    message.success('注册成功')
    router.push('/')
  } catch (err) {
    const msg = err.response?.data?.error || '注册失败'
    message.error(msg)
  } finally {
    loading.value = false
  }
}
</script>
