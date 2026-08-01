<template>
  <div class="auth-page">
    <div class="auth-card">
      <section class="auth-showcase" aria-label="鲜花定制介绍">
        <div class="brand-lockup">
          <span class="brand-mark">花</span>
          <span>花卉销售系统</span>
        </div>
        <div class="showcase-copy">
          <p class="eyebrow">创建你的花礼档案</p>
          <h1>从一枝花开始，搭配你的专属花束。</h1>
          <p>注册后可选购鲜花、保存 DIY 设计，并通过模拟支付完成下单。</p>
        </div>
        <div class="botanical-composition" aria-hidden="true">
          <img class="stem stem-main" :src="diyAsset('pink-tulip.webp')" alt="" />
          <img class="stem stem-soft" :src="diyAsset('white-daisy.webp')" alt="" />
          <img class="stem stem-leaf" :src="diyAsset('eucalyptus.webp')" alt="" />
        </div>
      </section>

      <section class="auth-panel">
        <div class="auth-header">
          <p class="eyebrow">新用户注册</p>
          <h2>创建账号</h2>
          <p>带星号的账号信息填写后即可开始使用</p>
        </div>

        <div class="auth-error" role="alert" v-if="errorMsg">{{ errorMsg }}</div>
        <div class="auth-success" role="status" v-if="successMsg">{{ successMsg }}</div>

        <form @submit.prevent="handleRegister" class="auth-form register-form">
          <div class="form-group">
            <label for="register-username">用户名 *</label>
            <input id="register-username" v-model="username" type="text" placeholder="请输入用户名" autocomplete="username" required />
          </div>
          <div class="form-group">
            <label for="register-password">密码 *</label>
            <input id="register-password" v-model="password" type="password" placeholder="请输入密码（至少6位）" autocomplete="new-password" required minlength="6" />
          </div>
          <div class="form-group">
            <label for="register-confirm">确认密码 *</label>
            <input id="register-confirm" v-model="confirmPassword" type="password" placeholder="请再次输入密码" autocomplete="new-password" required />
          </div>
          <div class="form-group">
            <label for="register-phone">手机号</label>
            <input id="register-phone" v-model="phone" type="text" placeholder="请输入手机号" autocomplete="tel" />
          </div>
          <div class="form-group">
            <label for="register-email">邮箱</label>
            <input id="register-email" v-model="email" type="email" placeholder="请输入邮箱" autocomplete="email" />
          </div>
          <button type="submit" class="submit-btn" :disabled="loading">
            {{ loading ? '注册中...' : '注册' }}
          </button>
        </form>

        <div class="auth-footer">
          已有账号？<router-link to="/login">立即登录</router-link>
        </div>
      </section>
    </div>
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
const diyAsset = (name) => `/images/diy/${name}`

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
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 2rem 1.5rem;
  background:
    radial-gradient(circle at 92% 86%, rgba(54, 95, 75, 0.08), transparent 30rem),
    var(--color-bg);
}

.auth-card {
  width: min(1000px, 100%);
  display: grid;
  grid-template-columns: 0.92fr 1.08fr;
  overflow: hidden;
  background: var(--color-surface-strong);
  border: 1px solid rgba(54, 95, 75, 0.14);
  border-radius: var(--radius-feature);
  box-shadow: var(--shadow-lift);
}

.auth-showcase {
  position: relative;
  min-height: 650px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 2.25rem 2.5rem;
  color: #f8f3ed;
  background: #593947;
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
  color: #593947;
  background: #f8f3ed;
  border-radius: 50%;
  font-family: "STKaiti", "KaiTi", serif;
  font-size: 1.05rem;
}

.showcase-copy {
  position: relative;
  z-index: 2;
  max-width: 23rem;
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
  color: #dfbec6;
}

.showcase-copy h1 {
  font-family: "STSong", "SimSun", serif;
  font-size: clamp(2.1rem, 3.6vw, 3.05rem);
  font-weight: 600;
  line-height: 1.26;
  letter-spacing: -0.03em;
}

.showcase-copy > p:last-child {
  margin-top: 1.25rem;
  color: rgba(248, 243, 237, 0.72);
  font-size: 0.94rem;
  line-height: 1.8;
}

.botanical-composition {
  position: absolute;
  right: -5rem;
  bottom: -4.5rem;
  width: 22rem;
  height: 25rem;
  opacity: 0.86;
  pointer-events: none;
}

.stem {
  position: absolute;
  object-fit: contain;
  filter: drop-shadow(0 12px 20px rgba(30, 20, 24, 0.2));
}

.stem-main {
  right: 3rem;
  bottom: 0;
  width: 10rem;
  transform: rotate(13deg);
}

.stem-soft {
  right: 10rem;
  bottom: 1rem;
  width: 9rem;
  transform: rotate(-14deg);
}

.stem-leaf {
  right: 0;
  bottom: 1rem;
  width: 11rem;
  transform: rotate(25deg);
}

.auth-panel {
  align-self: center;
  padding: 2.4rem 3.25rem;
}

.auth-header {
  margin-bottom: 1.5rem;
}

.auth-header h2 {
  margin-bottom: 0.4rem;
  color: var(--color-ink);
  font-size: 1.7rem;
  line-height: 1.25;
}

.auth-header p {
  color: var(--color-muted);
  font-size: 0.86rem;
}

.auth-error,
.auth-success {
  padding: 0.65rem 0.8rem;
  margin-bottom: 1rem;
  border-radius: var(--radius-control);
  font-size: 0.82rem;
}

.auth-error {
  color: var(--color-danger);
  background: #fbebee;
  border: 1px solid #edc5cc;
}

.auth-success {
  color: var(--color-leaf-dark);
  background: var(--color-leaf-soft);
  border: 1px solid #bed3c5;
}

.register-form {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.9rem 1rem;
}

.register-form .form-group:first-child,
.register-form .submit-btn {
  grid-column: 1 / -1;
}

.form-group label {
  display: block;
  margin-bottom: 0.4rem;
  color: var(--color-ink);
  font-size: 0.82rem;
  font-weight: 650;
}

.form-group input {
  width: 100%;
  min-height: 2.75rem;
  padding: 0.65rem 0.8rem;
  color: var(--color-ink);
  background: #fbfcfa;
  border: 1px solid var(--color-line);
  border-radius: var(--radius-control);
  font-size: 0.88rem;
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
  min-height: 2.85rem;
  padding: 0.7rem 1rem;
  margin-top: 0.25rem;
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
  margin-top: 1.15rem;
  color: var(--color-muted);
  font-size: 0.85rem;
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

@media (max-width: 800px) {
  .auth-page {
    place-items: start center;
    padding: 1rem;
  }

  .auth-card {
    grid-template-columns: 1fr;
  }

  .auth-showcase {
    min-height: 190px;
    padding: 1.25rem 1.4rem;
  }

  .showcase-copy {
    max-width: 22rem;
    margin-top: 1.8rem;
  }

  .showcase-copy h1 {
    font-size: 1.55rem;
  }

  .showcase-copy > p:last-child {
    display: none;
  }

  .botanical-composition {
    right: -6rem;
    bottom: -8rem;
    transform: scale(0.72);
  }

  .auth-panel {
    width: 100%;
    padding: 1.65rem 1.35rem 1.5rem;
  }
}

@media (max-width: 520px) {
  .register-form {
    grid-template-columns: 1fr;
    gap: 0.8rem;
  }

  .register-form .form-group:first-child,
  .register-form .submit-btn {
    grid-column: auto;
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
