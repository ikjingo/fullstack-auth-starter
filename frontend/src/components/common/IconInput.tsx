import type { LucideIcon } from 'lucide-react'
import { Input } from '@/components/ui/input'
import { inputIconLeft, iconSm } from '@/utils'

interface IconInputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  icon: LucideIcon
}

export function IconInput({ icon: Icon, className, ...props }: IconInputProps) {
  return (
    <div className="relative">
      <Icon className={`${inputIconLeft} ${iconSm}`} />
      <Input
        className={`pl-10${className ? ` ${className}` : ''}`}
        {...props}
      />
    </div>
  )
}
