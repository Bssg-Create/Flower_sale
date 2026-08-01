<template>
  <div class="admin-dashboard">
    <div class="content-header">
      <h2>{{ currentTitle }}</h2>
      <div class="header-actions">
        <button class="refresh-btn" @click="refreshData">刷新数据</button>
      </div>
    </div>

    <div v-if="$route.path === '/admin'" class="dashboard">
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon">人</div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.users }}</span>
            <span class="stat-label">用户总数</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">花</div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.flowers }}</span>
            <span class="stat-label">花卉种类</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">单</div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.orders }}</span>
            <span class="stat-label">订单数量</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">创</div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.diy }}</span>
            <span class="stat-label">DIY花束</span>
          </div>
        </div>
      </div>
    </div>

    <div v-if="$route.path === '/admin/users'" class="data-section">
      <div class="toolbar">
        <input v-model="userSearch" type="search" class="search-input" placeholder="搜索用户名或手机号" aria-label="搜索用户" />
      </div>
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>手机号</th>
            <th>邮箱</th>
            <th>用户类型</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in filteredUsers" :key="user.id">
            <td>{{ user.id }}</td>
            <td>{{ user.username }}</td>
            <td>{{ user.phone || '-' }}</td>
            <td>{{ user.email || '-' }}</td>
            <td>
              <span :class="['badge', user.userType === 'admin' ? 'badge-admin' : 'badge-user']">
                {{ user.userType === 'admin' ? '管理员' : '普通用户' }}
              </span>
            </td>
            <td>
              <span :class="['badge', user.status === '1' ? 'badge-active' : 'badge-disabled']">
                {{ user.status === '1' ? '启用' : '禁用' }}
              </span>
            </td>
            <td>
              <button class="action-btn" @click="toggleUserStatus(user)">
                {{ user.status === '1' ? '禁用' : '启用' }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="$route.path === '/admin/flowers'" class="data-section">
      <div class="toolbar">
        <input v-model="flowerSearch" type="search" class="search-input" placeholder="搜索花卉名称或分类" aria-label="搜索花卉" />
        <button class="add-btn" @click="showFlowerForm = true">+ 新增花卉</button>
      </div>

      <div v-if="showFlowerForm" class="form-overlay">
        <div class="form-card">
          <h3>{{ editFlower.id ? '编辑花卉' : '新增花卉' }}</h3>
          <div class="form-group">
            <label>名称</label><input v-model="editFlower.name" />
          </div>
          <div class="form-row">
            <div class="form-group"><label>分类</label><input v-model="editFlower.categoryName" /></div>
            <div class="form-group"><label>分类ID</label><input v-model="editFlower.categoryId" type="number" /></div>
          </div>
          <div class="form-row">
            <div class="form-group"><label>价格</label><input v-model="editFlower.price" type="number" step="0.01" /></div>
            <div class="form-group"><label>库存</label><input v-model="editFlower.stock" type="number" /></div>
          </div>
          <div class="form-group"><label>描述</label><textarea v-model="editFlower.description" rows="2"></textarea></div>
          <div class="form-group"><label>图片URL</label><input v-model="editFlower.imageUrl" /></div>
          <div class="form-actions">
            <button class="cancel-btn" @click="showFlowerForm = false">取消</button>
            <button class="save-btn" @click="saveFlower">保存</button>
          </div>
        </div>
      </div>

      <table class="table">
        <thead>
          <tr>
            <th>ID</th><th>名称</th><th>分类</th><th>价格</th><th>库存</th><th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="f in filteredFlowers" :key="f.id">
            <td>{{ f.id }}</td>
            <td>{{ f.name }}</td>
            <td>{{ f.categoryName }}</td>
            <td>¥{{ f.price?.toFixed(2) }}</td>
            <td>{{ f.stock }}</td>
            <td>
              <button class="action-btn" @click="editFlowerForm(f)">编辑</button>
              <button class="action-btn danger" @click="deleteFlower(f.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="$route.path === '/admin/orders'" class="data-section">
      <table class="table">
        <thead>
          <tr>
            <th>订单号</th><th>用户ID</th><th>金额</th><th>状态</th><th>支付状态</th><th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="o in orders" :key="o.id">
            <td>{{ o.orderNo }}</td>
            <td>{{ o.userId }}</td>
            <td>¥{{ o.totalAmount?.toFixed(2) }}</td>
            <td>
              <span :class="['badge', 'badge-' + getStatusClass(o.status)]">{{ getStatusText(o.status) }}</span>
            </td>
            <td>
              <span :class="['badge', o.payStatus === '1' ? 'badge-active' : 'badge-disabled']">
                {{ o.payStatus === '1' ? '已支付' : '未支付' }}
              </span>
            </td>
            <td>
              <select v-model="o._newStatus" @change="updateOrderStatus(o)" class="status-select">
                <option value="">选择状态</option>
                <option value="PENDING">待支付</option>
                <option value="PAID">已支付</option>
                <option value="SHIPPED">已发货</option>
                <option value="COMPLETED">已完成</option>
                <option value="CANCELED">已取消</option>
              </select>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="$route.path === '/admin/diy'" class="data-section">
      <table class="table">
        <thead>
          <tr>
            <th>ID</th><th>名称</th><th>用户</th><th>总价</th><th>包装</th><th>状态</th><th>创建时间</th><th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="d in diyList" :key="d.id">
            <td>{{ d.id }}</td>
            <td>{{ d.name }}</td>
            <td>{{ d.username || '用户' + d.userId }}</td>
            <td>¥{{ (d.totalPrice || 0).toFixed(2) }}</td>
            <td>{{ d.packageType || '-' }}</td>
            <td>
              <span :class="['badge', d.status === '1' ? 'badge-info' : 'badge-active']">
                {{ d.status === '1' ? '已保存' : d.status === '2' ? '已下单' : (d.status || '-') }}
              </span>
            </td>
            <td>{{ d.createTime || '-' }}</td>
            <td>
              <button class="action-btn danger" @click="deleteDiy(d.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../api'

const route = useRoute()
const router = useRouter()

const currentTitle = computed(() => {
  const map = {
    '/admin': '数据概览',
    '/admin/users': '用户管理',
    '/admin/flowers': '花卉管理',
    '/admin/orders': '订单管理',
    '/admin/diy': 'DIY管理'
  }
  return map[route.path] || '管理后台'
})

const stats = ref({ users: 0, flowers: 0, orders: 0, diy: 0 })
const users = ref([])
const userSearch = ref('')
const flowers = ref([])
const flowerSearch = ref('')
const orders = ref([])
const diyList = ref([])
const showFlowerForm = ref(false)
const editFlower = ref({})

const filteredUsers = computed(() => {
  if (!userSearch.value) return users.value
  const keyword = userSearch.value.trim().toLowerCase()
  return users.value.filter(u => u.username?.toLowerCase().includes(keyword) || u.phone?.includes(keyword))
})

const filteredFlowers = computed(() => {
  if (!flowerSearch.value) return flowers.value
  const keyword = flowerSearch.value.trim().toLowerCase()
  return flowers.value.filter(f =>
    f.name?.toLowerCase().includes(keyword) || f.categoryName?.toLowerCase().includes(keyword)
  )
})

const getStatusText = (s) => {
  const m = { PENDING: '待支付', PAID: '已支付', SHIPPED: '已发货', COMPLETED: '已完成', CANCELED: '已取消' }
  return m[s] || s
}

const getStatusClass = (s) => {
  const m = { PENDING: 'warning', PAID: 'info', SHIPPED: 'primary', COMPLETED: 'active', CANCELED: 'disabled' }
  return m[s] || ''
}

const loadData = async () => {
  try {
    const [uRes, fRes, oRes, dRes] = await Promise.all([
      api.get('/user/list'),
      api.get('/flower/list'),
      api.get('/order/list'),
      api.get('/admin/diy/list')
    ]).catch(() => [null, null, null, null])

    if (uRes) users.value = uRes.data.data || []
    if (fRes) flowers.value = fRes.data.data || []
    if (oRes) orders.value = (oRes.data.data || []).map(o => ({ ...o, _newStatus: '' }))
    if (dRes) diyList.value = (dRes.data.data || []).map(d => ({
      ...d,
      totalPrice: d.totalPrice || 0
    }))

    stats.value = {
      users: users.value.length,
      flowers: flowers.value.length,
      orders: orders.value.length,
      diy: diyList.value.length
    }
  } catch (e) { console.error(e) }
}

const refreshData = () => loadData()

const toggleUserStatus = async (user) => {
  const newStatus = user.status === '1' ? '0' : '1'
  await api.put('/user', { ...user, status: newStatus, password: null })
  loadData()
}

const editFlowerForm = (f) => { editFlower.value = { ...f }; showFlowerForm.value = true }
const saveFlower = async () => {
  if (editFlower.value.id) { await api.put('/flower', editFlower.value) }
  else { await api.post('/flower', editFlower.value) }
  showFlowerForm.value = false
  editFlower.value = {}
  loadData()
}

const deleteFlower = async (id) => {
  if (!confirm('确认删除？')) return
  await api.delete(`/flower/${id}`)
  loadData()
}

const updateOrderStatus = async (o) => {
  if (!o._newStatus) return
  await api.put(`/order/${o.id}/status`, { status: o._newStatus })
  o._newStatus = ''
  loadData()
}

const deleteDiy = async (id) => {
  if (!confirm('确认删除？')) return
  await api.delete(`/diy/${id}`)
  loadData()
}

onMounted(loadData)
watch(() => route.path, () => { if (route.path.startsWith('/admin')) loadData() })
</script>

<style scoped>
.admin-dashboard { width: 100%; max-width: 1480px; margin: 0 auto; }

.content-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 1.25rem;
}

.content-header h2 { color: var(--color-ink); font-size: clamp(1.35rem, 2vw, 1.7rem); }

.refresh-btn, .add-btn, .cancel-btn, .save-btn {
  padding: 0.58rem 0.9rem; border: 1px solid transparent; border-radius: var(--radius-control); cursor: pointer;
  font-size: 0.9rem;
  font-weight: 650;
  transition: all var(--ease-standard);
}

.refresh-btn { background: var(--color-surface); color: var(--color-ink-soft); border-color: var(--color-line); }
.refresh-btn:hover { border-color: var(--color-brand-line); color: var(--color-brand); }
.add-btn, .save-btn { background: var(--color-brand); color: white; }
.add-btn:hover, .save-btn:hover { background: var(--color-brand-strong); }
.cancel-btn { background: var(--color-surface-soft); color: var(--color-muted); border-color: var(--color-line); }

.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 1rem; margin-bottom: 2rem; }
.stat-card { min-height: 124px; background: var(--color-surface); border: 1px solid var(--color-line); border-radius: var(--radius-card); padding: 1.25rem; display: flex; align-items: center; gap: 1rem; box-shadow: var(--shadow-soft); }
.stat-card:nth-child(2n) .stat-icon { color: var(--color-forest); background: var(--color-forest-soft); }
.stat-icon { width: 46px; height: 46px; display: grid; place-items: center; flex: 0 0 auto; border-radius: 13px; color: var(--color-brand); background: var(--color-brand-soft); font-family: Georgia, "Times New Roman", serif; font-size: 1.05rem; font-weight: 700; }
.stat-value { font-size: 1.8rem; font-weight: 760; color: var(--color-ink); display: block; font-variant-numeric: tabular-nums; }
.stat-label { color: var(--color-muted); font-size: 0.82rem; }

.data-section { overflow-x: auto; background: var(--color-surface); border: 1px solid var(--color-line); border-radius: var(--radius-card); padding: clamp(1rem, 2vw, 1.5rem); box-shadow: var(--shadow-soft); }
.toolbar { display: flex; gap: 0.75rem; margin-bottom: 1.1rem; }
.search-input { padding: 0.62rem 0.85rem; border: 1px solid var(--color-line); border-radius: var(--radius-control); width: min(100%, 300px); outline: none; background: var(--color-surface-soft); }
.search-input:focus { border-color: var(--color-brand); background: var(--color-surface); box-shadow: 0 0 0 3px rgba(166, 63, 95, 0.08); }

.table { width: 100%; min-width: 760px; border-collapse: collapse; font-size: 0.88rem; }
.table th { text-align: left; padding: 0.78rem; background: var(--color-surface-soft); color: var(--color-muted); border-bottom: 1px solid var(--color-line); font-size: 0.78rem; font-weight: 700; }
.table td { padding: 0.82rem 0.78rem; color: var(--color-ink-soft); border-bottom: 1px solid var(--color-line); }
.table tbody tr:hover { background: #fafcf9; }

.badge { padding: 0.2rem 0.6rem; border-radius: 10px; font-size: 0.8rem; }
.badge-admin { background: #fff7e6; color: #fa8c16; }
.badge-user { background: #e6f7ff; color: #1890ff; }
.badge-active { background: #f6ffed; color: #52c41a; }
.badge-disabled { background: #fff2f0; color: #ff4d4f; }
.badge-warning { background: #fff7e6; color: #fa8c16; }
.badge-info { background: #e6f7ff; color: #1890ff; }
.badge-primary { background: #f0f5ff; color: #597ef7; }

.action-btn { padding: 0.38rem 0.65rem; border: 1px solid var(--color-line); border-radius: 8px; background: var(--color-surface); cursor: pointer; font-size: 0.78rem; margin-right: 0.3rem; }
.action-btn:hover { border-color: var(--color-brand); color: var(--color-brand); }
.action-btn.danger:hover { border-color: #ff4d4f; color: #ff4d4f; }

.status-select { padding: 0.4rem; border: 1px solid var(--color-line); border-radius: 8px; font-size: 0.8rem; }

.form-overlay { position: fixed; inset: 0; padding: 1rem; background: rgba(27, 43, 38, 0.54); backdrop-filter: blur(5px); display: flex; align-items: center; justify-content: center; z-index: 100; }
.form-card { background: var(--color-surface); border-radius: var(--radius-card); padding: clamp(1.25rem, 3vw, 2rem); width: min(100%, 520px); box-shadow: var(--shadow-lifted); }
.form-card h3 { margin-bottom: 1rem; }
.form-group { margin-bottom: 0.8rem; }
.form-group label { display: block; margin-bottom: 0.35rem; color: var(--color-muted); font-size: 0.82rem; font-weight: 650; }
.form-group input, .form-group textarea { width: 100%; padding: 0.62rem; border: 1px solid var(--color-line); border-radius: var(--radius-control); font-size: 0.9rem; outline: none; }
.form-group input:focus, .form-group textarea:focus { border-color: var(--color-brand); }
.form-row { display: flex; gap: 1rem; }
.form-row .form-group { flex: 1; }
.form-actions { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem; }
@media (max-width: 980px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 620px) {
  .content-header { align-items: flex-start; }
  .stats-grid { grid-template-columns: 1fr; gap: 0.75rem; }
  .stat-card { min-height: 98px; }
  .toolbar, .form-row { flex-direction: column; }
  .search-input { width: 100%; }
}
</style>
