import { createRouter, createWebHistory } from 'vue-router'
import CardList from '../views/CardList.vue'
import CardForm from '../views/CardForm.vue'
import TagList from '../views/TagList.vue'
import GeneratePage from '../views/GeneratePage.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'

const routes = [
  { path: '/login', name: 'login', component: Login, meta: { public: true } },
  { path: '/register', name: 'register', component: Register, meta: { public: true } },
  { path: '/', name: 'cards', component: CardList },
  { path: '/cards/new', name: 'card-new', component: CardForm },
  { path: '/cards/:id/edit', name: 'card-edit', component: CardForm },
  { path: '/tags', name: 'tags', component: TagList },
  { path: '/generate', name: 'generate', component: GeneratePage },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// Navigation guard
router.beforeEach(async (to, from, next) => {
  const token = localStorage.getItem('token')

  if (to.meta.public) {
    // If logged in and trying to access login/register, redirect to home
    if (token) {
      next({ path: '/' })
    } else {
      next()
    }
    return
  }

  // Protected route - need token
  if (token) {
    next()
    return
  }

  // No token - check if user exists in system
  try {
    const axios = (await import('../api/index.js')).default
    const { data } = await axios.get('/auth/check')
    if (data.hasUser) {
      next({ path: '/login' })
    } else {
      next({ path: '/register' })
    }
  } catch {
    // If check fails, default to login
    next({ path: '/login' })
  }
})

export default router
