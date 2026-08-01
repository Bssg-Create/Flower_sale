<template>
  <div class="home-page">
    <section class="hero-section">
      <div class="hero-content">
        <p class="section-kicker">当季花礼与自由定制</p>
        <h1>挑一束鲜花，送出恰到好处的心意。</h1>
        <p class="hero-desc">浏览在售花材，或进入 DIY 工作台，从花材、包装到造型完成专属设计。</p>
        <div class="hero-actions">
          <button class="cta-btn" @click="$router.push('/user/diy')">开始 DIY 花束</button>
          <button class="secondary-btn" @click="$router.push('/user/plans')">查看我的方案</button>
        </div>
      </div>
      <div class="hero-visual" aria-hidden="true">
        <div class="hero-image hero-image-main">
          <img :src="getCategoryImage('玫瑰')" alt="" />
        </div>
        <div class="hero-image hero-image-side">
          <img :src="getCategoryImage('百合')" alt="" />
        </div>
        <img class="hero-leaf" :src="getCategoryImage('配叶')" alt="" />
      </div>
    </section>

    <section class="categories-section">
      <div class="section-heading">
        <div>
          <p class="section-kicker">按花材浏览</p>
          <h2>花卉分类</h2>
        </div>
        <p>从经典花材到自然配叶，快速找到适合的搭配。</p>
      </div>
      <div class="loading-box" v-if="categoriesLoading">正在加载分类...</div>
      <div class="categories-grid" v-else>
        <button class="category-card all-btn" :class="{ active: !selectedCategory }" @click="showAll">
          <span class="category-letter">全</span>
          <span class="category-copy"><strong>全部</strong><small>所有花卉</small></span>
        </button>
        <button v-for="cat in categories" :key="cat.id" class="category-card" :class="{ active: selectedCategory === cat.id }" @click="filterByCategory(cat.id)">
          <span class="category-picture"><img :src="getCategoryImage(cat.name)" alt="" /></span>
          <span class="category-copy"><strong>{{ cat.name }}</strong><small>{{ cat.description || '精选花卉' }}</small></span>
        </button>
      </div>
    </section>

    <section class="products-section">
      <div class="section-heading product-heading">
        <div>
          <p class="section-kicker">在售花材</p>
          <h2>{{ selectedCategory ? selectedCategoryName : '热门花卉' }}</h2>
        </div>
        <span class="result-count" v-if="!flowersLoading">{{ flowers.length }} 种可选</span>
      </div>
      <div class="loading-box" v-if="flowersLoading">正在整理花材...</div>
      <div class="products-grid" v-else-if="flowers.length > 0">
        <article v-for="f in flowers" :key="f.id" class="product-card">
          <div class="product-image">
            <img :src="f.imageUrl || getCategoryImage(f.categoryName)" :alt="f.name" class="flower-img" />
          </div>
          <div class="product-info">
            <div class="product-title-row">
              <h3>{{ f.name }}</h3>
              <p class="price">¥{{ f.price?.toFixed(2) }}</p>
            </div>
            <p class="desc">{{ f.description?.substring(0, 28) || '新鲜花材，可用于日常花礼与 DIY 搭配' }}</p>
            <div class="product-footer">
              <p class="stock">库存 {{ f.stock }}</p>
              <button class="add-cart-btn" @click="addToOrder(f)">加入购物车</button>
            </div>
          </div>
        </article>
      </div>
      <div class="empty-box" v-else>
        <p>当前分类暂无花卉数据</p>
      </div>
    </section>

    <div v-if="cart.length > 0" class="cart-bar">
      <div class="cart-summary">
        <span class="cart-count">{{ cart.length }}</span>
        <span>已选 {{ cart.length }} 种花卉</span>
      </div>
      <strong>合计 ¥{{ totalPrice.toFixed(2) }}</strong>
      <div class="cart-actions">
        <button class="clear-btn" @click="cart = []">清空</button>
        <button class="order-btn" @click="showOrderForm = true">去下单</button>
      </div>
    </div>

    <div v-if="showOrderForm" class="form-overlay" @click.self="showOrderForm = false">
      <div class="form-card" role="dialog" aria-modal="true" aria-labelledby="order-title">
        <div class="modal-heading">
          <div>
            <p class="section-kicker">模拟支付订单</p>
            <h3 id="order-title">确认订单</h3>
          </div>
          <button class="close-btn" aria-label="关闭" @click="showOrderForm = false">×</button>
        </div>
        <div class="cart-items">
          <div v-for="(item, idx) in cart" :key="idx" class="cart-item">
            <span>{{ item.name }}</span>
            <span>x{{ item.qty }}</span>
            <strong>¥{{ (item.price * item.qty).toFixed(2) }}</strong>
          </div>
        </div>
        <div class="form-group"><label for="receiver-name">收货人</label><input id="receiver-name" v-model="receiverName" autocomplete="name" /></div>
        <div class="form-group"><label for="receiver-phone">手机号</label><input id="receiver-phone" v-model="receiverPhone" autocomplete="tel" /></div>
        <div class="form-group"><label for="shipping-address">收货地址</label><input id="shipping-address" v-model="shippingAddress" autocomplete="street-address" /></div>
        <div class="order-total"><span>订单总计</span><strong>¥{{ totalPrice.toFixed(2) }}</strong></div>
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

const getCategoryImage = (name) => {
  const images = {
    '玫瑰': 'red-rose.webp',
    '百合': 'white-lily.webp',
    '郁金香': 'pink-tulip.webp',
    '向日葵': 'sunflower.webp',
    '康乃馨': 'pink-carnation.webp',
    '配叶': 'eucalyptus.webp'
  }
  return `/images/diy/${images[name] || 'white-daisy.webp'}`
}
</script>

<style scoped>
.home-page {
  max-width: 1240px;
  margin: 0 auto;
}

.hero-section {
  position: relative;
  min-height: 430px;
  display: grid;
  grid-template-columns: 1.05fr 0.95fr;
  overflow: hidden;
  margin-bottom: 4.5rem;
  color: #f8f3ed;
  background: #315041;
  border-radius: var(--radius-feature);
  box-shadow: var(--shadow-card);
}

.hero-content {
  position: relative;
  z-index: 2;
  align-self: center;
  max-width: 38rem;
  padding: 4.2rem 1.5rem 4.2rem 4rem;
}

.section-kicker {
  margin-bottom: 0.6rem;
  color: var(--color-primary);
  font-size: 0.76rem;
  font-weight: 750;
  letter-spacing: 0.12em;
}

.hero-content .section-kicker {
  color: #d9b5bd;
}

.hero-content h1 {
  max-width: 9em;
  font-family: "STSong", "SimSun", serif;
  font-size: clamp(2.45rem, 4vw, 3.75rem);
  font-weight: 600;
  line-height: 1.18;
  letter-spacing: -0.04em;
}

.hero-desc {
  max-width: 33rem;
  margin-top: 1.25rem;
  color: rgba(248, 243, 237, 0.74);
  font-size: 0.95rem;
  line-height: 1.8;
}

.hero-actions {
  display: flex;
  gap: 0.75rem;
  margin-top: 1.8rem;
}

.cta-btn,
.secondary-btn {
  min-height: 2.8rem;
  padding: 0.65rem 1.2rem;
  border-radius: var(--radius-control);
  font-size: 0.9rem;
  font-weight: 700;
  cursor: pointer;
  transition: transform 160ms ease, background-color 160ms ease, border-color 160ms ease;
}

.cta-btn {
  color: #fdfbf7;
  background: var(--color-primary);
  border: 1px solid var(--color-primary);
}

.secondary-btn {
  color: #f8f3ed;
  background: transparent;
  border: 1px solid rgba(248, 243, 237, 0.32);
}

.cta-btn:hover,
.secondary-btn:hover {
  transform: translateY(-1px);
}

.cta-btn:hover {
  background: var(--color-primary-dark);
  border-color: var(--color-primary-dark);
}

.secondary-btn:hover {
  background: rgba(248, 243, 237, 0.08);
  border-color: rgba(248, 243, 237, 0.55);
}

.hero-visual {
  position: relative;
  min-height: 430px;
}

.hero-image {
  position: absolute;
  overflow: hidden;
  background: #e9eee9;
  box-shadow: 0 18px 40px rgba(20, 38, 28, 0.24);
}

.hero-image img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  object-position: bottom center;
}

.hero-image-main {
  right: 3rem;
  bottom: -2.75rem;
  width: 18rem;
  height: 23rem;
  transform: rotate(3deg);
  border-radius: 9rem 9rem 0 0;
}

.hero-image-side {
  top: 2.75rem;
  right: 18rem;
  width: 9.5rem;
  height: 12rem;
  transform: rotate(-5deg);
  border: 0.6rem solid #f8f3ed;
  border-radius: var(--radius-card);
}

.hero-leaf {
  position: absolute;
  right: -2rem;
  bottom: -5rem;
  width: 13rem;
  transform: rotate(20deg);
  filter: drop-shadow(0 12px 20px rgba(20, 38, 28, 0.18));
}

.categories-section,
.products-section {
  margin-bottom: 4rem;
}

.section-heading {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 2rem;
  margin-bottom: 1.4rem;
}

.section-heading h2 {
  color: var(--color-ink);
  font-size: 1.75rem;
  line-height: 1.2;
}

.section-heading > p {
  max-width: 28rem;
  color: var(--color-muted);
  font-size: 0.88rem;
}

.categories-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(155px, 1fr));
  gap: 0.75rem;
}

.category-card {
  min-width: 0;
  min-height: 4.5rem;
  display: flex;
  align-items: center;
  gap: 0.7rem;
  padding: 0.7rem;
  text-align: left;
  color: var(--color-ink);
  background: var(--color-surface);
  border: 1px solid var(--color-line);
  border-radius: var(--radius-card);
  cursor: pointer;
  transition: transform 160ms ease, border-color 160ms ease, background-color 160ms ease;
}

.category-card:hover {
  transform: translateY(-2px);
  border-color: #b7c7bb;
}

.category-card.active {
  background: var(--color-leaf-soft);
  border-color: #9eb7a7;
}

.category-picture,
.category-letter {
  flex: 0 0 3rem;
  width: 3rem;
  height: 3rem;
  display: grid;
  place-items: center;
  overflow: hidden;
  background: #edf1ed;
  border-radius: 8px;
}

.category-picture img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.category-letter {
  color: var(--color-leaf-dark);
  font-family: "STKaiti", "KaiTi", serif;
  font-size: 1.2rem;
}

.category-copy {
  min-width: 0;
  display: grid;
  gap: 0.15rem;
}

.category-copy strong {
  font-size: 0.92rem;
}

.category-copy small {
  overflow: hidden;
  color: var(--color-muted);
  font-size: 0.72rem;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.result-count {
  color: var(--color-muted);
  font-size: 0.82rem;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(230px, 1fr));
  gap: 1.15rem;
}

.product-card {
  min-width: 0;
  overflow: hidden;
  background: var(--color-surface-strong);
  border: 1px solid var(--color-line);
  border-radius: var(--radius-card);
  box-shadow: 0 4px 18px rgba(40, 63, 49, 0.05);
  transition: transform 180ms ease, box-shadow 180ms ease, border-color 180ms ease;
}

.product-card:hover {
  transform: translateY(-3px);
  border-color: #c4cec5;
  box-shadow: var(--shadow-card);
}

.product-image {
  height: 220px;
  overflow: hidden;
  background: #edf1ed;
}

.flower-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  object-position: center bottom;
  transition: transform 240ms ease;
}

.product-card:hover .flower-img {
  transform: scale(1.025);
}

.product-info {
  padding: 1rem 1rem 1.05rem;
}

.product-title-row,
.product-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
}

.product-title-row h3 {
  color: var(--color-ink);
  font-size: 1rem;
}

.price {
  color: var(--color-primary-dark);
  font-size: 1.05rem;
  font-weight: 750;
  white-space: nowrap;
}

.desc {
  min-height: 2.8em;
  margin: 0.45rem 0 0.85rem;
  color: var(--color-muted);
  font-size: 0.78rem;
  line-height: 1.45;
}

.stock {
  color: #7c857f;
  font-size: 0.76rem;
}

.add-cart-btn,
.order-btn,
.save-btn {
  color: #fdfbf7;
  background: var(--color-primary);
  border: 1px solid var(--color-primary);
}

.add-cart-btn {
  padding: 0.45rem 0.75rem;
  border-radius: var(--radius-control);
  font-size: 0.78rem;
  font-weight: 700;
  cursor: pointer;
  transition: background-color 160ms ease;
}

.add-cart-btn:hover,
.order-btn:hover,
.save-btn:hover:not(:disabled) {
  background: var(--color-primary-dark);
  border-color: var(--color-primary-dark);
}

.loading-box,
.empty-box {
  padding: 4rem 1rem;
  text-align: center;
  color: var(--color-muted);
  background: var(--color-surface);
  border: 1px dashed #cbd4cc;
  border-radius: var(--radius-card);
}

.cart-bar {
  position: fixed;
  right: 50%;
  bottom: 1.25rem;
  z-index: 45;
  width: min(720px, calc(100% - 2rem));
  display: flex;
  align-items: center;
  gap: 1.3rem;
  padding: 0.75rem 0.85rem 0.75rem 1rem;
  color: var(--color-ink);
  background: rgba(255, 253, 250, 0.98);
  border: 1px solid var(--color-line);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-lift);
  transform: translateX(50%);
}

.cart-summary {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 0.55rem;
  color: var(--color-muted);
  font-size: 0.84rem;
}

.cart-count {
  width: 1.65rem;
  height: 1.65rem;
  display: grid;
  place-items: center;
  color: var(--color-surface-strong);
  background: var(--color-leaf);
  border-radius: 50%;
  font-size: 0.75rem;
  font-weight: 700;
}

.cart-bar > strong {
  color: var(--color-primary-dark);
  font-size: 0.95rem;
  white-space: nowrap;
}

.cart-actions {
  display: flex;
  gap: 0.45rem;
}

.order-btn,
.clear-btn,
.cancel-btn,
.save-btn,
.close-btn {
  min-height: 2.35rem;
  padding: 0.45rem 0.85rem;
  border-radius: var(--radius-control);
  font-size: 0.8rem;
  font-weight: 650;
  cursor: pointer;
}

.clear-btn,
.cancel-btn {
  color: var(--color-muted);
  background: var(--color-surface-strong);
  border: 1px solid var(--color-line);
}

.form-overlay {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: grid;
  place-items: center;
  padding: 1rem;
  background: rgba(28, 38, 32, 0.52);
}

.form-card {
  width: min(470px, 100%);
  max-height: min(84vh, 720px);
  overflow-y: auto;
  padding: 1.65rem;
  background: var(--color-surface-strong);
  border: 1px solid var(--color-line);
  border-radius: var(--radius-card);
  box-shadow: 0 20px 60px rgba(25, 39, 30, 0.24);
}

.modal-heading {
  display: flex;
  align-items: start;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1rem;
}

.modal-heading h3 {
  font-size: 1.35rem;
}

.close-btn {
  min-width: 2.35rem;
  padding: 0;
  color: var(--color-muted);
  background: transparent;
  border: 1px solid var(--color-line);
  font-size: 1.2rem;
  line-height: 1;
}

.cart-items {
  padding: 0.35rem 0.85rem;
  margin-bottom: 1rem;
  background: #f6f8f5;
  border-radius: var(--radius-control);
}

.cart-item {
  display: grid;
  grid-template-columns: 1fr auto auto;
  gap: 1rem;
  padding: 0.65rem 0;
  color: var(--color-muted);
  border-bottom: 1px solid var(--color-line);
  font-size: 0.82rem;
}

.cart-item:last-child {
  border-bottom: 0;
}

.cart-item strong {
  color: var(--color-ink);
}

.form-group {
  margin-bottom: 0.85rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.38rem;
  color: var(--color-ink);
  font-size: 0.8rem;
  font-weight: 650;
}

.form-group input {
  width: 100%;
  min-height: 2.65rem;
  padding: 0.6rem 0.75rem;
  color: var(--color-ink);
  background: #fbfcfa;
  border: 1px solid var(--color-line);
  border-radius: var(--radius-control);
  outline: none;
}

.form-group input:focus {
  background: var(--color-surface-strong);
  border-color: var(--color-leaf);
  box-shadow: 0 0 0 3px rgba(54, 95, 75, 0.1);
}

.order-total {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.9rem 0;
  margin-top: 0.3rem;
  border-top: 1px solid var(--color-line);
}

.order-total span {
  color: var(--color-muted);
  font-size: 0.84rem;
}

.order-total strong {
  color: var(--color-primary-dark);
  font-size: 1.25rem;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.55rem;
}

.save-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.order-msg {
  margin-top: 0.8rem;
  padding: 0.65rem;
  border-radius: var(--radius-control);
  text-align: center;
  font-size: 0.82rem;
}

.order-msg.success {
  color: var(--color-leaf-dark);
  background: var(--color-leaf-soft);
}

.order-msg.error {
  color: var(--color-danger);
  background: #fbebee;
}

@media (max-width: 900px) {
  .hero-section {
    grid-template-columns: 1fr;
  }

  .hero-content {
    padding: 3rem 2rem 1rem;
  }

  .hero-visual {
    min-height: 270px;
  }

  .hero-image-main {
    right: 12%;
    width: 14rem;
    height: 18rem;
  }

  .hero-image-side {
    top: 1rem;
    right: calc(12% + 13rem);
  }
}

@media (max-width: 640px) {
  .hero-section {
    min-height: 0;
    margin-bottom: 3rem;
  }

  .hero-content {
    padding: 2.25rem 1.35rem 0.5rem;
  }

  .hero-content h1 {
    font-size: 2.2rem;
  }

  .hero-actions {
    align-items: stretch;
    flex-direction: column;
  }

  .hero-visual {
    min-height: 230px;
  }

  .hero-image-main {
    right: 5%;
    width: 12rem;
    height: 15rem;
  }

  .hero-image-side {
    right: auto;
    left: 1.5rem;
    width: 7.5rem;
    height: 9rem;
  }

  .section-heading {
    align-items: start;
    flex-direction: column;
    gap: 0.5rem;
  }

  .categories-grid {
    display: flex;
    margin-inline: -1rem;
    padding-inline: 1rem;
    overflow-x: auto;
    scroll-snap-type: x proximity;
  }

  .category-card {
    flex: 0 0 10.5rem;
    scroll-snap-align: start;
  }

  .products-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 0.75rem;
  }

  .product-image {
    height: 165px;
  }

  .product-info {
    padding: 0.8rem;
  }

  .product-title-row,
  .product-footer {
    align-items: start;
    flex-direction: column;
    gap: 0.35rem;
  }

  .desc {
    display: none;
  }

  .price {
    margin-bottom: 0.35rem;
  }

  .add-cart-btn {
    width: 100%;
  }

  .cart-bar {
    bottom: 0.6rem;
    flex-wrap: wrap;
    gap: 0.55rem 0.85rem;
  }

  .cart-summary {
    min-width: 50%;
  }

  .cart-actions {
    width: 100%;
  }

  .cart-actions button {
    flex: 1;
  }
}

@media (max-width: 390px) {
  .products-grid {
    grid-template-columns: 1fr;
  }

  .product-card {
    display: grid;
    grid-template-columns: 8rem 1fr;
  }

  .product-image {
    height: 100%;
    min-height: 155px;
  }
}
</style>
