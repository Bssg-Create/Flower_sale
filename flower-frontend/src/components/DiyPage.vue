<template>
  <div class="diy-page">
    <div class="diy-container">
      <div class="left-panel">
        <h2>选择花卉</h2>
        <div class="category-filter">
          <button
            v-for="cat in categories"
            :key="cat.id"
            @click="selectedCategory = cat.id"
            :class="['filter-btn', { active: selectedCategory === cat.id }]"
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
          >
            <img v-if="flower.imageUrl" :src="encodeUrl(flower.imageUrl)" :alt="flower.name" class="flower-thumb" />
            <span v-else class="flower-emoji">{{ getFlowerEmoji(flower.categoryName) }}</span>
            <div class="flower-info">
              <h4>{{ flower.name }}</h4>
              <p class="flower-price">¥{{ flower.price.toFixed(2) }}</p>
            </div>
            <button class="add-btn" @click.stop="addToDesign(flower)">+</button>
          </div>
        </div>
        <div v-else class="loading-box">加载花卉中...</div>
      </div>

      <div class="right-panel">
        <div class="design-header">
          <h2>花束设计区</h2>
          <div class="package-selector">
            <span>选择包装:</span>
            <select v-model="selectedPackage">
              <option v-for="pkg in packages" :key="pkg.id" :value="pkg.id">
                {{ pkg.name }} (¥{{ pkg.price.toFixed(2) }})
              </option>
            </select>
          </div>
        </div>

        <div
          class="design-area"
          @dragover.prevent
          @drop="handleDrop"
          @click="deselectFlower"
        >
          <div class="package-background" :class="getPackageClass()">
            <div
              v-for="(item, index) in designItems"
              :key="index"
              class="design-flower"
              :class="{ selected: selectedIndex === index }"
              :style="getFlowerStyle(item)"
              @mousedown.stop="startDrag($event, index)"
              @click.stop="selectFlower(index)"
            >
              <div class="flower-shape" v-html="getFlowerSvg(item)"></div>
              <div class="rotation-handle" @mousedown.stop="startRotate($event, index)">
                ⟳
              </div>
              <button class="remove-btn" @click.stop="removeItem(index)">×</button>
            </div>
            <div v-if="designItems.length === 0" class="empty-hint">
              拖拽花卉到包装区内自由放置
            </div>
          </div>
        </div>

        <div class="design-summary" v-if="selectedIndex !== null">
          <div class="selected-info">
            <h4>已选中: {{ selectedItem?.flowerName }}</h4>
            <div class="control-row">
              <span>旋转角度: {{ selectedItem?.rotation || 0 }}°</span>
              <input
                :value="selectedItem?.rotation || 0"
                @input="e => { const val = Number(e.target.value); if (selectedIndex !== null) designItems[selectedIndex].rotation = val }"
                type="range"
                min="-180"
                max="180"
                step="5"
              />
            </div>
            <div class="control-row">
              <span>大小缩放: {{ Math.round((selectedItem?.scale || 1) * 100) }}%</span>
              <input
                :value="selectedItem?.scale || 1"
                @input="e => { const val = Number(e.target.value); if (selectedIndex !== null) designItems[selectedIndex].scale = val }"
                type="range"
                min="0.5"
                max="1.5"
                step="0.1"
              />
            </div>
          </div>
        </div>

        <div class="design-summary">
          <div class="summary-row">
            <span>花卉数量:</span>
            <span>{{ totalFlowers }}</span>
          </div>
          <div class="summary-row">
            <span>花卉费用:</span>
            <span>¥{{ flowersTotal.toFixed(2) }}</span>
          </div>
          <div class="summary-row">
            <span>包装费用:</span>
            <span>¥{{ currentPackage.price.toFixed(2) }}</span>
          </div>
          <div class="summary-row total">
            <span>总计:</span>
            <span>¥{{ totalPrice.toFixed(2) }}</span>
          </div>

          <button class="save-btn" @click="saveDesign" :disabled="saving || designItems.length === 0">
            {{ saving ? '保存中...' : '保存花束设计' }}
          </button>
          <div v-if="savedId" class="order-section">
            <p class="save-success">花束已保存成功！</p>
            <router-link to="/user/plans" class="view-plans-link">📋 查看我的方案 →</router-link>
            <div class="order-form">
              <input v-model="orderForm.receiverName" placeholder="收货人姓名" class="order-input" />
              <input v-model="orderForm.receiverPhone" placeholder="收货人电话" class="order-input" />
              <input v-model="orderForm.shippingAddress" placeholder="收货地址" class="order-input" />
              <button class="order-btn" @click="placeOrder" :disabled="ordering">
                {{ ordering ? '下单中...' : '立即下单' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import api from '../api/index.js'

const flowers = ref([])
const packages = ref([])
const flowersLoading = ref(true)
const designItems = ref([])
const draggedFlower = ref(null)
const selectedIndex = ref(null)
const selectedCategory = ref(0)
const selectedPackage = ref(1)
const saving = ref(false)
const savedId = ref(null)
const ordering = ref(false)
const isDragging = ref(false)
const isRotating = ref(false)
const dragStart = ref({ x: 0, y: 0 })

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

const filteredFlowers = computed(() => {
  if (selectedCategory.value === 0) return flowers.value
  return flowers.value.filter(f => f.categoryId === selectedCategory.value)
})

const currentPackage = computed(() => {
  return packages.value.find(p => p.id === selectedPackage.value) || { name: '圆形包装', price: 10.00 }
})

const packageEmoji = computed(() => {
  const map = { '圆形包装': '⭕', '心形包装': '❤️', '长形包装': '▭', '礼盒包装': '🎁' }
  return map[currentPackage.value.name] || '📦'
})

const getPackageClass = () => {
  return `package-${currentPackage.value.name?.replace(/\s/g, '-')?.toLowerCase() || 'circle'}`
}

const totalFlowers = computed(() => {
  return designItems.value.reduce((sum, item) => sum + (item.quantity || 1), 0)
})

const flowersTotal = computed(() => {
  return designItems.value.reduce((sum, item) => sum + (item.price * (item.quantity || 1)), 0)
})

const totalPrice = computed(() => {
  return flowersTotal.value + (currentPackage.value.price || 0)
})

const selectedItem = computed(() => {
  return selectedIndex.value !== null ? designItems.value[selectedIndex.value] : null
})

const getFlowerStyle = (item) => {
  return {
    left: item.x + 'px',
    top: item.y + 'px',
    transform: `rotate(${item.rotation || 0}deg) scale(${item.scale || 1})`
  }
}

const getFlowerEmoji = (category) => {
  const emojiMap = { '玫瑰': '🌹', '百合': '💮', '郁金香': '🌷', '向日葵': '🌻', '康乃馨': '🌺', '配叶': '🌿' }
  return emojiMap[category] || '🌸'
}

const getFlowerColor = (name) => {
  const n = name || ''
  if (n.includes('红')) return { primary: '#e74c3c', dark: '#c0392b' }
  if (n.includes('白')) return { primary: '#f5f5f5', dark: '#e0e0e0', stroke: '#ccc' }
  if (n.includes('粉')) return { primary: '#f78fb3', dark: '#e77c9e' }
  if (n.includes('黄') || n.includes('向日葵')) return { primary: '#f9ca24', dark: '#e6a800' }
  if (n.includes('康乃馨')) return { primary: '#ff6b81', dark: '#ee5a6f' }
  if (n.includes('雏菊')) return { primary: '#ffffff', dark: '#ffc107', stroke: '#e0e0e0' }
  if (n.includes('尤加利') || n.includes('配叶')) return { primary: '#7bed9f', dark: '#55c473' }
  return { primary: '#ff6b9d', dark: '#c44569' }
}

const getFlowerSvg = (item) => {
  const cat = (item.categoryName || item.flowerName || '').trim()
  const name = (item.flowerName || item.name || '').trim()
  const c = getFlowerColor(name)
  const P = c.primary
  const D = c.dark
  const stroke = c.stroke || null
  const extra = stroke ? `stroke="${stroke}" stroke-width="1"` : ''

  const flowerParts = {
    '玫瑰': `
      <rect x="38" y="64" width="4" height="18" fill="#4caf50" rx="1"/>
      <circle cx="40" cy="42" r="22" fill="${P}" opacity="0.9" ${extra}/>
      <circle cx="40" cy="38" r="16" fill="${P}" opacity="0.75" ${extra}/>
      <circle cx="43" cy="35" r="10" fill="${P}" opacity="0.6" ${extra}/>
      <circle cx="37" cy="32" r="5" fill="${D}"/>
    `,
    '百合': `
      <rect x="38" y="62" width="4" height="20" fill="#4caf50" rx="1"/>
      <polygon points="40,8 48,30 70,30 52,44 58,66 40,54 22,66 28,44 10,30 32,30" fill="${P}" ${extra}/>
      <circle cx="40" cy="38" r="4" fill="${D}"/>
    `,
    '郁金香': `
      <rect x="38" y="60" width="4" height="22" fill="#4caf50" rx="1"/>
      <ellipse cx="40" cy="42" rx="16" ry="24" fill="${P}" ${extra}/>
      <ellipse cx="40" cy="46" rx="10" ry="14" fill="${D}" opacity="0.35"/>
      <path d="M24,30 Q32,20 40,30 Q48,20 56,30" fill="none" stroke="${D}" stroke-width="1.5" opacity="0.6"/>
    `,
    '向日葵': `
      <rect x="38" y="62" width="4" height="20" fill="#4caf50" rx="1"/>
      <g fill="#ffd32a">
        <ellipse cx="40" cy="16" rx="5" ry="12"/><ellipse cx="40" cy="16" rx="5" ry="12" transform="rotate(30 40 40)"/>
        <ellipse cx="40" cy="16" rx="5" ry="12" transform="rotate(60 40 40)"/><ellipse cx="40" cy="16" rx="5" ry="12" transform="rotate(90 40 40)"/>
        <ellipse cx="40" cy="16" rx="5" ry="12" transform="rotate(120 40 40)"/><ellipse cx="40" cy="16" rx="5" ry="12" transform="rotate(150 40 40)"/>
        <ellipse cx="40" cy="16" rx="5" ry="12" transform="rotate(180 40 40)"/><ellipse cx="40" cy="16" rx="5" ry="12" transform="rotate(210 40 40)"/>
        <ellipse cx="40" cy="16" rx="5" ry="12" transform="rotate(240 40 40)"/><ellipse cx="40" cy="16" rx="5" ry="12" transform="rotate(270 40 40)"/>
        <ellipse cx="40" cy="16" rx="5" ry="12" transform="rotate(300 40 40)"/><ellipse cx="40" cy="16" rx="5" ry="12" transform="rotate(330 40 40)"/>
      </g>
      <circle cx="40" cy="40" r="16" fill="#5D4037"/>
    `,
    '康乃馨': `
      <rect x="38" y="60" width="4" height="22" fill="#4caf50" rx="1"/>
      <circle cx="40" cy="40" r="24" fill="${P}" opacity="0.85" ${extra}/>
      <circle cx="33" cy="35" r="14" fill="${P}" opacity="0.7" ${extra}/>
      <circle cx="46" cy="37" r="12" fill="${P}" opacity="0.65" ${extra}/>
      <circle cx="38" cy="42" r="10" fill="${D}" opacity="0.5"/>
      <circle cx="44" cy="30" r="8" fill="${P}" opacity="0.55" ${extra}/>
    `,
    '小雏菊': `
      <rect x="38" y="58" width="4" height="24" fill="#4caf50" rx="1"/>
      <g fill="white" stroke="#e0e0e0" stroke-width="0.5">
        <ellipse cx="40" cy="16" rx="4" ry="12"/>
        <ellipse cx="40" cy="16" rx="4" ry="12" transform="rotate(45 40 40)"/>
        <ellipse cx="40" cy="16" rx="4" ry="12" transform="rotate(90 40 40)"/>
        <ellipse cx="40" cy="16" rx="4" ry="12" transform="rotate(135 40 40)"/>
        <ellipse cx="40" cy="16" rx="4" ry="12" transform="rotate(180 40 40)"/>
        <ellipse cx="40" cy="16" rx="4" ry="12" transform="rotate(225 40 40)"/>
        <ellipse cx="40" cy="16" rx="4" ry="12" transform="rotate(270 40 40)"/>
        <ellipse cx="40" cy="16" rx="4" ry="12" transform="rotate(315 40 40)"/>
      </g>
      <circle cx="40" cy="40" r="10" fill="#ffc107"/>
    `,
    '尤加利叶': `
      <rect x="38" y="50" width="4" height="32" fill="#4caf50" rx="1"/>
      <ellipse cx="30" cy="45" rx="8" ry="18" fill="#7bed9f" transform="rotate(-15 30 45)"/>
      <ellipse cx="50" cy="42" rx="8" ry="17" fill="#6dd68a" transform="rotate(15 50 42)"/>
      <ellipse cx="26" cy="30" rx="7" ry="15" fill="#8df0aa" transform="rotate(-30 26 30)"/>
      <ellipse cx="54" cy="28" rx="7" ry="14" fill="#7ae095" transform="rotate(25 54 28)"/>
      <ellipse cx="22" cy="18" rx="6" ry="12" fill="#90f5b0" transform="rotate(-40 22 18)"/>
    `,
    '配叶': `
      <rect x="38" y="50" width="4" height="32" fill="#4caf50" rx="1"/>
      <ellipse cx="30" cy="45" rx="14" ry="22" fill="#7bed9f" transform="rotate(-20 30 45)"/>
      <ellipse cx="50" cy="40" rx="13" ry="20" fill="#6dd68a" transform="rotate(15 50 40)"/>
      <ellipse cx="28" cy="30" rx="10" ry="18" fill="#8df0aa" transform="rotate(-35 28 30)"/>
      <ellipse cx="52" cy="28" rx="9" ry="16" fill="#7ae095" transform="rotate(30 52 28)"/>
    `
  }

  let svgBody = flowerParts[name]
  if (!svgBody) {
    svgBody = flowerParts[cat]
  }
  if (!svgBody) {
    const keys = ['玫瑰', '百合', '郁金香', '向日葵', '康乃馨', '配叶', '小雏菊', '尤加利叶']
    for (const key of keys) {
      if (name.includes(key) || cat.includes(key)) {
        svgBody = flowerParts[key]
        break
      }
    }
  }
  if (!svgBody) {
    svgBody = `
      <rect x="38" y="60" width="4" height="22" fill="#4caf50" rx="1"/>
      <circle cx="40" cy="42" r="22" fill="${P}" opacity="0.85" ${extra}/>
      <circle cx="36" cy="38" r="10" fill="${P}" opacity="0.65" ${extra}/>
      <circle cx="37" cy="32" r="5" fill="${D}"/>
    `
  }

  return `<svg viewBox="0 0 80 85" xmlns="http://www.w3.org/2000/svg">${svgBody}</svg>`
}

const encodeUrl = (url) => {
  if (!url) return ''
  const lastSlash = url.lastIndexOf('/')
  if (lastSlash >= 0) {
    return url.substring(0, lastSlash + 1) + encodeURIComponent(url.substring(lastSlash + 1))
  }
  return encodeURIComponent(url)
}

onMounted(async () => {
  try {
    const [fRes, pRes] = await Promise.all([
      api.get('/diy/flowers'),
      api.get('/diy/package/list')
    ])
    flowers.value = fRes.data.data || []
    packages.value = pRes.data.data || []
    if (packages.value.length > 0) selectedPackage.value = packages.value[0].id
  } catch (e) { console.error(e) }
  finally { flowersLoading.value = false }

  document.addEventListener('mouseup', handleMouseUp)
  document.addEventListener('mousemove', handleMouseMove)
})

const handleDragStart = (event, flower) => {
  draggedFlower.value = flower
  event.dataTransfer.effectAllowed = 'copy'
}

const handleDrop = (event) => {
  if (!draggedFlower.value) return
  const pkgEl = event.currentTarget.querySelector('.package-background')
  if (!pkgEl) return
  const rect = pkgEl.getBoundingClientRect()
  const x = event.clientX - rect.left - 40
  const y = event.clientY - rect.top - 40
  if (event.clientX < rect.left || event.clientX > rect.right ||
      event.clientY < rect.top || event.clientY > rect.bottom) {
    draggedFlower.value = null
    return
  }
  addToDesignAt(draggedFlower.value, Math.max(0, x), Math.max(0, y))
  draggedFlower.value = null
}

const addToDesign = (flower) => {
  addToDesignAt(flower, 50 + designItems.value.length * 20, 50 + designItems.value.length * 20)
}

const addToDesignAt = (flower, x, y) => {
  const newItem = {
    id: flower.id,
    flowerId: flower.id,
    flowerName: flower.name,
    name: flower.name,
    imageUrl: flower.imageUrl,
    price: flower.price,
    categoryName: flower.categoryName,
    quantity: 1,
    x: x,
    y: y,
    rotation: 0,
    scale: 1
  }
  designItems.value.push(newItem)
  selectedIndex.value = designItems.value.length - 1
}

const selectFlower = (index) => {
  selectedIndex.value = index
}

const deselectFlower = () => {
  if (isDragging.value || isRotating.value) return
  selectedIndex.value = null
}

const removeItem = (index) => {
  designItems.value.splice(index, 1)
  if (selectedIndex.value === index) {
    selectedIndex.value = null
  } else if (selectedIndex.value > index) {
    selectedIndex.value--
  }
}

const startDrag = (event, index) => {
  isDragging.value = true
  selectedIndex.value = index
  dragStart.value = {
    x: event.clientX - designItems.value[index].x,
    y: event.clientY - designItems.value[index].y
  }
}

const startRotate = (event, index) => {
  isRotating.value = true
  selectedIndex.value = index
  const rect = event.currentTarget.parentElement.getBoundingClientRect()
  const centerX = rect.left + rect.width / 2
  const centerY = rect.top + rect.height / 2
  dragStart.value = {
    x: centerX,
    y: centerY,
    angle: designItems.value[index].rotation || 0,
    startAngle: Math.atan2(event.clientY - centerY, event.clientX - centerX) * 180 / Math.PI
  }
}

const handleMouseUp = () => {
  isDragging.value = false
  isRotating.value = false
}

const handleMouseMove = (event) => {
  if (isDragging.value && selectedIndex.value !== null) {
    const item = designItems.value[selectedIndex.value]
    const pkgEl = document.querySelector('.design-area .package-background')
    if (pkgEl) {
      const rect = pkgEl.getBoundingClientRect()
      item.x = Math.max(0, Math.min(event.clientX - dragStart.value.x, rect.width - 90))
      item.y = Math.max(0, Math.min(event.clientY - dragStart.value.y, rect.height - 140))
    } else {
      item.x = event.clientX - dragStart.value.x
      item.y = event.clientY - dragStart.value.y
    }
  } else if (isRotating.value && selectedIndex.value !== null) {
    const item = designItems.value[selectedIndex.value]
    const currentAngle = Math.atan2(event.clientY - dragStart.value.y, event.clientX - dragStart.value.x) * 180 / Math.PI
    const delta = currentAngle - dragStart.value.startAngle
    item.rotation = (dragStart.value.angle + delta + 360) % 360
    if (item.rotation > 180) item.rotation -= 360
  }
}

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
        position: JSON.stringify({ x: Math.round(item.x), y: Math.round(item.y), rotation: item.rotation || 0, scale: item.scale || 1 })
      }))
    })
    if (res.data && res.data.data) {
      savedId.value = res.data.data.id
    } else {
      alert('保存失败: ' + (res.data?.message || '未知错误'))
    }
  } catch (e) { console.error(e); alert('保存失败: ' + (e.response?.data?.message || e.message)) }
  finally { saving.value = false }
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
    await api.post(`/diy/${savedId.value}/order`, {
      userId,
      ...orderForm.value
    })
    alert('下单成功！')
    savedId.value = null
    designItems.value = []
    selectedIndex.value = null
    orderForm.value = { receiverName: '', receiverPhone: '', shippingAddress: '' }
  } catch (e) { console.error(e); alert('下单失败: ' + (e.response?.data?.message || e.message)) }
  finally { ordering.value = false }
}
</script>

<style scoped>
.diy-page { max-width: 1400px; margin: 0 auto; }
.diy-container { display: grid; grid-template-columns: 350px 1fr; gap: 2rem; }

.left-panel { background: white; border-radius: 15px; padding: 1.5rem; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
.left-panel h2 { font-size: 1.3rem; margin-bottom: 1rem; color: #333; }

.category-filter { display: flex; flex-wrap: wrap; gap: 0.5rem; margin-bottom: 1rem; }
.filter-btn { padding: 0.4rem 0.8rem; border: 1px solid #e0e0e0; border-radius: 15px; background: white; cursor: pointer; font-size: 0.85rem; transition: all 0.3s ease; }
.filter-btn:hover { border-color: #ff6b9d; color: #ff6b9d; }
.filter-btn.active { background: linear-gradient(135deg, #ff6b9d 0%, #c44569 100%); color: white; border-color: transparent; }

.flowers-list { max-height: 500px; overflow-y: auto; }
.flower-item { display: flex; align-items: center; padding: 0.8rem; border-radius: 10px; background: #f9f9f9; margin-bottom: 0.5rem; cursor: grab; transition: all 0.3s ease; }
.flower-item:hover { background: #f0f0f0; }
.flower-item:active { cursor: grabbing; }
.flower-emoji { font-size: 1.8rem; margin-right: 0.8rem; }
.flower-thumb { width: 36px; height: 36px; object-fit: cover; border-radius: 6px; margin-right: 0.8rem; }
.flower-info { flex: 1; }
.flower-info h4 { font-size: 0.95rem; margin-bottom: 0.2rem; }
.flower-price { font-size: 0.85rem; color: #c44569; font-weight: bold; }

.add-btn { width: 28px; height: 28px; border-radius: 50%; background: linear-gradient(135deg, #ff6b9d 0%, #c44569 100%); color: white; border: none; cursor: pointer; font-size: 1.2rem; display: flex; align-items: center; justify-content: center; transition: all 0.3s ease; }
.add-btn:hover { transform: scale(1.1); }

.loading-box { text-align: center; padding: 2rem; color: #999; }

.right-panel { display: flex; flex-direction: column; gap: 1.5rem; }
.design-header { display: flex; justify-content: space-between; align-items: center; }
.design-header h2 { font-size: 1.3rem; color: #333; }
.package-selector { display: flex; align-items: center; gap: 0.5rem; }
.package-selector select { padding: 0.5rem; border: 1px solid #e0e0e0; border-radius: 8px; font-size: 0.9rem; }

.design-area { background: white; border-radius: 15px; padding: 2rem; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
.package-background {
  position: relative;
  width: 100%;
  min-height: 480px;
  border-radius: 10px;
  background: #f5f5f5;
  overflow: hidden;
}

.package-background.package-圆形包装,
.package-background.package-circle {
  background: radial-gradient(circle at center, #fff8e6 0%, #f5f5f5 70%);
  border: 2px dashed #e0d5c0;
  border-radius: 50%;
  width: 420px;
  height: 420px;
  margin: 30px auto;
}

.package-background.package-心形包装,
.package-background.package-heart {
  background: radial-gradient(ellipse at center, #ffe6f0 0%, #f5f5f5 70%);
  border: 2px dashed #e0c0d0;
  border-radius: 40% 40% 45% 45% / 45% 45% 50% 50%;
  width: 420px;
  height: 450px;
  margin: 15px auto;
}

.package-background.package-长形包装,
.package-background.package-long {
  background: linear-gradient(to right, #fff8e6 0%, #f5f5f5 95%);
  border: 2px dashed #e0d5c0;
  border-radius: 20px;
  width: 220px;
  height: 440px;
  margin: 20px auto;
}

.package-background.package-礼盒包装,
.package-background.package-gift {
  background: repeating-linear-gradient(45deg, #ffe6f0 0px, #ffe6f0 20px, #f5e6f0 20px, #f5e6f0 40px);
  border: 2px dashed #e0c0d0;
}

.design-flower {
  position: absolute;
  width: 90px;
  height: 140px;
  cursor: move;
  user-select: none;
  filter: drop-shadow(0 2px 3px rgba(0,0,0,0.08));
}

.design-flower.selected {
  filter: drop-shadow(0 0 0 2px #ff6b9d) drop-shadow(0 4px 8px rgba(0,0,0,0.15));
}

.design-flower .rotation-handle,
.design-flower .remove-btn {
  display: none;
}

.design-flower.selected .rotation-handle,
.design-flower.selected .remove-btn {
  display: flex;
}

.flower-shape {
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.flower-shape :deep(svg) {
  width: 100%;
  height: 100%;
  display: block;
}

.rotation-handle {
  position: absolute;
  top: -25px;
  right: -10px;
  width: 20px;
  height: 20px;
  background: #ff6b9d;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  cursor: grab;
  user-select: none;
}

.remove-btn {
  position: absolute;
  top: -25px;
  left: -10px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #ff4444;
  color: white;
  border: none;
  cursor: pointer;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  padding: 0;
}

.empty-hint {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #999;
  font-size: 1.1rem;
}

.selected-info {
  padding: 1rem;
  background: #f9f9f9;
  border-radius: 8px;
  margin-bottom: 1rem;
}

.selected-info h4 {
  margin: 0 0 0.8rem 0;
  color: #333;
}

.control-row {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 0.5rem;
}

.control-row span {
  min-width: 120px;
  font-size: 0.9rem;
  color: #666;
}

.control-row input[type="range"] {
  flex: 1;
}

.design-summary { background: white; border-radius: 15px; padding: 1.5rem; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
.summary-row { display: flex; justify-content: space-between; padding: 0.5rem 0; border-bottom: 1px solid #f0f0f0; }
.summary-row.total { font-size: 1.2rem; font-weight: bold; color: #c44569; padding-top: 1rem; border-bottom: none; }

.save-btn { width: 100%; background: linear-gradient(135deg, #ff6b9d 0%, #c44569 100%); color: white; border: none; padding: 1rem; border-radius: 10px; font-size: 1rem; cursor: pointer; margin-top: 1rem; transition: all 0.3s ease; }
.save-btn:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 5px 15px rgba(255,107,157,0.4); }
.save-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.order-section { margin-top: 1rem; padding-top: 1rem; border-top: 1px solid #f0f0f0; }
.save-success { color: #2e7d32; font-weight: bold; text-align: center; margin-bottom: 0.8rem; }
.order-form { display: flex; flex-direction: column; gap: 0.5rem; }
.order-input { padding: 0.6rem; border: 1px solid #e0e0e0; border-radius: 8px; font-size: 0.9rem; }
.order-btn { width: 100%; background: linear-gradient(135deg, #2e7d32 0%, #1b5e20 100%); color: white; border: none; padding: 0.8rem; border-radius: 10px; font-size: 1rem; cursor: pointer; transition: all 0.3s ease; }
.order-btn:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 5px 15px rgba(46,125,50,0.4); }
.order-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.view-plans-link { display: block; text-align: center; color: #c44569; font-weight: 600; text-decoration: none; margin-bottom: 0.8rem; font-size: 0.95rem; }
.view-plans-link:hover { text-decoration: underline; }
</style>