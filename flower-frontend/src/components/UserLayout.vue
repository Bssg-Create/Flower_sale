<template>
  <div class="user-layout">
    <header class="header">
      <div class="header-left">
        <span class="logo-icon">🌸</span>
        <span class="logo-text">花卉销售系统</span>
      </div>
      <nav class="nav">
        <router-link to="/user/home" :class="['nav-link', { active: $route.path === '/user/home' }]">
          🏠 首页
        </router-link>
        <router-link to="/user/diy" :class="['nav-link', { active: $route.path === '/user/diy' }]">
          🎨 DIY花束
        </router-link>
        <router-link to="/user/plans" :class="['nav-link', { active: $route.path.startsWith('/user/plan') }]">
          📋 DIY花束方案
        </router-link>
      </nav>
      <div class="header-right">
        <span class="user-info">👤 {{ username }}</span>
        <button class="logout-btn" @click="handleLogout">退出登录</button>
      </div>
    </header>

    <main class="content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const username = ref(localStorage.getItem('username') || '用户')

onMounted(() => {
  username.value = localStorage.getItem('username') || '用户'
})

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userId')
  localStorage.removeItem('userType')
  localStorage.removeItem('username')
  router.push('/login')
}
</script>

<style scoped>
.user-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4efe9 100%);
}

.header {
  background: linear-gradient(135deg, #ff6b9d 0%, #c44569 100%);
  color: white;
  padding: 1rem 2rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
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

.nav {
  display: flex;
  gap: 1rem;
}

.nav-link {
  color: rgba(255, 255, 255, 0.9);
  text-decoration: none;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  transition: all 0.3s;
}

.nav-link:hover {
  background: rgba(255, 255, 255, 0.2);
}

.nav-link.active {
  background: white;
  color: #c44569;
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
  transition: all 0.3s;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.content {
  flex: 1;
  padding: 2rem;
}
</style>