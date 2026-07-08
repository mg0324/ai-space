<template>
  <div>
    <div style="display: flex; justify-content: flex-end; margin-bottom: 16px">
      <a-button type="primary" @click="openCreate">新建标签</a-button>
    </div>

    <a-table :columns="columns" :dataSource="tags" :rowKey="'id'" :pagination="false">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'color'">
          <span
            v-if="record.color"
            :style="{
              display: 'inline-block',
              width: '24px',
              height: '24px',
              borderRadius: '4px',
              backgroundColor: record.color,
            }"
          />
          <span v-else style="color: #999">-</span>
        </template>
        <template v-if="column.key === 'actions'">
          <a-space>
            <a-button size="small" @click="startRename(record)">重命名</a-button>
            <a-popconfirm title="确定删除？" @confirm="deleteTag(record.id)">
              <a-button size="small" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <a-modal v-model:open="showCreateModal" title="新建标签" @ok="createTag">
      <a-input v-model:value="newTagName" placeholder="标签名" style="margin-bottom: 12px" />
      <div style="display: flex; align-items: center; gap: 8px">
        <span>背景色：</span>
        <input
          type="color"
          v-model="newTagColor"
          style="width: 40px; height: 32px; border: 1px solid #d9d9d9; border-radius: 4px; cursor: pointer; padding: 2px"
        />
        <a-button size="small" @click="newTagColor = ''">清除</a-button>
      </div>
    </a-modal>

    <a-modal v-model:open="showRenameModal" title="重命名标签" @ok="renameTag">
      <a-input v-model:value="renameName" placeholder="新名称" style="margin-bottom: 12px" />
      <div style="display: flex; align-items: center; gap: 8px">
        <span>背景色：</span>
        <input
          type="color"
          v-model="renameColor"
          style="width: 40px; height: 32px; border: 1px solid #d9d9d9; border-radius: 4px; cursor: pointer; padding: 2px"
        />
        <a-button size="small" @click="renameColor = ''">清除</a-button>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { tagApi } from '../api'
import { message } from 'ant-design-vue'

const tags = ref([])
const showCreateModal = ref(false)
const showRenameModal = ref(false)
const newTagName = ref('')
const newTagColor = ref('')
const renameId = ref(null)
const renameName = ref('')
const renameColor = ref('')

const columns = [
  { title: '名称', dataIndex: 'name', key: 'name' },
  { title: '背景色', key: 'color', width: 100 },
  { title: '卡片数', dataIndex: 'count', key: 'count' },
  { title: '操作', key: 'actions', width: 180 },
]

async function loadTags() {
  const { data } = await tagApi.list()
  tags.value = data
}

function openCreate() {
  newTagName.value = ''
  newTagColor.value = ''
  showCreateModal.value = true
}

async function createTag() {
  if (!newTagName.value.trim()) return
  await tagApi.create({ name: newTagName.value.trim(), color: newTagColor.value || null })
  message.success('创建成功')
  showCreateModal.value = false
  loadTags()
}

function startRename(record) {
  renameId.value = record.id
  renameName.value = record.name
  renameColor.value = record.color || ''
  showRenameModal.value = true
}

async function renameTag() {
  await tagApi.rename(renameId.value, { name: renameName.value.trim(), color: renameColor.value || null })
  message.success('重命名成功')
  showRenameModal.value = false
  loadTags()
}

async function deleteTag(id) {
  await tagApi.delete(id)
  message.success('删除成功')
  loadTags()
}

onMounted(loadTags)
</script>
