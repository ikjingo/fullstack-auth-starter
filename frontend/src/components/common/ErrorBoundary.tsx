import { Component, type ReactNode } from 'react'
import { AlertTriangle, RefreshCw, Home } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { iconWithText, iconLg, flexCenter, logError } from '@/utils'

interface ErrorBoundaryProps {
  children: ReactNode
  fallback?: ReactNode
}

interface ErrorBoundaryState {
  hasError: boolean
  error: Error | null
}

export class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
  constructor(props: ErrorBoundaryProps) {
    super(props)
    this.state = { hasError: false, error: null }
  }

  static getDerivedStateFromError(error: Error): ErrorBoundaryState {
    return { hasError: true, error }
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    // 로컬 에러 로깅
    logError('ErrorBoundary caught an error:', { error, errorInfo })
  }

  handleRetry = () => {
    this.setState({ hasError: false, error: null })
  }

  handleGoHome = () => {
    window.location.href = '/'
  }

  render() {
    if (this.state.hasError) {
      if (this.props.fallback) {
        return this.props.fallback
      }

      return <ErrorFallback onRetry={this.handleRetry} onGoHome={this.handleGoHome} />
    }

    return this.props.children
  }
}

interface ErrorFallbackProps {
  onRetry: () => void
  onGoHome: () => void
}

function ErrorFallback({ onRetry, onGoHome }: ErrorFallbackProps) {
  return (
    <div className={`w-full min-h-[400px] ${flexCenter} bg-background`}>
      <div className="max-w-md mx-auto px-4 text-center">
        <div className={`w-16 h-16 bg-destructive/10 rounded-full ${flexCenter} mx-auto mb-6`}>
          <AlertTriangle className={`${iconLg} text-destructive`} />
        </div>
        <h2 className="text-xl font-semibold text-foreground mb-2">
          Something went wrong
        </h2>
        <p className="text-muted-foreground mb-6">
          An unexpected error occurred. Please try again later.
        </p>
        <div className="flex flex-col sm:flex-row gap-3 justify-center">
          <Button variant="outline" onClick={onRetry}>
            <RefreshCw className={iconWithText} />
            Retry
          </Button>
          <Button onClick={onGoHome}>
            <Home className={iconWithText} />
            Go Home
          </Button>
        </div>
      </div>
    </div>
  )
}
