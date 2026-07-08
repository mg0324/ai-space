<template>
  <div>
    <a-row :gutter="24">
      <a-col :span="10">
        <a-card title="选择卡片" style="margin-bottom: 16px">
          <a-space style="margin-bottom: 12px">
            <a-select
              v-model:value="filterTag"
              placeholder="按标签筛选"
              allowClear
              style="width: 150px"
              @change="filterByTag"
            >
              <a-select-option v-for="tag in allTags" :key="tag.name" :value="tag.name">
                <span v-if="tag.color" :style="{ display: 'inline-block', width: '8px', height: '8px', borderRadius: '50%', backgroundColor: tag.color, marginRight: '6px' }" />
                {{ tag.name }}
              </a-select-option>
            </a-select>
            <a-button @click="toggleSelectAll">全选/取消</a-button>
          </a-space>
          <a-checkbox-group v-model:value="selectedCardIds" style="width: 100%">
            <div v-for="card in filteredCards" :key="card.id" style="margin-bottom: 8px">
              <a-checkbox :value="card.id">{{ card.title }}</a-checkbox>
              <a-tag v-for="t in card.tags" :key="t.name" :color="t.color || 'blue'" style="margin-left: 4px">{{ t.name }}</a-tag>
            </div>
          </a-checkbox-group>
        </a-card>

        <a-card title="选择模板" style="margin-bottom: 16px">
          <a-radio-group v-model:value="selectedTemplate">
            <a-radio v-for="tpl in templates" :key="tpl.name" :value="tpl.name" style="display: block; margin-bottom: 8px">
              {{ tpl.label }} <span style="color: #999">— {{ tpl.description }}</span>
            </a-radio>
          </a-radio-group>
        </a-card>

        <a-space>
          <a-button type="primary" @click="doPreview" :disabled="!canGenerate">预览</a-button>
          <a-button @click="doExport" :disabled="!canGenerate">导出</a-button>
        </a-space>
      </a-col>

      <a-col :span="14">
        <a-card title="预览" :style="previewHtml ? {} : { minHeight: '500px' }">
          <iframe
            v-if="previewHtml"
            :srcdoc="previewHtml"
            style="width: 100%; height: 600px; border: 1px solid #e0e0e0; border-radius: 4px"
          />
          <a-empty v-else description="选择卡片和模板后点击预览" />
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { cardApi, tagApi, templateApi, generateApi } from '../api'
import { message } from 'ant-design-vue'

const cards = ref([])
const allTags = ref([])
const templates = ref([])
const selectedCardIds = ref([])
const selectedTemplate = ref(undefined)
const filterTag = ref(undefined)
const previewHtml = ref('')

const filteredCards = computed(() => {
  if (!filterTag.value) return cards.value
  return cards.value.filter(c => c.tags.some(t => t.name === filterTag.value))
})

const canGenerate = computed(() => selectedCardIds.value.length > 0 && selectedTemplate.value)

async function loadData() {
  const [cardsRes, tagsRes, tplRes] = await Promise.all([
    cardApi.list(),
    tagApi.list(),
    templateApi.list(),
  ])
  cards.value = cardsRes.data
  const tagMap = new Map()
  cards.value.flatMap(c => c.tags).forEach(t => {
    if (!tagMap.has(t.name)) tagMap.set(t.name, t)
  })
  allTags.value = [...tagMap.values()].sort((a, b) => a.name.localeCompare(b.name))
  templates.value = tplRes.data
  if (templates.value.length > 0 && !selectedTemplate.value) {
    selectedTemplate.value = templates.value[0].name
  }
}

function filterByTag() {}

function toggleSelectAll() {
  if (selectedCardIds.value.length === filteredCards.value.length) {
    selectedCardIds.value = []
  } else {
    selectedCardIds.value = filteredCards.value.map(c => c.id)
  }
}

async function doPreview() {
  const { data } = await generateApi.preview({
    cardIds: selectedCardIds.value,
    template: selectedTemplate.value,
  })
  if (data.html) {
    previewHtml.value = data.html
  } else if (data.error) {
    message.error(data.error)
  }
}

async function doExport() {
  const { data } = await generateApi.export({
    cardIds: selectedCardIds.value,
    template: selectedTemplate.value,
  })
  if (data.message) {
    const pathInfo = data.path ? `\n保存路径：${data.path}` : ''
    message.success(data.message + pathInfo, 5)
  } else if (data.error) {
    message.error(data.error)
  }
}

onMounted(loadData)
</script>
