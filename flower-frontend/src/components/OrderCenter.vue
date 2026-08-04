<template>
  <div class="order-center-page">
    <div class="page-header">
      <div>
        <p class="section-kicker">购买记录与配送进度</p>
        <h2>我的订单</h2>
        <p class="page-desc">查看订单明细，并完成模拟支付、取消订单或确认收货。</p>
      </div>
      <button class="refresh-btn" :disabled="loading" @click="loadOrders">
        {{ loading ? '刷新中...' : '刷新订单' }}
      </button>
    </div>

    <p v-if="notice.text" class="notice" :class="notice.type" role="status" aria-live="polite">
      {{ notice.text }}
    </p>

    <div v-if="loading && orders.length === 0" class="state-panel">正在加载订单...</div>
    <div v-else-if="loadError" class="state-panel error-panel">
      <p>{{ loadError }}</p>
      <button class="secondary-btn" @click="loadOrders">重新加载</button>
    </div>
    <div v-else-if="orders.length === 0" class="state-panel empty-panel">
      <h3>还没有订单</h3>
      <p>可以从首页选择花材，或前往 DIY 工作台设计专属花束。</p>
      <div class="empty-actions">
        <router-link to="/user/home">浏览花材</router-link>
        <router-link to="/user/diy" class="secondary-link">开始 DIY</router-link>
      </div>
    </div>

    <div v-else class="order-list">
      <article v-for="order in orders" :key="order.id" class="order-card">
        <header class="order-summary">
          <div class="order-identity">
            <span class="order-label">订单号</span>
            <strong :title="order.orderNo">{{ order.orderNo }}</strong>
            <time>{{ formatTime(order.createTime) }}</time>
          </div>
          <div class="order-statuses">
            <span class="status-badge" :data-status="normalizedOrderStatus(order)">
              {{ orderStatusText(order.status) }}
            </span>
            <span class="pay-badge">{{ paymentStatusText(order.payStatus) }}</span>
          </div>
          <p class="order-amount">¥{{ formatAmount(order.totalAmount) }}</p>
        </header>

        <div v-if="!isConsistent(order)" class="anomaly-message" role="alert">
          订单状态与支付状态不一致，当前操作已暂停，请联系管理员核对。
        </div>

        <div class="order-actions">
          <button class="detail-btn" :disabled="isBusy(order.id)" @click="toggleDetail(order)">
            {{ expandedOrderId === order.id ? '收起详情' : '查看详情' }}
          </button>
          <button v-if="canPay(order)" class="primary-btn" :disabled="isBusy(order.id)" @click="pay(order)">
            {{ actionText(order.id, '模拟支付') }}
          </button>
          <button v-if="canCancel(order)" class="danger-btn" :disabled="isBusy(order.id)" @click="cancel(order)">
            {{ actionText(order.id, '取消订单') }}
          </button>
          <button v-if="canConfirm(order)" class="primary-btn" :disabled="isBusy(order.id)" @click="confirmReceipt(order)">
            {{ actionText(order.id, '确认收货') }}
          </button>
        </div>

        <section v-if="expandedOrderId === order.id" class="order-detail" aria-label="订单详情">
          <div v-if="detailLoading" class="detail-loading">正在加载订单详情...</div>
          <div v-else-if="detailError" class="detail-error">
            <span>{{ detailError }}</span>
            <button @click="loadDetail(order)">重试</button>
          </div>
          <template v-else-if="detailOrder">
            <div class="receiver-grid">
              <div><span>收货人</span><strong>{{ detailOrder.receiverName || '-' }}</strong></div>
              <div><span>联系电话</span><strong>{{ detailOrder.receiverPhone || '-' }}</strong></div>
              <div class="address"><span>收货地址</span><strong>{{ detailOrder.shippingAddress || '-' }}</strong></div>
            </div>
            <div class="item-list">
              <div v-for="item in detailItems" :key="item.id" class="item-row">
                <div>
                  <strong>{{ item.flowerName || `花材 ${item.flowerId}` }}</strong>
                  <span>¥{{ formatAmount(item.price) }} × {{ item.quantity }}</span>
                </div>
                <strong>¥{{ formatAmount(item.totalPrice) }}</strong>
              </div>
            </div>
          </template>
        </section>
      </article>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import api from '../api'
import {
  ORDER_STATUS,
  PAYMENT_STATUS,
  normalizeOrderStatus,
  normalizePaymentStatus,
  orderStatusText,
  paymentStatusText
} from '../constants/businessStatus'

const orders = ref([])
const loading = ref(false)
const loadError = ref('')
const expandedOrderId = ref(null)
const detailOrder = ref(null)
const detailItems = ref([])
const detailLoading = ref(false)
const detailError = ref('')
const busyOrderId = ref(null)
const notice = reactive({ text: '', type: 'success' })

const normalizedOrderStatus = (order) => normalizeOrderStatus(order.status)
const normalizedPayStatus = (order) => normalizePaymentStatus(order.payStatus)

const isConsistent = (order) => {
  const status = normalizedOrderStatus(order)
  const payStatus = normalizedPayStatus(order)
  return (status === ORDER_STATUS.PENDING && payStatus === PAYMENT_STATUS.UNPAID) ||
    ([ORDER_STATUS.PAID, ORDER_STATUS.SHIPPED, ORDER_STATUS.COMPLETED].includes(status) && payStatus === PAYMENT_STATUS.PAID) ||
    (status === ORDER_STATUS.CANCELED && [PAYMENT_STATUS.UNPAID, PAYMENT_STATUS.REFUNDED].includes(payStatus))
}

const canPay = (order) => isConsistent(order) &&
  normalizedOrderStatus(order) === ORDER_STATUS.PENDING && normalizedPayStatus(order) === PAYMENT_STATUS.UNPAID

const canCancel = (order) => isConsistent(order) && (
  (normalizedOrderStatus(order) === ORDER_STATUS.PENDING && normalizedPayStatus(order) === PAYMENT_STATUS.UNPAID) ||
  (normalizedOrderStatus(order) === ORDER_STATUS.PAID && normalizedPayStatus(order) === PAYMENT_STATUS.PAID)
)

const canConfirm = (order) => isConsistent(order) &&
  normalizedOrderStatus(order) === ORDER_STATUS.SHIPPED && normalizedPayStatus(order) === PAYMENT_STATUS.PAID

const isBusy = (orderId) => busyOrderId.value === orderId
const actionText = (orderId, text) => isBusy(orderId) ? '处理中...' : text
const formatAmount = (value) => Number(value || 0).toFixed(2)
const formatTime = (value) => value ? new Intl.DateTimeFormat('zh-CN', {
  year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', hour12: false
}).format(new Date(value)) : '-'

const showNotice = (text, type = 'success') => {
  notice.text = text
  notice.type = type
}

const loadOrders = async () => {
  loading.value = true
  loadError.value = ''
  try {
    const userId = localStorage.getItem('userId')
    const res = await api.get(`/order/user/${userId}`)
    orders.value = res.data.data || []
  } catch (error) {
    loadError.value = error.response?.data?.message || '订单加载失败，请检查网络后重试。'
  } finally {
    loading.value = false
  }
}

const loadDetail = async (order) => {
  detailLoading.value = true
  detailError.value = ''
  try {
    const [orderRes, itemsRes] = await Promise.all([
      api.get(`/order/${order.id}`),
      api.get(`/order/${order.id}/items`)
    ])
    detailOrder.value = orderRes.data.data
    detailItems.value = itemsRes.data.data || []
  } catch (error) {
    detailError.value = error.response?.data?.message || '订单详情加载失败。'
  } finally {
    detailLoading.value = false
  }
}

const toggleDetail = async (order) => {
  if (expandedOrderId.value === order.id) {
    expandedOrderId.value = null
    return
  }
  expandedOrderId.value = order.id
  detailOrder.value = null
  detailItems.value = []
  await loadDetail(order)
}

const runAction = async (order, request, successMessage) => {
  busyOrderId.value = order.id
  showNotice('')
  try {
    await request()
    showNotice(successMessage)
    await loadOrders()
    if (expandedOrderId.value === order.id) {
      const refreshed = orders.value.find(item => item.id === order.id)
      if (refreshed) await loadDetail(refreshed)
    }
  } catch (error) {
    showNotice(error.response?.data?.message || '操作失败，请刷新订单后重试。', 'error')
  } finally {
    busyOrderId.value = null
  }
}

const pay = (order) => {
  if (!window.confirm('确认完成该订单的模拟支付？')) return
  return runAction(order, () => api.put(`/order/${order.id}/pay`, { payStatus: PAYMENT_STATUS.PAID }), '模拟支付成功。')
}

const cancel = (order) => {
  if (!window.confirm('确认取消该订单？库存将在取消成功后恢复。')) return
  return runAction(order, () => api.put(`/order/${order.id}/cancel`), '订单已取消，库存已恢复。')
}

const confirmReceipt = (order) => {
  if (!window.confirm('确认已经收到该订单？')) return
  return runAction(order, () => api.put(`/order/${order.id}/confirm`), '已确认收货。')
}

onMounted(loadOrders)
</script>

<style scoped>
.order-center-page { max-width: 1120px; margin: 0 auto; }
.page-header { display: flex; align-items: end; justify-content: space-between; gap: 2rem; margin-bottom: 1.5rem; }
.section-kicker { margin-bottom: 0.45rem; color: var(--color-primary); font-size: 0.76rem; font-weight: 750; letter-spacing: 0.12em; }
.page-header h2 { color: var(--color-ink); font-size: 1.85rem; line-height: 1.2; }
.page-desc { max-width: 42rem; margin-top: 0.45rem; color: var(--color-muted); font-size: 0.88rem; }
.refresh-btn, .secondary-btn, .order-actions button, .detail-error button { min-height: 2.45rem; padding: 0.55rem 0.9rem; border-radius: var(--radius-control); font-size: 0.82rem; font-weight: 650; cursor: pointer; }
.refresh-btn, .detail-btn, .secondary-btn { color: var(--color-muted); background: var(--color-surface-strong); border: 1px solid var(--color-line); }
.refresh-btn:hover:not(:disabled), .detail-btn:hover:not(:disabled), .secondary-btn:hover { color: var(--color-primary-dark); border-color: #d7aeb7; }
button:disabled { cursor: wait; opacity: 0.62; }
.notice { margin-bottom: 1rem; padding: 0.8rem 1rem; border-radius: var(--radius-control); font-size: 0.85rem; }
.notice.success { color: var(--color-leaf-dark); background: var(--color-leaf-soft); }
.notice.error { color: var(--color-danger); background: #fbebee; }
.state-panel { padding: 4.5rem 1.5rem; text-align: center; color: var(--color-muted); background: var(--color-surface); border: 1px dashed #cbd4cc; border-radius: var(--radius-card); }
.state-panel h3 { margin-bottom: 0.55rem; color: var(--color-ink); }
.state-panel p { overflow-wrap: anywhere; }
.state-panel button { margin-top: 1rem; }
.empty-actions { display: flex; justify-content: center; gap: 0.65rem; margin-top: 1.25rem; }
.empty-actions a { min-height: 2.45rem; display: inline-flex; align-items: center; padding: 0.55rem 1rem; color: #fdfbf7; background: var(--color-primary); border: 1px solid var(--color-primary); border-radius: var(--radius-control); text-decoration: none; font-size: 0.84rem; font-weight: 700; }
.empty-actions .secondary-link { color: var(--color-primary-dark); background: transparent; }
.order-list { display: grid; gap: 1rem; }
.order-card { overflow: hidden; background: var(--color-surface-strong); border: 1px solid var(--color-line); border-radius: var(--radius-card); box-shadow: 0 5px 18px rgba(40, 63, 49, 0.05); }
.order-summary { display: grid; grid-template-columns: minmax(0, 1fr) auto auto; align-items: center; gap: 1.25rem; padding: 1.1rem 1.25rem; }
.order-identity { min-width: 0; display: grid; gap: 0.22rem; }
.order-label, .receiver-grid span { color: #8b938e; font-size: 0.7rem; }
.order-identity strong { overflow: hidden; color: var(--color-ink); font-size: 0.92rem; text-overflow: ellipsis; white-space: nowrap; }
.order-identity time { color: var(--color-muted); font-size: 0.73rem; }
.order-statuses { display: flex; align-items: center; gap: 0.45rem; }
.status-badge, .pay-badge { padding: 0.25rem 0.5rem; border-radius: 5px; font-size: 0.72rem; white-space: nowrap; }
.status-badge { color: #6b4c20; background: #f6ead8; }
.status-badge[data-status="paid"], .status-badge[data-status="shipped"] { color: #31556b; background: #e4eef3; }
.status-badge[data-status="completed"] { color: var(--color-leaf-dark); background: var(--color-leaf-soft); }
.status-badge[data-status="canceled"] { color: var(--color-danger); background: #fbebee; }
.pay-badge { color: var(--color-muted); background: var(--color-surface); }
.order-amount { color: var(--color-primary-dark); font-size: 1.22rem; font-weight: 750; white-space: nowrap; }
.anomaly-message { margin: 0 1.25rem; padding: 0.72rem 0.85rem; color: #7a5120; background: #fbf0dc; border-radius: var(--radius-control); font-size: 0.8rem; }
.order-actions { display: flex; justify-content: flex-end; gap: 0.55rem; padding: 0.9rem 1.25rem; background: var(--color-surface); border-top: 1px solid var(--color-line); }
.primary-btn { color: #fdfbf7; background: var(--color-primary); border: 1px solid var(--color-primary); }
.primary-btn:hover:not(:disabled) { background: var(--color-primary-dark); }
.danger-btn { color: var(--color-danger); background: transparent; border: 1px solid #e4c3c9; }
.danger-btn:hover:not(:disabled) { background: #fbebee; }
.order-detail { padding: 1.1rem 1.25rem 1.3rem; border-top: 1px solid var(--color-line); }
.detail-loading, .detail-error { padding: 1rem; color: var(--color-muted); text-align: center; }
.detail-error { display: flex; align-items: center; justify-content: center; gap: 0.7rem; color: var(--color-danger); }
.receiver-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 0.85rem 1.5rem; padding-bottom: 1rem; }
.receiver-grid div { min-width: 0; display: grid; gap: 0.25rem; }
.receiver-grid strong { overflow-wrap: anywhere; color: var(--color-ink); font-size: 0.83rem; }
.receiver-grid .address { grid-column: 1 / -1; }
.item-list { border-top: 1px solid var(--color-line); }
.item-row { display: flex; align-items: center; justify-content: space-between; gap: 1rem; padding: 0.75rem 0; border-bottom: 1px solid var(--color-line); }
.item-row:last-child { border-bottom: 0; }
.item-row div { min-width: 0; display: grid; gap: 0.2rem; }
.item-row strong { color: var(--color-ink); font-size: 0.84rem; overflow-wrap: anywhere; }
.item-row span { color: var(--color-muted); font-size: 0.74rem; }
button:focus-visible, a:focus-visible { outline: 3px solid rgba(146, 65, 83, 0.25); outline-offset: 2px; }
@media (max-width: 720px) {
  .page-header { align-items: stretch; flex-direction: column; gap: 1rem; }
  .refresh-btn { align-self: flex-start; }
  .order-summary { grid-template-columns: minmax(0, 1fr) auto; gap: 0.8rem; }
  .order-statuses { grid-column: 1; grid-row: 2; justify-self: start; }
  .order-amount { grid-column: 2; grid-row: 1 / 3; }
  .order-actions { flex-wrap: wrap; }
  .order-actions button { flex: 1 1 8rem; }
}
@media (max-width: 480px) {
  .order-summary { grid-template-columns: 1fr; }
  .order-statuses, .order-amount { grid-column: 1; grid-row: auto; }
  .order-amount { justify-self: start; }
  .receiver-grid { grid-template-columns: 1fr; }
  .receiver-grid .address { grid-column: auto; }
  .empty-actions { align-items: stretch; flex-direction: column; }
  .empty-actions a { justify-content: center; }
}
</style>
