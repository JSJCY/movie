import api from './index'
import type { ApiResponse, LoginVO, UserVO } from '@/types'

export function login(username: string, password: string) {
  return api.post<any, ApiResponse<LoginVO>>('/auth/login', { username, password })
}

export function register(username: string, password: string, email: string, nickname: string) {
  return api.post<any, ApiResponse<LoginVO>>('/auth/register', { username, password, email, nickname })
}

export function refreshToken(refreshToken: string) {
  return api.post<any, ApiResponse<LoginVO>>('/auth/refresh', null, { params: { refreshToken } })
}

export function getCurrentUser() {
  return api.get<any, ApiResponse<UserVO>>('/users/me')
}

export function updateUser(data: any) {
  return api.put<any, ApiResponse<UserVO>>('/users/me', data)
}

export function updateAvatar(avatarUrl: string) {
  return api.put<any, ApiResponse<UserVO>>('/users/me/avatar', null, { params: { avatarUrl } })
}

export function listUsers(params: any) {
  return api.get<any, ApiResponse<any>>('/admin/users', { params })
}

export function updateUserStatus(userId: number, status: number) {
  return api.put<any, ApiResponse<any>>(`/admin/users/${userId}/status`, { status })
}

export function updateUserRole(userId: number, role: string) {
  return api.put<any, ApiResponse<any>>(`/admin/users/${userId}/role`, { role })
}
