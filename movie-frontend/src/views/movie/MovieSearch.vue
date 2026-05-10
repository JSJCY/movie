<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { searchMovies } from '@/api/movie'
import type { MovieVO } from '@/types'

const route = useRoute()
const router = useRouter()
const keyword = ref('')
const movies = ref<MovieVO[]>([])
const loading = ref(false)

onMounted(() => {
  keyword.value = (route.query.q as string) || ''
  if (keyword.value) doSearch()
})

function doSearch() {
  if (!keyword.value.trim()) return
  loading.value = true
  searchMovies(keyword.value.trim())
    .then((res) => {
      movies.value = res.data || []
    })
    .finally(() => {
      loading.value = false
    })
}

function goDetail(id: number) {
  router.push(`/movies/${id}`)
}
</script>

<template>
  <div class="search-page">
    <div class="search-header">
      <h2>搜索结果</h2>
      <el-input
        v-model="keyword"
        placeholder="搜索电影..."
        size="large"
        @keyup.enter="doSearch"
        style="max-width: 500px"
      />
      <el-button type="primary" size="large" @click="doSearch">搜索</el-button>
    </div>

    <div v-if="keyword && !loading" class="result-info">
      找到 {{ movies.length }} 部与 "{{ keyword }}" 相关的电影
    </div>

    <div class="movie-grid" v-loading="loading">
      <el-card
        v-for="movie in movies"
        :key="movie.id"
        class="movie-card"
        shadow="hover"
        @click="goDetail(movie.id)"
      >
        <img :src="movie.posterUrl || 'https://placehold.co/300x450?text=暂无海报'" class="poster" />
        <div class="card-info">
          <div class="card-title">{{ movie.title }}</div>
          <div class="card-meta">{{ movie.averageRating }} 分 / {{ movie.releaseDate?.substring(0, 4) }}</div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.search-page {
  max-width: 1200px;
  margin: 0 auto;
}

.search-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.result-info {
  margin-bottom: 20px;
  color: #909399;
  font-size: 14px;
}

.movie-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.movie-card {
  cursor: pointer;
}

.poster {
  width: 100%;
  aspect-ratio: 2/3;
  object-fit: cover;
  border-radius: 4px;
}

.card-title {
  font-size: 15px;
  font-weight: 500;
  margin: 8px 0 4px;
}

.card-meta {
  font-size: 13px;
  color: #909399;
}
</style>
