import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'Home',
      component: () => import('@/views/HomeView.vue'),
    },
    {
      path: '/movies',
      name: 'MovieList',
      component: () => import('@/views/movie/MovieList.vue'),
    },
    {
      path: '/movies/:id',
      name: 'MovieDetail',
      component: () => import('@/views/movie/MovieDetail.vue'),
    },
    {
      path: '/search',
      name: 'Search',
      component: () => import('@/views/movie/MovieSearch.vue'),
    },
    {
      path: '/rankings',
      name: 'Rankings',
      component: () => import('@/views/ranking/RankingView.vue'),
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/auth/LoginView.vue'),
      meta: { guestOnly: true },
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/auth/RegisterView.vue'),
      meta: { guestOnly: true },
    },
    {
      path: '/profile',
      name: 'Profile',
      component: () => import('@/views/user/UserProfile.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/my/watchlist',
      name: 'MyWatchlist',
      component: () => import('@/views/watchlist/WatchlistView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/admin',
      component: () => import('@/views/admin/AdminLayout.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        {
          path: 'movies',
          name: 'AdminMovies',
          component: () => import('@/views/admin/MovieManage.vue'),
        },
        {
          path: 'movies/create',
          name: 'AdminMovieCreate',
          component: () => import('@/views/admin/MovieForm.vue'),
        },
        {
          path: 'movies/:id/edit',
          name: 'AdminMovieEdit',
          component: () => import('@/views/admin/MovieForm.vue'),
        },
        {
          path: 'users',
          name: 'AdminUsers',
          component: () => import('@/views/admin/UserManage.vue'),
        },
      ],
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/views/NotFound.vue'),
    },
  ],
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('accessToken')
  const userStr = localStorage.getItem('user')
  const user = userStr ? JSON.parse(userStr) : null

  if (to.meta.requiresAuth && !token) {
    next('/login')
    return
  }

  if (to.meta.guestOnly && token) {
    next('/')
    return
  }

  if (to.meta.requiresAdmin && user?.role !== 'ADMIN') {
    next('/')
    return
  }

  next()
})

export default router
