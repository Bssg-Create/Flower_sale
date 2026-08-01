<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-header">
        <span class="auth-icon">🌸</span>
        <h1>花卉销售管理系统</h1>
        <p>登录您的账号</p>
      </div>

      <div class="auth-tabs">
        <button :class="['tab-btn', { active: userType === 'user' }]" @click="userType = 'user'">用户登录</button>
        <button :class="['tab-btn', { active: userType === 'admin' }]" @click="userType = 'admin'">管理员登录</button>
      </div>

      <div class="auth-error" v-if="errorMsg">{{ errorMsg }}</div>

      <form @submit.prevent="handleLogin" class="auth-form">
        <div class="form-group">
          <label>用户名</label>
          <input v-model="username" type="text" placeholder="请输入用户名" required />
        </div>
        <div class="form-group">
          <label>密码</label>
          <input v-model="password" type="password" placeholder="请输入密码" required />
        </div>
        <button type="submit" class="submit-btn" :disabled="loading">
          {{ loading ? '登录中...' : '登 录' }}
        </button>
      </form>

      <div class="auth-footer">
        还没有账号？<router-link to="/register">立即注册</router-link>
      </div>
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
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4efe9 100%);
}

.auth-card {
  background: white;
  border-radius: 20px;
  padding: 2.5rem;
  width: 420px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

.auth-header {
  text-align: center;
  margin-bottom: 1.5rem;
}

.auth-icon {
  font-size: 3rem;
  display: block;
}

.auth-header h1 {
  font-size: 1.5rem;
  color: #333;
  margin: 0.5rem 0;
}

.auth-header p {
  color: #888;
  font-size: 0.9rem;
}

.auth-tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
}

.tab-btn {
  flex: 1;
  padding: 0.6rem;
  border: 2px solid #eee;
  border-radius: 10px;
  background: white;
  font-size: 0.95rem;
  cursor: pointer;
  color: #666;
  transition: all 0.3s;
}

.tab-btn.active {
  border-color: #c44569;
  background: linear-gradient(135deg, #ff6b9d 0%, #c44569 100%);
  color: white;
}

.auth-error {
  background: #fff2f0;
  border: 1px solid #ffccc7;
  color: #ff4d4f;
  padding: 0.6rem 1rem;
  border-radius: 8px;
  font-size: 0.85rem;
  margin-bottom: 1rem;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.3rem;
  color: #555;
  font-size: 0.9rem;
}

.form-group input {
  width: 100%;
  padding: 0.7rem 1rem;
  border: 2px solid #eee;
  border-radius: 10px;
  font-size: 0.95rem;
  transition: border-color 0.3s;
  outline: none;
}

.form-group input:focus {
  border-color: #c44569;
}

.submit-btn {
  width: 100%;
  padding: 0.8rem;
  background: linear-gradient(135deg, #ff6b9d 0%, #c44569 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 1rem;
  cursor: pointer;
  margin-top: 1rem;
  transition: opacity 0.3s;
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.submit-btn:hover:not(:disabled) {
  opacity: 0.9;
}

.auth-footer {
  text-align: center;
  margin-top: 1rem;
  color: #888;
  font-size: 0.9rem;
}

.auth-footer a {
  color: #c44569;
  text-decoration: none;
}
</style>