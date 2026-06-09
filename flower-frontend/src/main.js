import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from './router'

console.log('========== App starting ==========')
console.log('Vue version:', createApp.toString().substring(0, 50))

const app = createApp(App)
console.log('App created, installing router...')

app.use(router)
console.log('Router installed, mounting...')

app.mount('#app')
console.log('========== App mounted ==========')