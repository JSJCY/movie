<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const auth = useAuthStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  password2: '',
  email: '',
  nickname: '',
})

const validatePass2 = (_rule: any, value: string, callback: any) => {
  if (value !== form.password) {
    callback(new Error('两次密码输入不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, { min: 3, max: 50, message: '长度3-50字符', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码至少6位', trigger: 'blur' }],
  password2: [{ required: true, message: '请确认密码', trigger: 'blur' }, { validator: validatePass2, trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
}

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await register(form.username, form.password, form.email, form.nickname)
    auth.setLogin(res.data)
    ElMessage.success('注册成功')
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="register-page">
    <el-card class="register-card">
      <h2>注册</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0" size="large">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input v-model="form.nickname" placeholder="昵称（选填）" />
        </el-form-item>
        <el-form-item prop="email">
          <el-input v-model="form.email" placeholder="邮箱（选填）" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" show-password />
        </el-form-item>
        <el-form-item prop="password2">
          <el-input v-model="form.password2" type="password" placeholder="确认密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleRegister" style="width: 100%">
            注册
          </el-button>
        </el-form-item>
      </el-form>
      <p class="link">
        已有账号？<router-link to="/login">立即登录</router-link>
      </p>
    </el-card>
  </div>
</template>

<style scoped>
.register-page {
  display: flex;
  justify-content: center;
  padding-top: 60px;
}

.register-card {
  width: 420px;
}

.register-card h2 {
  text-align: center;
  margin-bottom: 24px;
}

.link {
  text-align: center;
  font-size: 14px;
  color: #909399;
}

.link a {
  color: #409eff;
}
</style>
