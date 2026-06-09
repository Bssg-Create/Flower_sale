<template>
  <div class="admin-dashboard">
    <div class="content-header">
      <h2>{{ currentTitle }}</h2>
      <div class="header-actions">
        <button class="refresh-btn" @click="refreshData">🔄 刷新</button>
      </div>
    </div>

    <div v-if="$route.path === '/admin'" class="dashboard">
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon">👥</div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.users }}</span>
            <span class="stat-label">用户总数</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">🌸</div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.flowers }}</span>
            <span class="stat-label">花卉种类</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">🛒</div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.orders }}</span>
            <span class="stat-label">订单数量</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">🎨</div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.diy }}</span>
            <span class="stat-label">DIY花束</span>
          </div>
        </div>
      </div>
    </div>

    <div v-if="$route.path === '/admin/users'" class="data-section">
      <div class="toolbar">
        <input v-model="userSearch" type="text" class="search-input" placeholder="搜索用户名..." @input="filterUsers" />
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
        <input v-model="flowerSearch" type="text" class="search-input" placeholder="搜索花卉..." />
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
          <tr v-for="f in flowers" :key="f.id">
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
            <th>ID</th><th>名称</th><th>用户ID</th><th>总价</th><th>包装</th><th>状态</th><th>创建时间</th><th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="d in diyList" :key="d.id">
            <td>{{ d.id }}</td>
            <td>{{ d.name }}</td>
            <td>{{ d.userId }}</td>
            <td>¥{{ d.totalPrice?.toFixed(2) }}</td>
            <td>{{ d.packageType || '-' }}</td>
            <td>{{ d.status || '-' }}</td>
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
  return users.value.filter(u => u.username?.includes(userSearch.value) || u.phone?.includes(userSearch.value))
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
      api.get('/diy/list')
    ]).catch(() => [null, null, null, null])

    if (uRes) users.value = uRes.data.data || []
    if (fRes) flowers.value = fRes.data.data || []
    if (oRes) orders.value = (oRes.data.data || []).map(o => ({ ...o, _newStatus: '' }))
    if (dRes) diyList.value = dRes.data.data || []

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
.admin-dashboard { max-width: 100%; }

.content-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 1.5rem;
}

.content-header h2 { color: #333; font-size: 1.4rem; }

.refresh-btn, .add-btn, .cancel-btn, .save-btn {
  padding: 0.5rem 1rem; border: none; border-radius: 8px; cursor: pointer;
  font-size: 0.9rem;
}

.refresh-btn { background: #f0f0f0; color: #666; }
.add-btn, .save-btn { background: linear-gradient(135deg, #ff6b9d 0%, #c44569 100%); color: white; }
.cancel-btn { background: #f0f0f0; color: #666; }

.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 1rem; margin-bottom: 2rem; }
.stat-card { background: white; border-radius: 12px; padding: 1.5rem; display: flex; align-items: center; gap: 1rem; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.stat-icon { font-size: 2rem; }
.stat-value { font-size: 1.8rem; font-weight: bold; color: #333; display: block; }
.stat-label { color: #888; font-size: 0.85rem; }

.data-section { background: white; border-radius: 12px; padding: 1.5rem; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.toolbar { display: flex; gap: 1rem; margin-bottom: 1rem; }
.search-input { padding: 0.5rem 1rem; border: 1px solid #e0e0e0; border-radius: 8px; width: 250px; outline: none; }
.search-input:focus { border-color: #c44569; }

.table { width: 100%; border-collapse: collapse; font-size: 0.9rem; }
.table th { text-align: left; padding: 0.7rem; background: #fafafa; color: #666; border-bottom: 2px solid #eee; }
.table td { padding: 0.7rem; border-bottom: 1px solid #f0f0f0; }

.badge { padding: 0.2rem 0.6rem; border-radius: 10px; font-size: 0.8rem; }
.badge-admin { background: #fff7e6; color: #fa8c16; }
.badge-user { background: #e6f7ff; color: #1890ff; }
.badge-active { background: #f6ffed; color: #52c41a; }
.badge-disabled { background: #fff2f0; color: #ff4d4f; }
.badge-warning { background: #fff7e6; color: #fa8c16; }
.badge-info { background: #e6f7ff; color: #1890ff; }
.badge-primary { background: #f0f5ff; color: #597ef7; }

.action-btn { padding: 0.3rem 0.7rem; border: 1px solid #e0e0e0; border-radius: 6px; background: white; cursor: pointer; font-size: 0.8rem; margin-right: 0.3rem; }
.action-btn:hover { border-color: #c44569; color: #c44569; }
.action-btn.danger:hover { border-color: #ff4d4f; color: #ff4d4f; }

.status-select { padding: 0.3rem; border: 1px solid #e0e0e0; border-radius: 4px; font-size: 0.8rem; }

.form-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.3); display: flex; align-items: center; justify-content: center; z-index: 100; }
.form-card { background: white; border-radius: 12px; padding: 2rem; width: 500px; }
.form-card h3 { margin-bottom: 1rem; }
.form-group { margin-bottom: 0.8rem; }
.form-group label { display: block; margin-bottom: 0.3rem; color: #555; font-size: 0.85rem; }
.form-group input, .form-group textarea { width: 100%; padding: 0.5rem; border: 1px solid #e0e0e0; border-radius: 6px; font-size: 0.9rem; outline: none; }
.form-group input:focus, .form-group textarea:focus { border-color: #c44569; }
.form-row { display: flex; gap: 1rem; }
.form-row .form-group { flex: 1; }
.form-actions { display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem; }
</style>