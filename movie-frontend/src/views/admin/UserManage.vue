<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { listUsers, updateUserStatus, updateUserRole } from '@/api/auth'
import type { UserVO } from '@/types'
import { ElMessage } from 'element-plus'

const users = ref<UserVO[]>([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const keyword = ref('')

onMounted(() => fetchUsers())

async function fetchUsers() {
  loading.value = true
  try {
    const res = await listUsers({ page: page.value, size: 10, keyword: keyword.value })
    users.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function onSearch() {
  page.value = 1
  fetchUsers()
}

async function handleToggleStatus(user: UserVO) {
  const newStatus = user.status === 1 ? 0 : 1
  await updateUserStatus(user.id, newStatus)
  user.status = newStatus
  ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
}

async function handleToggleRole(user: UserVO) {
  const newRole = user.role === 'ADMIN' ? 'USER' : 'ADMIN'
  await updateUserRole(user.id, newRole)
  user.role = newRole
  ElMessage.success(`已设为${newRole === 'ADMIN' ? '管理员' : '普通用户'}`)
}
</script>

<template>
  <div class="user-manage">
    <div class="toolbar">
      <h3>用户管理</h3>
      <div style="display: flex; gap: 8px">
        <el-input v-model="keyword" placeholder="搜索用户名/邮箱" clearable @keyup.enter="onSearch" style="width: 220px" />
        <el-button @click="onSearch">搜索</el-button>
      </div>
    </div>

    <el-table :data="users" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" width="140" />
      <el-table-column prop="nickname" label="昵称" width="140" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column prop="role" label="角色" width="90">
        <template #default="{ row }">
          <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'" size="small">{{ row.role }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="handleToggleStatus(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button size="small" @click="handleToggleRole(row)">
            {{ row.role === 'ADMIN' ? '降级' : '升管理' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-if="total > 10"
      v-model:current-page="page"
      :total="total"
      :page-size="10"
      layout="prev, pager, next"
      background
      style="margin-top: 20px; justify-content: center"
      @current-change="fetchUsers"
    />
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.toolbar h3 {
  margin: 0;
}
</style>
