import api from './index'
import type { ApiResponse, RankingVO } from '@/types'

export function getWeeklyRanking(period?: string) {
  return api.get<any, ApiResponse<RankingVO[]>>('/rankings/weekly', { params: { period } })
}

export function getMonthlyRanking(period?: string) {
  return api.get<any, ApiResponse<RankingVO[]>>('/rankings/monthly', { params: { period } })
}
