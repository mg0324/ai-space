<template>
  <div>
    <div style="display: flex; justify-content: flex-end; margin-bottom: 16px">
      <a-button type="primary" @click="showCreateModal = true">新建标签</a-button>
    </div>

    <a-table :columns="columns" :dataSource="tags" :rowKey="'id'" :pagination="false">
      <template #bodyCell="{ column, record }">
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
      <a-input v-model:value="newTagName" placeholder="标签名" />
    </a-modal>

    <a-modal v-model:open="showRenameModal" title="重命名标签" @ok="renameTag">
      <a-input v-model:value="renameName" placeholder="新名称" />
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
const renameId = ref(null)
const renameName = ref('')

const columns = [
  { title: '名称', dataIndex: 'name', key: 'name' },
  { title: '卡片数', dataIndex: 'count', key: 'count' },
  { title: '操作', key: 'actions', width: 180 },
]

async function loadTags() {
  const { data } = await tagApi.list()
  tags.value = data
}

async function createTag() {
  if (!newTagName.value.trim()) return
  await tagApi.create({ name: newTagName.value.trim() })
  message.success('创建成功')
  newTagName.value = ''
  showCreateModal.value = false
  loadTags()
}

function startRename(record) {
  renameId.value = record.id
  renameName.value = record.name
  showRenameModal.value = true
}

async function renameTag() {
  await tagApi.rename(renameId.value, { name: renameName.value.trim() })
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
