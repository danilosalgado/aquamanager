export interface ApiResponse<T> {
  data: T
}

export interface ApiFieldError {
  field: string
  message: string
}

export interface ApiErrorPayload {
  code: string
  message: string
  details: ApiFieldError[]
  timestamp: string
  path: string
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  last: boolean
}
