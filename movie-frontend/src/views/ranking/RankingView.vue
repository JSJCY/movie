<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getWeeklyRanking, getMonthlyRanking } from '@/api/ranking'
import type { RankingVO } from '@/types'

const router = useRouter()
const tab = ref('weekly')
const rankings = ref<RankingVO[]>([])
const loading = ref(false)

onMounted(() => fetchRanking())

async function fetchRanking() {
  loading.value = true
  try {
    const res = tab.value === 'weekly'
      ? await getWeeklyRanking()
      : await getMonthlyRanking()
    rankings.value = res.data || []
  } finally {
    loading.value = false
  }
}

function onTabChange() {
  fetchRanking()
}

function goMovie(movieId: number) {
  router.push(`/movies/${movieId}`)
}
</script>

<template>
  <div class="ranking-page">
    <h2>电影排行榜</h2>

    <el-tabs v-model="tab" @tab-change="onTabChange">
      <el-tab-pane label="周排行" name="weekly" />
      <el-tab-pane label="月排行" name="monthly" />
    </el-tabs>

    <div class="ranking-list" v-loading="loading">
      <div
        v-for="item in rankings"
        :key="item.movieId"
        class="ranking-item"
        @click="goMovie(item.movieId)"
      >
        <div class="rank-num" :class="`rank-${item.rank}`">
          <span v-if="item.rank <= 3" class="medal">{{ ['', '🥇','🥈','🥉'][item.rank] }}</span>
          <span v-else>{{ item.rank }}</span>
        </div>
        <img :src="item.moviePoster || 'https://placehold.co/60x90?text=?'" class="rank-poster" />
        <div class="rank-info">
          <div class="rank-title">{{ item.movieTitle || `电影 #${item.movieId}` }}</div>
          <div class="rank-stats">
            <span>评分 {{ item.avgRating }}</span>
            <span>{{ item.reviewCount }} 条评论</span>
            <span>{{ item.watchCount }} 次观看</span>
          </div>
        </div>
      </div>

      <el-empty v-if="!loading && rankings.length === 0" description="暂无排行数据" />
    </div>
  </div>
</template>

<style scoped>
.ranking-page {
  max-width: 800px;
  margin: 0 auto;
}

.ranking-page h2 {
  margin-bottom: 16px;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 16px;
  background: #fff;
  border-radius: 8px;
  cursor: pointer;
  transition: box-shadow 0.2s;
}

.ranking-item:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.rank-num {
  width: 40px;
  text-align: center;
  font-size: 20px;
  font-weight: bold;
  color: #909399;
}

.rank-1, .rank-2, .rank-3 {
  font-size: 28px;
}

.rank-poster {
  width: 50px;
  height: 70px;
  object-fit: cover;
  border-radius: 4px;
}

.rank-title {
  font-size: 16px;
  font-weight: 500;
}

.rank-stats {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
  display: flex;
  gap: 16px;
}
</style>
