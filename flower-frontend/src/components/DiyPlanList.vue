<template>
  <div class="plan-list-page">
    <div class="page-header">
      <div>
        <p class="eyebrow">已保存的花礼设计</p>
        <h2>我的 DIY 花束方案</h2>
        <p class="page-desc">查看、恢复或继续下单你的定制花束。</p>
      </div>
      <router-link to="/user/diy" class="create-btn">+ 创建新方案</router-link>
    </div>

    <div class="loading-box" v-if="loading">加载中...</div>
    <div class="empty-hint" v-else-if="plans.length === 0">
      <p>还没有保存的花束方案</p>
      <router-link to="/user/diy" class="go-diy-btn">去设计花束</router-link>
    </div>
    <div class="plan-grid" v-else>
      <article v-for="(plan, index) in plans" :key="plan.id" class="plan-card" @click="viewPlan(plan.id)">
        <div class="plan-preview" aria-hidden="true">
          <img :src="previewImage(previewFlowers[index % previewFlowers.length][0])" alt="" />
          <img :src="previewImage(previewFlowers[index % previewFlowers.length][1])" alt="" />
          <img :src="previewImage('eucalyptus.webp')" alt="" />
        </div>
        <div class="plan-body">
          <div class="plan-title-row">
            <h3>{{ plan.name }}</h3>
            <p class="plan-status" :class="plan.status">{{ statusText(plan.status) }}</p>
          </div>
          <p class="plan-pkg">{{ plan.packageType || '无包装' }}</p>
          <div class="plan-meta">
            <p class="plan-price">¥{{ Number(plan.totalPrice || 0).toFixed(2) }}</p>
            <p class="plan-time">{{ formatTime(plan.createTime) }}</p>
          </div>
          <div class="plan-actions" @click.stop>
            <button class="btn-preview" @click="viewPlan(plan.id)">查看详情</button>
            <button class="btn-delete" @click="deletePlan(plan.id)">删除</button>
          </div>
        </div>
      </article>
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
const previewFlowers = [
  ['red-rose.webp', 'white-rose.webp'],
  ['sunflower.webp', 'white-daisy.webp'],
  ['pink-tulip.webp', 'white-lily.webp']
]
const previewImage = (name) => `/images/diy/${name}`

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
.plan-list-page { max-width: 1200px; margin: 0 auto; }
.page-header { display: flex; justify-content: space-between; align-items: end; gap: 2rem; margin-bottom: 2rem; }
.eyebrow { color: var(--color-primary); font-size: 0.76rem; font-weight: 750; letter-spacing: 0.12em; margin-bottom: 0.45rem; }
.page-header h2 { color: var(--color-ink); font-size: 1.85rem; line-height: 1.2; }
.page-desc { color: var(--color-muted); font-size: 0.88rem; margin-top: 0.45rem; }
.create-btn, .go-diy-btn { display: inline-block; text-decoration: none; color: #fdfbf7; background: var(--color-primary); border-radius: var(--radius-control); font-size: 0.88rem; font-weight: 700; }
.create-btn { padding: 0.7rem 1.1rem; white-space: nowrap; }
.create-btn:hover, .go-diy-btn:hover { background: var(--color-primary-dark); }
.loading-box, .empty-hint { text-align: center; padding: 5rem 1rem; color: var(--color-muted); background: var(--color-surface); border: 1px dashed #cbd4cc; border-radius: var(--radius-card); }
.empty-hint p { font-size: 1rem; margin-bottom: 1.25rem; }
.go-diy-btn { padding: 0.7rem 1.2rem; }
.plan-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(270px, 1fr)); gap: 1.2rem; }
.plan-card { overflow: hidden; background: var(--color-surface-strong); border: 1px solid var(--color-line); border-radius: var(--radius-card); box-shadow: 0 4px 18px rgba(40,63,49,0.05); cursor: pointer; transition: transform 180ms ease, box-shadow 180ms ease; }
.plan-card:hover { transform: translateY(-3px); box-shadow: var(--shadow-card); }
.plan-preview { position: relative; height: 190px; overflow: hidden; background: #e9eee9; }
.plan-preview img { position: absolute; bottom: -2.5rem; width: 8.5rem; height: 15rem; object-fit: contain; filter: drop-shadow(0 8px 10px rgba(40,63,49,0.12)); }
.plan-preview img:nth-child(1) { left: 50%; transform: translateX(-72%) rotate(-12deg); z-index: 2; }
.plan-preview img:nth-child(2) { left: 50%; transform: translateX(-25%) rotate(12deg); z-index: 3; }
.plan-preview img:nth-child(3) { right: 0.5rem; transform: rotate(25deg); z-index: 1; }
.plan-body { padding: 1rem; }
.plan-title-row, .plan-meta { display: flex; align-items: center; justify-content: space-between; gap: 0.75rem; }
.plan-card h3 { min-width: 0; overflow: hidden; color: var(--color-ink); font-size: 1.02rem; text-overflow: ellipsis; white-space: nowrap; }
.plan-pkg { min-height: 2.8em; color: var(--color-muted); font-size: 0.78rem; margin: 0.45rem 0 0.75rem; }
.plan-price { color: var(--color-primary-dark); font-size: 1.2rem; font-weight: 750; }
.plan-status { flex: 0 0 auto; padding: 0.2rem 0.48rem; color: var(--color-leaf-dark); background: var(--color-leaf-soft); border-radius: 5px; font-size: 0.7rem; }
.plan-status.ordered { color: #31556b; background: #e4eef3; }
.plan-time { color: #8b938e; font-size: 0.7rem; }
.plan-actions { display: flex; gap: 0.5rem; margin-top: 0.9rem; padding-top: 0.85rem; border-top: 1px solid var(--color-line); }
.btn-preview, .btn-delete { flex: 1; min-height: 2.3rem; border-radius: var(--radius-control); cursor: pointer; font-size: 0.78rem; font-weight: 650; }
.btn-preview { color: #fdfbf7; background: var(--color-primary); border: 1px solid var(--color-primary); }
.btn-delete { color: var(--color-danger); background: transparent; border: 1px solid #e4c3c9; }
.btn-preview:hover { background: var(--color-primary-dark); }
.btn-delete:hover { background: #fbebee; }
@media (max-width: 600px) { .page-header { align-items: stretch; flex-direction: column; gap: 1rem; } .create-btn { text-align: center; } .plan-grid { grid-template-columns: 1fr; } .plan-card { display: grid; grid-template-columns: 9rem 1fr; } .plan-preview { height: 100%; min-height: 210px; } .plan-preview img { width: 6.5rem; } }
</style>
