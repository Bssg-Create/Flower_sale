<template>
  <div class="plan-detail-page">
    <div class="back-nav">
      <router-link to="/user/plans" class="back-link">← 返回方案列表</router-link>
    </div>

    <div class="loading-box" v-if="loading">加载中...</div>
    <div class="error-box" v-else-if="!plan">方案不存在</div>
    <div class="detail-content" v-else>
      <div class="detail-left">
        <div class="plan-preview-card">
          <div class="preview-bouquet-wrap" :style="{ width: scaledW + 'px', height: scaledH + 'px' }">
            <div class="package-background preview-mode" :class="packageClass" :style="{ transform: `scale(${previewScale})`, transformOrigin: '0 0' }">
              <div
                v-for="(item, index) in designItems"
                :key="index"
                class="design-flower"
                :style="getFlowerStyle(item)"
              >
                <div class="flower-shape" v-html="getFlowerSvg(item)"></div>
              </div>
            </div>
          </div>
          <h2>{{ plan.name }}</h2>
          <p class="preview-pkg">包装: {{ plan.packageType || '无' }}</p>
          <p class="preview-price">¥{{ plan.totalPrice.toFixed(2) }}</p>
          <p class="preview-status">{{ statusText(plan.status) }}</p>
        </div>
      </div>

      <div class="detail-right">
        <h3>花束内容</h3>
        <div class="items-list">
          <div v-for="(item, index) in items" :key="index" class="item-row">
            <span class="item-emoji">{{ getFlowerEmoji(item.flowerName) }}</span>
            <span class="item-name">{{ item.flowerName }}</span>
            <span class="item-qty">x{{ item.quantity }}</span>
          </div>
        </div>

        <div class="order-section" v-if="plan.status !== 'ordered'">
          <h3>立即购买</h3>
          <div class="order-form">
            <input v-model="form.receiverName" placeholder="收货人姓名" class="order-input" />
            <input v-model="form.receiverPhone" placeholder="收货人电话" class="order-input" />
            <textarea v-model="form.shippingAddress" placeholder="收货地址" class="order-input" rows="2"></textarea>
            <button class="order-btn" @click="placeOrder" :disabled="ordering">
              {{ ordering ? '下单中...' : `立即下单 ¥${plan.totalPrice.toFixed(2)}` }}
            </button>
          </div>
        </div>
        <div class="ordered-hint" v-else>
          <p>✅ 该方案已下单</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../api/index.js'

const route = useRoute()
const router = useRouter()
const plan = ref(null)
const items = ref([])
const loading = ref(true)
const ordering = ref(false)

const form = ref({
  receiverName: '',
  receiverPhone: '',
  shippingAddress: ''
})

const statusText = (s) => {
  if (s === 'ordered') return '已下单'
  if (s === 'saved' || s === '1') return '已保存'
  return '草稿'
}

const getFlowerEmoji = (name) => {
  if (!name) return '🌸'
  if (name.includes('玫瑰')) return '🌹'
  if (name.includes('百合')) return '💮'
  if (name.includes('郁金香')) return '🌷'
  if (name.includes('向日葵')) return '🌻'
  if (name.includes('康乃馨')) return '🌺'
  if (name.includes('尤加利') || name.includes('配叶')) return '🌿'
  if (name.includes('雏菊')) return '🌸'
  return '🌸'
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
  const name = (item.flowerName || item.name || '').trim()
  const c = getFlowerColor(name)
  const P = c.primary
  const D = c.dark
  const stroke = c.stroke || null
  const extra = stroke ? `stroke="${stroke}" stroke-width="1"` : ''

  const flowerParts = {
    '玫瑰': `<rect x="38" y="64" width="4" height="18" fill="#4caf50" rx="1"/><circle cx="40" cy="42" r="22" fill="${P}" opacity="0.9" ${extra}/><circle cx="40" cy="38" r="16" fill="${P}" opacity="0.75" ${extra}/><circle cx="43" cy="35" r="10" fill="${P}" opacity="0.6" ${extra}/><circle cx="37" cy="32" r="5" fill="${D}"/>`,
    '百合': `<rect x="38" y="62" width="4" height="20" fill="#4caf50" rx="1"/><polygon points="40,8 48,30 70,30 52,44 58,66 40,54 22,66 28,44 10,30 32,30" fill="${P}" ${extra}/><circle cx="40" cy="38" r="4" fill="${D}"/>`,
    '郁金香': `<rect x="38" y="60" width="4" height="22" fill="#4caf50" rx="1"/><ellipse cx="40" cy="42" rx="16" ry="24" fill="${P}" ${extra}/><ellipse cx="40" cy="46" rx="10" ry="14" fill="${D}" opacity="0.35"/><path d="M24,30 Q32,20 40,30 Q48,20 56,30" fill="none" stroke="${D}" stroke-width="1.5" opacity="0.6"/>`,
    '向日葵': `<rect x="38" y="62" width="4" height="20" fill="#4caf50" rx="1"/><g fill="#ffd32a"><ellipse cx="40" cy="16" rx="5" ry="12"/><ellipse cx="40" cy="16" rx="5" ry="12" transform="rotate(30 40 40)"/><ellipse cx="40" cy="16" rx="5" ry="12" transform="rotate(60 40 40)"/><ellipse cx="40" cy="16" rx="5" ry="12" transform="rotate(90 40 40)"/><ellipse cx="40" cy="16" rx="5" ry="12" transform="rotate(120 40 40)"/><ellipse cx="40" cy="16" rx="5" ry="12" transform="rotate(150 40 40)"/><ellipse cx="40" cy="16" rx="5" ry="12" transform="rotate(180 40 40)"/><ellipse cx="40" cy="16" rx="5" ry="12" transform="rotate(210 40 40)"/><ellipse cx="40" cy="16" rx="5" ry="12" transform="rotate(240 40 40)"/><ellipse cx="40" cy="16" rx="5" ry="12" transform="rotate(270 40 40)"/><ellipse cx="40" cy="16" rx="5" ry="12" transform="rotate(300 40 40)"/><ellipse cx="40" cy="16" rx="5" ry="12" transform="rotate(330 40 40)"/></g><circle cx="40" cy="40" r="16" fill="#5D4037"/>`,
    '康乃馨': `<rect x="38" y="60" width="4" height="22" fill="#4caf50" rx="1"/><circle cx="40" cy="40" r="24" fill="${P}" opacity="0.85" ${extra}/><circle cx="33" cy="35" r="14" fill="${P}" opacity="0.7" ${extra}/><circle cx="46" cy="37" r="12" fill="${P}" opacity="0.65" ${extra}/><circle cx="38" cy="42" r="10" fill="${D}" opacity="0.5"/><circle cx="44" cy="30" r="8" fill="${P}" opacity="0.55" ${extra}/>`,
    '小雏菊': `<rect x="38" y="58" width="4" height="24" fill="#4caf50" rx="1"/><g fill="white" stroke="#e0e0e0" stroke-width="0.5"><ellipse cx="40" cy="16" rx="4" ry="12"/><ellipse cx="40" cy="16" rx="4" ry="12" transform="rotate(45 40 40)"/><ellipse cx="40" cy="16" rx="4" ry="12" transform="rotate(90 40 40)"/><ellipse cx="40" cy="16" rx="4" ry="12" transform="rotate(135 40 40)"/><ellipse cx="40" cy="16" rx="4" ry="12" transform="rotate(180 40 40)"/><ellipse cx="40" cy="16" rx="4" ry="12" transform="rotate(225 40 40)"/><ellipse cx="40" cy="16" rx="4" ry="12" transform="rotate(270 40 40)"/><ellipse cx="40" cy="16" rx="4" ry="12" transform="rotate(315 40 40)"/></g><circle cx="40" cy="40" r="10" fill="#ffc107"/>`,
    '尤加利叶': `<rect x="38" y="50" width="4" height="32" fill="#4caf50" rx="1"/><ellipse cx="30" cy="45" rx="8" ry="18" fill="#7bed9f" transform="rotate(-15 30 45)"/><ellipse cx="50" cy="42" rx="8" ry="17" fill="#6dd68a" transform="rotate(15 50 42)"/><ellipse cx="26" cy="30" rx="7" ry="15" fill="#8df0aa" transform="rotate(-30 26 30)"/><ellipse cx="54" cy="28" rx="7" ry="14" fill="#7ae095" transform="rotate(25 54 28)"/><ellipse cx="22" cy="18" rx="6" ry="12" fill="#90f5b0" transform="rotate(-40 22 18)"/>`,
    '配叶': `<rect x="38" y="50" width="4" height="32" fill="#4caf50" rx="1"/><ellipse cx="30" cy="45" rx="14" ry="22" fill="#7bed9f" transform="rotate(-20 30 45)"/><ellipse cx="50" cy="40" rx="13" ry="20" fill="#6dd68a" transform="rotate(15 50 40)"/><ellipse cx="28" cy="30" rx="10" ry="18" fill="#8df0aa" transform="rotate(-35 28 30)"/><ellipse cx="52" cy="28" rx="9" ry="16" fill="#7ae095" transform="rotate(30 52 28)"/>`
  }

  let svgBody = flowerParts[name]
  if (!svgBody) {
    const keys = ['玫瑰', '百合', '郁金香', '向日葵', '康乃馨', '配叶', '小雏菊', '尤加利叶']
    for (const key of keys) {
      if (name.includes(key)) { svgBody = flowerParts[key]; break }
    }
  }
  if (!svgBody) {
    svgBody = `<rect x="38" y="60" width="4" height="22" fill="#4caf50" rx="1"/><circle cx="40" cy="42" r="22" fill="${P}" opacity="0.85" ${extra}/><circle cx="36" cy="38" r="10" fill="${P}" opacity="0.65" ${extra}/><circle cx="37" cy="32" r="5" fill="${D}"/>`
  }

  return `<svg viewBox="0 0 80 85" xmlns="http://www.w3.org/2000/svg">${svgBody}</svg>`
}

const designItems = computed(() => {
  return items.value.map(item => {
    let pos = { x: 50, y: 50, rotation: 0, scale: 1 }
    if (item.position) {
      try { pos = { ...pos, ...JSON.parse(item.position) } } catch (e) {}
    }
    return {
      ...item,
      x: pos.x,
      y: pos.y,
      rotation: pos.rotation || 0,
      scale: pos.scale || 1
    }
  })
})

const packageClass = computed(() => {
  const name = (plan.value?.packageType || '圆形包装').replace(/\s/g, '-')
  const pinyin = { '圆形包装': 'package-circle', '心形包装': 'package-heart', '长形包装': 'package-long', '礼盒包装': 'package-gift' }
  return pinyin[name] || 'package-' + name.toLowerCase()
})

const getFlowerStyle = (item) => {
  return {
    left: item.x + 'px',
    top: item.y + 'px',
    transform: `rotate(${item.rotation || 0}deg) scale(${item.scale || 1})`
  }
}

const previewScale = computed(() => {
  const pkg = plan.value?.packageType || '圆形包装'
  const dims = { '圆形包装': [420, 420], '心形包装': [420, 450], '长形包装': [220, 440], '礼盒包装': [420, 420] }
  const [w, h] = dims[pkg] || [420, 420]
  const targetW = 360
  const targetH = 370
  return Math.min(targetW / w, targetH / h).toFixed(3)
})

const scaledW = computed(() => {
  const pkg = plan.value?.packageType || '圆形包装'
  const dims = { '圆形包装': [420, 420], '心形包装': [420, 450], '长形包装': [220, 440], '礼盒包装': [420, 420] }
  return Math.round(dims[pkg][0] * Number(previewScale.value))
})

const scaledH = computed(() => {
  const pkg = plan.value?.packageType || '圆形包装'
  const dims = { '圆形包装': [420, 420], '心形包装': [420, 450], '长形包装': [220, 440], '礼盒包装': [420, 420] }
  return Math.round(dims[pkg][1] * Number(previewScale.value))
})

onMounted(async () => {
  try {
    const id = route.params.id
    const res = await api.get(`/diy/${id}`)
    plan.value = res.data.data.bouquet || res.data.data
    items.value = res.data.data.items || []
  } catch (e) { console.error(e) }
  finally { loading.value = false }
})

const placeOrder = async () => {
  if (!form.value.receiverName || !form.value.receiverPhone || !form.value.shippingAddress) {
    alert('请填写完整的收货信息')
    return
  }
  ordering.value = true
  try {
    const userId = Number(localStorage.getItem('userId'))
    api.defaults.headers.Authorization = 'Bearer ' + localStorage.getItem('token')
    await api.post(`/diy/${route.params.id}/order`, {
      userId,
      ...form.value
    })
    alert('下单成功！')
    router.push('/user/plans')
  } catch (e) { console.error(e); alert('下单失败: ' + (e.response?.data?.message || e.message)) }
  finally { ordering.value = false }
}
</script>

<style scoped>
.plan-detail-page { max-width: 1000px; margin: 0 auto; padding: 2rem; }
.back-nav { margin-bottom: 1.5rem; }
.back-link { text-decoration: none; color: #c44569; font-size: 0.95rem; font-weight: 500; }
.back-link:hover { text-decoration: underline; }

.loading-box, .error-box { text-align: center; padding: 4rem; color: #999; font-size: 1.1rem; }

.detail-content { display: grid; grid-template-columns: 400px 1fr; gap: 2rem; }

.plan-preview-card { background: white; border-radius: 20px; padding: 1.5rem 1.5rem 2rem; text-align: center; box-shadow: 0 4px 20px rgba(0,0,0,0.08); }

.preview-bouquet-wrap {
  margin: 0 auto 1rem;
  position: relative;
  overflow: hidden;
  border-radius: 12px;
  background: #fafafa;
}

.package-background.preview-mode {
  position: absolute;
  left: 0;
  top: 0;
  margin: 0;
  min-height: auto;
}

.package-background {
  position: relative;
  min-height: 480px;
  background: #f5f5f5;
  overflow: hidden;
}

.package-background.package-circle {
  background: radial-gradient(circle at center, #fff8e6 0%, #f5f5f5 70%);
  border: 2px dashed #e0d5c0;
  border-radius: 50%;
  width: 420px;
  height: 420px;
  margin: 30px auto;
}

.package-background.package-heart {
  background: radial-gradient(ellipse at center, #ffe6f0 0%, #f5f5f5 70%);
  border: 2px dashed #e0c0d0;
  border-radius: 40% 40% 45% 45% / 45% 45% 50% 50%;
  width: 420px;
  height: 450px;
  margin: 15px auto;
}

.package-background.package-long {
  background: linear-gradient(to right, #fff8e6 0%, #f5f5f5 95%);
  border: 2px dashed #e0d5c0;
  border-radius: 20px;
  width: 220px;
  height: 440px;
  margin: 20px auto;
}

.package-background.package-gift {
  background: repeating-linear-gradient(45deg, #ffe6f0 0px, #ffe6f0 20px, #f5e6f0 20px, #f5e6f0 40px);
  border: 2px dashed #e0c0d0;
}

.design-flower {
  position: absolute;
  width: 90px;
  height: 140px;
  user-select: none;
  pointer-events: none;
  filter: drop-shadow(0 2px 3px rgba(0,0,0,0.08));
}

.flower-shape {
  width: 100%;
  height: 100%;
}

.flower-shape :deep(svg) {
  width: 100%;
  height: 100%;
  display: block;
}

.plan-preview-card h2 { font-size: 1.4rem; color: #333; margin-bottom: 0.5rem; }
.preview-pkg { color: #888; margin-bottom: 0.5rem; }
.preview-price { font-size: 1.8rem; font-weight: bold; color: #c44569; margin-bottom: 0.5rem; }
.preview-status { font-size: 0.85rem; color: #2e7d32; background: #e8f5e9; display: inline-block; padding: 0.2rem 0.8rem; border-radius: 10px; }

.detail-right { }
.detail-right h3 { font-size: 1.2rem; color: #333; margin-bottom: 1rem; }

.items-list { background: white; border-radius: 15px; padding: 1rem 1.5rem; box-shadow: 0 2px 10px rgba(0,0,0,0.05); margin-bottom: 1.5rem; }
.item-row { display: flex; align-items: center; padding: 0.7rem 0; border-bottom: 1px solid #f0f0f0; }
.item-row:last-child { border-bottom: none; }
.item-emoji { font-size: 1.5rem; margin-right: 0.8rem; }
.item-name { flex: 1; font-size: 0.95rem; }
.item-qty { font-size: 0.9rem; color: #c44569; font-weight: bold; }

.order-section { background: white; border-radius: 15px; padding: 1.5rem; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
.order-section h3 { margin-bottom: 1rem; }
.order-form { display: flex; flex-direction: column; gap: 0.6rem; }
.order-input { padding: 0.7rem; border: 1px solid #e0e0e0; border-radius: 8px; font-size: 0.9rem; font-family: inherit; }
.order-input:focus { outline: none; border-color: #c44569; }
.order-btn { width: 100%; background: linear-gradient(135deg, #2e7d32 0%, #1b5e20 100%); color: white; border: none; padding: 0.9rem; border-radius: 10px; font-size: 1.05rem; cursor: pointer; transition: all 0.3s; margin-top: 0.3rem; }
.order-btn:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 5px 15px rgba(46,125,50,0.4); }
.order-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.ordered-hint { text-align: center; padding: 2rem; color: #2e7d32; font-size: 1.1rem; }
</style>