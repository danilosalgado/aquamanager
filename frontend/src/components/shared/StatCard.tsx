import type { LucideIcon } from 'lucide-react'
import { Card, CardContent } from '@/components/ui/card'
import { cn } from '@/lib/utils'
import { Skeleton } from '@/components/ui/skeleton'

export function StatCard({
  label,
  value,
  icon: Icon,
  trend,
  trendLabel,
  loading,
  tone = 'default',
}: {
  label: string
  value: string
  icon: LucideIcon
  trend?: 'up' | 'down' | 'neutral'
  trendLabel?: string
  loading?: boolean
  tone?: 'default' | 'success' | 'warning' | 'destructive'
}) {
  return (
    <Card className="relative overflow-hidden">
      <CardContent className="p-5">
        <div className="flex items-start justify-between">
          <div>
            <p className="text-xs font-medium text-muted-foreground">{label}</p>
            {loading ? (
              <Skeleton className="mt-2 h-7 w-24" />
            ) : (
              <p className="mt-1 text-2xl font-semibold tracking-tight">{value}</p>
            )}
            {trendLabel && !loading && (
              <p
                className={cn(
                  'mt-1 text-xs font-medium',
                  trend === 'up' && 'text-success',
                  trend === 'down' && 'text-destructive',
                  trend === 'neutral' && 'text-muted-foreground',
                )}
              >
                {trendLabel}
              </p>
            )}
          </div>
          <div
            className={cn(
              'flex h-9 w-9 items-center justify-center rounded-lg',
              tone === 'default' && 'bg-primary/10 text-primary',
              tone === 'success' && 'bg-success/10 text-success',
              tone === 'warning' && 'bg-warning/10 text-warning',
              tone === 'destructive' && 'bg-destructive/10 text-destructive',
            )}
          >
            <Icon className="h-4.5 w-4.5" />
          </div>
        </div>
      </CardContent>
    </Card>
  )
}
