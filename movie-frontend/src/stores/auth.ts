import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { UserVO } from '@/types'
import { getCurrentUser } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<UserVO | null>(null)
  const isLoggedIn = ref(false)

  function loadFromStorage() {
    const token = localStorage.getItem('accessToken')
    const userStr = localStorage.getItem('user')
    if (token && userStr) {
      user.value = JSON.parse(userStr)
      isLoggedIn.value = true
    }
  }

  function setLogin(data: { accessToken: string; refreshToken: string; user: UserVO }) {
    localStorage.setItem('accessToken', data.accessToken)
    localStorage.setItem('refreshToken', data.refreshToken)
    localStorage.setItem('user', JSON.stringify(data.user))
    user.value = data.user
    isLoggedIn.value = true
  }

  function logout() {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('user')
    user.value = null
    isLoggedIn.value = false
  }

  async function fetchUser() {
    try {
      const res = await getCurrentUser()
      user.value = res.data
      localStorage.setItem('user', JSON.stringify(res.data))
    } catch {
      logout()
    }
  }

  function isAdmin(): boolean {
    return user.value?.role === 'ADMIN'
  }

  return { user, isLoggedIn, loadFromStorage, setLogin, logout, fetchUser, isAdmin }
})
