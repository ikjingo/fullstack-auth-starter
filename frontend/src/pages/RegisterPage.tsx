import { useState, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Mail, User, Loader2 } from 'lucide-react'
import { registerFormSchema, type RegisterFormSchema } from '@/schemas/auth'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form'
import { PasswordInput, AuthPageLayout } from '@/components/common'
import { toast } from '@/stores/useToastStore'
import { useUserStore } from '@/stores/useUserStore'
import { authApi } from '@/api/auth'
import { toApiError } from '@/api/client/errors'
import { useTranslation } from '@/hooks'
import {
  inputIconLeft,
  iconSm,
  buttonBase,
  formGap,
} from '@/utils'

export function RegisterPage() {
  const navigate = useNavigate()
  const login = useUserStore((state) => state.login)
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const { t, tError } = useTranslation()

  const form = useForm<RegisterFormSchema>({
    resolver: zodResolver(registerFormSchema),
    defaultValues: {
      email: '',
      password: '',
      confirmPassword: '',
      nickname: '',
    },
  })

  const onSubmit = async (data: RegisterFormSchema) => {
    setIsLoading(true)
    try {
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      const { confirmPassword, ...registerData } = data
      const response = await authApi.register(registerData)
      login(response.user, response.token, response.refreshToken)
      toast.success(t('register.success'), t('register.successMessage'))
      navigate('/')
    } catch (error) {
      const apiError = toApiError(error)

      // 서버 validation 에러인 경우 필드별 에러 메시지 표시
      if (apiError.isValidationError() && apiError.data) {
        const fieldErrors = apiError.data as Record<string, string>
        let hasFieldError = false

        // 필드별 에러를 폼에 설정
        Object.entries(fieldErrors).forEach(([field, message]) => {
          if (['email', 'password', 'nickname'].includes(field)) {
            form.setError(field as 'email' | 'password' | 'nickname', {
              type: 'server',
              message,
            })
            hasFieldError = true
          }
        })

        // 필드 에러가 있으면 toast는 간단하게, 없으면 전체 메시지 표시
        if (hasFieldError) {
          toast.error(t('register.failed'), t('validation.checkFields'))
        } else {
          const message = apiError.errorType ? tError(apiError.errorType) : t('common.tryAgain')
          toast.error(t('register.failed'), message)
        }
      } else {
        const message = apiError.errorType ? tError(apiError.errorType) : t('common.tryAgain')
        toast.error(t('register.failed'), message)
      }
    } finally {
      setIsLoading(false)
    }
  }

  const handleTogglePassword = useCallback(() => {
    setShowPassword(prev => !prev)
  }, [])

  const handleToggleConfirmPassword = useCallback(() => {
    setShowConfirmPassword(prev => !prev)
  }, [])

  return (
    <AuthPageLayout
      title={t('register.title')}
      subtitle={t('register.subtitle')}
      footerLink={{
        text: t('auth.hasAccount'),
        linkText: t('common.login'),
        to: '/login',
      }}
    >
      <Form {...form}>
        <form onSubmit={form.handleSubmit(onSubmit)} className={formGap}>
          {/* 이메일 */}
          <FormField
            control={form.control}
            name="email"
            render={({ field }) => (
              <FormItem>
                <FormLabel>{t('auth.email')}</FormLabel>
                <FormControl>
                  <div className="relative">
                    <Mail className={`${inputIconLeft} ${iconSm}`} />
                    <Input
                      type="email"
                      placeholder={t('auth.emailPlaceholder')}
                      className="pl-10"
                      disabled={isLoading}
                      autoComplete="email"
                      {...field}
                    />
                  </div>
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />

          {/* 닉네임 */}
          <FormField
            control={form.control}
            name="nickname"
            render={({ field }) => (
              <FormItem>
                <FormLabel>{t('auth.nickname')}</FormLabel>
                <FormControl>
                  <div className="relative">
                    <User className={`${inputIconLeft} ${iconSm}`} />
                    <Input
                      type="text"
                      placeholder={t('auth.nicknamePlaceholder')}
                      className="pl-10"
                      disabled={isLoading}
                      autoComplete="username"
                      {...field}
                    />
                  </div>
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />

          {/* 비밀번호 */}
          <FormField
            control={form.control}
            name="password"
            render={({ field }) => (
              <FormItem>
                <FormLabel>{t('auth.password')}</FormLabel>
                <FormControl>
                  <PasswordInput
                    field={field}
                    placeholder={t('auth.passwordRequirements')}
                    disabled={isLoading}
                    showPassword={showPassword}
                    onToggleVisibility={handleTogglePassword}
                    autoComplete="new-password"
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />

          {/* 비밀번호 확인 */}
          <FormField
            control={form.control}
            name="confirmPassword"
            render={({ field }) => (
              <FormItem>
                <FormLabel>{t('auth.confirmPassword')}</FormLabel>
                <FormControl>
                  <PasswordInput
                    field={field}
                    placeholder={t('auth.confirmPasswordPlaceholder')}
                    disabled={isLoading}
                    showPassword={showConfirmPassword}
                    onToggleVisibility={handleToggleConfirmPassword}
                    autoComplete="new-password"
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />

          {/* 회원가입 버튼 */}
          <Button
            type="submit"
            className={`w-full ${buttonBase} btn-shine glow-primary-hover`}
            disabled={isLoading}
          >
            {isLoading ? (
              <>
                <Loader2 className={`${iconSm} animate-spin`} aria-hidden="true" />
                <span>{t('auth.registering')}</span>
              </>
            ) : (
              <span>{t('common.register')}</span>
            )}
          </Button>
        </form>
      </Form>
    </AuthPageLayout>
  )
}
