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
          <BouquetCanvas
            :model-value="designItems"
            :package-type="plan.packageType || '圆形包装'"
            :message="messageText"
            readonly
            compact
          />
          <h2>{{ plan.name }}</h2>
          <p class="preview-pkg">包装: {{ plan.packageType || '无' }}</p>
          <p class="preview-price">¥{{ Number(plan.totalPrice || 0).toFixed(2) }}</p>
          <p class="preview-status">{{ statusText(plan.status) }}</p>
        </div>
      </div>

      <div class="detail-right">
        <section class="info-card">
          <h3>花束内容</h3>
          <div class="items-list">
            <div v-for="(item, index) in items" :key="index" class="item-row">
              <img :src="getFlowerImage(item.flowerName)" :alt="item.flowerName" class="item-thumb" />
              <span class="item-name">{{ item.flowerName }}</span>
              <span class="item-qty">x{{ item.quantity }}</span>
            </div>
          </div>
        </section>

        <section class="info-card order-section" v-if="plan.status !== 'ordered'">
          <h3>立即购买</h3>
          <div class="order-form">
            <input v-model="form.receiverName" placeholder="收货人姓名" class="order-input" />
            <input v-model="form.receiverPhone" placeholder="收货人电话" class="order-input" />
            <textarea v-model="form.shippingAddress" placeholder="收货地址" class="order-input" rows="2"></textarea>
            <button class="order-btn" @click="placeOrder" :disabled="ordering">
              {{ ordering ? '下单中...' : `立即下单 ¥${Number(plan.totalPrice || 0).toFixed(2)}` }}
            </button>
          </div>
        </section>
        <div class="ordered-hint" v-else>
          <p>✅ 该方案已下单</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BouquetCanvas from './BouquetCanvas.vue'
import api from '../api/index.js'

const route = useRoute()
const router = useRouter()
const plan = ref(null)
const items = ref([])
const loading = ref(true)
const ordering = ref(false)
const messageText = ref('')

const form = ref({
  receiverName: '',
  receiverPhone: '',
  shippingAddress: ''
})

const flowerProfiles = [
  { key: '红玫瑰', asset: 'red-rose.webp', photoWidth: 126, photoHeight: 226 },
  { key: '白玫瑰', asset: 'white-rose.webp', photoWidth: 126, photoHeight: 226 },
  { key: '粉玫瑰', asset: 'pink-rose.webp', photoWidth: 126, photoHeight: 226 },
  { key: '粉郁金香', asset: 'pink-tulip.webp', photoWidth: 120, photoHeight: 226 },
  { key: '黄郁金香', asset: 'yellow-tulip.webp', photoWidth: 122, photoHeight: 236 },
  { key: '白百合', asset: 'white-lily.webp', photoWidth: 132, photoHeight: 228 },
  { key: '粉百合', asset: 'pink-lily.webp', photoWidth: 154, photoHeight: 238 },
  { key: '向日葵', asset: 'sunflower.webp', photoWidth: 136, photoHeight: 226 },
  { key: '康乃馨', asset: 'pink-carnation.webp', photoWidth: 118, photoHeight: 222 },
  { key: '小雏菊', asset: 'white-daisy.webp', photoWidth: 128, photoHeight: 226 },
  { key: '尤加利叶', asset: 'eucalyptus.webp', photoWidth: 132, photoHeight: 220 }
]

const statusText = (s) => {
  if (s === 'ordered') return '已下单'
  if (s === 'saved' || s === '1') return '已保存'
  return '草稿'
}

const getProfile = (name) => flowerProfiles.find(profile => (name || '').includes(profile.key)) || null

const getFlowerImage = (name) => {
  const profile = getProfile(name)
  return profile ? `/images/diy/${profile.asset}` : '/images/diy/red-rose.webp'
}

const parsePosition = (item, index) => {
  const defaults = {
    x: 238 + (index % 5 - 2) * 26,
    y: 134 + Math.floor(index / 5) * 18,
    rotation: 0,
    scale: 1,
    z: 20 + index,
    bend: 0,
    tilt: 0
  }
  if (!item.position) return defaults
  try {
    return { ...defaults, ...JSON.parse(item.position) }
  } catch (e) {
    return defaults
  }
}

const designItems = computed(() => {
  return items.value.map((item, index) => {
    const pos = parsePosition(item, index)
    const profile = getProfile(item.flowerName)
    return {
      ...item,
      uid: `detail-${index}`,
      name: item.flowerName,
      assetUrl: profile ? `/images/diy/${profile.asset}` : '',
      photoWidth: pos.photoWidth || profile?.photoWidth || 132,
      photoHeight: pos.photoHeight || profile?.photoHeight || 230,
      x: pos.x,
      y: pos.y,
      rotation: pos.rotation || 0,
      scale: pos.scale || 1,
      z: pos.z ?? Math.round(pos.y),
      bend: pos.bend || 0,
      tilt: pos.tilt || 0
    }
  })
})

onMounted(async () => {
  try {
    const id = route.params.id
    const res = await api.get(`/diy/${id}`)
    plan.value = res.data.data.bouquet || res.data.data
    items.value = res.data.data.items || []
    const firstWithMessage = items.value.find(item => {
      if (!item.position) return false
      try {
        return Boolean(JSON.parse(item.position).message)
      } catch (e) {
        return false
      }
    })
    if (firstWithMessage) {
      messageText.value = JSON.parse(firstWithMessage.position).message
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
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
  } catch (e) {
    console.error(e)
    alert('下单失败: ' + (e.response?.data?.message || e.message))
  } finally {
    ordering.value = false
  }
}
</script>

<style scoped>
.plan-detail-page {
  max-width: 1120px;
  margin: 0 auto;
  padding: 2rem;
}

.back-nav {
  margin-bottom: 1.5rem;
}

.back-link {
  text-decoration: none;
  color: #c44569;
  font-size: 0.95rem;
  font-weight: 600;
}

.loading-box,
.error-box {
  text-align: center;
  padding: 4rem;
  color: #999;
  font-size: 1.1rem;
}

.detail-content {
  display: grid;
  grid-template-columns: minmax(420px, 1fr) minmax(300px, 360px);
  gap: 1.5rem;
  align-items: start;
}

.plan-preview-card,
.info-card {
  background: white;
  border-radius: 18px;
  box-shadow: 0 4px 18px rgba(0,0,0,0.06);
}

.plan-preview-card {
  padding: 1.25rem 1.25rem 1.6rem;
  text-align: center;
}

.plan-preview-card h2 {
  color: #333;
  font-size: 1.35rem;
  margin-top: 1rem;
  margin-bottom: 0.45rem;
}

.preview-pkg {
  color: #888;
  margin-bottom: 0.45rem;
}

.preview-price {
  color: #c44569;
  font-size: 1.8rem;
  font-weight: 700;
  margin-bottom: 0.45rem;
}

.preview-status {
  display: inline-block;
  border-radius: 999px;
  padding: 0.25rem 0.85rem;
  color: #2e7d32;
  background: #e8f5e9;
  font-size: 0.85rem;
}

.info-card {
  padding: 1.25rem;
  margin-bottom: 1.2rem;
}

.info-card h3 {
  color: #333;
  font-size: 1.12rem;
  margin-bottom: 1rem;
}

.item-row {
  display: grid;
  grid-template-columns: 44px 1fr auto;
  align-items: center;
  gap: 0.75rem;
  padding: 0.7rem 0;
  border-bottom: 1px solid #f2f2f2;
}

.item-row:last-child {
  border-bottom: none;
}

.item-thumb {
  width: 44px;
  height: 44px;
  object-fit: contain;
  border-radius: 12px;
  background: #fff7fa;
}

.item-name {
  color: #333;
  font-size: 0.95rem;
}

.item-qty {
  color: #c44569;
  font-weight: 700;
}

.order-form {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
}

.order-input {
  padding: 0.72rem;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  font-size: 0.9rem;
  font-family: inherit;
}

.order-input:focus {
  outline: none;
  border-color: #c44569;
}

.order-btn {
  width: 100%;
  border: none;
  border-radius: 12px;
  padding: 0.9rem;
  color: white;
  background: linear-gradient(135deg, #2e7d32 0%, #1b5e20 100%);
  cursor: pointer;
  transition: all 0.2s;
}

.order-btn:hover:not(:disabled) {
  transform: translateY(-2px);
}

.order-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.ordered-hint {
  text-align: center;
  padding: 2rem;
  color: #2e7d32;
  font-size: 1.1rem;
}

@media (max-width: 900px) {
  .plan-detail-page {
    padding: 1rem;
  }

  .detail-content {
    grid-template-columns: 1fr;
  }
}
</style>
