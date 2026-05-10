<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="admin-layout">
    <el-menu mode="horizontal" :default-active="router.currentRoute.value.path" router>
      <el-menu-item index="/admin/movies">电影管理</el-menu-item>
      <el-menu-item index="/admin/users">用户管理</el-menu-item>
    </el-menu>
    <div class="admin-content">
      <router-view />
    </div>
  </div>
</template>

<style scoped>
.admin-layout {
  max-width: 1200px;
  margin: 0 auto;
}

.admin-content {
  padding: 20px 0;
}
</style>
