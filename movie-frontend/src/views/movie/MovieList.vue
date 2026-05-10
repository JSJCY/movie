<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { listMovies, listCategories } from '@/api/movie'
import type { MovieVO, Category } from '@/types'

const route = useRoute()
const router = useRouter()

const movies = ref<MovieVO[]>([])
const categories = ref<Category[]>([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const size = 12

const filters = ref({
  categoryId: undefined as number | undefined,
  country: '',
  keyword: '',
  sortBy: 'created_at',
})

onMounted(async () => {
  // 从路由参数初始化筛选条件
  if (route.query.keyword) filters.value.keyword = route.query.keyword as string
  if (route.query.sortBy) filters.value.sortBy = route.query.sortBy as string
  if (route.query.categoryId) filters.value.categoryId = Number(route.query.categoryId)

  const catRes = await listCategories()
  categories.value = catRes.data || []
  await fetchMovies()
})

async function fetchMovies() {
  loading.value = true
  try {
    const res = await listMovies({
      page: page.value,
      size,
      ...filters.value,
    })
    movies.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

watch(page, fetchMovies)

function onFilterChange() {
  page.value = 1
  fetchMovies()
}

function goDetail(id: number) {
  router.push(`/movies/${id}`)
}
</script>

<template>
  <div class="movie-list-page">
    <div class="filters">
      <el-select
        v-model="filters.categoryId"
        placeholder="全部分类"
        clearable
        @change="onFilterChange"
        style="width: 140px"
      >
        <el-option
          v-for="cat in categories"
          :key="cat.id"
          :label="cat.name"
          :value="cat.id"
        />
      </el-select>

      <el-input
        v-model="filters.keyword"
        placeholder="搜索电影名称"
        clearable
        @keyup.enter="onFilterChange"
        style="width: 200px"
      />

      <el-select v-model="filters.sortBy" @change="onFilterChange" style="width: 140px">
        <el-option label="默认排序" value="created_at" />
        <el-option label="评分最高" value="rating" />
        <el-option label="最新上映" value="release_date" />
      </el-select>
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
          <div class="card-meta">
            <span>{{ movie.averageRating }} 分</span>
            <span>{{ movie.country }}</span>
            <span>{{ movie.releaseDate?.substring(0, 4) }}</span>
          </div>
          <div class="card-categories">
            <el-tag v-for="cat in movie.categories?.slice(0, 3)" :key="cat" size="small" type="info">
              {{ cat }}
            </el-tag>
          </div>
        </div>
      </el-card>
    </div>

    <div class="pagination" v-if="total > size">
      <el-pagination
        v-model:current-page="page"
        :total="total"
        :page-size="size"
        layout="prev, pager, next"
        background
      />
    </div>
  </div>
</template>

<style scoped>
.movie-list-page {
  max-width: 1200px;
  margin: 0 auto;
}

.filters {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
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
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-meta {
  font-size: 13px;
  color: #909399;
  display: flex;
  gap: 8px;
}

.card-categories {
  margin-top: 6px;
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.pagination {
  margin-top: 32px;
  display: flex;
  justify-content: center;
}
</style>
