import type { ReactNode } from 'react'
import { Loader2 } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { iconSm, buttonBase } from '@/utils'

interface LoadingButtonProps {
  isLoading: boolean
  loadingText: string
  children: ReactNode
  type?: 'button' | 'submit' | 'reset'
  disabled?: boolean
  className?: string
  onClick?: () => void
}

export function LoadingButton({
  isLoading,
  loadingText,
  children,
  type = 'submit',
  disabled,
  className,
  onClick,
}: LoadingButtonProps) {
  return (
    <Button
      type={type}
      className={`w-full ${buttonBase} btn-shine glow-primary-hover${className ? ` ${className}` : ''}`}
      disabled={isLoading || disabled}
      onClick={onClick}
    >
      {isLoading ? (
        <>
          <Loader2 className={`${iconSm} animate-spin`} aria-hidden="true" />
          <span>{loadingText}</span>
        </>
      ) : (
        <span>{children}</span>
      )}
    </Button>
  )
}
