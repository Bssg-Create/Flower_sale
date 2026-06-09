<template>
  <div class="admin-page">
    <div class="admin-container">
      <aside class="sidebar">
        <div class="sidebar-header">
          <span class="admin-icon">🔧</span>
          <span>管理后台</span>
        </div>
        <nav class="sidebar-nav">
          <button 
            v-for="menu in menuItems" 
            :key="menu.id"
            @click="currentMenu = menu.id"
            :class="['menu-btn', { active: currentMenu === menu.id }]"
          >
            {{ menu.icon }} {{ menu.name }}
          </button>
        </nav>
      </aside>

      <main class="admin-content">
        <div class="content-header">
          <h2>{{ currentMenuName }}</h2>
          <div class="header-actions">
            <button class="add-btn">+ 新增</button>
            <button class="refresh-btn">🔄 刷新</button>
          </div>
        </div>

        <div class="content-body">
          <div v-if="currentMenu === 'dashboard'" class="dashboard">
            <div class="stats-grid">
              <div class="stat-card">
                <div class="stat-icon">👥</div>
                <div class="stat-info">
                  <span class="stat-value">128</span>
                  <span class="stat-label">用户总数</span>
                </div>
              </div>
              <div class="stat-card">
                <div class="stat-icon">🌸</div>
                <div class="stat-info">
                  <span class="stat-value">156</span>
                  <span class="stat-label">花卉种类</span>
                </div>
              </div>
              <div class="stat-card">
                <div class="stat-icon">🛒</div>
                <div class="stat-info">
                  <span class="stat-value">324</span>
                  <span class="stat-label">订单数量</span>
                </div>
              </div>
              <div class="stat-card">
                <div class="stat-icon">💰</div>
                <div class="stat-info">
                  <span class="stat-value">¥28,456</span>
                  <span class="stat-label">销售额</span>
                </div>
              </div>
            </div>
            <div class="charts-row">
              <div class="chart-card">
                <h3>销售趋势</h3>
                <div class="chart-placeholder">
                  <div class="bar-chart">
                    <div class="bar" style="height: 40%"></div>
                    <div class="bar" style="height: 65%"></div>
                    <div class="bar" style="height: 50%"></div>
                    <div class="bar" style="height: 80%"></div>
                    <div class="bar" style="height: 70%"></div>
                    <div class="bar" style="height: 90%"></div>
                    <div class="bar" style="height: 75%"></div>
                  </div>
                </div>
              </div>
              <div class="chart-card">
                <h3>订单状态</h3>
                <div class="chart-placeholder">
                  <div class="pie-chart">
                    <div class="pie-slice pending"></div>
                    <div class="pie-slice paid"></div>
                    <div class="pie-slice shipped"></div>
                    <div class="pie-slice completed"></div>
                  </div>
                  <div class="legend">
                    <span class="legend-item"><span class="dot pending"></span> 待支付</span>
                    <span class="legend-item"><span class="dot paid"></span> 已支付</span>
                    <span class="legend-item"><span class="dot shipped"></span> 已发货</span>
                    <span class="legend-item"><span class="dot completed"></span> 已完成</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div v-else-if="currentMenu === 'users'" class="data-table">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>用户名</th>
                  <th>手机号</th>
                  <th>用户类型</th>
                  <th>状态</th>
                  <th>创建时间</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="user in users" :key="user.id">
                  <td>{{ user.id }}</td>
                  <td>{{ user.username }}</td>
                  <td>{{ user.phone }}</td>
                  <td :class="['type-badge', user.type]">{{ user.type === 'admin' ? '管理员' : '普通用户' }}</td>
                  <td :class="['status-badge', user.status]">{{ user.status === 'active' ? '正常' : '禁用' }}</td>
                  <td>{{ user.createTime }}</td>
                  <td>
                    <button class="action-btn edit">✏️</button>
                    <button class="action-btn delete">🗑️</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div v-else-if="currentMenu === 'flowers'" class="data-table">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>名称</th>
                  <th>分类</th>
                  <th>价格</th>
                  <th>库存</th>
                  <th>状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="flower in flowers" :key="flower.id">
                  <td>{{ flower.id }}</td>
                  <td>{{ flower.emoji }} {{ flower.name }}</td>
                  <td>{{ flower.category }}</td>
                  <td>¥{{ flower.price }}</td>
                  <td>{{ flower.stock }}</td>
                  <td :class="['status-badge', flower.status]">{{ flower.status === 'active' ? '上架' : '下架' }}</td>
                  <td>
                    <button class="action-btn edit">✏️</button>
                    <button class="action-btn delete">🗑️</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div v-else-if="currentMenu === 'orders'" class="data-table">
            <table>
              <thead>
                <tr>
                  <th>订单号</th>
                  <th>用户</th>
                  <th>金额</th>
                  <th>状态</th>
                  <th>支付状态</th>
                  <th>创建时间</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="order in orders" :key="order.id">
                  <td>{{ order.orderNo }}</td>
                  <td>{{ order.userName }}</td>
                  <td>¥{{ order.amount }}</td>
                  <td :class="['status-badge', order.status]">{{ getStatusText(order.status) }}</td>
                  <td :class="['status-badge', order.payStatus]">{{ order.payStatus === 'paid' ? '已支付' : '未支付' }}</td>
                  <td>{{ order.createTime }}</td>
                  <td>
                    <button class="action-btn view">👁️</button>
                    <button class="action-btn edit">✏️</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div v-else-if="currentMenu === 'diy'" class="diy-management">
            <div class="diy-grid">
              <div 
                v-for="diy in diyList" 
                :key="diy.id"
                class="diy-card"
              >
                <div class="diy-preview">
                  <span class="diy-icon">{{ diy.icon }}</span>
                </div>
                <div class="diy-info">
                  <h4>{{ diy.name }}</h4>
                  <p class="diy-price">¥{{ diy.price }}</p>
                  <p class="diy-status">{{ diy.status }}</p>
                </div>
                <div class="diy-actions">
                  <button class="action-btn edit">✏️</button>
                  <button class="action-btn delete">🗑️</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const menuItems = [
  { id: 'dashboard', name: '数据概览', icon: '📊' },
  { id: 'users', name: '用户管理', icon: '👥' },
  { id: 'flowers', name: '花卉管理', icon: '🌸' },
  { id: 'orders', name: '订单管理', icon: '🛒' },
  { id: 'diy', name: 'DIY管理', icon: '🎨' }
]

const currentMenu = ref('dashboard')

const currentMenuName = computed(() => {
  const menu = menuItems.find(m => m.id === currentMenu.value)
  return menu ? menu.name : ''
})

const users = ref([
  { id: 1, username: 'admin', phone: '13900000000', type: 'admin', status: 'active', createTime: '2024-01-01' },
  { id: 2, username: 'user001', phone: '13800000001', type: 'user', status: 'active', createTime: '2024-01-05' },
  { id: 3, username: 'user002', phone: '13800000002', type: 'user', status: 'active', createTime: '2024-01-10' },
  { id: 4, username: 'user003', phone: '13800000003', type: 'user', status: 'disabled', createTime: '2024-01-15' }
])

const flowers = ref([
  { id: 1, name: '红玫瑰', emoji: '🌹', category: '玫瑰', price: 15.00, stock: 100, status: 'active' },
  { id: 2, name: '白百合', emoji: '💮', category: '百合', price: 20.00, stock: 60, status: 'active' },
  { id: 3, name: '粉郁金香', emoji: '🌷', category: '郁金香', price: 18.00, stock: 75, status: 'active' },
  { id: 4, name: '向日葵', emoji: '🌻', category: '向日葵', price: 12.00, stock: 120, status: 'active' },
  { id: 5, name: '康乃馨', emoji: '🌺', category: '康乃馨', price: 10.00, stock: 150, status: 'active' },
  { id: 6, name: '尤加利叶', emoji: '🌿', category: '配叶', price: 5.00, stock: 200, status: 'active' }
])

const orders = ref([
  { id: 1, orderNo: 'ORD20240101001', userName: 'user001', amount: 150.00, status: 'completed', payStatus: 'paid', createTime: '2024-01-01 10:30' },
  { id: 2, orderNo: 'ORD20240102002', userName: 'user002', amount: 280.00, status: 'shipped', payStatus: 'paid', createTime: '2024-01-02 14:20' },
  { id: 3, orderNo: 'ORD20240103003', userName: 'user001', amount: 120.00, status: 'pending', payStatus: 'unpaid', createTime: '2024-01-03 09:15' },
  { id: 4, orderNo: 'ORD20240104004', userName: 'user003', amount: 320.00, status: 'paid', payStatus: 'paid', createTime: '2024-01-04 16:45' }
])

const diyList = ref([
  { id: 1, name: '情人节花束', icon: '💝', price: 188.00, status: '已保存' },
  { id: 2, name: '生日花束', icon: '🎂', price: 258.00, status: '已提交' },
  { id: 3, name: '毕业花束', icon: '🎓', price: 168.00, status: '草稿' }
])

const getStatusText = (status) => {
  const statusMap = {
    'pending': '待支付',
    'paid': '已支付',
    'shipped': '已发货',
    'completed': '已完成',
    'canceled': '已取消'
  }
  return statusMap[status] || status
}
</script>

<style scoped>
.admin-page {
  min-height: 100vh;
}

.admin-container {
  display: grid;
  grid-template-columns: 200px 1fr;
  min-height: 600px;
}

.sidebar {
  background: linear-gradient(180deg, #2d3748 0%, #1a202c 100%);
  border-radius: 15px;
  padding: 1.5rem;
  margin-right: 1rem;
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: white;
  font-size: 1.1rem;
  font-weight: bold;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  margin-bottom: 1rem;
}

.admin-icon {
  font-size: 1.3rem;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}

.menu-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.7rem 1rem;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: #a0aec0;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s ease;
  text-align: left;
}

.menu-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

.menu-btn.active {
  background: linear-gradient(135deg, #ff6b9d 0%, #c44569 100%);
  color: white;
}

.admin-content {
  background: white;
  border-radius: 15px;
  padding: 1.5rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #f0f0f0;
}

.content-header h2 {
  font-size: 1.4rem;
  color: #333;
}

.header-actions {
  display: flex;
  gap: 0.5rem;
}

.add-btn {
  background: linear-gradient(135deg, #ff6b9d 0%, #c44569 100%);
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.add-btn:hover {
  transform: translateY(-2px);
}

.refresh-btn {
  background: #f0f0f0;
  color: #666;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.refresh-btn:hover {
  background: #e0e0e0;
}

.content-body {
  min-height: 400px;
}

.dashboard {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1rem;
}

.stat-card {
  background: linear-gradient(135deg, #f5f7fa 0%, #e4efe9 100%);
  border-radius: 12px;
  padding: 1.5rem;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.stat-icon {
  font-size: 2.5rem;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 0.85rem;
  color: #666;
}

.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.chart-card {
  background: #f9f9f9;
  border-radius: 12px;
  padding: 1.5rem;
}

.chart-card h3 {
  font-size: 1rem;
  margin-bottom: 1rem;
  color: #333;
}

.chart-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 1rem;
}

.bar-chart {
  display: flex;
  align-items: flex-end;
  gap: 1rem;
  height: 150px;
  width: 100%;
  justify-content: center;
}

.bar {
  width: 30px;
  background: linear-gradient(180deg, #ff6b9d 0%, #c44569 100%);
  border-radius: 4px 4px 0 0;
  transition: height 0.5s ease;
}

.pie-chart {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: conic-gradient(
    #ff6b9d 0deg 90deg,
    #ffa8a8 90deg 180deg,
    #ffc876 180deg 270deg,
    #98d8aa 270deg 360deg
  );
  margin-bottom: 1rem;
}

.legend {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.85rem;
  color: #666;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.dot.pending { background: #ff6b9d; }
.dot.paid { background: #ffa8a8; }
.dot.shipped { background: #ffc876; }
.dot.completed { background: #98d8aa; }

.data-table {
  overflow-x: auto;
}

.data-table table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 0.8rem;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

.data-table th {
  background: #f9f9f9;
  font-weight: 600;
  color: #666;
}

.type-badge {
  padding: 0.3rem 0.6rem;
  border-radius: 15px;
  font-size: 0.8rem;
}

.type-badge.admin {
  background: #ffebf0;
  color: #c44569;
}

.type-badge.user {
  background: #e8f5e9;
  color: #2e7d32;
}

.status-badge {
  padding: 0.3rem 0.6rem;
  border-radius: 15px;
  font-size: 0.8rem;
}

.status-badge.active, .status-badge.paid, .status-badge.shipped, .status-badge.completed {
  background: #e8f5e9;
  color: #2e7d32;
}

.status-badge.disabled, .status-badge.pending, .status-badge.unpaid {
  background: #fff3e0;
  color: #ef6c00;
}

.action-btn {
  margin-right: 0.3rem;
  padding: 0.4rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.85rem;
  transition: all 0.3s ease;
}

.action-btn.edit {
  background: #e3f2fd;
  color: #1976d2;
}

.action-btn.delete {
  background: #ffebee;
  color: #c62828;
}

.action-btn.view {
  background: #e8f5e9;
  color: #2e7d32;
}

.action-btn:hover {
  transform: scale(1.1);
}

.diy-management {
  padding: 1rem;
}

.diy-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
}

.diy-card {
  background: #f9f9f9;
  border-radius: 12px;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.diy-preview {
  width: 80px;
  height: 80px;
  background: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 1rem;
}

.diy-icon {
  font-size: 2.5rem;
}

.diy-info {
  text-align: center;
  margin-bottom: 1rem;
}

.diy-info h4 {
  margin-bottom: 0.3rem;
}

.diy-price {
  font-size: 1.1rem;
  font-weight: bold;
  color: #c44569;
  margin-bottom: 0.3rem;
}

.diy-status {
  font-size: 0.8rem;
  color: #666;
}

.diy-actions {
  display: flex;
  gap: 0.5rem;
}
</style>