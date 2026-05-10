import api from './index'
import type { ApiResponse, PageResult, ReviewVO, ReviewStatsVO } from '@/types'

export function listReviewsByMovie(movieId: number, params: any) {
  return api.get<any, ApiResponse<PageResult<ReviewVO>>>(`/reviews/movie/${movieId}`, { params })
}

export function listReviewsByUser(userId: number, params: any) {
  return api.get<any, ApiResponse<PageResult<ReviewVO>>>(`/reviews/user/${userId}`, { params })
}

export function createReview(data: any) {
  return api.post<any, ApiResponse<ReviewVO>>('/reviews', data)
}

export function updateReview(id: number, data: any) {
  return api.put<any, ApiResponse<ReviewVO>>(`/reviews/${id}`, data)
}

export function deleteReview(id: number) {
  return api.delete<any, ApiResponse<any>>(`/reviews/${id}`)
}

export function likeReview(id: number) {
  return api.post<any, ApiResponse<any>>(`/reviews/${id}/like`)
}

export function unlikeReview(id: number) {
  return api.delete<any, ApiResponse<any>>(`/reviews/${id}/like`)
}

export function getMovieReviewStats(movieId: number) {
  return api.get<any, ApiResponse<ReviewStatsVO>>(`/reviews/movie/${movieId}/stats`)
}
