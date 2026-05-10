// 统一响应体
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

// 分页结果
export interface PageResult<T> {
  total: number
  page: number
  size: number
  records: T[]
}

// 用户
export interface UserVO {
  id: number
  username: string
  email: string
  phone: string
  nickname: string
  avatarUrl: string
  role: string
  status: number
  createdAt: string
}

export interface LoginVO {
  accessToken: string
  refreshToken: string
  user: UserVO
}

// 电影
export interface MovieVO {
  id: number
  title: string
  originalTitle: string
  releaseDate: string
  duration: number
  country: string
  language: string
  description: string
  posterUrl: string
  trailerUrl: string
  averageRating: number
  ratingCount: number
  categories: string[]
  actors: ActorBrief[]
  directors: DirectorBrief[]
}

export interface ActorBrief {
  id: number
  name: string
  characterName: string
  avatarUrl: string
}

export interface DirectorBrief {
  id: number
  name: string
  avatarUrl: string
}

export interface Category {
  id: number
  name: string
  description: string
}

export interface Actor {
  id: number
  name: string
  avatarUrl: string
  bio: string
  birthDate: string
  nationality: string
}

export interface Director {
  id: number
  name: string
  avatarUrl: string
  bio: string
  birthDate: string
  nationality: string
}

// 影评
export interface ReviewVO {
  id: number
  movieId: number
  userId: number
  username: string
  userAvatar: string
  rating: number
  content: string
  likeCount: number
  likedByMe: boolean
  createdAt: string
  updatedAt: string
}

export interface ReviewStatsVO {
  averageRating: number
  ratingCount: number
  reviewCount: number
}

// 观影记录
export interface WatchRecordVO {
  id: number
  movieId: number
  movieTitle: string
  moviePoster: string
  status: string
  rating: number
  watchedAt: string
  createdAt: string
  updatedAt: string
}

// 排行
export interface RankingVO {
  rank: number
  movieId: number
  movieTitle: string
  moviePoster: string
  avgRating: number
  reviewCount: number
  watchCount: number
}
