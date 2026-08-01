<template>
  <div class="admin-layout">
    <header class="header">
      <div class="header-left">
        <span class="logo-icon">花</span>
        <div class="logo-copy">
          <strong>花序管理台</strong>
          <span>鲜花销售管理系统</span>
        </div>
      </div>
      <div class="header-right">
        <span class="user-info">管理员 · {{ username }}</span>
        <button class="logout-btn" @click="handleLogout">退出登录</button>
      </div>
    </header>

    <div class="body">
      <aside class="sidebar">
        <nav>
          <router-link to="/admin" class="menu-item" exact-active-class="active">
            <span class="menu-index">01</span> 数据概览
          </router-link>
          <router-link to="/admin/users" class="menu-item" active-class="active">
            <span class="menu-index">02</span> 用户管理
          </router-link>
          <router-link to="/admin/flowers" class="menu-item" active-class="active">
            <span class="menu-index">03</span> 花卉管理
          </router-link>
          <router-link to="/admin/orders" class="menu-item" active-class="active">
            <span class="menu-index">04</span> 订单管理
          </router-link>
          <router-link to="/admin/diy" class="menu-item" active-class="active">
            <span class="menu-index">05</span> DIY 管理
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
  background: var(--color-canvas);
}

.header {
  min-height: 72px;
  background: #29453d;
  color: white;
  padding: 0.85rem 1.5rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 8px 24px rgba(32, 59, 51, 0.14);
  position: sticky;
  top: 0;
  z-index: 20;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.logo-icon {
  width: 38px;
  height: 38px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  color: #29453d;
  background: #f6e8ec;
  font-family: Georgia, "Times New Roman", serif;
  font-size: 1.15rem;
  font-weight: 700;
}

.logo-copy { display: flex; flex-direction: column; gap: 0.08rem; }
.logo-copy strong { font-size: 1rem; letter-spacing: 0.04em; }
.logo-copy span { color: rgba(255, 255, 255, 0.62); font-size: 0.73rem; }
.logo-text { display: none; }

.body {
  flex: 1;
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.user-info {
  color: rgba(255, 255, 255, 0.76);
  font-size: 0.86rem;
}

.logout-btn {
  background: transparent;
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.22);
  padding: 0.5rem 0.85rem;
  border-radius: var(--radius-control);
  cursor: pointer;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.sidebar {
  background: #314f46;
  padding: 1.25rem 0.9rem;
}

.sidebar nav { position: sticky; top: 96px; }

.menu-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.78rem 0.85rem;
  color: rgba(255, 255, 255, 0.68);
  text-decoration: none;
  border-radius: 8px;
  margin-bottom: 0.3rem;
  transition: all var(--ease-standard);
}

.menu-item:hover {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

.menu-item.active {
  background: #f7eef1;
  color: var(--color-brand-strong);
  box-shadow: 0 8px 20px rgba(27, 50, 43, 0.18);
}

.menu-index { width: 24px; color: currentColor; font-size: 0.68rem; font-weight: 750; opacity: 0.66; }

.content {
  flex: 1;
  min-width: 0;
  padding: clamp(1rem, 2.4vw, 2rem);
  background: var(--color-canvas);
  overflow-y: auto;
}

.router-link-exact-active.active {
  background: #f7eef1;
  color: var(--color-brand-strong);
}

@media (max-width: 860px) {
  .header { padding-inline: 1rem; }
  .user-info, .logo-copy span { display: none; }
  .body { display: block; }
  .sidebar { padding: 0.6rem; overflow-x: auto; }
  .sidebar nav { position: static; display: flex; min-width: max-content; }
  .menu-item { margin: 0 0.25rem 0 0; padding: 0.65rem 0.8rem; }
  .menu-index { display: none; }
}
</style>
