<template>
  <div class="auth-page">
    <div class="auth-card">
      <section class="auth-showcase" aria-label="鲜花定制介绍">
        <div class="brand-lockup">
          <span class="brand-mark">花</span>
          <span>花卉销售系统</span>
        </div>
        <div class="showcase-copy">
          <p class="eyebrow">花礼目录与定制工作台</p>
          <h1>为每一束花，留下一份清晰的心意。</h1>
          <p>选购鲜花、设计专属花束，并保存你的 DIY 方案。</p>
        </div>
        <div class="botanical-composition" aria-hidden="true">
          <img class="stem stem-main" :src="diyAsset('red-rose.webp')" alt="" />
          <img class="stem stem-soft" :src="diyAsset('white-rose.webp')" alt="" />
          <img class="stem stem-leaf" :src="diyAsset('eucalyptus.webp')" alt="" />
        </div>
      </section>

      <section class="auth-panel">
        <div class="auth-header">
          <p class="eyebrow">欢迎回来</p>
          <h2>登录账号</h2>
          <p>继续管理你的鲜花订单与花束方案</p>
        </div>

        <div class="auth-tabs" aria-label="登录身份">
          <button type="button" :class="['tab-btn', { active: userType === 'user' }]" @click="userType = 'user'">用户登录</button>
          <button type="button" :class="['tab-btn', { active: userType === 'admin' }]" @click="userType = 'admin'">管理员登录</button>
        </div>

        <div class="auth-error" role="alert" v-if="errorMsg">{{ errorMsg }}</div>

        <form @submit.prevent="handleLogin" class="auth-form">
          <div class="form-group">
            <label for="login-username">用户名</label>
            <input id="login-username" v-model="username" type="text" placeholder="请输入用户名" autocomplete="username" required />
          </div>
          <div class="form-group">
            <label for="login-password">密码</label>
            <input id="login-password" v-model="password" type="password" placeholder="请输入密码" autocomplete="current-password" required />
          </div>
          <button type="submit" class="submit-btn" :disabled="loading">
            {{ loading ? '登录中...' : '登录' }}
          </button>
        </form>

        <div class="auth-footer">
          还没有账号？<router-link to="/register">立即注册</router-link>
        </div>
      </section>
    </div>
    <div class="auth-page-note">鲜花销售管理系统 · 毕业设计演示</div>
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
const diyAsset = (name) => `/images/diy/${name}`

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
  min-height: 100vh;
  display: grid;
  place-items: center;
  gap: 1rem;
  padding: 2.5rem 1.5rem 1.25rem;
  background:
    radial-gradient(circle at 8% 12%, rgba(157, 65, 85, 0.07), transparent 28rem),
    var(--color-bg);
}

.auth-card {
  width: min(960px, 100%);
  min-height: 590px;
  display: grid;
  grid-template-columns: 1.04fr 0.96fr;
  overflow: hidden;
  background: var(--color-surface-strong);
  border: 1px solid rgba(54, 95, 75, 0.14);
  border-radius: var(--radius-feature);
  box-shadow: var(--shadow-lift);
}

.auth-showcase {
  position: relative;
  min-height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 2.25rem 2.5rem;
  color: #f8f3ed;
  background: #304f40;
}

.brand-lockup {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 0.95rem;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.brand-mark {
  width: 2rem;
  height: 2rem;
  display: grid;
  place-items: center;
  color: #304f40;
  background: #f8f3ed;
  border-radius: 50%;
  font-family: "STKaiti", "KaiTi", serif;
  font-size: 1.05rem;
}

.showcase-copy {
  position: relative;
  z-index: 2;
  max-width: 25rem;
  margin-top: 5.5rem;
}

.eyebrow {
  margin-bottom: 0.65rem;
  color: var(--color-primary);
  font-size: 0.78rem;
  font-weight: 700;
  letter-spacing: 0.12em;
}

.showcase-copy .eyebrow {
  color: #d8b7bd;
}

.showcase-copy h1 {
  max-width: 8.5em;
  font-family: "STSong", "SimSun", serif;
  font-size: clamp(2.15rem, 4vw, 3.3rem);
  font-weight: 600;
  line-height: 1.22;
  letter-spacing: -0.03em;
}

.showcase-copy > p:last-child {
  max-width: 23rem;
  margin-top: 1.25rem;
  color: rgba(248, 243, 237, 0.72);
  font-size: 0.94rem;
  line-height: 1.8;
}

.botanical-composition {
  position: absolute;
  right: -4rem;
  bottom: -4rem;
  width: 21rem;
  height: 24rem;
  opacity: 0.88;
  pointer-events: none;
}

.stem {
  position: absolute;
  object-fit: contain;
  filter: drop-shadow(0 12px 20px rgba(15, 33, 24, 0.22));
}

.stem-main {
  right: 3rem;
  bottom: 0;
  width: 10.5rem;
  transform: rotate(12deg);
}

.stem-soft {
  right: 9.5rem;
  bottom: 1rem;
  width: 8.5rem;
  transform: rotate(-12deg);
}

.stem-leaf {
  right: 0;
  bottom: 1.25rem;
  width: 11rem;
  transform: rotate(24deg);
}

.auth-panel {
  align-self: center;
  padding: 3.25rem 3.5rem;
}

.auth-header {
  margin-bottom: 1.75rem;
}

.auth-header h2 {
  margin-bottom: 0.45rem;
  color: var(--color-ink);
  font-size: 1.75rem;
  line-height: 1.25;
}

.auth-header p {
  color: var(--color-muted);
  font-size: 0.88rem;
}

.auth-tabs {
  display: flex;
  padding: 0.25rem;
  margin-bottom: 1.4rem;
  background: #f0f3ef;
  border: 1px solid var(--color-line);
  border-radius: 10px;
}

.tab-btn {
  flex: 1;
  min-height: 2.6rem;
  padding: 0.55rem 0.75rem;
  border: 0;
  border-radius: 7px;
  color: var(--color-muted);
  background: transparent;
  font-size: 0.88rem;
  font-weight: 600;
  cursor: pointer;
  transition: color 160ms ease, background-color 160ms ease, box-shadow 160ms ease;
}

.tab-btn.active {
  color: var(--color-primary-dark);
  background: var(--color-surface-strong);
  box-shadow: 0 2px 8px rgba(40, 63, 49, 0.08);
}

.auth-error {
  padding: 0.7rem 0.85rem;
  margin-bottom: 1rem;
  color: var(--color-danger);
  background: #fbebee;
  border: 1px solid #edc5cc;
  border-radius: var(--radius-control);
  font-size: 0.84rem;
}

.form-group {
  margin-bottom: 1.1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.45rem;
  color: var(--color-ink);
  font-size: 0.84rem;
  font-weight: 650;
}

.form-group input {
  width: 100%;
  min-height: 2.85rem;
  padding: 0.7rem 0.85rem;
  color: var(--color-ink);
  background: #fbfcfa;
  border: 1px solid var(--color-line);
  border-radius: var(--radius-control);
  font-size: 0.9rem;
  outline: none;
  transition: border-color 160ms ease, background-color 160ms ease, box-shadow 160ms ease;
}

.form-group input:focus {
  background: var(--color-surface-strong);
  border-color: var(--color-leaf);
  box-shadow: 0 0 0 3px rgba(54, 95, 75, 0.1);
}

.form-group input::placeholder {
  color: #9aa29d;
}

.submit-btn {
  width: 100%;
  min-height: 2.9rem;
  padding: 0.75rem 1rem;
  margin-top: 0.35rem;
  color: #fdfbf7;
  background: var(--color-primary);
  border: none;
  border-radius: var(--radius-control);
  font-size: 0.94rem;
  font-weight: 700;
  cursor: pointer;
  transition: background-color 160ms ease, transform 160ms ease;
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.submit-btn:hover:not(:disabled) {
  background: var(--color-primary-dark);
  transform: translateY(-1px);
}

.auth-footer {
  text-align: center;
  margin-top: 1.35rem;
  color: var(--color-muted);
  font-size: 0.86rem;
}

.auth-footer a {
  margin-left: 0.25rem;
  color: var(--color-primary-dark);
  font-weight: 700;
  text-decoration: none;
}

.auth-footer a:hover {
  text-decoration: underline;
  text-underline-offset: 3px;
}

.auth-page-note {
  color: #7b857f;
  font-size: 0.75rem;
  letter-spacing: 0.04em;
}

@media (max-width: 760px) {
  .auth-page {
    place-items: start center;
    padding: 1rem;
  }

  .auth-card {
    min-height: 0;
    grid-template-columns: 1fr;
  }

  .auth-showcase {
    min-height: 200px;
    padding: 1.35rem 1.4rem;
  }

  .showcase-copy {
    margin-top: 2rem;
  }

  .showcase-copy h1 {
    max-width: 10.5em;
    font-size: 1.65rem;
  }

  .showcase-copy > p:last-child {
    display: none;
  }

  .botanical-composition {
    right: -5rem;
    bottom: -7rem;
    transform: scale(0.75);
  }

  .auth-panel {
    width: 100%;
    padding: 1.75rem 1.35rem 1.5rem;
  }

  .auth-page-note {
    display: none;
  }
}

@media (max-width: 390px) {
  .auth-page {
    padding: 0;
  }

  .auth-card {
    border: 0;
    border-radius: 0;
    box-shadow: none;
  }
}
</style>
