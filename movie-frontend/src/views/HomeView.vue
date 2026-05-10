<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getWeeklyRanking } from '@/api/ranking'
import { listMovies } from '@/api/movie'
import type { RankingVO, MovieVO } from '@/types'

const router = useRouter()
const rankings = ref<RankingVO[]>([])
const latestMovies = ref<MovieVO[]>([])
const topRatedMovies = ref<MovieVO[]>([])
const loading = ref(true)

onMounted(async () => {
  try {
    const [rankRes, latestRes, topRes] = await Promise.all([
      getWeeklyRanking(),
      listMovies({ page: 1, size: 8, sortBy: 'release_date' }),
      listMovies({ page: 1, size: 8, sortBy: 'rating' }),
    ])
    rankings.value = rankRes.data || []
    latestMovies.value = latestRes.data.records || []
    topRatedMovies.value = topRes.data.records || []
  } catch {
    // ignore
  } finally {
    loading.value = false
  }
})

function goToMovie(id: number) {
  router.push(`/movies/${id}`)
}
</script>

<template>
  <div class="home" v-loading="loading">
    <!-- 轮播横幅 -->
    <section class="hero">
      <h1>欢迎来到电影影评系统</h1>
      <p>发现好电影，分享你的观影体验</p>
      <el-button type="primary" size="large" @click="router.push('/movies')">浏览电影库</el-button>
    </section>

    <!-- 本周排行榜 -->
    <section class="section" v-if="rankings.length">
      <div class="section-header">
        <h2>本周排行榜</h2>
        <el-button text @click="router.push('/rankings')">查看更多</el-button>
      </div>
      <div class="ranking-grid">
        <div
          v-for="item in rankings.slice(0, 5)"
          :key="item.movieId"
          class="ranking-card"
          @click="goToMovie(item.movieId)"
        >
          <div class="rank-badge" :class="`rank-${item.rank}`">{{ item.rank }}</div>
          <div class="rank-info">
            <div class="rank-title">{{ item.movieTitle || `电影 #${item.movieId}` }}</div>
            <div class="rank-meta">
              <span>评分 {{ item.avgRating }}</span>
              <span>{{ item.watchCount }} 人看过</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 最新上映 -->
    <section class="section" v-if="latestMovies.length">
      <div class="section-header">
        <h2>最新上映</h2>
        <el-button text @click="router.push('/movies?sortBy=release_date')">查看更多</el-button>
      </div>
      <div class="movie-grid">
        <el-card
          v-for="movie in latestMovies"
          :key="movie.id"
          class="movie-card"
          shadow="hover"
          @click="goToMovie(movie.id)"
        >
          <img :src="movie.posterUrl || 'https://placehold.co/300x450?text=暂无海报'" class="poster" />
          <div class="card-info">
            <div class="card-title">{{ movie.title }}</div>
            <div class="card-meta">
              <el-rate :model-value="(movie.averageRating || 0) / 2" disabled show-score text-color="#f7ba2a" />
              <span class="year">{{ movie.releaseDate?.substring(0, 4) }}</span>
            </div>
          </div>
        </el-card>
      </div>
    </section>

    <!-- 高分推荐 -->
    <section class="section" v-if="topRatedMovies.length">
      <div class="section-header">
        <h2>高分推荐</h2>
        <el-button text @click="router.push('/movies?sortBy=rating')">查看更多</el-button>
      </div>
      <div class="movie-grid">
        <el-card
          v-for="movie in topRatedMovies"
          :key="movie.id"
          class="movie-card"
          shadow="hover"
          @click="goToMovie(movie.id)"
        >
          <img :src="movie.posterUrl || 'https://placehold.co/300x450?text=暂无海报'" class="poster" />
          <div class="card-info">
            <div class="card-title">{{ movie.title }}</div>
            <div class="card-meta">
              <el-rate :model-value="(movie.averageRating || 0) / 2" disabled show-score text-color="#f7ba2a" />
              <span class="year">{{ movie.releaseDate?.substring(0, 4) }}</span>
            </div>
          </div>
        </el-card>
      </div>
    </section>
  </div>
</template>

<style scoped>
.home {
  max-width: 1200px;
  margin: 0 auto;
}

.hero {
  text-align: center;
  padding: 60px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: #fff;
  margin-bottom: 40px;
}

.hero h1 {
  font-size: 32px;
  margin-bottom: 12px;
}

.hero p {
  font-size: 16px;
  opacity: 0.9;
  margin-bottom: 24px;
}

.section {
  margin-bottom: 40px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  font-size: 22px;
  color: #303133;
}

.movie-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.movie-card {
  cursor: pointer;
}

.movie-card .poster {
  width: 100%;
  aspect-ratio: 2/3;
  object-fit: cover;
  border-radius: 4px;
}

.card-info {
  padding: 8px 0;
}

.card-title {
  font-size: 15px;
  font-weight: 500;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.year {
  font-size: 12px;
  color: #999;
}

.ranking-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ranking-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 16px;
  background: #fff;
  border-radius: 8px;
  cursor: pointer;
  transition: box-shadow 0.2s;
}

.ranking-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.rank-badge {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 16px;
  color: #fff;
  background: #909399;
}

.rank-1 { background: #f56c6c; }
.rank-2 { background: #e6a23c; }
.rank-3 { background: #67c23a; }

.rank-title {
  font-size: 16px;
  font-weight: 500;
}

.rank-meta {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
  display: flex;
  gap: 16px;
}
</style>
