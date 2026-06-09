<template>
  <div class="admin-layout">
    <header class="header">
      <div class="header-left">
        <span class="logo-icon">🌸</span>
        <span class="logo-text">花卉销售系统 - 管理后台</span>
      </div>
      <div class="header-right">
        <span class="user-info">🔧 {{ username }}</span>
        <button class="logout-btn" @click="handleLogout">退出登录</button>
      </div>
    </header>

    <div class="body">
      <aside class="sidebar">
        <nav>
          <router-link to="/admin" class="menu-item" exact-active-class="active">
            <span>📊</span> 数据概览
          </router-link>
          <router-link to="/admin/users" class="menu-item" active-class="active">
            <span>👥</span> 用户管理
          </router-link>
          <router-link to="/admin/flowers" class="menu-item" active-class="active">
            <span>🌸</span> 花卉管理
          </router-link>
          <router-link to="/admin/orders" class="menu-item" active-class="active">
            <span>🛒</span> 订单管理
          </router-link>
          <router-link to="/admin/diy" class="menu-item" active-class="active">
            <span>🎨</span> DIY管理
          </router-link>
        </nav>
      </aside>

      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const username = ref(localStorage.getItem('username') || '管理员')

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userId')
  localStorage.removeItem('userType')
  localStorage.removeItem('username')
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: linear-gradient(135deg, #2d3748 0%, #1a202c 100%);
  color: white;
  padding: 1rem 2rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.3rem;
  font-weight: bold;
}

.logo-icon {
  font-size: 1.8rem;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.user-info {
  font-size: 0.95rem;
}

.logout-btn {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: none;
  padding: 0.4rem 1rem;
  border-radius: 15px;
  cursor: pointer;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.body {
  flex: 1;
  display: flex;
}

.sidebar {
  width: 200px;
  background: linear-gradient(180deg, #2d3748 0%, #1a202c 100%);
  padding: 1rem;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.8rem 1rem;
  color: #a0aec0;
  text-decoration: none;
  border-radius: 8px;
  margin-bottom: 0.3rem;
  transition: all 0.3s;
}

.menu-item:hover {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

.menu-item.active {
  background: linear-gradient(135deg, #ff6b9d 0%, #c44569 100%);
  color: white;
}

.content {
  flex: 1;
  padding: 1.5rem;
  background: #f5f7fa;
  overflow-y: auto;
}

.router-link-exact-active.active {
  background: linear-gradient(135deg, #ff6b9d 0%, #c44569 100%);
  color: white;
}
</style>