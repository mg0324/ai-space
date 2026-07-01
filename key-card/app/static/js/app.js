const { createApp, ref, reactive, computed, onMounted, watch, toRaw } = Vue;
const { ElMessage, ElMessageBox } = ElementPlus;

const API_BASE = '/api';

const api = {
  async get(url, params) {
    const r = await axios.get(API_BASE + url, { params });
    return r.data;
  },
  async post(url, data) {
    const r = await axios.post(API_BASE + url, data);
    return r.data;
  },
  async put(url, data) {
    const r = await axios.put(API_BASE + url, data);
    return r.data;
  },
  async del(url) {
    const r = await axios.delete(API_BASE + url);
    return r.data;
  }
};

// ====== CardList ======
const CardList = {
  template: '#card-list-tpl',
  setup() {
    const cards = ref([]);
    const tags = ref([]);
    const search = ref('');
    const tagFilter = ref('');
    const loading = ref(false);
    const router = VueRouter.useRouter();

    async function load() {
      loading.value = true;
      try {
        const params = {};
        if (search.value) params.q = search.value;
        if (tagFilter.value) params.tag = tagFilter.value;
        cards.value = await api.get('/cards', params);
        tags.value = await api.get('/tags');
      } finally {
        loading.value = false;
      }
    }

    async function removeCard(id, title) {
      try {
        await ElMessageBox.confirm('确认删除「' + title + '」？', '提示', { type: 'warning' });
        await api.del('/cards/' + id);
        ElMessage.success('已删除');
        load();
      } catch (e) { if (e !== 'cancel') console.error(e); }
    }

    function editCard(id) { router.push('/cards/' + id + '/edit'); }
    function viewCard(id) { router.push('/cards/' + id); }

    async function handleImport() {
      const input = document.createElement('input');
      input.type = 'file';
      input.accept = '.json';
      input.onchange = async (e) => {
        const file = e.target.files[0];
        if (!file) return;
        try {
          const text = await file.text();
          const data = JSON.parse(text);
          await api.post('/cards/import', data);
          ElMessage.success('导入成功');
          load();
        } catch (err) {
          ElMessage.error('导入失败: ' + err.message);
        }
      };
      input.click();
    }

    async function handleExport() {
      const data = await api.get('/cards/export');
      const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url; a.download = 'cards-export.json';
      a.click(); URL.revokeObjectURL(url);
    }

    onMounted(load);

    return { cards, tags, search, tagFilter, loading, load, removeCard, editCard, viewCard, handleImport, handleExport };
  }
};

// ====== CardForm ======
const CardForm = {
  template: '#card-form-tpl',
  setup() {
    const router = VueRouter.useRouter();
    const route = VueRouter.useRoute();
    const isEdit = computed(() => !!route.params.id);
    const form = reactive({ title: '', content: '', source: '', tag_ids: [] });
    const allTags = ref([]);
    const loading = ref(false);
    const submitting = ref(false);

    onMounted(async () => {
      allTags.value = await api.get('/tags');
      if (isEdit.value) {
        const card = await api.get('/cards/' + route.params.id);
        form.title = card.title;
        form.content = card.content;
        form.source = card.source || '';
        form.tag_ids = card.tags.map(t => {
          const found = allTags.value.find(at => at.name === t);
          return found ? found.id : null;
        }).filter(Boolean);
      }
    });

    async function submit() {
      if (!form.title.trim() || !form.content.trim()) {
        ElMessage.warning('标题和内容不能为空');
        return;
      }
      submitting.value = true;
      try {
        const payload = { title: form.title.trim(), content: form.content.trim(), source: form.source.trim(), tag_ids: form.tag_ids };
        if (isEdit.value) {
          await api.put('/cards/' + route.params.id, payload);
          ElMessage.success('已更新');
        } else {
          await api.post('/cards', payload);
          ElMessage.success('已创建');
        }
        router.push('/cards');
      } catch (e) {
        ElMessage.error(e.response?.data?.error || '操作失败');
      } finally {
        submitting.value = false;
      }
    }

    function cancel() { router.push('/cards'); }

    return { form, allTags, isEdit, loading, submitting, submit, cancel };
  }
};

// ====== CardDetail ======
const CardDetail = {
  template: '#card-detail-tpl',
  setup() {
    const router = VueRouter.useRouter();
    const route = VueRouter.useRoute();
    const card = ref(null);
    const loading = ref(false);

    onMounted(async () => {
      loading.value = true;
      try {
        card.value = await api.get('/cards/' + route.params.id);
      } finally {
        loading.value = false;
      }
    });

    function goBack() { router.push('/cards'); }
    function goEdit() { router.push('/cards/' + route.params.id + '/edit'); }

    return { card, loading, goBack, goEdit };
  }
};

// ====== TagManager ======
const TagManager = {
  template: '#tag-manager-tpl',
  setup() {
    const tags = ref([]);
    const loading = ref(false);

    async function load() {
      loading.value = true;
      try { tags.value = await api.get('/tags'); } finally { loading.value = false; }
    }

    async function addTag() {
      const { value } = await ElMessageBox.prompt('输入标签名：', '新建标签', { inputValidator: v => !!v.trim(), inputErrorMessage: '不能为空' });
      if (value) {
        try {
          await api.post('/tags', { name: value.trim() });
          ElMessage.success('已创建');
          load();
        } catch (e) { ElMessage.error(e.response?.data?.error || '创建失败'); }
      }
    }

    async function renameTag(tag) {
      const { value } = await ElMessageBox.prompt('重命名标签：', '重命名', { inputValue: tag.name, inputValidator: v => !!v.trim(), inputErrorMessage: '不能为空' });
      if (value && value.trim() !== tag.name) {
        try {
          await api.put('/tags/' + tag.id, { name: value.trim() });
          ElMessage.success('已重命名');
          load();
        } catch (e) { ElMessage.error(e.response?.data?.error || '重命名失败'); }
      }
    }

    async function removeTag(tag) {
      try {
        await ElMessageBox.confirm('确认删除标签「' + tag.name + '」？将从所有卡片中移除。', '提示', { type: 'warning' });
        await api.del('/tags/' + tag.id);
        ElMessage.success('已删除');
        load();
      } catch (e) { if (e !== 'cancel') console.error(e); }
    }

    onMounted(load);

    return { tags, loading, addTag, renameTag, removeTag };
  }
};

// ====== TemplateList ======
const TemplateList = {
  template: '#template-list-tpl',
  setup() {
    const templates = ref([]);
    const loading = ref(false);

    onMounted(async () => {
      loading.value = true;
      try { templates.value = await api.get('/templates'); } finally { loading.value = false; }
    });

    return { templates, loading };
  }
};

// ====== GeneratePage ======
const GeneratePage = {
  template: '#generate-page-tpl',
  setup() {
    const cards = ref([]);
    const templates = ref([]);
    const tags = ref([]);
    const selectedCardIds = ref([]);
    const selectedTemplate = ref('');
    const previewHtml = ref('');
    const showPreview = ref(false);
    const filterText = ref('');
    const filterTags = ref([]);
    const loading = ref(false);
    const generating = ref(false);

    const filteredCards = computed(() => {
      let list = cards.value;
      if (filterText.value) {
        const q = filterText.value.toLowerCase();
        list = list.filter(c => c.title.toLowerCase().includes(q));
      }
      if (filterTags.value.length) {
        list = list.filter(c => filterTags.value.some(t => (c.tags || []).includes(t)));
      }
      return list;
    });

    function handleCardCheck(cardId) {
      const idx = selectedCardIds.value.indexOf(cardId);
      if (idx >= 0) selectedCardIds.value.splice(idx, 1);
      else selectedCardIds.value.push(cardId);
    }

    function isSelected(cardId) { return selectedCardIds.value.includes(cardId); }

    async function preview() {
      if (!selectedCardIds.value.length || !selectedTemplate.value) {
        ElMessage.warning('请至少选择一张卡片和一个模板');
        return;
      }
      generating.value = true;
      try {
        const result = await api.post('/generate/preview', { card_ids: selectedCardIds.value, template: selectedTemplate.value });
        previewHtml.value = result.html;
        showPreview.value = true;
      } catch (e) {
        ElMessage.error('生成失败');
      } finally {
        generating.value = false;
      }
    }

    async function exportSite() {
      if (!selectedCardIds.value.length || !selectedTemplate.value) {
        ElMessage.warning('请至少选择一张卡片和一个模板');
        return;
      }
      generating.value = true;
      try {
        const result = await api.post('/generate/export', { card_ids: selectedCardIds.value, template: selectedTemplate.value });
        ElMessage.success(result.message || '导出成功');
      } catch (e) {
        ElMessage.error('导出失败');
      } finally {
        generating.value = false;
      }
    }

    onMounted(async () => {
      loading.value = true;
      try {
        cards.value = await api.get('/cards');
        templates.value = await api.get('/templates');
        if (templates.value.length) selectedTemplate.value = templates.value[0].name;
        const allTags = new Set();
        cards.value.forEach(c => (c.tags || []).forEach(t => allTags.add(t)));
        tags.value = [...allTags].sort();
      } finally {
        loading.value = false;
      }
    });

    return { cards, templates, tags, selectedCardIds, selectedTemplate, previewHtml, showPreview, filterText, filterTags, filteredCards, loading, generating, handleCardCheck, isSelected, preview, exportSite };
  }
};

// ====== Routes ======
const routes = [
  { path: '/', redirect: '/cards' },
  { path: '/cards', component: CardList },
  { path: '/cards/new', component: CardForm },
  { path: '/cards/:id', component: CardDetail },
  { path: '/cards/:id/edit', component: CardForm },
  { path: '/tags', component: TagManager },
  { path: '/templates', component: TemplateList },
  { path: '/generate', component: GeneratePage },
];

const router = VueRouter.createRouter({
  history: VueRouter.createWebHashHistory(),
  routes,
});

// ====== App ======
const AppRoot = {
  computed: {
    currentRoute() {
      return this.$route.path;
    }
  }
};
const app = createApp(AppRoot);
app.config.globalProperties.STATIC_URL = '/static/';
app.config.globalProperties.formatDate = function(v) {
  if (!v) return '';
  var d = new Date(v);
  if (isNaN(d.getTime())) return v;
  var pad = function(n) { return n < 10 ? '0' + n : n; };
  return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) + ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes()) + ':' + pad(d.getSeconds());
};
app.use(ElementPlus);
app.use(router);
app.mount('#app');
