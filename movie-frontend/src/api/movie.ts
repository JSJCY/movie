import api from './index'
import type { ApiResponse, PageResult, MovieVO, Category, Actor, Director } from '@/types'

export function listMovies(params: any) {
  return api.get<any, ApiResponse<PageResult<MovieVO>>>('/movies', { params })
}

export function searchMovies(q: string) {
  return api.get<any, ApiResponse<MovieVO[]>>('/movies/search', { params: { q } })
}

export function getMovieDetail(id: number) {
  return api.get<any, ApiResponse<MovieVO>>(`/movies/${id}`)
}

export function createMovie(data: any) {
  return api.post<any, ApiResponse<MovieVO>>('/movies', data)
}

export function updateMovie(id: number, data: any) {
  return api.put<any, ApiResponse<MovieVO>>(`/movies/${id}`, data)
}

export function deleteMovie(id: number) {
  return api.delete<any, ApiResponse<any>>(`/movies/${id}`)
}

export function listCategories() {
  return api.get<any, ApiResponse<Category[]>>('/categories')
}

export function createCategory(data: any) {
  return api.post<any, ApiResponse<Category>>('/categories', data)
}

export function deleteCategory(id: number) {
  return api.delete<any, ApiResponse<any>>(`/categories/${id}`)
}

export function listActors(params: any) {
  return api.get<any, ApiResponse<PageResult<Actor>>>('/actors', { params })
}

export function createActor(data: any) {
  return api.post<any, ApiResponse<Actor>>('/actors', data)
}

export function updateActor(id: number, data: any) {
  return api.put<any, ApiResponse<Actor>>(`/actors/${id}`, data)
}

export function deleteActor(id: number) {
  return api.delete<any, ApiResponse<any>>(`/actors/${id}`)
}

export function listDirectors(params: any) {
  return api.get<any, ApiResponse<PageResult<Director>>>('/directors', { params })
}

export function createDirector(data: any) {
  return api.post<any, ApiResponse<Director>>('/directors', data)
}

export function updateDirector(id: number, data: any) {
  return api.put<any, ApiResponse<Director>>(`/directors/${id}`, data)
}

export function deleteDirector(id: number) {
  return api.delete<any, ApiResponse<any>>(`/directors/${id}`)
}
