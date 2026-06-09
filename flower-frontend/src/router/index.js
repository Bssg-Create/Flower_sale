import { createRouter, createWebHashHistory } from 'vue-router'
import LoginPage from '../components/LoginPage.vue'
import RegisterPage from '../components/RegisterPage.vue'
import HomePage from '../components/HomePage.vue'
import DiyPage from '../components/DiyPage.vue'
import DiyPlanList from '../components/DiyPlanList.vue'
import DiyPlanDetail from '../components/DiyPlanDetail.vue'
import AdminDashboard from '../components/AdminDashboard.vue'
import UserLayout from '../components/UserLayout.vue'
import AdminLayout from '../components/AdminLayout.vue'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: LoginPage },
  { path: '/register', component: RegisterPage },
  {
    path: '/user',
    component: UserLayout,
    children: [
      { path: '', redirect: '/user/home' },
      { path: 'home', component: HomePage },
      { path: 'diy', component: DiyPage },
      { path: 'plans', component: DiyPlanList },
      { path: 'plan/:id', component: DiyPlanDetail }
    ]
  },
  {
    path: '/admin',
    component: AdminLayout,
    children: [
      { path: '', component: AdminDashboard },
      { path: 'users', component: AdminDashboard },
      { path: 'flowers', component: AdminDashboard },
      { path: 'orders', component: AdminDashboard },
      { path: 'diy', component: AdminDashboard }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userType = localStorage.getItem('userType')
  const path = to.path

  if (path === '/') {
    next('/login')
    return
  }

  const isPublic = path === '/login' || path === '/register'

  if (!token) {
    next(isPublic ? undefined : '/login')
    return
  }

  if (isPublic) {
    next(userType === 'admin' ? '/admin' : '/user/home')
    return
  }

  if (path.startsWith('/admin') && userType !== 'admin') {
    next('/user/home')
    return
  }

  if (path.startsWith('/user') && userType !== 'user') {
    next('/admin')
    return
  }

  next()
})

export default router