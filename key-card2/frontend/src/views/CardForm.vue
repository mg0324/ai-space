<template>
  <div style="max-width: 700px">
    <h2 style="margin-bottom: 24px">{{ isEdit ? '编辑卡片' : '新建卡片' }}</h2>
    <a-form :model="form" layout="vertical" @finish="onSubmit">
      <a-form-item label="标题" name="title" :rules="[{ required: true, message: '请输入标题' }]">
        <a-input v-model:value="form.title" />
      </a-form-item>
      <a-form-item label="内容" name="content" :rules="[{ required: true, message: '请输入内容' }]">
        <a-textarea v-model:value="form.content" :rows="6" />
      </a-form-item>
      <a-form-item label="来源">
        <a-input v-model:value="form.source" />
      </a-form-item>
      <a-form-item label="标签">
        <a-select
          v-model:value="form.tagIds"
          mode="multiple"
          placeholder="选择标签"
        >
          <a-select-option v-for="tag in tags" :key="tag.id" :value="tag.id">
            {{ tag.name }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item>
        <a-space>
          <a-button type="primary" html-type="submit">保存</a-button>
          <a-button @click="$router.push('/')">取消</a-button>
        </a-space>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { cardApi, tagApi } from '../api'
import { message } from 'ant-design-vue'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)
const tags = ref([])

const form = ref({
  title: '',
  content: '',
  source: '',
  tagIds: [],
})

async function loadTags() {
  const { data } = await tagApi.list()
  tags.value = data
}

async function loadCard(id) {
  const { data } = await cardApi.get(id)
  form.value = {
    title: data.title,
    content: data.content,
    source: data.source || '',
    tagIds: [],
  }
}

async function onSubmit() {
  if (isEdit.value) {
    await cardApi.update(route.params.id, form.value)
    message.success('更新成功')
  } else {
    await cardApi.create(form.value)
    message.success('创建成功')
  }
  router.push('/')
}

onMounted(() => {
  loadTags()
  if (isEdit.value) loadCard(route.params.id)
})
</script>
