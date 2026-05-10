<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getMovieDetail } from '@/api/movie'
import { getMovieReviewStats, listReviewsByMovie, createReview, deleteReview, likeReview, unlikeReview } from '@/api/review'
import { getMyStatus, addWatchRecord, updateWatchRecord, deleteWatchRecord } from '@/api/watchlist'
import { useAuthStore } from '@/stores/auth'
import type { MovieVO, ReviewVO, ReviewStatsVO, WatchRecordVO } from '@/types'
import { formatDate, getWatchStatusText, getWatchStatusType } from '@/utils/format'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const auth = useAuthStore()
const movieId = Number(route.params.id)

const movie = ref<MovieVO | null>(null)
const stats = ref<ReviewStatsVO | null>(null)
const reviews = ref<ReviewVO[]>([])
const myStatus = ref<WatchRecordVO | null>(null)
const reviewTotal = ref(0)

// 发表/修改影评
const showReviewDialog = ref(false)
const reviewForm = ref({ rating: 0, content: '' })
const isSubmitting = ref(false)

// 加载数据
onMounted(async () => {
  await Promise.all([fetchMovie(), fetchStats(), fetchReviews(), fetchMyStatus()])
})

async function fetchMovie() {
  const res = await getMovieDetail(movieId)
  movie.value = res.data
}

async function fetchStats() {
  try {
    const res = await getMovieReviewStats(movieId)
    stats.value = res.data
  } catch { /* ignore */ }
}

async function fetchReviews() {
  try {
    const res = await listReviewsByMovie(movieId, { page: 1, size: 20 })
    reviews.value = res.data.records || []
    reviewTotal.value = res.data.total || 0
  } catch { /* ignore */ }
}

async function fetchMyStatus() {
  if (!auth.isLoggedIn) return
  try {
    const res = await getMyStatus(movieId)
    myStatus.value = res.data
  } catch { /* ignore */ }
}

// 观影状态
async function handleWatchStatus(status: string) {
  if (!auth.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    if (myStatus.value) {
      await updateWatchRecord(myStatus.value.id, { status })
      myStatus.value.status = status
    } else {
      const res = await addWatchRecord({ movieId, status })
      myStatus.value = res.data
    }
    ElMessage.success('更新成功')
  } catch { /* ignore */ }
}

async function handleRemoveWatch() {
  if (!myStatus.value) return
  await deleteWatchRecord(myStatus.value.id)
  myStatus.value = null
  ElMessage.success('已移除')
}

// 影评
async function handleSubmitReview() {
  isSubmitting.value = true
  try {
    await createReview({
      movieId,
      rating: reviewForm.value.rating,
      content: reviewForm.value.content,
    })
    ElMessage.success('评价成功')
    showReviewDialog.value = false
    reviewForm.value = { rating: 0, content: '' }
    await Promise.all([fetchReviews(), fetchStats()])
  } finally {
    isSubmitting.value = false
  }
}

async function handleDeleteReview(reviewId: number) {
  try {
    await ElMessageBox.confirm('确定删除这条影评？', '提示', { type: 'warning' })
    await deleteReview(reviewId)
    ElMessage.success('已删除')
    await Promise.all([fetchReviews(), fetchStats()])
  } catch { /* cancelled */ }
}

async function handleLike(reviewId: number) {
  if (!auth.isLoggedIn) { ElMessage.warning('请先登录'); return }
  await likeReview(reviewId)
  await fetchReviews()
}

async function handleUnlike(reviewId: number) {
  await unlikeReview(reviewId)
  await fetchReviews()
}
</script>

<template>
  <div class="movie-detail" v-if="movie">
    <!-- 电影基本信息 -->
    <div class="detail-top">
      <div class="poster-wrap">
        <img :src="movie.posterUrl || 'https://placehold.co/300x450?text=暂无海报'" class="poster" />
      </div>
      <div class="detail-info">
        <h1>{{ movie.title }}</h1>
        <p class="original-title" v-if="movie.originalTitle">{{ movie.originalTitle }}</p>
        <div class="info-grid">
          <span><strong>评分:</strong> {{ stats?.averageRating || movie.averageRating }} / 10 ({{ stats?.ratingCount || movie.ratingCount }}人评分)</span>
          <span><strong>上映日期:</strong> {{ movie.releaseDate }}</span>
          <span><strong>片长:</strong> {{ movie.duration }}分钟</span>
          <span><strong>地区:</strong> {{ movie.country }}</span>
          <span><strong>语言:</strong> {{ movie.language }}</span>
          <span><strong>分类:</strong>
            <el-tag v-for="cat in movie.categories" :key="cat" size="small" style="margin-right: 4px">{{ cat }}</el-tag>
          </span>
        </div>

        <div class="action-buttons" v-if="auth.isLoggedIn">
          <template v-if="myStatus">
            <el-tag :type="getWatchStatusType(myStatus.status)" size="large">
              {{ getWatchStatusText(myStatus.status) }}
            </el-tag>
            <el-dropdown @command="handleWatchStatus">
              <el-button size="small">更改状态</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="WANT_TO_WATCH">想看</el-dropdown-item>
                  <el-dropdown-item command="WATCHING">在看</el-dropdown-item>
                  <el-dropdown-item command="WATCHED">看过</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button size="small" type="danger" plain @click="handleRemoveWatch">移除</el-button>
          </template>
          <template v-else>
            <el-button type="primary" @click="handleWatchStatus('WANT_TO_WATCH')">想看</el-button>
            <el-button @click="handleWatchStatus('WATCHING')">在看</el-button>
            <el-button @click="handleWatchStatus('WATCHED')">看过</el-button>
          </template>
        </div>
      </div>
    </div>

    <!-- 剧情简介 -->
    <div class="section" v-if="movie.description">
      <h3>剧情简介</h3>
      <p class="description">{{ movie.description }}</p>
    </div>

    <!-- 演职人员 -->
    <div class="section" v-if="movie.actors?.length || movie.directors?.length">
      <h3>演职人员</h3>
      <div class="crew-grid">
        <div v-for="actor in movie.actors" :key="actor.id" class="crew-item">
          <el-avatar :size="60" :src="actor.avatarUrl" />
          <div class="crew-name">{{ actor.name }}</div>
          <div class="crew-role">{{ actor.characterName }}</div>
        </div>
        <div v-for="dir in movie.directors" :key="dir.id" class="crew-item">
          <el-avatar :size="60" :src="dir.avatarUrl" />
          <div class="crew-name">{{ dir.name }}</div>
          <div class="crew-role">导演</div>
        </div>
      </div>
    </div>

    <!-- 影评区域 -->
    <div class="section">
      <div class="section-header">
        <h3>影评 ({{ reviewTotal }})</h3>
        <el-button
          v-if="auth.isLoggedIn"
          type="primary"
          @click="showReviewDialog = true"
        >
          写影评
        </el-button>
      </div>

      <div v-if="stats" class="stats-bar">
        <div class="stat-item">
          <span class="stat-value">{{ stats.averageRating }}</span>
          <span class="stat-label">平均分</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ stats.ratingCount }}</span>
          <span class="stat-label">评分人数</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ stats.reviewCount }}</span>
          <span class="stat-label">文字评论</span>
        </div>
      </div>

      <div class="review-list" v-if="reviews.length">
        <div v-for="review in reviews" :key="review.id" class="review-item">
          <div class="review-header">
            <el-avatar :size="36" />
            <span class="review-user">{{ review.username || `用户${review.userId}` }}</span>
            <el-rate :model-value="review.rating / 2" disabled show-score text-color="#f7ba2a" />
            <span class="review-time">{{ formatDate(review.createdAt) }}</span>
          </div>
          <div class="review-content" v-if="review.content">{{ review.content }}</div>
          <div class="review-actions">
            <el-button
              text
              size="small"
              :type="review.likedByMe ? 'primary' : ''"
              @click="review.likedByMe ? handleUnlike(review.id) : handleLike(review.id)"
            >
              {{ review.likeCount }} 赞
            </el-button>
            <el-button
              v-if="auth.user?.id === review.userId || auth.isAdmin()"
              text
              size="small"
              type="danger"
              @click="handleDeleteReview(review.id)"
            >
              删除
            </el-button>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无影评" />
    </div>

    <!-- 写影评弹窗 -->
    <el-dialog v-model="showReviewDialog" title="发表影评" width="500px">
      <el-form label-width="80px">
        <el-form-item label="评分">
          <el-rate
            v-model="reviewForm.rating"
            :max="10"
            show-score
            :texts="['极差','很差','较差','一般','中等','还行','不错','很好','极好','神作']"
          />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input
            v-model="reviewForm.content"
            type="textarea"
            :rows="5"
            placeholder="分享你的观影感受..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReviewDialog = false">取消</el-button>
        <el-button type="primary" :loading="isSubmitting" @click="handleSubmitReview">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.movie-detail {
  max-width: 1000px;
  margin: 0 auto;
}

.detail-top {
  display: flex;
  gap: 32px;
  margin-bottom: 36px;
}

.poster-wrap .poster {
  width: 260px;
  border-radius: 8px;
}

.detail-info h1 {
  font-size: 28px;
  margin-bottom: 8px;
}

.original-title {
  color: #909399;
  margin-bottom: 16px;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  font-size: 14px;
  color: #606266;
}

.action-buttons {
  margin-top: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.section {
  margin-bottom: 36px;
}

.section h3 {
  font-size: 18px;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #409eff;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h3 {
  margin-bottom: 0;
  border-bottom: none;
  padding-bottom: 0;
}

.description {
  line-height: 1.8;
  color: #606266;
}

.crew-grid {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.crew-item {
  text-align: center;
  width: 80px;
}

.crew-name {
  font-size: 13px;
  margin-top: 4px;
}

.crew-role {
  font-size: 12px;
  color: #909399;
}

.stats-bar {
  display: flex;
  gap: 32px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  display: block;
}

.stat-label {
  font-size: 13px;
  color: #909399;
}

.review-item {
  padding: 16px 0;
  border-bottom: 1px solid #eee;
}

.review-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.review-user {
  font-weight: 500;
}

.review-time {
  font-size: 12px;
  color: #c0c4cc;
  margin-left: auto;
}

.review-content {
  line-height: 1.6;
  color: #606266;
  margin-bottom: 8px;
}

.review-actions {
  display: flex;
  gap: 8px;
}
</style>
