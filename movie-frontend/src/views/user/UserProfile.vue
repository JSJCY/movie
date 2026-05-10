<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { updateUser } from '@/api/auth'
import { ElMessage } from 'element-plus'

const auth = useAuthStore()

const form = reactive({
  email: auth.user?.email || '',
  phone: auth.user?.phone || '',
  nickname: auth.user?.nickname || '',
})

const saving = ref(false)

async function handleSave() {
  saving.value = true
  try {
    await updateUser(form)
    await auth.fetchUser()
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="profile-page">
    <el-card>
      <template #header>
        <span>个人中心</span>
      </template>

      <div class="profile-header">
        <el-avatar :size="80" :src="auth.user?.avatarUrl" />
        <div>
          <h3>{{ auth.user?.nickname || auth.user?.username }}</h3>
          <p class="role">{{ auth.user?.role === 'ADMIN' ? '管理员' : '普通用户' }}</p>
          <p class="join-date">加入于 {{ auth.user?.createdAt?.substring(0, 10) }}</p>
        </div>
      </div>

      <el-divider />

      <el-form :model="form" label-width="80px" style="max-width: 500px">
        <el-form-item label="用户名">
          <el-input :model-value="auth.user?.username" disabled />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.profile-page {
  max-width: 700px;
  margin: 0 auto;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
}

.profile-header h3 {
  font-size: 20px;
  margin-bottom: 4px;
}

.role {
  color: #e6a23c;
  font-size: 13px;
}

.join-date {
  color: #909399;
  font-size: 13px;
}
</style>
