<template>
  <div class="auth-page">
    <section class="auth-visual" aria-label="花卉销售系统介绍">
      <div class="brand-mark">花</div>
      <div class="visual-copy">
        <p class="brand-name">花卉销售系统</p>
        <h1>选一束花，<br />把心意认真送达。</h1>
        <p>浏览鲜花、设计专属花束，并在同一个系统中完成下单与管理。</p>
      </div>
      <div class="flower-scene" aria-hidden="true">
        <span class="scene-disc"></span>
        <img class="flower flower-main" :src="'/images/diy/pink-lily.webp'" alt="" />
        <img class="flower flower-side" :src="'/images/diy/red-rose.webp'" alt="" />
        <img class="flower flower-leaf" :src="'/images/diy/eucalyptus.webp'" alt="" />
      </div>
      <p class="visual-note">从日常选购到 DIY 花束设计</p>
    </section>

    <main class="auth-panel">
      <div class="auth-card">
        <div class="auth-header">
          <p>欢迎回来</p>
          <h2>登录您的账号</h2>
          <span>选择身份后继续进入系统</span>
        </div>

        <div class="auth-tabs" aria-label="登录身份">
          <button type="button" :class="['tab-btn', { active: userType === 'user' }]" @click="userType = 'user'">普通用户</button>
          <button type="button" :class="['tab-btn', { active: userType === 'admin' }]" @click="userType = 'admin'">管理员</button>
        </div>

        <div class="auth-error" role="alert" v-if="errorMsg">{{ errorMsg }}</div>

        <form @submit.prevent="handleLogin" class="auth-form">
          <div class="form-group">
            <label for="login-username">用户名</label>
            <input id="login-username" v-model="username" type="text" autocomplete="username" placeholder="请输入用户名" required />
          </div>
          <div class="form-group">
            <label for="login-password">密码</label>
            <input id="login-password" v-model="password" type="password" autocomplete="current-password" placeholder="请输入密码" required />
          </div>
          <button type="submit" class="submit-btn" :disabled="loading">
            {{ loading ? '正在登录' : '登录' }}
          </button>
        </form>

        <div class="auth-footer">
          还没有账号？<router-link to="/register">创建普通用户账号</router-link>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'

const router = useRouter()
const username = ref('')
const password = ref('')
const userType = ref('user')
const errorMsg = ref('')
const loading = ref(false)

const handleLogin = async () => {
  errorMsg.value = ''
  loading.value = true
  try {
    const res = await api.post('/login', {
      username: username.value,
      password: password.value,
      userType: userType.value
    })
    const data = res.data.data
    localStorage.setItem('token', data.token)
    localStorage.setItem('userId', data.id)
    localStorage.setItem('userType', data.userType)
    localStorage.setItem('username', data.username)

    if (data.userType === 'admin') {
      router.push('/admin')
    } else {
      router.push('/user/home')
    }
  } catch (err) {
    errorMsg.value = err.response?.data?.message || '登录失败，请重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100dvh;
  display: grid;
  grid-template-columns: minmax(460px, 1.08fr) minmax(440px, 0.92fr);
  background: var(--color-canvas);
}

.auth-visual {
  position: relative;
  min-height: 100dvh;
  overflow: hidden;
  padding: clamp(2rem, 5vw, 5rem);
  display: flex;
  flex-direction: column;
  color: #f8fbf8;
  background:
    linear-gradient(145deg, rgba(29, 57, 49, 0.96), rgba(43, 82, 69, 0.93)),
    #2f5147;
}

.auth-visual::after {
  content: "";
  position: absolute;
  inset: auto -16% -24% 22%;
  height: 58%;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.045);
  transform: rotate(-9deg);
}

.brand-mark {
  position: relative;
  z-index: 2;
  width: 46px;
  height: 46px;
  display: grid;
  place-items: center;
  border: 1px solid rgba(255, 255, 255, 0.38);
  border-radius: 14px;
  font-weight: 800;
  font-size: 1.15rem;
  background: rgba(255, 255, 255, 0.08);
}

.visual-copy {
  position: relative;
  z-index: 2;
  max-width: 560px;
  margin-top: clamp(3rem, 10vh, 7rem);
}

.brand-name {
  margin-bottom: 1.1rem;
  color: #d9e8df;
  font-weight: 650;
}

.visual-copy h1 {
  max-width: 10ch;
  font-size: clamp(2.8rem, 5.2vw, 5rem);
  line-height: 1.08;
  letter-spacing: -0.035em;
  text-wrap: balance;
}

.visual-copy > p:last-child {
  max-width: 34rem;
  margin-top: 1.5rem;
  color: #d2e0d8;
  font-size: 1rem;
}

.flower-scene {
  position: absolute;
  z-index: 1;
  right: clamp(-3rem, 2vw, 2rem);
  bottom: -3rem;
  width: min(49vw, 620px);
  height: min(52vh, 540px);
  pointer-events: none;
}

.scene-disc {
  position: absolute;
  right: 3%;
  bottom: 2%;
  width: 72%;
  aspect-ratio: 1;
  border-radius: 50%;
  background: #edf2ed;
  box-shadow: 0 24px 80px rgba(12, 30, 25, 0.28);
}

.flower {
  position: absolute;
  object-fit: contain;
  filter: drop-shadow(0 24px 22px rgba(18, 35, 30, 0.22));
  transform-origin: 50% 100%;
}

.flower-main { right: 14%; bottom: 5%; width: 41%; transform: rotate(3deg); }
.flower-side { right: 43%; bottom: 2%; width: 32%; transform: rotate(-17deg); }
.flower-leaf { right: 2%; bottom: 0; width: 36%; transform: rotate(20deg); }

.visual-note {
  position: absolute;
  z-index: 2;
  left: clamp(2rem, 5vw, 5rem);
  bottom: 2.4rem;
  color: #c9d9d0;
  font-size: 0.88rem;
}

.auth-panel {
  width: 100%;
  min-width: 0;
  min-height: 100dvh;
  display: grid;
  place-items: center;
  padding: clamp(1.25rem, 5vw, 4rem);
  background: #f7f9f6;
}

.auth-card {
  width: 100%;
  max-width: 440px;
  min-width: 0;
  padding: clamp(1.4rem, 4vw, 2.6rem);
  border-radius: var(--radius-card);
  background: var(--color-surface);
  box-shadow: var(--shadow-card);
}

.auth-header {
  margin-bottom: 1.7rem;
}

.auth-header p {
  color: var(--color-brand);
  font-weight: 700;
  font-size: 0.9rem;
}

.auth-header h2 {
  margin: 0.25rem 0 0.45rem;
  color: var(--color-ink);
  font-size: clamp(1.7rem, 3vw, 2.15rem);
  letter-spacing: -0.025em;
}

.auth-header span {
  color: var(--color-ink-soft);
  font-size: 0.9rem;
}

.auth-tabs {
  display: flex;
  padding: 4px;
  gap: 4px;
  margin-bottom: 1.5rem;
  border-radius: 12px;
  background: var(--color-surface-soft);
}

.tab-btn {
  flex: 1;
  padding: 0.68rem;
  border: 0;
  border-radius: 9px;
  background: transparent;
  font-size: 0.92rem;
  font-weight: 650;
  cursor: pointer;
  color: var(--color-ink-soft);
}

.tab-btn.active {
  color: var(--color-brand-strong);
  background: var(--color-surface);
  box-shadow: 0 6px 16px rgba(37, 67, 58, 0.08);
}

.auth-error {
  color: #8f2f3a;
  background: #fbedef;
  padding: 0.75rem 0.9rem;
  border-radius: var(--radius-control);
  font-size: 0.85rem;
  margin-bottom: 1rem;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.45rem;
  color: var(--color-ink);
  font-size: 0.88rem;
  font-weight: 650;
}

.form-group input {
  width: 100%;
  min-height: 46px;
  padding: 0.72rem 0.9rem;
  border: 1px solid var(--color-line);
  border-radius: var(--radius-control);
  color: var(--color-ink);
  background: #fbfcfb;
  font-size: 0.95rem;
  outline: none;
}

.form-group input:focus {
  border-color: var(--color-brand);
  box-shadow: 0 0 0 3px rgba(166, 63, 95, 0.11);
  background: #ffffff;
}

.submit-btn {
  width: 100%;
  min-height: 48px;
  padding: 0.8rem 1rem;
  background: var(--color-brand);
  color: white;
  border: none;
  border-radius: var(--radius-control);
  font-size: 0.96rem;
  font-weight: 700;
  cursor: pointer;
  margin-top: 0.6rem;
  box-shadow: 0 10px 22px rgba(166, 63, 95, 0.22);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.submit-btn:hover:not(:disabled) {
  background: var(--color-brand-strong);
  transform: translateY(-1px);
}

.auth-footer {
  text-align: center;
  margin-top: 1.2rem;
  color: var(--color-ink-soft);
  font-size: 0.9rem;
}

.auth-footer a {
  color: var(--color-brand-strong);
  font-weight: 700;
  text-decoration: none;
}

@media (max-width: 900px) {
  .auth-page { width: 100%; min-width: 0; grid-template-columns: minmax(0, 1fr); overflow-x: hidden; }
  .auth-visual { display: none; }
  .auth-panel { padding: 1rem; }
  .auth-card { box-shadow: 0 12px 32px rgba(42, 70, 61, 0.1); }
}

@media (max-width: 420px) {
  .auth-card { padding: 1.25rem; }
  .auth-header h2 { font-size: 1.6rem; }
}
</style>
