import { useState, useEffect } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Cloud, Sun, CloudRain, Wind, Loader2 } from 'lucide-react'
import { weatherApi } from '../api/weather-api'

export function WeatherWidget() {
  const [weather, setWeather] = useState<any>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    // Para simplificar, estamos usando uma localização padrão (ex: São Paulo).
    // Idealmente, poderíamos usar a geolocalização do navegador: navigator.geolocation
    const lat = -23.5505
    const lon = -46.6333
    
    weatherApi.getWeather(lat, lon)
      .then(setWeather)
      .catch(console.error)
      .finally(() => setLoading(false))
  }, [])

  if (loading) {
    return (
      <Card className="flex items-center justify-center h-32">
        <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
      </Card>
    )
  }

  if (!weather || weather.error) {
    return (
      <Card className="h-32 flex items-center justify-center border-dashed">
        <span className="text-sm text-muted-foreground">Previsão do tempo indisponível.</span>
      </Card>
    )
  }

  const icon = weather.weather[0]?.main === 'Clear' ? <Sun className="h-8 w-8 text-yellow-500" /> 
             : weather.weather[0]?.main === 'Rain' ? <CloudRain className="h-8 w-8 text-blue-400" />
             : <Cloud className="h-8 w-8 text-slate-400" />

  return (
    <Card className="overflow-hidden relative bg-gradient-to-br from-card to-card/50">
      <CardHeader className="pb-2">
        <CardTitle className="text-sm font-medium text-muted-foreground flex items-center gap-2">
          Previsão do Tempo
        </CardTitle>
      </CardHeader>
      <CardContent>
        <div className="flex items-center justify-between">
          <div>
            <div className="text-3xl font-bold">{Math.round(weather.main.temp)}°C</div>
            <p className="text-xs text-muted-foreground capitalize mt-1">
              {weather.weather[0]?.description}
            </p>
            <p className="text-xs text-muted-foreground mt-1 flex items-center gap-1">
              <Wind className="h-3 w-3" /> {weather.wind.speed} m/s
            </p>
          </div>
          <div className="p-3 bg-background/50 rounded-full">
            {icon}
          </div>
        </div>
      </CardContent>
    </Card>
  )
}
