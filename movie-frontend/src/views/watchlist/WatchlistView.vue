<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listMyWatchlist, updateWatchRecord, deleteWatchRecord } from '@/api/watchlist'
import type { WatchRecordVO } from '@/types'
import { getWatchStatusText, formatDate } from '@/utils/format'
import { ElMessage } from 'element-plus'

const router = useRouter()
const statusTab = ref('')
const records = ref<WatchRecordVO[]>([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)

onMounted(() => fetchRecords())

async function fetchRecords() {
  loading.value = true
  try {
    const res = await listMyWatchlist({
      page: page.value,
      size: 12,
      status: statusTab.value || undefined,
    })
    records.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function onTabChange() {
  page.value = 1
  fetchRecords()
}

async function handleUpdateStatus(record: WatchRecordVO, newStatus: string) {
  await updateWatchRecord(record.id, { status: newStatus })
  ElMessage.success('更新成功')
  fetchRecords()
}

async function handleDelete(recordId: number) {
  await deleteWatchRecord(recordId)
  ElMessage.success('已移除')
  fetchRecords()
}

function goMovie(movieId: number) {
  router.push(`/movies/${movieId}`)
}
</script>

<template>
  <div class="watchlist-page">
    <h2>我的观影</h2>

    <el-tabs v-model="statusTab" @tab-change="onTabChange">
      <el-tab-pane label="全部" name="" />
      <el-tab-pane label="想看" name="WANT_TO_WATCH" />
      <el-tab-pane label="在看" name="WATCHING" />
      <el-tab-pane label="看过" name="WATCHED" />
    </el-tabs>

    <div class="record-grid" v-loading="loading">
      <el-card
        v-for="record in records"
        :key="record.id"
        class="record-card"
        shadow="hover"
      >
        <img
          :src="record.moviePoster || 'https://placehold.co/200x300?text=暂无海报'"
          class="record-poster"
          @click="goMovie(record.movieId)"
        />
        <div class="record-info">
          <div class="record-title" @click="goMovie(record.movieId)">
            {{ record.movieTitle || `电影 #${record.movieId}` }}
          </div>
          <div class="record-meta">
            <el-tag :type="statusTab ? 'primary' : 'info'" size="small">
              {{ getWatchStatusText(record.status) }}
            </el-tag>
            <span v-if="record.rating">评分: {{ record.rating }}/10</span>
            <span>{{ formatDate(record.createdAt) }}</span>
          </div>
          <div class="record-actions">
            <el-dropdown @command="(cmd: string) => handleUpdateStatus(record, cmd)">
              <el-button size="small">更改状态</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="WANT_TO_WATCH">想看</el-dropdown-item>
                  <el-dropdown-item command="WATCHING">在看</el-dropdown-item>
                  <el-dropdown-item command="WATCHED">看过</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button size="small" type="danger" plain @click="handleDelete(record.id)">移除</el-button>
          </div>
        </div>
      </el-card>
    </div>

    <el-empty v-if="!loading && records.length === 0" description="暂无观影记录" />

    <div class="pagination" v-if="total > 12">
      <el-pagination
        v-model:current-page="page"
        :total="total"
        :page-size="12"
        layout="prev, pager, next"
        background
        @current-change="fetchRecords"
      />
    </div>
  </div>
</template>

<style scoped>
.watchlist-page {
  max-width: 1000px;
  margin: 0 auto;
}

.watchlist-page h2 {
  margin-bottom: 16px;
}

.record-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.record-card {
  display: flex;
  gap: 16px;
}

.record-card :deep(.el-card__body) {
  display: flex;
  gap: 16px;
}

.record-poster {
  width: 60px;
  height: 90px;
  object-fit: cover;
  border-radius: 4px;
  cursor: pointer;
  flex-shrink: 0;
}

.record-info {
  flex: 1;
}

.record-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 6px;
  cursor: pointer;
}

.record-title:hover {
  color: #409eff;
}

.record-meta {
  font-size: 13px;
  color: #909399;
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 8px;
}

.record-actions {
  display: flex;
  gap: 8px;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>
