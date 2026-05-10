import api from './index'
import type { ApiResponse, PageResult, WatchRecordVO } from '@/types'

export function listMyWatchlist(params: any) {
  return api.get<any, ApiResponse<PageResult<WatchRecordVO>>>('/watchlist/me', { params })
}

export function listMyHistory(params: any) {
  return api.get<any, ApiResponse<PageResult<WatchRecordVO>>>('/watchlist/me/history', { params })
}

export function addWatchRecord(data: any) {
  return api.post<any, ApiResponse<WatchRecordVO>>('/watchlist', data)
}

export function updateWatchRecord(id: number, data: any) {
  return api.put<any, ApiResponse<WatchRecordVO>>(`/watchlist/${id}`, data)
}

export function deleteWatchRecord(id: number) {
  return api.delete<any, ApiResponse<any>>(`/watchlist/${id}`)
}

export function getMyStatus(movieId: number) {
  return api.get<any, ApiResponse<WatchRecordVO>>(`/watchlist/movie/${movieId}/my-status`)
}
