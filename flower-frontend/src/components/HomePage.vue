<template>
  <div class="home-page">
    <div class="hero-section">
      <div class="hero-content">
        <h1>欢迎来到花卉销售管理系统</h1>
        <p>探索美丽花卉，打造专属花束</p>
        <button class="cta-btn" @click="$router.push('/user/diy')">
          开始DIY花束
        </button>
      </div>
    </div>

    <div class="categories-section">
      <h2>花卉分类</h2>
      <div class="loading-box" v-if="categoriesLoading">加载中...</div>
      <div class="categories-grid" v-else>
        <div class="category-card all-btn" :class="{ active: !selectedCategory }" @click="showAll">
          <span class="category-icon">🏵️</span>
          <h3>全部</h3>
          <p>所有花卉</p>
        </div>
        <div v-for="cat in categories" :key="cat.id" class="category-card" :class="{ active: selectedCategory === cat.id }" @click="filterByCategory(cat.id)">
          <span class="category-icon">{{ getCategoryEmoji(cat.name) }}</span>
          <h3>{{ cat.name }}</h3>
          <p>{{ cat.description || '精选花卉' }}</p>
        </div>
      </div>
    </div>

    <div class="products-section">
      <h2>{{ selectedCategory ? selectedCategoryName + ' · 热销花卉' : '热门花卉' }}</h2>
      <div class="loading-box" v-if="flowersLoading">加载中...</div>
      <div class="products-grid" v-else-if="flowers.length > 0">
        <div v-for="f in flowers" :key="f.id" class="product-card">
          <div class="product-image">
            <img v-if="f.imageUrl" :src="f.imageUrl" :alt="f.name" class="flower-img" />
            <span v-else class="flower-emoji">{{ getFlowerEmoji(f.categoryName) }}</span>
          </div>
          <h3>{{ f.name }}</h3>
          <p class="desc">{{ f.description?.substring(0, 20) || '' }}</p>
          <p class="price">¥{{ f.price?.toFixed(2) }}</p>
          <p class="stock">库存: {{ f.stock }}</p>
          <button class="add-cart-btn" @click="addToOrder(f)">加入购物车</button>
        </div>
      </div>
      <div class="empty-box" v-else>
        <p>暂无花卉数据</p>
      </div>
    </div>

    <div v-if="cart.length > 0" class="cart-bar">
      <span>🛒 已选 {{ cart.length }} 种花卉</span>
      <span>合计 ¥{{ totalPrice.toFixed(2) }}</span>
      <button class="order-btn" @click="showOrderForm = true">去下单</button>
      <button class="clear-btn" @click="cart = []">清空</button>
    </div>

    <div v-if="showOrderForm" class="form-overlay">
      <div class="form-card">
        <h3>确认订单</h3>
        <div class="cart-items">
          <div v-for="(item, idx) in cart" :key="idx" class="cart-item">
            <span>{{ item.name }}</span>
            <span>x{{ item.qty }}</span>
            <span>¥{{ (item.price * item.qty).toFixed(2) }}</span>
          </div>
        </div>
        <div class="form-group"><label>收货人</label><input v-model="receiverName" /></div>
        <div class="form-group"><label>手机号</label><input v-model="receiverPhone" /></div>
        <div class="form-group"><label>收货地址</label><input v-model="shippingAddress" /></div>
        <p class="total">总计: ¥{{ totalPrice.toFixed(2) }}</p>
        <div class="form-actions">
          <button class="cancel-btn" @click="showOrderForm = false">取消</button>
          <button class="save-btn" @click="submitOrder" :disabled="orderLoading">
            {{ orderLoading ? '提交中...' : '确认下单' }}
          </button>
        </div>
        <div class="order-msg" v-if="orderMsg" :class="orderOk ? 'success' : 'error'">{{ orderMsg }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '../api'

const categories = ref([])
const flowers = ref([])
const categoriesLoading = ref(false)
const flowersLoading = ref(false)
const selectedCategory = ref(null)
const selectedCategoryName = ref('')

const cart = ref([])
const showOrderForm = ref(false)
const receiverName = ref('')
const receiverPhone = ref('')
const shippingAddress = ref('')
const orderLoading = ref(false)
const orderMsg = ref('')
const orderOk = ref(false)

const totalPrice = computed(() => cart.value.reduce((s, i) => s + i.price * i.qty, 0))

onMounted(async () => {
  if (!localStorage.getItem('token')) {
    window.location.href = '/login'
    return
  }
  categoriesLoading.value = true
  flowersLoading.value = true
  try {
    const [catRes, fRes] = await Promise.all([
      api.get('/flower/category/list'),
      api.get('/flower/list')
    ])
    categories.value = catRes.data.data || []
    flowers.value = fRes.data.data || []
  } catch (e) {
    if (e.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userType')
      localStorage.removeItem('userId')
      window.location.href = '/login'
    }
    console.error(e)
  }
  finally {
    categoriesLoading.value = false
    flowersLoading.value = false
  }
})

const filterByCategory = async (catId) => {
  selectedCategory.value = catId
  const cat = categories.value.find(c => c.id === catId)
  selectedCategoryName.value = cat?.name || ''
  flowersLoading.value = true
  try {
    const res = await api.get('/flower/list', { params: { categoryId: catId } })
    flowers.value = res.data.data || []
  } catch (e) { console.error(e) }
  finally { flowersLoading.value = false }
}

const showAll = async () => {
  selectedCategory.value = null
  selectedCategoryName.value = ''
  flowersLoading.value = true
  try {
    const res = await api.get('/flower/list')
    flowers.value = res.data.data || []
  } catch (e) { console.error(e) }
  finally { flowersLoading.value = false }
}

const addToOrder = (f) => {
  const exist = cart.value.find(i => i.id === f.id)
  if (exist) { exist.qty++ }
  else { cart.value.push({ id: f.id, name: f.name, price: f.price, qty: 1 }) }
}

const submitOrder = async () => {
  orderMsg.value = ''
  orderLoading.value = true
  try {
    const items = cart.value.map(i => ({ flowerId: i.id, quantity: i.qty }))
    await api.post('/order', {
      userId: Number(localStorage.getItem('userId')),
      items,
      receiverName: receiverName.value,
      receiverPhone: receiverPhone.value,
      shippingAddress: shippingAddress.value
    })
    orderOk.value = true
    orderMsg.value = '下单成功！'
    cart.value = []
    setTimeout(() => { showOrderForm.value = false; orderMsg.value = '' }, 2000)
  } catch (e) {
    orderOk.value = false
    orderMsg.value = e.response?.data?.message || '下单失败'
  } finally { orderLoading.value = false }
}

const getFlowerEmoji = (cat) => {
  const m = { '玫瑰': '🌹', '百合': '💮', '郁金香': '🌷', '向日葵': '🌻', '康乃馨': '🌺', '配叶': '🌿' }
  return m[cat] || '🌸'
}

const getCategoryEmoji = (name) => {
  const m = { '玫瑰': '🌹', '百合': '💮', '郁金香': '🌷', '向日葵': '🌻', '康乃馨': '🌺', '配叶': '🌿' }
  return m[name] || '🌸'
}
</script>

<style scoped>
.home-page { max-width: 1200px; margin: 0 auto; }

.hero-section {
  background: linear-gradient(135deg, #ff6b9d 0%, #c44569 100%);
  border-radius: 20px; padding: 4rem; text-align: center; color: white; margin-bottom: 2rem;
}

.hero-content h1 { font-size: 2.5rem; margin-bottom: 1rem; }
.hero-content p { font-size: 1.2rem; margin-bottom: 2rem; opacity: 0.9; }

.cta-btn {
  background: white; color: #c44569; border: none; padding: 1rem 3rem;
  font-size: 1.1rem; border-radius: 30px; cursor: pointer; transition: all 0.3s;
}

.cta-btn:hover { transform: translateY(-3px); box-shadow: 0 5px 20px rgba(0,0,0,0.2); }

.categories-section, .products-section { margin-bottom: 2rem; }
.categories-section h2, .products-section h2 { font-size: 1.8rem; margin-bottom: 1.5rem; color: #333; }

.categories-grid {
  display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 1rem;
}

.category-card { background: white; border-radius: 15px; padding: 1.5rem; text-align: center; box-shadow: 0 2px 10px rgba(0,0,0,0.05); cursor: pointer; transition: all 0.3s; border: 2px solid transparent; }
.category-card:hover { transform: translateY(-5px); box-shadow: 0 5px 20px rgba(0,0,0,0.1); }
.category-card.active { border-color: #c44569; background: #fff0f5; }
.category-icon { font-size: 2.5rem; display: block; margin-bottom: 0.5rem; }
.category-card h3 { font-size: 1.2rem; margin-bottom: 0.5rem; color: #333; }
.category-card p { color: #666; font-size: 0.9rem; }

.products-grid {
  display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1.5rem;
}

.product-card {
  background: white; border-radius: 15px; padding: 1.5rem; text-align: center;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05); transition: all 0.3s;
}

.product-card:hover { transform: translateY(-5px); box-shadow: 0 5px 20px rgba(0,0,0,0.1); }

.product-image {
  width: 140px; height: 140px; margin: 0 auto 1rem;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4efe9 100%);
  border-radius: 12px; display: flex; align-items: center; justify-content: center;
  overflow: hidden;
}

.flower-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 12px;
}

.flower-emoji { font-size: 2.5rem; }
.product-card h3 { font-size: 1.1rem; margin-bottom: 0.3rem; color: #333; }
.desc { font-size: 0.8rem; color: #999; margin-bottom: 0.5rem; }
.price { font-size: 1.3rem; font-weight: bold; color: #c44569; }
.stock { font-size: 0.85rem; color: #888; margin-bottom: 0.5rem; }

.add-cart-btn {
  background: linear-gradient(135deg, #ff6b9d 0%, #c44569 100%);
  color: white; border: none; padding: 0.5rem 1.2rem; border-radius: 20px; cursor: pointer;
}

.add-cart-btn:hover { transform: scale(1.05); }
.loading-box, .empty-box { text-align: center; padding: 3rem; color: #999; }

.cart-bar {
  position: fixed; bottom: 0; left: 0; right: 0; background: white;
  padding: 1rem 2rem; display: flex; align-items: center; gap: 2rem;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.1); z-index: 50;
}

.order-btn {
  background: linear-gradient(135deg, #ff6b9d 0%, #c44569 100%);
  color: white; border: none; padding: 0.6rem 1.5rem; border-radius: 20px; cursor: pointer;
}

.clear-btn { background: #f5f5f5; border: none; padding: 0.6rem 1rem; border-radius: 20px; cursor: pointer; }

.form-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.3); display: flex; align-items: center; justify-content: center; z-index: 100; }
.form-card { background: white; border-radius: 12px; padding: 2rem; width: 450px; max-height: 80vh; overflow-y: auto; }
.form-card h3 { margin-bottom: 1rem; }
.cart-items { margin-bottom: 1rem; }
.cart-item { display: flex; justify-content: space-between; padding: 0.5rem 0; border-bottom: 1px solid #f0f0f0; }
.form-group { margin-bottom: 0.8rem; }
.form-group label { display: block; margin-bottom: 0.3rem; color: #555; font-size: 0.85rem; }
.form-group input { width: 100%; padding: 0.5rem; border: 1px solid #e0e0e0; border-radius: 6px; }
.total { font-size: 1.2rem; font-weight: bold; color: #c44569; margin: 1rem 0; }
.form-actions { display: flex; justify-content: flex-end; gap: 0.5rem; }
.cancel-btn, .save-btn { padding: 0.5rem 1.2rem; border: none; border-radius: 8px; cursor: pointer; }
.cancel-btn { background: #f0f0f0; }
.save-btn { background: linear-gradient(135deg, #ff6b9d 0%, #c44569 100%); color: white; }
.order-msg { margin-top: 0.5rem; padding: 0.5rem; border-radius: 6px; text-align: center; }
.order-msg.success { background: #f6ffed; color: #52c41a; }
.order-msg.error { background: #fff2f0; color: #ff4d4f; }
</style>