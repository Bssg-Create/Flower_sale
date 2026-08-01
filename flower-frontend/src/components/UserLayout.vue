<template>
  <div class="user-layout">
    <header class="header">
      <div class="header-left">
        <span class="logo-icon">花</span>
        <span class="logo-text">花卉销售系统</span>
      </div>
      <nav class="nav">
        <router-link to="/user/home" :class="['nav-link', { active: $route.path === '/user/home' }]">
          首页
        </router-link>
        <router-link to="/user/diy" :class="['nav-link', { active: $route.path === '/user/diy' }]">
          DIY花束
        </router-link>
        <router-link to="/user/plans" :class="['nav-link', { active: $route.path.startsWith('/user/plan') }]">
          DIY花束方案
        </router-link>
      </nav>
      <div class="header-right">
        <span class="user-info"><span class="user-avatar">{{ username.slice(0, 1) }}</span>{{ username }}</span>
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
  background: var(--color-bg);
}

.header {
  position: sticky;
  top: 0;
  z-index: 40;
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 4.5rem;
  padding: 0.75rem max(1.5rem, calc((100vw - 1320px) / 2));
  color: var(--color-ink);
  background: rgba(255, 253, 250, 0.96);
  border-bottom: 1px solid var(--color-line);
  box-shadow: 0 5px 18px rgba(40, 63, 49, 0.04);
}

.header-left {
  display: flex;
  align-items: center;
  flex: 0 0 auto;
  gap: 0.65rem;
  color: var(--color-leaf-dark);
  font-size: 1.05rem;
  font-weight: 750;
  letter-spacing: 0.02em;
}

.logo-icon {
  width: 2.15rem;
  height: 2.15rem;
  display: grid;
  place-items: center;
  color: var(--color-surface-strong);
  background: var(--color-leaf);
  border-radius: 50%;
  font-family: "STKaiti", "KaiTi", serif;
  font-size: 1.05rem;
}

.nav {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  margin: 0 1.25rem;
}

.nav-link {
  position: relative;
  padding: 0.65rem 0.85rem;
  color: var(--color-muted);
  text-decoration: none;
  border-radius: var(--radius-control);
  font-size: 0.9rem;
  font-weight: 650;
  white-space: nowrap;
  transition: color 160ms ease, background-color 160ms ease;
}

.nav-link:hover {
  color: var(--color-leaf-dark);
  background: var(--color-leaf-soft);
}

.nav-link.active {
  color: var(--color-primary-dark);
  background: var(--color-primary-soft);
}

.header-right {
  display: flex;
  align-items: center;
  flex: 0 0 auto;
  gap: 0.75rem;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 0.45rem;
  color: var(--color-muted);
  font-size: 0.84rem;
  white-space: nowrap;
}

.user-avatar {
  width: 1.8rem;
  height: 1.8rem;
  display: grid;
  place-items: center;
  color: var(--color-primary-dark);
  background: var(--color-primary-soft);
  border-radius: 50%;
  font-size: 0.76rem;
  font-weight: 700;
}

.logout-btn {
  min-height: 2.25rem;
  padding: 0.4rem 0.8rem;
  color: var(--color-muted);
  background: transparent;
  border: 1px solid var(--color-line);
  border-radius: var(--radius-control);
  font-size: 0.82rem;
  cursor: pointer;
  transition: color 160ms ease, border-color 160ms ease, background-color 160ms ease;
}

.logout-btn:hover {
  color: var(--color-primary-dark);
  background: var(--color-primary-soft);
  border-color: #d7aeb7;
}

.content {
  flex: 1;
  width: 100%;
  padding: 2rem max(1.5rem, calc((100vw - 1320px) / 2)) 4rem;
}

@media (max-width: 860px) {
  .header {
    flex-wrap: wrap;
    gap: 0.6rem 1rem;
    padding: 0.7rem 1rem 0;
  }

  .header-left {
    flex: 1;
  }

  .nav {
    order: 3;
    width: calc(100% + 2rem);
    margin: 0 -1rem;
    padding: 0 1rem 0.55rem;
    overflow-x: auto;
    scrollbar-width: none;
  }

  .nav::-webkit-scrollbar {
    display: none;
  }

  .nav-link {
    flex: 0 0 auto;
    padding: 0.55rem 0.75rem;
  }

  .content {
    padding: 1rem 1rem 5rem;
  }
}

@media (max-width: 520px) {
  .logo-text {
    font-size: 0.94rem;
  }

  .user-info {
    font-size: 0;
  }

  .user-avatar {
    font-size: 0.74rem;
  }

  .logout-btn {
    padding-inline: 0.65rem;
  }
}
</style>
