<template>
  <div class="auth-page">
    <section class="auth-visual" aria-label="普通用户注册说明">
      <router-link to="/login" class="brand-mark">花</router-link>
      <div class="visual-copy">
        <p class="brand-name">花卉销售系统</p>
        <h1>建立你的<br />专属花礼档案。</h1>
        <p>保存 DIY 花束方案，管理订单，让每一次送花都有迹可循。</p>
      </div>
      <div class="flower-scene" aria-hidden="true">
        <span class="scene-disc"></span>
        <img class="flower flower-main" :src="'/images/diy/sunflower.webp'" alt="" />
        <img class="flower flower-side" :src="'/images/diy/pink-rose.webp'" alt="" />
        <img class="flower flower-leaf" :src="'/images/diy/eucalyptus.webp'" alt="" />
      </div>
    </section>

    <main class="auth-panel">
      <div class="auth-card">
        <div class="auth-header">
          <p>创建账号</p>
          <h2>注册普通用户</h2>
          <span>填写基本信息后即可开始选花</span>
        </div>

        <div class="auth-error" role="alert" v-if="errorMsg">{{ errorMsg }}</div>
        <div class="auth-success" role="status" v-if="successMsg">{{ successMsg }}</div>

        <form @submit.prevent="handleRegister" class="auth-form">
          <div class="form-row">
            <div class="form-group">
              <label for="register-username">用户名</label>
              <input id="register-username" v-model="username" type="text" autocomplete="username" placeholder="请输入用户名" required />
            </div>
            <div class="form-group">
              <label for="register-phone">手机号</label>
              <input id="register-phone" v-model="phone" type="tel" autocomplete="tel" placeholder="选填" />
            </div>
          </div>
          <div class="form-group">
            <label for="register-email">邮箱</label>
            <input id="register-email" v-model="email" type="email" autocomplete="email" placeholder="选填" />
          </div>
          <div class="form-row">
            <div class="form-group">
              <label for="register-password">密码</label>
              <input id="register-password" v-model="password" type="password" autocomplete="new-password" placeholder="至少 6 位" required minlength="6" />
            </div>
            <div class="form-group">
              <label for="register-confirm">确认密码</label>
              <input id="register-confirm" v-model="confirmPassword" type="password" autocomplete="new-password" placeholder="再次输入" required />
            </div>
          </div>
          <button type="submit" class="submit-btn" :disabled="loading">
            {{ loading ? '正在注册' : '创建账号' }}
          </button>
        </form>

        <div class="auth-footer">
          已有账号？<router-link to="/login">返回登录</router-link>
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
const confirmPassword = ref('')
const phone = ref('')
const email = ref('')
const errorMsg = ref('')
const successMsg = ref('')
const loading = ref(false)

const handleRegister = async () => {
  errorMsg.value = ''
  successMsg.value = ''

  if (password.value !== confirmPassword.value) {
    errorMsg.value = '两次输入的密码不一致'
    return
  }
  if (password.value.length < 6) {
    errorMsg.value = '密码长度不能少于6位'
    return
  }

  loading.value = true
  try {
    await api.post('/register', {
      username: username.value,
      password: password.value,
      phone: phone.value,
      email: email.value,
      userType: 'user'
    })
    successMsg.value = '注册成功！3秒后跳转到登录页...'
    setTimeout(() => router.push('/login'), 3000)
  } catch (err) {
    errorMsg.value = err.response?.data?.message || '注册失败，请重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100dvh;
  display: grid;
  grid-template-columns: minmax(420px, 0.9fr) minmax(560px, 1.1fr);
  background: var(--color-canvas);
}

.auth-visual {
  position: relative;
  min-height: 100dvh;
  overflow: hidden;
  padding: clamp(2rem, 5vw, 5rem);
  color: #f8fbf8;
  background: linear-gradient(150deg, #2e4e45, #41675b);
}

.brand-mark {
  width: 46px;
  height: 46px;
  display: grid;
  place-items: center;
  border: 1px solid rgba(255,255,255,0.36);
  border-radius: 14px;
  color: #ffffff;
  background: rgba(255,255,255,0.08);
  text-decoration: none;
  font-weight: 800;
}

.visual-copy { position: relative; z-index: 2; margin-top: clamp(3rem, 10vh, 7rem); }
.brand-name { margin-bottom: 1rem; color: #d6e5dc; font-weight: 700; }
.visual-copy h1 { max-width: 10ch; font-size: clamp(2.7rem, 5vw, 4.7rem); line-height: 1.08; letter-spacing: -0.035em; }
.visual-copy > p:last-child { max-width: 30rem; margin-top: 1.4rem; color: #d4e2da; }

.flower-scene { position: absolute; right: -2rem; bottom: -3rem; width: min(46vw, 520px); height: min(48vh, 470px); }
.scene-disc { position: absolute; right: 4%; bottom: 4%; width: 72%; aspect-ratio: 1; border-radius: 50%; background: #eef3ee; box-shadow: 0 24px 70px rgba(18, 35, 30, 0.24); }
.flower { position: absolute; object-fit: contain; filter: drop-shadow(0 24px 22px rgba(18, 35, 30, 0.2)); transform-origin: 50% 100%; }
.flower-main { right: 16%; bottom: 4%; width: 40%; transform: rotate(4deg); }
.flower-side { right: 45%; bottom: 3%; width: 31%; transform: rotate(-16deg); }
.flower-leaf { right: 0; bottom: 0; width: 38%; transform: rotate(22deg); }

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
  max-width: 620px;
  min-width: 0;
  padding: clamp(1.4rem, 4vw, 2.6rem);
  border-radius: var(--radius-card);
  background: var(--color-surface);
  box-shadow: var(--shadow-card);
}

.auth-header { margin-bottom: 1.5rem; }
.auth-header p { color: var(--color-brand); font-size: 0.9rem; font-weight: 700; }
.auth-header h2 { margin: 0.25rem 0 0.4rem; color: var(--color-ink); font-size: clamp(1.7rem, 3vw, 2.15rem); letter-spacing: -0.025em; }
.auth-header span { color: var(--color-ink-soft); font-size: 0.9rem; }

.auth-error {
  color: #8f2f3a;
  background: #fbedef;
  padding: 0.72rem 0.9rem;
  border-radius: var(--radius-control);
  font-size: 0.85rem;
  margin-bottom: 1rem;
}

.auth-success {
  color: #205f46;
  background: #e9f5ef;
  padding: 0.72rem 0.9rem;
  border-radius: var(--radius-control);
  font-size: 0.85rem;
  margin-bottom: 1rem;
}

.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 0.85rem; }
.form-group { margin-bottom: 0.9rem; }

.form-group label {
  display: block;
  margin-bottom: 0.42rem;
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
  margin-top: 0.45rem;
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

@media (max-width: 980px) {
  .auth-page { width: 100%; min-width: 0; grid-template-columns: minmax(0, 1fr); overflow-x: hidden; }
  .auth-visual { display: none; }
  .auth-panel { padding: 1rem; }
}

@media (max-width: 620px) {
  .auth-card { padding: 1.25rem; }
  .form-row { grid-template-columns: 1fr; gap: 0; }
}
</style>
