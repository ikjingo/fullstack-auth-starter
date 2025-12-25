import { X, CheckCircle, AlertCircle, AlertTriangle, Info } from 'lucide-react'
import { useToastStore } from '@/stores/useToastStore'
import { cn } from '@/lib/utils'
import { iconSm, iconMd, toastContainer, toastCloseButton } from '@/utils'

const icons = {
  success: CheckCircle,
  error: AlertCircle,
  warning: AlertTriangle,
  info: Info,
}

const styles = {
  success: 'bg-green-500/10 border-green-500/20 text-green-400',
  error: 'bg-red-500/10 border-red-500/20 text-red-400',
  warning: 'bg-yellow-500/10 border-yellow-500/20 text-yellow-400',
  info: 'bg-blue-500/10 border-blue-500/20 text-blue-400',
}

export function ToastContainer() {
  const { toasts, removeToast } = useToastStore()

  if (toasts.length === 0) return null

  return (
    <div className={toastContainer}>
      {toasts.map((toast) => {
        const Icon = icons[toast.type]

        return (
          <div
            key={toast.id}
            role="alert"
            aria-live="polite"
            className={cn(
              'flex items-start gap-3 p-4 rounded-lg border backdrop-blur-sm',
              'animate-in slide-in-from-right-full duration-300',
              styles[toast.type]
            )}
          >
            <Icon className={`${iconMd} flex-shrink-0 mt-0.5`} />
            <div className="flex-1 min-w-0">
              <p className="font-medium">{toast.title}</p>
              {toast.message && (
                <p className="text-sm opacity-80 mt-1">{toast.message}</p>
              )}
            </div>
            <button
              onClick={() => removeToast(toast.id)}
              className={toastCloseButton}
              aria-label="Close notification"
            >
              <X className={iconSm} />
            </button>
          </div>
        )
      })}
    </div>
  )
}
