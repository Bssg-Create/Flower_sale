<template>
  <div class="plan-list-page">
    <div class="page-header">
      <div>
        <p class="page-kicker">我的花束</p>
        <h2>DIY 花束方案</h2>
      </div>
      <router-link to="/user/diy" class="create-btn">创建新方案</router-link>
    </div>

    <div class="loading-box" v-if="loading">加载中...</div>
    <div class="empty-hint" v-else-if="plans.length === 0">
      <p>还没有保存的花束方案</p>
      <router-link to="/user/diy" class="go-diy-btn">去设计花束</router-link>
    </div>
    <div class="plan-grid" v-else>
      <div v-for="plan in plans" :key="plan.id" class="plan-card" @click="viewPlan(plan.id)">
        <div class="plan-cover" aria-hidden="true">
          <img class="cover-leaf" :src="'/images/diy/eucalyptus.webp'" alt="" />
          <img class="cover-flower" :src="'/images/diy/pink-rose.webp'" alt="" />
        </div>
        <div class="plan-body">
          <div class="plan-heading">
            <h3>{{ plan.name }}</h3>
            <p class="plan-status" :class="plan.status">{{ statusText(plan.status) }}</p>
          </div>
          <p class="plan-pkg">{{ plan.packageType || '暂未选择包装' }}</p>
          <div class="plan-meta">
            <p class="plan-price">¥{{ Number(plan.totalPrice || 0).toFixed(2) }}</p>
            <p class="plan-time">{{ formatTime(plan.createTime) }}</p>
          </div>
          <div class="plan-actions" @click.stop>
            <button class="btn-preview" @click="viewPlan(plan.id)">查看方案</button>
            <button class="btn-delete" @click="deletePlan(plan.id)">删除</button>
          </div>
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
.plan-list-page { max-width: 1240px; margin: 0 auto; padding: 0.5rem 0 2rem; }
.page-header { display: flex; justify-content: space-between; align-items: flex-end; gap: 1rem; margin-bottom: 1.5rem; }
.page-kicker { color: var(--color-brand); font-size: 0.82rem; font-weight: 700; margin-bottom: 0.25rem; }
.page-header h2 { font-size: clamp(1.55rem, 3vw, 2.1rem); color: var(--color-ink); }
.create-btn { text-decoration: none; background: var(--color-brand); color: white; padding: 0.72rem 1.15rem; border-radius: var(--radius-control); font-size: 0.92rem; font-weight: 650; transition: all var(--ease-standard); }
.create-btn:hover { background: var(--color-brand-strong); transform: translateY(-1px); box-shadow: var(--shadow-soft); }

.loading-box { text-align: center; padding: 4rem; color: var(--color-muted); }
.empty-hint { text-align: center; padding: 4rem; color: var(--color-muted); background: var(--color-surface); border: 1px dashed var(--color-line); border-radius: var(--radius-card); }
.empty-hint p { font-size: 1.1rem; margin-bottom: 1.5rem; }
.go-diy-btn { text-decoration: none; display: inline-block; background: var(--color-brand); color: white; padding: 0.8rem 1.4rem; border-radius: var(--radius-control); font-size: 0.95rem; }

.plan-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(290px, 1fr)); gap: 1.1rem; }
.plan-card { overflow: hidden; background: var(--color-surface); border: 1px solid var(--color-line); border-radius: var(--radius-card); box-shadow: var(--shadow-soft); cursor: pointer; transition: all var(--ease-standard); }
.plan-card:hover { transform: translateY(-3px); border-color: var(--color-brand-line); box-shadow: var(--shadow-card); }
.plan-cover { position: relative; height: 142px; overflow: hidden; background: linear-gradient(135deg, #dfe9e3 0%, #f4e4e8 100%); }
.plan-cover img { position: absolute; object-fit: contain; filter: drop-shadow(0 14px 16px rgba(49, 79, 70, 0.16)); }
.cover-leaf { width: 170px; right: 16px; top: -28px; transform: rotate(28deg); opacity: 0.72; }
.cover-flower { width: 144px; left: 50%; top: -28px; transform: translateX(-50%) rotate(-8deg); }
.plan-body { padding: 1.1rem; }
.plan-heading, .plan-meta { display: flex; justify-content: space-between; align-items: baseline; gap: 0.8rem; }
.plan-card h3 { font-size: 1.08rem; color: var(--color-ink); }
.plan-pkg { min-height: 1.3rem; font-size: 0.84rem; color: var(--color-muted); margin: 0.35rem 0 1rem; }
.plan-price { font-size: 1.28rem; font-weight: 750; color: var(--color-brand); }
.plan-status { flex: 0 0 auto; font-size: 0.76rem; padding: 0.22rem 0.55rem; border-radius: 999px; }
.plan-status.saved, .plan-status\:saved, .plan-status[class*="1"] { background: #e8f5e9; color: #2e7d32; }
.plan-status.ordered { background: #e3f2fd; color: #1565c0; }
.plan-time { font-size: 0.74rem; color: var(--color-muted); }
.plan-actions { display: flex; gap: 0.55rem; margin-top: 1rem; }
.btn-preview { flex: 1; padding: 0.58rem 1rem; background: var(--color-brand); color: white; border: none; border-radius: var(--radius-control); cursor: pointer; font-size: 0.84rem; transition: all var(--ease-standard); }
.btn-preview:hover { background: var(--color-brand-strong); }
.btn-delete { padding: 0.58rem 0.9rem; background: #fff1f2; color: #a8323e; border: 1px solid #f2d4d8; border-radius: var(--radius-control); cursor: pointer; font-size: 0.84rem; transition: all var(--ease-standard); }
.btn-delete:hover { background: #ffcdd2; }
@media (max-width: 640px) {
  .page-header { align-items: flex-start; }
  .create-btn { padding-inline: 0.85rem; }
  .plan-grid { grid-template-columns: 1fr; }
}
</style>
