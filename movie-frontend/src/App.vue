<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'

const auth = useAuthStore()
const router = useRouter()

auth.loadFromStorage()

const searchKeyword = ref('')

function handleSearch() {
  if (searchKeyword.value.trim()) {
    router.push({ name: 'Search', query: { q: searchKeyword.value.trim() } })
  }
}

function handleLogout() {
  auth.logout()
  router.push('/')
}
</script>

<template>
  <el-container class="app-container">
    <el-header class="app-header">
      <div class="header-left">
        <router-link to="/" class="logo">影评系统</router-link>
        <el-menu mode="horizontal" :ellipsis="false" router class="header-menu">
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/movies">电影库</el-menu-item>
          <el-menu-item index="/rankings">排行榜</el-menu-item>
        </el-menu>
      </div>

      <div class="header-right">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索电影..."
          :prefix-icon="Search"
          class="search-input"
          @keyup.enter="handleSearch"
          clearable
        />

        <template v-if="auth.isLoggedIn">
          <el-dropdown>
            <span class="user-trigger">
              <el-avatar :size="32" :src="auth.user?.avatarUrl" />
              <span class="username">{{ auth.user?.nickname || auth.user?.username }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/profile')">个人中心</el-dropdown-item>
                <el-dropdown-item @click="router.push('/my/watchlist')">我的观影</el-dropdown-item>
                <el-dropdown-item v-if="auth.isAdmin()" @click="router.push('/admin/movies')">后台管理</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>

        <template v-else>
          <el-button type="primary" @click="router.push('/login')">登录</el-button>
          <el-button @click="router.push('/register')">注册</el-button>
        </template>
      </div>
    </el-header>

    <el-main class="app-main">
      <router-view />
    </el-main>

    <el-footer class="app-footer">
      <p>电影影评观影系统 &copy; 2026</p>
    </el-footer>
  </el-container>
</template>

<script lang="ts">
import { ref } from 'vue'
export default {
  name: 'App',
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Microsoft YaHei', sans-serif;
  background: #f5f7fa;
}

.app-container {
  min-height: 100vh;
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 24px;
}

.logo {
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
  text-decoration: none;
  white-space: nowrap;
}

.header-menu {
  border-bottom: none !important;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.search-input {
  width: 240px;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.username {
  font-size: 14px;
  color: #333;
}

.app-main {
  min-height: calc(100vh - 120px);
  padding: 24px 40px;
}

.app-footer {
  text-align: center;
  color: #999;
  font-size: 13px;
  padding: 20px;
  background: #fff;
  border-top: 1px solid #eee;
}
</style>
