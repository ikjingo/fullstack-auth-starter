import { memo, type ChangeEvent, type Ref } from 'react'
import { Lock, Eye, EyeOff } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { cn } from '@/lib/utils'
import { iconSm, inputIconRight } from '@/utils'

// react-hook-form ControllerRenderProps 호환 타입
interface FieldProps {
  value: string | undefined
  onChange: (event: ChangeEvent<HTMLInputElement>) => void
  onBlur: () => void
  ref: Ref<HTMLInputElement>
  name: string
}

export interface PasswordInputProps {
  placeholder: string
  disabled?: boolean
  showPassword: boolean
  onToggleVisibility: () => void
  autoComplete?: string

  // 패턴 1: 직접 제어 (기존 방식 - AccountInfoSection에서 사용)
  value?: string
  onChange?: (value: string) => void

  // 패턴 2: react-hook-form field (신규 - LoginPage, RegisterPage에서 사용)
  field?: FieldProps
}

export const PasswordInput = memo(function PasswordInput({
  value,
  onChange,
  field,
  placeholder,
  disabled,
  showPassword,
  onToggleVisibility,
  autoComplete,
}: PasswordInputProps) {
  // field가 있으면 field 값 사용, 없으면 직접 전달된 value 사용
  const inputValue = field?.value ?? value ?? ''

  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    if (field) {
      // react-hook-form 패턴: event 전달
      field.onChange(e)
    } else if (onChange) {
      // 기존 패턴: 문자열 전달
      onChange(e.target.value)
    }
  }

  return (
    <div className="relative">
      <Lock className={cn('absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground', iconSm)} />
      <Input
        ref={field?.ref}
        name={field?.name}
        type={showPassword ? 'text' : 'password'}
        placeholder={placeholder}
        className="pl-10 pr-10"
        disabled={disabled}
        value={inputValue}
        onChange={handleChange}
        onBlur={field?.onBlur}
        autoComplete={autoComplete}
      />
      <Button
        type="button"
        variant="ghost"
        size="icon"
        onClick={onToggleVisibility}
        className={cn(inputIconRight, 'h-auto w-auto')}
        tabIndex={-1}
      >
        {showPassword ? <EyeOff className={iconSm} /> : <Eye className={iconSm} />}
      </Button>
    </div>
  )
})
