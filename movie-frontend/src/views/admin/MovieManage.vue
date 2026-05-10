<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listMovies, deleteMovie } from '@/api/movie'
import type { MovieVO } from '@/types'
import { formatDate } from '@/utils/format'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const movies = ref<MovieVO[]>([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)

onMounted(() => fetchMovies())

async function fetchMovies() {
  loading.value = true
  try {
    const res = await listMovies({ page: page.value, size: 10 })
    movies.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('确定删除该电影？', '提示', { type: 'warning' })
    await deleteMovie(id)
    ElMessage.success('已下架')
    fetchMovies()
  } catch { /* cancelled */ }
}

function handleEdit(id: number) {
  router.push(`/admin/movies/${id}/edit`)
}
</script>

<template>
  <div class="movie-manage">
    <div class="toolbar">
      <h3>电影管理</h3>
      <el-button type="primary" @click="router.push('/admin/movies/create')">新增电影</el-button>
    </div>

    <el-table :data="movies" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="片名" min-width="180" />
      <el-table-column prop="country" label="地区" width="100" />
      <el-table-column prop="releaseDate" label="上映日期" width="120">
        <template #default="{ row }">{{ formatDate(row.releaseDate) }}</template>
      </el-table-column>
      <el-table-column prop="averageRating" label="评分" width="80" />
      <el-table-column prop="ratingCount" label="评分人数" width="90" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row.id)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">下架</el-button>
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
      @current-change="fetchMovies"
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
</style>
