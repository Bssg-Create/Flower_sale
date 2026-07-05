<template>
  <div class="diy-page">
    <div class="page-header">
      <div>
        <p class="eyebrow">DIY BOUQUET</p>
        <h2>设计一束只属于你的花</h2>
      </div>
      <button class="ghost-btn" type="button" @click="floristArrange" :disabled="designItems.length === 0">一键整理</button>
    </div>

    <div class="diy-layout">
      <aside class="side-panel flower-panel">
        <div class="panel-title">
          <h3>花材架</h3>
          <span>{{ filteredFlowers.length }} 种可选</span>
        </div>

        <div class="category-filter">
          <button
            v-for="cat in categories"
            :key="cat.id"
            type="button"
            :class="{ active: selectedCategory === cat.id }"
            @click="selectedCategory = cat.id"
          >
            {{ cat.icon }} {{ cat.name }}
          </button>
        </div>

        <div class="flowers-list" v-if="!flowersLoading">
          <div
            v-for="flower in filteredFlowers"
            :key="flower.id"
            class="flower-item"
            draggable="true"
            @dragstart="handleDragStart($event, flower)"
            @dragend="draggedFlower = null"
          >
            <div class="flower-thumb-wrap">
              <img :src="getFlowerImage(flower)" :alt="flower.name" class="flower-thumb" />
            </div>
            <div class="flower-info">
              <h4>{{ flower.name }}</h4>
              <p>{{ flower.categoryName || '花材' }} / ¥{{ Number(flower.price || 0).toFixed(2) }}</p>
              <div class="flower-tags">
                <span v-for="tag in getFlowerTags(flower.name)" :key="tag">{{ tag }}</span>
              </div>
            </div>
            <button class="add-btn" type="button" @click.stop="addToDesign(flower)">+</button>
          </div>
        </div>
        <div v-else class="loading-box">加载花卉中...</div>
      </aside>

      <main class="workspace-panel">
        <section class="template-strip">
          <button
            v-for="template in templates"
            :key="template.id"
            type="button"
            :class="['template-card', { active: activeTemplate === template.id }]"
            @click="loadTemplate(template.id)"
          >
            <strong>{{ template.name }}</strong>
            <span>{{ template.note }}</span>
          </button>
        </section>

        <section class="canvas-card">
          <div class="design-header">
            <div>
              <h3>花束工作台</h3>
              <p>{{ designItems.length ? '拖动花材调整层次，靠近束口会轻轻吸附' : '从左侧加入花材，或直接使用灵感模板' }}</p>
            </div>
            <div class="package-select">
              <span>包装</span>
              <select v-model="selectedPackage">
                <option v-for="pkg in packages" :key="pkg.id" :value="pkg.id">
                  {{ pkg.name }} / ¥{{ Number(pkg.price || 0).toFixed(2) }}
                </option>
              </select>
            </div>
          </div>

          <BouquetCanvas
            v-model="designItems"
            v-model:selected-index="selectedIndex"
            :package-type="currentPackage.name"
            :message="messageText"
            :drag-enabled="!!draggedFlower"
            @drop-flower="handleCanvasDrop"
          />
        </section>
      </main>

      <aside class="side-panel summary-panel">
        <div class="panel-title">
          <h3>花束清单</h3>
          <span>{{ totalFlowers }} 支</span>
        </div>

        <div class="recipe-list" v-if="recipeRows.length">
          <div v-for="row in recipeRows" :key="row.name" class="recipe-item">
            <strong>{{ row.name }}</strong>
            <span>{{ row.count }} 支 / ¥{{ row.total.toFixed(2) }}</span>
          </div>
        </div>
        <div v-else class="empty-state">还没有加入花材。</div>

        <label class="card-input">
          <span>祝福卡片</span>
          <textarea v-model="messageText" rows="3" maxlength="36" placeholder="愿今日有花，也有好心情"></textarea>
        </label>

        <div class="mood-box">
          <span>花语氛围</span>
          <div>
            <em v-for="tag in moodTags" :key="tag">{{ tag }}</em>
            <em v-if="moodTags.length === 0">待选择</em>
          </div>
        </div>

        <div class="summary-card">
          <div class="summary-row">
            <span>花材费用</span>
            <strong>¥{{ flowersTotal.toFixed(2) }}</strong>
          </div>
          <div class="summary-row">
            <span>包装费用</span>
            <strong>¥{{ Number(currentPackage.price || 0).toFixed(2) }}</strong>
          </div>
          <div class="summary-row total">
            <span>总计</span>
            <strong>¥{{ totalPrice.toFixed(2) }}</strong>
          </div>
        </div>

        <button class="save-btn" @click="saveDesign" :disabled="saving || designItems.length === 0">
          {{ saving ? '保存中...' : '保存花束设计' }}
        </button>

        <div v-if="savedId" class="order-section">
          <p class="save-success">花束已保存成功！</p>
          <router-link to="/user/plans" class="view-plans-link">查看我的方案</router-link>
          <div class="order-form">
            <input v-model="orderForm.receiverName" placeholder="收货人姓名" class="order-input" />
            <input v-model="orderForm.receiverPhone" placeholder="收货人电话" class="order-input" />
            <input v-model="orderForm.shippingAddress" placeholder="收货地址" class="order-input" />
            <button class="order-btn" @click="placeOrder" :disabled="ordering">
              {{ ordering ? '下单中...' : '立即下单' }}
            </button>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import BouquetCanvas from './BouquetCanvas.vue'
import api from '../api/index.js'

const flowers = ref([])
const packages = ref([])
const flowersLoading = ref(true)
const designItems = ref([])
const draggedFlower = ref(null)
const selectedIndex = ref(null)
const selectedCategory = ref(0)
const selectedPackage = ref(null)
const saving = ref(false)
const savedId = ref(null)
const ordering = ref(false)
const activeTemplate = ref(null)
const messageText = ref('愿今日有花，也有好心情')

const orderForm = ref({
  receiverName: '',
  receiverPhone: '',
  shippingAddress: ''
})

const categories = ref([
  { id: 0, name: '全部', icon: '🌸' },
  { id: 1, name: '玫瑰', icon: '🌹' },
  { id: 2, name: '百合', icon: '💮' },
  { id: 3, name: '郁金香', icon: '🌷' },
  { id: 4, name: '向日葵', icon: '🌻' },
  { id: 5, name: '康乃馨', icon: '🌺' },
  { id: 6, name: '配叶', icon: '🌿' }
])

const flowerProfiles = [
  { key: '红玫瑰', asset: 'red-rose.webp', photoWidth: 126, photoHeight: 226, tags: ['热恋', '告白'] },
  { key: '白玫瑰', asset: 'white-rose.webp', photoWidth: 126, photoHeight: 226, tags: ['纯净', '纪念'] },
  { key: '粉玫瑰', asset: 'pink-rose.webp', photoWidth: 126, photoHeight: 226, tags: ['温柔', '心意'] },
  { key: '黄玫瑰', asset: 'yellow-rose.webp', photoWidth: 128, photoHeight: 236, tags: ['心意', '祝福'] },
  { key: '红郁金香', asset: 'red-tulip.webp', photoWidth: 118, photoHeight: 236, tags: ['热烈', '告白'] },
  { key: '粉郁金香', asset: 'pink-tulip.webp', photoWidth: 120, photoHeight: 226, tags: ['浪漫', '春日'] },
  { key: '黄郁金香', asset: 'yellow-tulip.webp', photoWidth: 122, photoHeight: 236, tags: ['祝福', '活力'] },
  { key: '白百合', asset: 'white-lily.webp', photoWidth: 132, photoHeight: 228, tags: ['高雅', '慰问'] },
  { key: '粉百合', asset: 'pink-lily.webp', photoWidth: 154, photoHeight: 238, tags: ['甜美', '生日'] },
  { key: '向日葵', asset: 'sunflower.webp', photoWidth: 136, photoHeight: 226, tags: ['阳光', '鼓励'] },
  { key: '康乃馨', asset: 'pink-carnation.webp', photoWidth: 118, photoHeight: 222, tags: ['感恩', '亲情'] },
  { key: '小雏菊', asset: 'white-daisy.webp', photoWidth: 128, photoHeight: 226, tags: ['清新', '陪伴'] },
  { key: '尤加利叶', asset: 'eucalyptus.webp', photoWidth: 132, photoHeight: 220, tags: ['自然', '森系'] }
]

const templates = [
  {
    id: 'confession',
    name: '告白红玫瑰',
    note: '红玫瑰为主，适合表白和纪念日',
    packageName: '心形包装',
    message: '想把今天的浪漫，都交给你',
    items: [['尤加利叶', 158, 92, -34, 1.12, 10], ['尤加利叶', 318, 94, 32, 1.1, 11], ['白玫瑰', 206, 120, -15, 0.98, 18], ['红玫瑰', 238, 104, -3, 1.14, 26], ['红玫瑰', 276, 128, 13, 1.08, 27], ['粉玫瑰', 198, 162, -22, 0.96, 23], ['粉郁金香', 316, 166, 20, 0.94, 21], ['小雏菊', 170, 198, -28, 0.82, 19], ['小雏菊', 344, 196, 27, 0.82, 20]]
  },
  {
    id: 'forest',
    name: '森系自然风',
    note: '叶材拉开轮廓，适合清新日常礼',
    packageName: '长形包装',
    message: '愿你被温柔和绿意包围',
    items: [['尤加利叶', 152, 82, -36, 1.18, 10], ['尤加利叶', 326, 84, 34, 1.16, 11], ['尤加利叶', 236, 72, 0, 1.04, 12], ['白百合', 204, 124, -13, 1.02, 22], ['小雏菊', 266, 120, 9, 0.92, 21], ['白玫瑰', 238, 154, -3, 1.04, 26], ['黄郁金香', 308, 164, 21, 0.92, 24], ['小雏菊', 178, 198, -24, 0.86, 20]]
  },
  {
    id: 'birthday',
    name: '生日向日葵',
    note: '明亮主花，适合生日和鼓励',
    packageName: '圆形包装',
    message: '生日快乐，愿你一路向阳',
    items: [['尤加利叶', 176, 104, -26, 1.04, 10], ['尤加利叶', 316, 106, 27, 1.04, 11], ['向日葵', 216, 100, -10, 1.12, 26], ['向日葵', 278, 112, 12, 1.08, 27], ['黄郁金香', 190, 164, -20, 0.96, 22], ['粉百合', 314, 164, 20, 0.94, 21], ['康乃馨', 240, 170, 0, 0.98, 25], ['小雏菊', 166, 200, -28, 0.82, 18], ['小雏菊', 344, 198, 29, 0.82, 19]]
  },
  {
    id: 'gentle',
    name: '温柔粉白',
    note: '粉白低饱和，适合感谢和探望',
    packageName: '礼盒包装',
    message: '把轻轻的祝福送给你',
    items: [['尤加利叶', 174, 98, -28, 1.04, 10], ['尤加利叶', 314, 100, 27, 1.04, 11], ['白百合', 214, 112, -10, 1.04, 23], ['粉百合', 276, 124, 10, 0.98, 24], ['白玫瑰', 238, 150, -3, 1.04, 26], ['粉玫瑰', 198, 166, -20, 0.94, 22], ['粉郁金香', 318, 168, 20, 0.92, 21], ['小雏菊', 168, 200, -29, 0.82, 18], ['小雏菊', 342, 198, 29, 0.82, 19]]
  }
]

const filteredFlowers = computed(() => selectedCategory.value === 0 ? flowers.value : flowers.value.filter(f => f.categoryId === selectedCategory.value))
const currentPackage = computed(() => packages.value.find(p => p.id === selectedPackage.value) || packages.value[0] || { name: '圆形包装', price: 10 })
const totalFlowers = computed(() => designItems.value.reduce((sum, item) => sum + (item.quantity || 1), 0))
const flowersTotal = computed(() => designItems.value.reduce((sum, item) => sum + Number(item.price || 0) * (item.quantity || 1), 0))
const totalPrice = computed(() => flowersTotal.value + Number(currentPackage.value.price || 0))

const recipeRows = computed(() => {
  const rows = new Map()
  designItems.value.forEach(item => {
    const name = item.flowerName || item.name
    const current = rows.get(name) || { name, count: 0, total: 0 }
    current.count += item.quantity || 1
    current.total += Number(item.price || 0) * (item.quantity || 1)
    rows.set(name, current)
  })
  return Array.from(rows.values())
})

const moodTags = computed(() => {
  const counts = new Map()
  designItems.value.forEach(item => getFlowerTags(item.flowerName).forEach(tag => counts.set(tag, (counts.get(tag) || 0) + 1)))
  return Array.from(counts.entries()).sort((a, b) => b[1] - a[1]).slice(0, 4).map(([tag]) => tag)
})

const encodeUrl = (url) => {
  if (!url) return ''
  const lastSlash = url.lastIndexOf('/')
  if (lastSlash >= 0) return url.substring(0, lastSlash + 1) + encodeURIComponent(url.substring(lastSlash + 1))
  return encodeURIComponent(url)
}

const getProfile = (name) => flowerProfiles.find(profile => (name || '').includes(profile.key)) || null
const getFlowerImage = (flower) => {
  const profile = getProfile(flower.name || flower.flowerName)
  return profile?.asset ? `/images/diy/${profile.asset}` : encodeUrl(flower.imageUrl)
}
const getFlowerTags = (name) => getProfile(name)?.tags || ['心意']

const makeDesignItem = (flower, patch = {}) => {
  const index = designItems.value.length
  const profile = getProfile(flower.name)
  return {
    uid: `f${Date.now()}-${index}-${Math.round(Math.random() * 1000)}`,
    id: flower.id,
    flowerId: flower.id,
    flowerName: flower.name,
    name: flower.name,
    imageUrl: flower.imageUrl,
    assetUrl: profile?.asset ? `/images/diy/${profile.asset}` : '',
    photoWidth: profile?.photoWidth || 132,
    photoHeight: profile?.photoHeight || 230,
    price: Number(flower.price || 0),
    categoryName: flower.categoryName,
    quantity: 1,
    x: patch.x ?? 238 + (index % 5 - 2) * 26,
    y: patch.y ?? 134 + Math.floor(index / 5) * 18,
    rotation: patch.rotation ?? [-18, 12, -8, 18, 4][index % 5],
    scale: patch.scale ?? 1,
    z: patch.z ?? 20 + index,
    bend: patch.bend ?? [-4, 3, 7, -8, 2][index % 5],
    tilt: patch.tilt ?? [-6, 4, -3, 7, 2][index % 5]
  }
}

const addToDesign = (flower, patch = {}) => {
  activeTemplate.value = null
  const next = makeDesignItem(flower, patch)
  designItems.value.push(next)
  selectedIndex.value = designItems.value.length - 1
}

const handleDragStart = (event, flower) => {
  draggedFlower.value = flower
  event.dataTransfer.effectAllowed = 'copy'
}

const handleCanvasDrop = (point) => {
  if (!draggedFlower.value) return
  addToDesign(draggedFlower.value, { x: point.x, y: point.y, z: Math.round(point.y) })
  draggedFlower.value = null
}

const findFlower = (name) => flowers.value.find(f => f.name === name || f.name?.includes(name) || name.includes(f.name))

const loadTemplate = (templateId) => {
  const template = templates.find(item => item.id === templateId)
  if (!template) return
  const nextItems = []
  template.items.forEach(row => {
    const flower = findFlower(row[0])
    if (!flower) return
    nextItems.push(makeDesignItem(flower, { x: row[1], y: row[2], rotation: row[3], scale: row[4], z: row[5] }))
  })
  designItems.value = nextItems
  selectedIndex.value = Math.floor(nextItems.length / 2)
  activeTemplate.value = template.id
  messageText.value = template.message
  const pkg = packages.value.find(item => item.name === template.packageName)
  if (pkg) selectedPackage.value = pkg.id
}

const floristArrange = () => {
  const count = designItems.value.length
  if (!count) return
  activeTemplate.value = null
  designItems.value = designItems.value.map((item, index) => {
    const offset = index - (count - 1) / 2
    const fan = count > 1 ? offset / ((count - 1) / 2) : 0
    return {
      ...item,
      x: 238 + fan * 104 + (index % 2 ? 8 : -6),
      y: 104 + Math.abs(fan) * 58 + (index % 3) * 11,
      rotation: Math.round(fan * 30),
      scale: Math.max(0.84, Math.min(item.scale || 1, 1.12)),
      z: Math.round(104 + Math.abs(fan) * 58) + index
    }
  })
}

onMounted(async () => {
  try {
    const [fRes, pRes] = await Promise.all([api.get('/diy/flowers'), api.get('/diy/package/list')])
    flowers.value = fRes.data.data || []
    packages.value = pRes.data.data || []
    if (packages.value.length > 0) selectedPackage.value = packages.value[0].id
  } catch (e) {
    console.error(e)
  } finally {
    flowersLoading.value = false
  }
})

const saveDesign = async () => {
  if (designItems.value.length === 0) return
  saving.value = true
  savedId.value = null
  try {
    const userId = Number(localStorage.getItem('userId'))
    const pkg = currentPackage.value
    const res = await api.post('/diy/save', {
      userId,
      name: '我的花束设计',
      packageType: pkg.name,
      totalPrice: totalPrice.value,
      items: designItems.value.map(item => ({
        flowerId: item.flowerId,
        flowerName: item.flowerName,
        quantity: item.quantity || 1,
        position: JSON.stringify({
          x: Math.round(item.x),
          y: Math.round(item.y),
          rotation: Math.round(item.rotation || 0),
          scale: Number(item.scale || 1),
          z: item.z ?? Math.round(item.y),
          bend: item.bend || 0,
          tilt: item.tilt || 0,
          photoWidth: item.photoWidth,
          photoHeight: item.photoHeight,
          message: messageText.value
        })
      }))
    })
    if (res.data && res.data.data) savedId.value = res.data.data.id
    else alert('保存失败: ' + (res.data?.message || '未知错误'))
  } catch (e) {
    console.error(e)
    alert('保存失败: ' + (e.response?.data?.message || e.message))
  } finally {
    saving.value = false
  }
}

const placeOrder = async () => {
  if (!savedId.value) return
  if (!orderForm.value.receiverName || !orderForm.value.receiverPhone || !orderForm.value.shippingAddress) {
    alert('请填写完整的收货信息')
    return
  }
  ordering.value = true
  try {
    const userId = Number(localStorage.getItem('userId'))
    api.defaults.headers.Authorization = 'Bearer ' + localStorage.getItem('token')
    await api.post(`/diy/${savedId.value}/order`, { userId, ...orderForm.value })
    alert('下单成功！')
    savedId.value = null
    designItems.value = []
    selectedIndex.value = null
    orderForm.value = { receiverName: '', receiverPhone: '', shippingAddress: '' }
  } catch (e) {
    console.error(e)
    alert('下单失败: ' + (e.response?.data?.message || e.message))
  } finally {
    ordering.value = false
  }
}
</script>

<style scoped>
.diy-page {
  --diy-surface: #fffefa;
  --diy-paper: #f6f4ef;
  --diy-line: rgba(70, 62, 49, 0.12);
  --diy-ink: #25231f;
  --diy-muted: #746d60;
  --diy-accent: #b44646;
  --diy-green: #2f7356;
  max-width: 1440px;
  margin: 0 auto;
}
.page-header { display: flex; align-items: center; justify-content: space-between; gap: 1rem; margin-bottom: 1.5rem; }
.eyebrow { color: var(--diy-accent); font-size: 0.78rem; font-weight: 700; letter-spacing: 0; margin-bottom: 0.25rem; }
.page-header h2 { color: var(--diy-ink); font-size: 1.55rem; }
.ghost-btn { border: 1px solid rgba(180, 70, 70, 0.22); border-radius: 999px; padding: 0.65rem 1.1rem; color: var(--diy-accent); background: var(--diy-surface); cursor: pointer; box-shadow: 0 10px 22px rgba(180, 70, 70, 0.08); }
.ghost-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.diy-layout { display: grid; grid-template-columns: minmax(280px, 330px) minmax(460px, 1fr) minmax(280px, 330px); gap: 1.2rem; align-items: start; }
.side-panel, .canvas-card {
  border: 1px solid var(--diy-line);
  border-radius: 10px;
  background:
    linear-gradient(180deg, rgba(255,254,250,0.96), rgba(250,246,238,0.94)),
    repeating-linear-gradient(90deg, rgba(111, 88, 57, 0.035) 0 1px, transparent 1px 22px);
  box-shadow: 0 14px 32px rgba(48, 41, 32, 0.08);
}
.side-panel { padding: 1.25rem; }
.panel-title, .design-header { display: flex; justify-content: space-between; align-items: flex-start; gap: 1rem; }
.panel-title h3, .design-header h3 { color: var(--diy-ink); font-size: 1.12rem; }
.panel-title span, .design-header p { color: var(--diy-muted); font-size: 0.86rem; }
.category-filter { display: flex; flex-wrap: wrap; gap: 0.5rem; margin: 1rem 0; }
.category-filter button { border: 1px solid rgba(70, 62, 49, 0.12); border-radius: 999px; background: rgba(255,254,250,0.8); color: var(--diy-muted); padding: 0.4rem 0.72rem; cursor: pointer; transition: all 0.2s; }
.category-filter button.active, .category-filter button:hover { color: white; border-color: transparent; background: var(--diy-accent); }
.flowers-list { max-height: 660px; overflow-y: auto; padding-right: 0.2rem; }
.flower-item { display: grid; grid-template-columns: 58px 1fr 30px; align-items: center; gap: 0.75rem; padding: 0.75rem; margin-bottom: 0.65rem; border-radius: 8px; background: rgba(255,254,250,0.74); border: 1px solid transparent; cursor: grab; transition: all 0.2s; }
.flower-item:hover { border-color: rgba(180, 70, 70, 0.24); background: #fffaf3; box-shadow: 0 8px 18px rgba(68, 51, 32, 0.06); }
.flower-thumb-wrap { width: 58px; height: 58px; border-radius: 8px; background: linear-gradient(180deg, #fffaf3, #f1e4d5); display: flex; align-items: center; justify-content: center; }
.flower-thumb { width: 52px; height: 52px; object-fit: contain; }
.flower-info h4 { color: var(--diy-ink); font-size: 0.95rem; margin-bottom: 0.2rem; }
.flower-info p { color: var(--diy-accent); font-size: 0.82rem; font-weight: 600; }
.flower-tags { display: flex; gap: 0.3rem; margin-top: 0.35rem; }
.flower-tags span, .mood-box em { border-radius: 999px; padding: 0.15rem 0.45rem; color: #8d513b; background: #f5e7df; font-size: 0.74rem; font-style: normal; }
.add-btn { width: 30px; height: 30px; border: none; border-radius: 50%; background: var(--diy-accent); color: white; font-size: 1.1rem; cursor: pointer; }
.workspace-panel { min-width: 0; }
.template-strip { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 0.75rem; margin-bottom: 1rem; }
.template-card { min-height: 78px; text-align: left; border: 1px solid var(--diy-line); border-radius: 8px; background: rgba(255,254,250,0.86); padding: 0.8rem; cursor: pointer; box-shadow: 0 6px 16px rgba(68, 51, 32, 0.05); }
.template-card.active { border-color: rgba(180, 70, 70, 0.48); background: #fffaf3; box-shadow: 0 0 0 3px rgba(180, 70, 70, 0.08); }
.template-card strong, .template-card span { display: block; }
.template-card strong { color: var(--diy-ink); margin-bottom: 0.35rem; }
.template-card span { color: var(--diy-muted); font-size: 0.78rem; line-height: 1.45; }
.canvas-card { padding: 1.25rem; }
.design-header { margin-bottom: 1rem; }
.package-select { display: flex; align-items: center; gap: 0.5rem; color: var(--diy-muted); white-space: nowrap; }
.package-select select { border: 1px solid rgba(70, 62, 49, 0.14); border-radius: 8px; padding: 0.55rem 0.7rem; background: var(--diy-surface); color: var(--diy-ink); }
.recipe-list, .summary-card { margin-top: 1rem; }
.recipe-item, .summary-row { display: flex; justify-content: space-between; gap: 0.8rem; padding: 0.65rem 0; border-bottom: 1px solid rgba(70, 62, 49, 0.1); color: #555; }
.recipe-item strong { color: var(--diy-ink); }
.recipe-item span, .summary-row span { color: var(--diy-muted); }
.empty-state, .loading-box { padding: 1.5rem 0; color: #999; text-align: center; }
.card-input { display: block; margin-top: 1rem; }
.card-input span, .mood-box > span { display: block; color: var(--diy-muted); font-size: 0.9rem; margin-bottom: 0.45rem; }
.card-input textarea { width: 100%; resize: none; border: 1px solid rgba(180, 70, 70, 0.18); border-radius: 8px; padding: 0.75rem; font-family: inherit; color: #444; background: var(--diy-surface); }
.mood-box { margin-top: 1rem; }
.mood-box div { display: flex; flex-wrap: wrap; gap: 0.4rem; }
.summary-row.total { border-bottom: none; padding-top: 0.9rem; }
.summary-row.total strong { color: var(--diy-accent); font-size: 1.35rem; }
.save-btn, .order-btn { width: 100%; border: none; border-radius: 12px; color: white; cursor: pointer; transition: all 0.2s; }
.save-btn { margin-top: 1rem; padding: 0.95rem; background: linear-gradient(135deg, #cf6d76 0%, #b44646 100%); font-size: 1rem; }
.save-btn:hover:not(:disabled), .order-btn:hover:not(:disabled) { transform: translateY(-2px); }
.save-btn:disabled, .order-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.order-section { margin-top: 1rem; padding-top: 1rem; border-top: 1px solid rgba(70, 62, 49, 0.1); }
.save-success { color: #2e7d32; font-weight: 700; text-align: center; margin-bottom: 0.7rem; }
.view-plans-link { display: block; color: var(--diy-accent); text-align: center; text-decoration: none; margin-bottom: 0.8rem; font-weight: 600; }
.order-form { display: flex; flex-direction: column; gap: 0.55rem; }
.order-input { border: 1px solid rgba(70, 62, 49, 0.14); border-radius: 8px; padding: 0.65rem; background: var(--diy-surface); }
.order-btn { padding: 0.8rem; background: linear-gradient(135deg, #3d8263 0%, #2f7356 100%); }
@media (max-width: 1180px) {
  .diy-layout { grid-template-columns: 1fr; }
  .flower-panel { order: 2; }
  .workspace-panel { order: 1; }
  .summary-panel { order: 3; }
  .flowers-list { display: grid; grid-auto-flow: column; grid-auto-columns: minmax(230px, 260px); overflow-x: auto; overflow-y: hidden; max-height: none; padding-bottom: 0.5rem; }
}
@media (max-width: 720px) {
  .page-header, .design-header { flex-direction: column; align-items: stretch; }
  .template-strip { display: flex; overflow-x: auto; padding-bottom: 0.4rem; }
  .template-card { min-width: 190px; }
  .package-select { justify-content: space-between; }
  .package-select select { min-width: 0; width: 72%; }
}
</style>
