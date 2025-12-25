/**
 * 폼 입력 필드 래퍼 컴포넌트
 * shadcn/ui Form 컴포넌트들을 조합하여 반복 코드를 줄입니다.
 */
import { type ReactNode } from 'react'
import {
  useFormContext,
  type FieldPath,
  type FieldValues,
  type ControllerRenderProps,
} from 'react-hook-form'
import { Input } from '@/components/ui/input'
import {
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
  FormDescription,
} from '@/components/ui/form'
import { cn } from '@/lib/utils'

type InputType = 'text' | 'email' | 'tel' | 'url' | 'number'

interface FormInputFieldProps<
  TFieldValues extends FieldValues = FieldValues,
  TName extends FieldPath<TFieldValues> = FieldPath<TFieldValues>,
> {
  /** 필드 이름 (폼 스키마의 필드 키) */
  name: TName
  /** 라벨 텍스트 */
  label: string
  /** 플레이스홀더 */
  placeholder?: string
  /** 입력 타입 */
  type?: InputType
  /** 비활성화 여부 */
  disabled?: boolean
  /** 설명 텍스트 */
  description?: string
  /** 왼쪽 아이콘 */
  leftIcon?: ReactNode
  /** 오른쪽 아이콘 또는 액션 버튼 */
  rightElement?: ReactNode
  /** 자동 완성 힌트 */
  autoComplete?: string
  /** 추가 클래스명 */
  className?: string
  /** 입력 필드 추가 클래스명 */
  inputClassName?: string
}

/**
 * 기본 텍스트 입력 폼 필드
 *
 * @example
 * ```tsx
 * <FormInputField
 *   name="email"
 *   label="이메일"
 *   type="email"
 *   placeholder="example@email.com"
 *   leftIcon={<Mail className="w-4 h-4" />}
 * />
 * ```
 */
export function FormInputField<
  TFieldValues extends FieldValues = FieldValues,
  TName extends FieldPath<TFieldValues> = FieldPath<TFieldValues>,
>({
  name,
  label,
  placeholder,
  type = 'text',
  disabled = false,
  description,
  leftIcon,
  rightElement,
  autoComplete,
  className,
  inputClassName,
}: FormInputFieldProps<TFieldValues, TName>) {
  const form = useFormContext<TFieldValues>()

  return (
    <FormField
      control={form.control}
      name={name}
      render={({ field }: { field: ControllerRenderProps<TFieldValues, TName> }) => (
        <FormItem className={className}>
          <FormLabel>{label}</FormLabel>
          <FormControl>
            <div className={cn('relative', (leftIcon || rightElement) && 'flex items-center')}>
              {leftIcon && (
                <div className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground pointer-events-none">
                  {leftIcon}
                </div>
              )}
              <Input
                type={type}
                placeholder={placeholder}
                disabled={disabled}
                autoComplete={autoComplete}
                className={cn(
                  leftIcon && 'pl-10',
                  rightElement && 'pr-10',
                  inputClassName
                )}
                {...field}
              />
              {rightElement && (
                <div className="absolute right-3 top-1/2 -translate-y-1/2">
                  {rightElement}
                </div>
              )}
            </div>
          </FormControl>
          {description && <FormDescription>{description}</FormDescription>}
          <FormMessage />
        </FormItem>
      )}
    />
  )
}

interface CustomFormFieldProps<
  TFieldValues extends FieldValues = FieldValues,
  TName extends FieldPath<TFieldValues> = FieldPath<TFieldValues>,
> {
  /** 필드 이름 */
  name: TName
  /** 라벨 텍스트 */
  label: string
  /** 설명 텍스트 */
  description?: string
  /** 커스텀 렌더 함수 */
  render: (props: {
    field: ControllerRenderProps<TFieldValues, TName>
  }) => ReactNode
  /** 추가 클래스명 */
  className?: string
}

/**
 * 커스텀 폼 필드 (비밀번호 입력, 셀렉트 등 커스텀 컴포넌트용)
 *
 * @example
 * ```tsx
 * <CustomFormField
 *   name="password"
 *   label="비밀번호"
 *   render={({ field }) => (
 *     <PasswordInput
 *       field={field}
 *       showPassword={showPassword}
 *       onToggleVisibility={() => setShowPassword(!showPassword)}
 *     />
 *   )}
 * />
 * ```
 */
export function CustomFormField<
  TFieldValues extends FieldValues = FieldValues,
  TName extends FieldPath<TFieldValues> = FieldPath<TFieldValues>,
>({
  name,
  label,
  description,
  render,
  className,
}: CustomFormFieldProps<TFieldValues, TName>) {
  const form = useFormContext<TFieldValues>()

  return (
    <FormField
      control={form.control}
      name={name}
      render={({ field }) => (
        <FormItem className={className}>
          <FormLabel>{label}</FormLabel>
          <FormControl>
            {render({ field })}
          </FormControl>
          {description && <FormDescription>{description}</FormDescription>}
          <FormMessage />
        </FormItem>
      )}
    />
  )
}
