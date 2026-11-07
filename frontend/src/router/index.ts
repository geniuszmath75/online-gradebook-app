import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import ClassesView from '@/views/ClassesView.vue'
import GradesView from '@/views/GradesView.vue'
import LoginView from '@/views/LoginView.vue'
import ProfileView from '@/views/ProfileView.vue'
import StudentsView from '@/views/StudentsView.vue'

function defaultAuthenticatedRoute() {
  const auth = useAuthStore()
  if (auth.isStudent) return { name: 'profile' as const }
  if (auth.isStaff) return { name: 'students' as const }
  return null
}

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { guestOnly: true },
    },
    {
      path: '/',
      redirect: () => {
        const auth = useAuthStore()
        if (!auth.isAuthenticated) {
          return { name: 'login' }
        }
        return defaultAuthenticatedRoute() ?? { name: 'login' }
      },
    },
    {
      path: '/students',
      name: 'students',
      component: StudentsView,
      meta: { requiresAuth: true, staffOnly: true },
    },
    {
      path: '/grades',
      name: 'grades',
      component: GradesView,
      meta: { requiresAuth: true, staffOnly: true },
    },
    {
      path: '/classes',
      name: 'classes',
      component: ClassesView,
      meta: { requiresAuth: true, staffOnly: true },
    },
    {
      path: '/profile',
      name: 'profile',
      component: ProfileView,
      meta: { requiresAuth: true, studentOnly: true },
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/',
    },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()

  // Stale session: token without role → clear and allow login
  if (auth.isAuthenticated && !auth.role) {
    auth.logout()
    if (to.name === 'login') return true
    return { name: 'login' }
  }

  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return {
      name: 'login',
      query: { redirect: to.fullPath },
    }
  }

  if (to.meta.guestOnly && auth.isAuthenticated) {
    return defaultAuthenticatedRoute() ?? true
  }

  if (to.meta.staffOnly && !auth.isStaff) {
    return defaultAuthenticatedRoute() ?? { name: 'login' }
  }

  if (to.meta.studentOnly && !auth.isStudent) {
    return defaultAuthenticatedRoute() ?? { name: 'login' }
  }

  return true
})

export default router
