<template>
  <div>
    <a-row :gutter="16" style="margin-bottom: 16px">
      <a-col :span="6">
        <a-card>
          <a-statistic title="卡片总数" :value="cards.length">
            <template #prefix><FileTextOutlined /></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic title="标签总数" :value="tags.length">
            <template #prefix><TagsOutlined /></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic title="有来源" :value="cardsWithSource">
            <template #prefix><LinkOutlined /></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic title="有标签" :value="cardsWithTags">
            <template #prefix><TagOutlined /></template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <a-space>
        <a-input-search
          v-model:value="searchQuery"
          placeholder="搜索卡片"
          style="width: 200px"
          @search="loadCards"
        />
        <a-select
          v-model:value="filterTag"
          placeholder="按标签筛选"
          allowClear
          style="width: 150px"
          @change="loadCards"
        >
          <a-select-option v-for="tag in tags" :key="tag.name" :value="tag.name">
            {{ tag.name }}
          </a-select-option>
        </a-select>
      </a-space>
      <a-space>
        <a-button @click="handleImport">导入</a-button>
        <a-button @click="handleExport">导出</a-button>
        <a-button type="primary" @click="$router.push('/cards/new')">新建卡片</a-button>
      </a-space>
    </div>

    <a-table
      :columns="columns"
      :dataSource="cards"
      :rowKey="'id'"
      :pagination="pagination"
      :scroll="{ x: 800, y: 'calc(100vh - 360px)' }""
      size="middle"
      bordered
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'tags'">
          <a-tag v-for="tag in record.tags" :key="tag" color="blue">{{ tag }}</a-tag>
        </template>
        <template v-if="column.key === 'actions'">
          <a-space size="small">
            <a-button size="small" type="link" @click="viewCard(record)">查看</a-button>
            <a-button size="small" type="link" @click="$router.push(`/cards/${record.id}/edit`)">编辑</a-button>
            <a-popconfirm title="确定删除？" @confirm="deleteCard(record.id)">
              <a-button size="small" type="link" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <input
      ref="fileInput"
      type="file"
      accept=".json"
      style="display: none"
      @change="onFileChange"
    />

    <a-modal v-model:open="viewModalOpen" :footer="null" width="680px" :bodyStyle="{ padding: 0 }">
      <template #title>
        <div style="font-size: 20px; font-weight: 600">{{ viewCardData?.title }}</div>
      </template>
      <div style="padding: 0 24px 24px">
        <div style="display: flex; align-items: center; gap: 16px; color: #999; font-size: 13px; margin-bottom: 20px; padding-bottom: 16px; border-bottom: 1px solid #f0f0f0">
          <span v-if="viewCardData?.source"><LinkOutlined style="margin-right: 4px" />{{ viewCardData.source }}</span>
          <span><ClockCircleOutlined style="margin-right: 4px" />{{ viewCardData?.updatedAt ? viewCardData.updatedAt.replace('T', ' ').substring(0, 19) : '' }}</span>
        </div>
        <div v-if="viewCardData?.tags?.length" style="margin-bottom: 16px">
          <a-tag v-for="tag in viewCardData.tags" :key="tag" color="blue">{{ tag }}</a-tag>
        </div>
        <div style="font-size: 15px; line-height: 1.8; color: #333; white-space: pre-wrap; max-height: 500px; overflow-y: auto">{{ viewCardData?.content }}</div>
      </div>
      <div style="text-align: right; padding: 12px 24px; border-top: 1px solid #f0f0f0">
        <a-button @click="viewModalOpen = false">关闭</a-button>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { cardApi, tagApi } from '../api'
import { message } from 'ant-design-vue'
import { FileTextOutlined, TagsOutlined, TagOutlined, LinkOutlined, ClockCircleOutlined } from '@ant-design/icons-vue'

const cards = ref([])
const tags = ref([])
const searchQuery = ref('')
const filterTag = ref(undefined)
const fileInput = ref(null)
const viewModalOpen = ref(false)
const viewCardData = ref(null)

const cardsWithSource = computed(() => cards.value.filter(c => c.source && c.source.trim()).length)
const cardsWithTags = computed(() => cards.value.filter(c => c.tags && c.tags.length > 0).length)

const pagination = ref({
  current: 1,
  pageSize: 10,
  showSizeChanger: true,
  pageSizeOptions: ['10', '20', '50'],
  showTotal: (total) => `共 ${total} 条`,
  onChange: (page) => { pagination.value.current = page },
  onShowSizeChange: (current, size) => { pagination.value.current = 1; pagination.value.pageSize = size },
})

function viewCard(record) {
  viewCardData.value = record
  viewModalOpen.value = true
}

const columns = [
  { title: '标题', dataIndex: 'title', key: 'title', ellipsis: true, sorter: (a, b) => a.title.localeCompare(b.title) },
  { title: '来源', dataIndex: 'source', key: 'source', ellipsis: true, width: 280, sorter: (a, b) => (a.source || '').localeCompare(b.source || '') },
  { title: '标签', key: 'tags', width: 150 },
  { title: '更新时间', dataIndex: 'updatedAt', key: 'updatedAt', width: 170, sorter: (a, b) => (a.updatedAt || '').localeCompare(b.updatedAt || ''), customRender: ({ text }) => text ? text.replace('T', ' ').substring(0, 19) : '' },
  { title: '操作', key: 'actions', width: 180, fixed: 'right' },
]

async function loadCards() {
  const params = {}
  if (searchQuery.value) params.q = searchQuery.value
  if (filterTag.value) params.tag = filterTag.value
  const { data } = await cardApi.list(params)
  cards.value = data
}

async function loadTags() {
  const { data } = await tagApi.list()
  tags.value = data
}

async function deleteCard(id) {
  await cardApi.delete(id)
  message.success('删除成功')
  loadCards()
}

function handleExport() {
  cardApi.exportAll().then(({ data }) => {
    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'cards.json'
    a.click()
    URL.revokeObjectURL(url)
  })
}

function handleImport() {
  fileInput.value.click()
}

async function onFileChange(e) {
  const file = e.target.files[0]
  if (!file) return
  const text = await file.text()
  try {
    const data = JSON.parse(text)
    const { data: result } = await cardApi.importCards(data)
    message.success(`导入成功，共 ${result.count} 张卡片`)
    loadCards()
  } catch {
    message.error('导入失败，请检查 JSON 格式')
  }
  e.target.value = ''
}

onMounted(() => {
  loadCards()
  loadTags()
  const setTableOverflow = () => {
    const body = document.querySelector('.ant-table-body')
    if (body) body.style.overflowY = 'auto'
  }
  setTimeout(setTableOverflow, 300)
  const observer = new MutationObserver(setTableOverflow)
  observer.observe(document.body, { childList: true, subtree: true })
})
</script>
