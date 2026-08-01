<template>
  <div class="plan-list-page">
    <div class="page-header">
      <h2>💐 我的DIY花束方案</h2>
      <router-link to="/user/diy" class="create-btn">+ 创建新方案</router-link>
    </div>

    <div class="loading-box" v-if="loading">加载中...</div>
    <div class="empty-hint" v-else-if="plans.length === 0">
      <p>还没有保存的花束方案</p>
      <router-link to="/user/diy" class="go-diy-btn">去设计花束</router-link>
    </div>
    <div class="plan-grid" v-else>
      <div v-for="plan in plans" :key="plan.id" class="plan-card" @click="viewPlan(plan.id)">
        <div class="plan-icon">💐</div>
        <h3>{{ plan.name }}</h3>
        <p class="plan-pkg">包装: {{ plan.packageType || '无' }}</p>
        <p class="plan-price">¥{{ plan.totalPrice.toFixed(2) }}</p>
        <p class="plan-status" :class="plan.status">{{ statusText(plan.status) }}</p>
        <p class="plan-time">{{ formatTime(plan.createTime) }}</p>
        <div class="plan-actions" @click.stop>
          <button class="btn-preview" @click="viewPlan(plan.id)">预览</button>
          <button class="btn-delete" @click="deletePlan(plan.id)">删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api/index.js'

const router = useRouter()
const plans = ref([])
const loading = ref(true)

const statusText = (s) => {
  if (s === 'ordered') return '已下单'
  if (s === 'saved' || s === '1') return '已保存'
  return '草稿'
}

const formatTime = (t) => {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 19)
}

onMounted(async () => {
  try {
    const userId = localStorage.getItem('userId')
    const res = await api.get('/diy/list', { params: { userId } })
    plans.value = res.data.data || []
  } catch (e) { console.error(e) }
  finally { loading.value = false }
})

const viewPlan = (id) => {
  router.push(`/user/plan/${id}`)
}

const deletePlan = async (id) => {
  try {
    api.defaults.headers.Authorization = 'Bearer ' + localStorage.getItem('token')
    await api.delete(`/diy/${id}`)
    plans.value = plans.value.filter(p => p.id !== id)
  } catch (e) { console.error(e); alert('删除失败') }
}
</script>

<style scoped>
.plan-list-page { max-width: 1200px; margin: 0 auto; padding: 2rem; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; }
.page-header h2 { font-size: 1.5rem; color: #333; }
.create-btn { text-decoration: none; background: linear-gradient(135deg, #ff6b9d 0%, #c44569 100%); color: white; padding: 0.7rem 1.5rem; border-radius: 25px; font-size: 0.95rem; transition: all 0.3s; }
.create-btn:hover { transform: translateY(-2px); box-shadow: 0 5px 15px rgba(255,107,157,0.4); }

.loading-box { text-align: center; padding: 4rem; color: #999; }
.empty-hint { text-align: center; padding: 4rem; color: #999; }
.empty-hint p { font-size: 1.1rem; margin-bottom: 1.5rem; }
.go-diy-btn { text-decoration: none; display: inline-block; background: linear-gradient(135deg, #ff6b9d 0%, #c44569 100%); color: white; padding: 0.8rem 2rem; border-radius: 25px; font-size: 1rem; }

.plan-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 1.5rem; }
.plan-card { background: white; border-radius: 16px; padding: 1.5rem; box-shadow: 0 2px 12px rgba(0,0,0,0.06); cursor: pointer; transition: all 0.3s; text-align: center; position: relative; }
.plan-card:hover { transform: translateY(-4px); box-shadow: 0 8px 25px rgba(0,0,0,0.1); }
.plan-icon { font-size: 3rem; margin-bottom: 0.8rem; }
.plan-card h3 { font-size: 1.1rem; margin-bottom: 0.4rem; color: #333; }
.plan-pkg { font-size: 0.85rem; color: #888; margin-bottom: 0.3rem; }
.plan-price { font-size: 1.3rem; font-weight: bold; color: #c44569; margin-bottom: 0.3rem; }
.plan-status { font-size: 0.8rem; padding: 0.2rem 0.6rem; border-radius: 10px; display: inline-block; margin-bottom: 0.3rem; }
.plan-status.saved, .plan-status\:saved, .plan-status[class*="1"] { background: #e8f5e9; color: #2e7d32; }
.plan-status.ordered { background: #e3f2fd; color: #1565c0; }
.plan-time { font-size: 0.75rem; color: #bbb; margin-bottom: 0.8rem; }
.plan-actions { display: flex; gap: 0.5rem; justify-content: center; }
.btn-preview { padding: 0.5rem 1.2rem; background: linear-gradient(135deg, #ff6b9d 0%, #c44569 100%); color: white; border: none; border-radius: 20px; cursor: pointer; font-size: 0.85rem; transition: all 0.3s; }
.btn-preview:hover { transform: scale(1.05); }
.btn-delete { padding: 0.5rem 1.2rem; background: #ffebee; color: #c62828; border: none; border-radius: 20px; cursor: pointer; font-size: 0.85rem; transition: all 0.3s; }
.btn-delete:hover { background: #ffcdd2; }
</style>