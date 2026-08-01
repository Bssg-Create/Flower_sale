<template>
  <div class="home-page">
    <section class="hero-section">
      <div class="hero-content">
        <p class="hero-kicker">鲜花选购与 DIY 定制</p>
        <h1>把今天的心意，交给一束好花。</h1>
        <p class="hero-subtitle">从单支花材到专属花束，自由挑选、搭配并完成下单。</p>
        <button class="cta-btn" @click="$router.push('/user/diy')">
          开始设计花束
        </button>
      </div>
      <div class="hero-visual" aria-hidden="true">
        <span class="hero-disc"></span>
        <img class="hero-flower hero-flower-main" :src="'/images/diy/pink-lily.webp'" alt="" />
        <img class="hero-flower hero-flower-side" :src="'/images/diy/pink-rose.webp'" alt="" />
        <img class="hero-flower hero-flower-leaf" :src="'/images/diy/eucalyptus.webp'" alt="" />
      </div>
    </section>

    <section class="categories-section">
      <div class="section-header">
        <div>
          <h2>按花材挑选</h2>
          <p>先选喜欢的花，再决定如何组合。</p>
        </div>
      </div>
      <div class="loading-box" v-if="categoriesLoading">加载中...</div>
      <div class="categories-grid" v-else>
        <button type="button" class="category-card all-btn" :class="{ active: !selectedCategory }" @click="showAll">
          <strong>全部花材</strong>
          <span>查看完整花材架</span>
        </button>
        <button type="button" v-for="cat in categories" :key="cat.id" class="category-card" :class="{ active: selectedCategory === cat.id }" @click="filterByCategory(cat.id)">
          <strong>{{ cat.name }}</strong>
          <span>{{ cat.description || '精选花卉' }}</span>
        </button>
      </div>
    </section>

    <section class="products-section">
      <div class="section-header product-heading">
        <div>
          <h2>{{ selectedCategory ? selectedCategoryName : '本店花材' }}</h2>
          <p>{{ selectedCategory ? '当前分类中的在售花材' : '挑一支喜欢的花，加入你的购物清单。' }}</p>
        </div>
        <span class="result-count">{{ flowers.length }} 种在售</span>
      </div>
      <div class="loading-box" v-if="flowersLoading">加载中...</div>
      <div class="products-grid" v-else-if="flowers.length > 0">
        <div v-for="f in flowers" :key="f.id" class="product-card">
          <div class="product-image">
            <img v-if="f.imageUrl" :src="f.imageUrl" :alt="f.name" class="flower-img" />
            <span v-else class="flower-emoji">{{ getFlowerEmoji(f.categoryName) }}</span>
          </div>
          <div class="product-copy">
            <span class="product-category">{{ f.categoryName || '鲜花' }}</span>
            <h3>{{ f.name }}</h3>
            <p class="desc">{{ f.description?.substring(0, 34) || '门店精选新鲜花材' }}</p>
            <div class="product-meta">
              <div>
                <p class="price">¥{{ f.price?.toFixed(2) }}</p>
                <p class="stock">库存 {{ f.stock }}</p>
              </div>
              <button class="add-cart-btn" @click="addToOrder(f)">加入清单</button>
            </div>
          </div>
        </div>
      </div>
      <div class="empty-box" v-else>
        <p>暂无花卉数据</p>
      </div>
    </section>

    <div v-if="cart.length > 0" class="cart-bar">
      <div class="cart-summary">
        <strong>购物清单</strong>
        <span>已选 {{ cart.length }} 种花材</span>
      </div>
      <div class="cart-total"><small>合计</small>¥{{ totalPrice.toFixed(2) }}</div>
      <div class="cart-actions">
        <button class="clear-btn" @click="cart = []">清空</button>
        <button class="order-btn" @click="showOrderForm = true">确认订单</button>
      </div>
    </div>

    <div v-if="showOrderForm" class="form-overlay">
      <div class="form-card">
        <div class="modal-header">
          <div>
            <span>购物清单</span>
            <h3>确认订单</h3>
          </div>
          <button type="button" class="modal-close" aria-label="关闭" @click="showOrderForm = false">×</button>
        </div>
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
.home-page { max-width: 1320px; margin: 0 auto; padding-bottom: 6rem; }

.hero-section {
  position: relative;
  min-height: 420px;
  overflow: hidden;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(360px, 0.82fr);
  align-items: center;
  margin-bottom: 3.5rem;
  border-radius: 24px;
  color: #f8fbf8;
  background: linear-gradient(138deg, #29493f, #3f6a5d);
  box-shadow: 0 22px 54px rgba(39, 70, 61, 0.16);
}

.hero-content { position: relative; z-index: 2; padding: clamp(2.4rem, 5vw, 5rem); }
.hero-kicker { margin-bottom: 0.8rem; color: #cfe0d6; font-size: 0.9rem; font-weight: 700; }
.hero-content h1 { max-width: 11ch; font-size: clamp(2.7rem, 4.8vw, 4.6rem); line-height: 1.08; letter-spacing: -0.038em; text-wrap: balance; }
.hero-subtitle { max-width: 29rem; margin: 1.35rem 0 1.8rem; color: #d9e6de; font-size: 1rem; }

.cta-btn {
  min-height: 46px;
  padding: 0.72rem 1.25rem;
  border: 0;
  border-radius: 10px;
  color: var(--color-brand-strong);
  background: #ffffff;
  box-shadow: 0 10px 22px rgba(12, 32, 26, 0.18);
  font-size: 0.94rem;
  font-weight: 750;
  cursor: pointer;
}

.cta-btn:hover { transform: translateY(-2px); box-shadow: 0 14px 28px rgba(12, 32, 26, 0.22); }

.hero-visual { position: relative; align-self: stretch; min-height: 420px; }
.hero-disc { position: absolute; right: 6%; bottom: -10%; width: 83%; aspect-ratio: 1; border-radius: 50%; background: #eef3ee; box-shadow: 0 24px 60px rgba(15, 34, 28, 0.2); }
.hero-flower { position: absolute; object-fit: contain; filter: drop-shadow(0 22px 18px rgba(21, 42, 35, 0.2)); transform-origin: 50% 100%; }
.hero-flower-main { right: 23%; bottom: 0; width: 39%; transform: rotate(4deg); }
.hero-flower-side { right: 52%; bottom: -2%; width: 31%; transform: rotate(-18deg); }
.hero-flower-leaf { right: 4%; bottom: -1%; width: 36%; transform: rotate(20deg); }

.categories-section, .products-section { margin-bottom: 3.5rem; }
.section-header { display: flex; justify-content: space-between; align-items: end; gap: 1rem; margin-bottom: 1.25rem; }
.section-header h2 { color: var(--color-ink); font-size: clamp(1.55rem, 2.4vw, 2rem); letter-spacing: -0.025em; }
.section-header p { margin-top: 0.25rem; color: var(--color-ink-soft); font-size: 0.92rem; }
.result-count { color: var(--color-muted); font-size: 0.84rem; }

.categories-grid { display: flex; gap: 0.75rem; overflow-x: auto; padding: 0.2rem 0.1rem 0.75rem; scrollbar-width: thin; }
.category-card {
  min-width: 170px;
  max-width: 210px;
  min-height: 88px;
  padding: 0.9rem 1rem;
  text-align: left;
  border: 1px solid var(--color-line);
  border-radius: 14px;
  color: var(--color-ink);
  background: rgba(255,255,255,0.72);
  cursor: pointer;
}
.category-card strong, .category-card span { display: block; }
.category-card strong { font-size: 0.95rem; }
.category-card span { margin-top: 0.3rem; color: var(--color-muted); font-size: 0.76rem; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.category-card:hover { border-color: rgba(166,63,95,0.3); background: #ffffff; transform: translateY(-2px); }
.category-card.active { color: #ffffff; border-color: var(--color-brand); background: var(--color-brand); box-shadow: 0 10px 24px rgba(166,63,95,0.2); }
.category-card.active span { color: #f8e9ee; }

.products-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 1.1rem; }
.product-card { overflow: hidden; border-radius: var(--radius-card); background: var(--color-surface); box-shadow: 0 12px 30px rgba(42,70,61,0.075); }
.product-card:hover { transform: translateY(-4px); box-shadow: 0 18px 38px rgba(42,70,61,0.12); }
.product-image { height: 225px; display: flex; align-items: center; justify-content: center; overflow: hidden; background: linear-gradient(160deg, #edf2ee, #f7f3f4); }
.flower-img { width: 100%; height: 100%; object-fit: cover; transition: transform 420ms var(--ease-out); }
.product-card:hover .flower-img { transform: scale(1.035); }
.flower-emoji { font-size: 3rem; }
.product-copy { padding: 1rem 1rem 1.1rem; }
.product-category { color: var(--color-brand); font-size: 0.75rem; font-weight: 700; }
.product-card h3 { margin: 0.18rem 0 0.35rem; color: var(--color-ink); font-size: 1.08rem; }
.desc { min-height: 2.7em; color: var(--color-ink-soft); font-size: 0.82rem; line-height: 1.55; }
.product-meta { display: flex; justify-content: space-between; align-items: end; gap: 0.75rem; margin-top: 0.9rem; }
.price { color: var(--color-brand-strong); font-size: 1.25rem; font-weight: 800; line-height: 1.2; }
.stock { margin-top: 0.15rem; color: var(--color-muted); font-size: 0.74rem; }
.add-cart-btn { min-height: 38px; padding: 0.48rem 0.76rem; border: 0; border-radius: 9px; color: #ffffff; background: var(--color-forest); font-size: 0.82rem; font-weight: 700; cursor: pointer; white-space: nowrap; }
.add-cart-btn:hover { background: #243f37; transform: translateY(-1px); }
.loading-box, .empty-box { min-height: 180px; display: grid; place-items: center; border-radius: var(--radius-card); color: var(--color-muted); background: rgba(255,255,255,0.62); }

.cart-bar { position: fixed; z-index: 50; right: clamp(1rem, 3vw, 2.5rem); bottom: 1.4rem; left: clamp(1rem, 3vw, 2.5rem); max-width: 920px; margin: auto; padding: 0.8rem 0.9rem 0.8rem 1.15rem; display: flex; align-items: center; gap: 1rem; border: 1px solid rgba(255,255,255,0.46); border-radius: var(--radius-card); color: #ffffff; background: rgba(31,45,41,0.96); box-shadow: var(--shadow-float); }
.cart-summary { min-width: 0; margin-right: auto; }
.cart-summary strong, .cart-summary span { display: block; }
.cart-summary strong { font-size: 0.9rem; }
.cart-summary span { color: #bdcbc4; font-size: 0.74rem; }
.cart-total { color: #ffffff; font-size: 1.12rem; font-weight: 800; white-space: nowrap; }
.cart-total small { margin-right: 0.4rem; color: #bdcbc4; font-size: 0.72rem; font-weight: 500; }
.cart-actions { display: flex; gap: 0.5rem; }
.order-btn, .clear-btn { min-height: 40px; border: 0; border-radius: 9px; cursor: pointer; font-size: 0.82rem; font-weight: 700; }
.order-btn { padding: 0.55rem 1rem; color: #ffffff; background: var(--color-brand); }
.order-btn:hover { background: var(--color-brand-strong); }
.clear-btn { padding: 0.55rem 0.75rem; color: #dce5e0; background: rgba(255,255,255,0.1); }

.form-overlay { position: fixed; inset: 0; z-index: 100; display: flex; align-items: center; justify-content: center; padding: 1rem; background: rgba(20,35,30,0.56); backdrop-filter: blur(6px); }
.form-card { width: min(100%, 480px); max-height: 86vh; overflow-y: auto; padding: 1.5rem; border-radius: var(--radius-card); background: var(--color-surface); box-shadow: var(--shadow-float); }
.modal-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1rem; }
.modal-header span { color: var(--color-brand); font-size: 0.78rem; font-weight: 700; }
.modal-header h3 { color: var(--color-ink); font-size: 1.45rem; }
.modal-close { width: 36px; height: 36px; border: 0; border-radius: 9px; color: var(--color-ink-soft); background: var(--color-surface-soft); cursor: pointer; font-size: 1.25rem; }
.cart-items { margin-bottom: 1rem; padding: 0.5rem 0.8rem; border-radius: 12px; background: var(--color-surface-soft); }
.cart-item { display: grid; grid-template-columns: 1fr auto auto; gap: 1rem; padding: 0.55rem 0; color: var(--color-ink-soft); font-size: 0.86rem; }
.cart-item + .cart-item { border-top: 1px solid rgba(49,79,70,0.1); }
.form-group { margin-bottom: 0.8rem; }
.form-group label { display: block; margin-bottom: 0.38rem; color: var(--color-ink); font-size: 0.84rem; font-weight: 650; }
.form-group input { width: 100%; min-height: 44px; padding: 0.65rem 0.75rem; border: 1px solid var(--color-line); border-radius: var(--radius-control); color: var(--color-ink); background: #fbfcfb; outline: none; }
.form-group input:focus { border-color: var(--color-brand); box-shadow: 0 0 0 3px rgba(166,63,95,0.1); }
.total { margin: 1rem 0; color: var(--color-brand-strong); font-size: 1.2rem; font-weight: 800; }
.form-actions { display: flex; justify-content: flex-end; gap: 0.6rem; }
.cancel-btn, .save-btn { min-height: 42px; padding: 0.55rem 1rem; border: 0; border-radius: 9px; cursor: pointer; font-weight: 700; }
.cancel-btn { color: var(--color-ink-soft); background: var(--color-surface-soft); }
.save-btn { color: #ffffff; background: var(--color-brand); }
.order-msg { margin-top: 0.7rem; padding: 0.65rem; border-radius: 9px; text-align: center; font-size: 0.84rem; }
.order-msg.success { color: #205f46; background: #e9f5ef; }
.order-msg.error { color: #8f2f3a; background: #fbedef; }

@media (max-width: 1120px) {
  .products-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); }
}

@media (max-width: 820px) {
  .hero-section { grid-template-columns: 1fr; min-height: 560px; }
  .hero-content { align-self: start; }
  .hero-visual { position: absolute; right: 0; bottom: 0; width: 70%; min-height: 300px; opacity: 0.82; }
  .products-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}

@media (max-width: 560px) {
  .hero-section { min-height: 520px; margin-bottom: 2.5rem; border-radius: 18px; }
  .hero-content { padding: 1.6rem; }
  .hero-content h1 { font-size: 2.45rem; }
  .hero-visual { width: 92%; }
  .section-header { align-items: flex-start; }
  .product-heading { display: block; }
  .result-count { display: block; margin-top: 0.45rem; }
  .products-grid { grid-template-columns: 1fr; }
  .product-card { display: grid; grid-template-columns: 128px 1fr; }
  .product-image { height: 100%; min-height: 190px; }
  .product-meta { align-items: center; }
  .cart-bar { align-items: stretch; flex-wrap: wrap; }
  .cart-summary { flex: 1 1 45%; }
  .cart-actions { width: 100%; }
  .cart-actions button { flex: 1; }
}
</style>
