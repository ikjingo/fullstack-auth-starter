import { type ReactNode } from 'react'
import { SearchX, FileQuestion, Inbox } from 'lucide-react'
import { cn } from '@/lib/utils'
import { iconLg, emptyCenter, emptyStateIcon } from '@/utils'

type EmptyStateVariant = 'search' | 'data' | 'default'

const icons = {
  search: SearchX,
  data: Inbox,
  default: FileQuestion,
}

interface EmptyStateProps {
  variant?: EmptyStateVariant
  title: string
  description?: string
  action?: ReactNode
  className?: string
  icon?: ReactNode
}

export function EmptyState({
  variant = 'default',
  title,
  description,
  action,
  className,
  icon,
}: EmptyStateProps) {
  const Icon = icons[variant]

  return (
    <div className={cn(emptyCenter, 'px-4', className)}>
      <div className={emptyStateIcon}>
        {icon ?? <Icon className={`${iconLg} text-muted-foreground`} />}
      </div>
      <h3 className="text-lg font-semibold text-foreground mb-1">{title}</h3>
      {description && (
        <p className="text-sm text-muted-foreground text-center max-w-sm mb-4">
          {description}
        </p>
      )}
      {action && <div className="mt-2">{action}</div>}
    </div>
  )
}
