<template>
  <div class="user-layout">
    <header class="header">
      <div class="header-left">
        <span class="logo-icon">花</span>
        <span class="logo-text">花卉销售系统</span>
      </div>
      <nav class="nav">
        <router-link to="/user/home" :class="['nav-link', { active: $route.path === '/user/home' }]">
          花店首页
        </router-link>
        <router-link to="/user/diy" :class="['nav-link', { active: $route.path === '/user/diy' }]">
          DIY 花束
        </router-link>
        <router-link to="/user/plans" :class="['nav-link', { active: $route.path.startsWith('/user/plan') }]">
          我的方案
        </router-link>
      </nav>
      <div class="header-right">
        <span class="user-info"><small>当前用户</small>{{ username }}</span>
        <button class="logout-btn" @click="handleLogout">退出登录</button>
      </div>
    </header>

    <main class="content">
      <router-view />
    </main>

    <AiAssistant />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AiAssistant from './AiAssistant.vue'

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
  background: var(--color-canvas);
}

.header {
  position: sticky;
  top: 0;
  z-index: 40;
  min-height: 72px;
  color: var(--color-ink);
  padding: 0.7rem clamp(1rem, 4vw, 3rem);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1.25rem;
  border-bottom: 1px solid rgba(49, 79, 70, 0.1);
  background: rgba(248, 250, 247, 0.94);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 0.7rem;
  font-size: 1.05rem;
  font-weight: 750;
  white-space: nowrap;
}

.logo-icon {
  width: 38px;
  height: 38px;
  display: grid;
  place-items: center;
  border-radius: 12px;
  color: #ffffff;
  background: var(--color-forest);
  font-size: 0.95rem;
}

.nav {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  padding: 4px;
  border-radius: 12px;
  background: var(--color-surface-soft);
}

.nav-link {
  color: var(--color-ink-soft);
  text-decoration: none;
  padding: 0.55rem 0.9rem;
  border-radius: 9px;
  font-size: 0.9rem;
  font-weight: 650;
  white-space: nowrap;
}

.nav-link:hover {
  color: var(--color-brand-strong);
  background: rgba(255, 255, 255, 0.7);
}

.nav-link.active {
  color: var(--color-brand-strong);
  background: var(--color-surface);
  box-shadow: 0 5px 14px rgba(42, 70, 61, 0.08);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  color: var(--color-ink);
  font-size: 0.88rem;
  font-weight: 700;
  line-height: 1.2;
}

.user-info small {
  color: var(--color-muted);
  font-size: 0.67rem;
  font-weight: 500;
}

.logout-btn {
  color: var(--color-ink-soft);
  background: transparent;
  border: 1px solid var(--color-line);
  padding: 0.48rem 0.78rem;
  border-radius: 9px;
  cursor: pointer;
  font-size: 0.82rem;
}

.logout-btn:hover {
  color: var(--color-brand-strong);
  border-color: rgba(166, 63, 95, 0.32);
  background: var(--color-surface-rose);
}

.content {
  flex: 1;
  padding: clamp(1.2rem, 3vw, 2.5rem);
}

@media (max-width: 900px) {
  .header {
    position: static;
    flex-wrap: wrap;
  }

  .nav {
    order: 3;
    width: 100%;
    overflow-x: auto;
  }

  .nav-link { flex: 1; text-align: center; }
}

@media (max-width: 560px) {
  .logo-text { display: none; }
  .user-info small { display: none; }
  .content { padding: 0.85rem; }
}
</style>
