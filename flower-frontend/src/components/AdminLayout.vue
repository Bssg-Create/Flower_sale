<template>
  <div class="admin-layout">
    <header class="header">
      <div class="header-left">
        <span class="logo-icon">花</span>
        <span class="logo-text">花卉销售系统 - 管理后台</span>
      </div>
      <div class="header-right">
        <span class="user-info">{{ username }}</span>
        <button class="logout-btn" @click="handleLogout">退出登录</button>
      </div>
    </header>

    <div class="body">
      <aside class="sidebar">
        <nav>
          <router-link to="/admin" class="menu-item" exact-active-class="active">
            <span>概</span> 数据概览
          </router-link>
          <router-link to="/admin/users" class="menu-item" active-class="active">
            <span>用</span> 用户管理
          </router-link>
          <router-link to="/admin/flowers" class="menu-item" active-class="active">
            <span>花</span> 花卉管理
          </router-link>
          <router-link to="/admin/orders" class="menu-item" active-class="active">
            <span>单</span> 订单管理
          </router-link>
          <router-link to="/admin/diy" class="menu-item" active-class="active">
            <span>定</span> DIY管理
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
  color: var(--color-ink);
  background: var(--color-bg);
}

.header {
  min-height: 4.25rem;
  padding: 0.75rem 1.5rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: var(--color-ink);
  background: var(--color-surface-strong);
  border-bottom: 1px solid var(--color-line);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 0.65rem;
  color: var(--color-leaf-dark);
  font-size: 1.05rem;
  font-weight: 750;
}

.logo-icon {
  width: 2.1rem;
  height: 2.1rem;
  display: grid;
  place-items: center;
  color: #f8f3ed;
  background: var(--color-leaf);
  border-radius: 50%;
  font-family: "STKaiti", "KaiTi", serif;
  font-size: 1rem;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.user-info {
  color: var(--color-muted);
  font-size: 0.84rem;
}

.logout-btn {
  color: var(--color-muted);
  background: transparent;
  border: 1px solid var(--color-line);
  padding: 0.4rem 1rem;
  border-radius: var(--radius-control);
  cursor: pointer;
}

.logout-btn:hover {
  color: var(--color-primary-dark);
  background: var(--color-primary-soft);
}

.body {
  flex: 1;
  display: flex;
}

.sidebar {
  flex: 0 0 210px;
  padding: 1rem 0.85rem;
  background: #263e33;
  border-right: 1px solid rgba(255,255,255,0.05);
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 0.65rem;
  padding: 0.8rem 1rem;
  color: rgba(245,248,244,0.68);
  text-decoration: none;
  border-radius: 8px;
  margin-bottom: 0.25rem;
  font-size: 0.88rem;
  transition: color 160ms ease, background-color 160ms ease;
}

.menu-item span {
  width: 1.6rem;
  height: 1.6rem;
  display: grid;
  place-items: center;
  color: inherit;
  background: rgba(255,255,255,0.08);
  border-radius: 5px;
  font-size: 0.7rem;
  font-weight: 700;
}

.menu-item:hover {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

.menu-item.active {
  color: #ffffff;
  background: rgba(255,255,255,0.12);
}

.content {
  flex: 1;
  min-width: 0;
  padding: 1.5rem;
  background: var(--color-bg);
  overflow-y: auto;
}

.router-link-exact-active.active {
  background: rgba(255,255,255,0.12);
  color: white;
}

@media (max-width: 800px) {
  .header { padding: 0.7rem 1rem; }
  .logo-text { font-size: 0.9rem; }
  .user-info { display: none; }
  .body { display: block; }
  .sidebar { width: 100%; padding: 0.55rem 0.75rem; overflow-x: auto; }
  .sidebar nav { display: flex; min-width: max-content; gap: 0.25rem; }
  .menu-item { margin: 0; padding: 0.6rem 0.75rem; white-space: nowrap; }
  .content { padding: 1rem; }
}
</style>
