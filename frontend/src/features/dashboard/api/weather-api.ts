import { apiClient } from '@/lib/api-client'

export const weatherApi = {
  getWeather: async (lat: number, lon: number) => {
    const response = await apiClient.get(`/weather?lat=${lat}&lon=${lon}`)
    return response.data
  }
}
