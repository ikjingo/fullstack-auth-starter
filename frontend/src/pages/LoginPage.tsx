import { useState, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Mail, Loader2 } from 'lucide-react'
import { loginFormSchema, type LoginFormSchema } from '@/schemas/auth'
import { Button } from '@/components/ui/button'
import { Checkbox } from '@/components/ui/checkbox'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
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

export function LoginPage() {
  const navigate = useNavigate()
  const login = useUserStore((state) => state.login)
  const [showPassword, setShowPassword] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const [rememberMe, setRememberMe] = useState(false)
  const { t, tError } = useTranslation()

  const form = useForm<LoginFormSchema>({
    resolver: zodResolver(loginFormSchema),
    defaultValues: {
      email: '',
      password: '',
    },
  })

  const onSubmit = async (data: LoginFormSchema) => {
    setIsLoading(true)
    try {
      const response = await authApi.login(data)
      login(response.user, response.token, response.refreshToken, rememberMe)
      toast.success(t('login.success'), t('login.successMessage'))
      navigate('/')
    } catch (error) {
      const apiError = toApiError(error)
      const message = apiError.errorType ? tError(apiError.errorType) : tError('INVALID_CREDENTIALS')
      toast.error(t('login.failed'), message)
    } finally {
      setIsLoading(false)
    }
  }

  const handleTogglePassword = useCallback(() => {
    setShowPassword(prev => !prev)
  }, [])

  const handleRememberMeChange = useCallback((checked: boolean | 'indeterminate') => {
    setRememberMe(!!checked)
  }, [])

  return (
    <AuthPageLayout
      title={t('login.title')}
      subtitle={t('login.subtitle')}
      footerLink={{
        text: t('auth.noAccount'),
        linkText: t('common.register'),
        to: '/register',
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
                    placeholder={t('auth.passwordPlaceholder')}
                    disabled={isLoading}
                    showPassword={showPassword}
                    onToggleVisibility={handleTogglePassword}
                    autoComplete="current-password"
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />

          {/* 로그인 상태 유지 */}
          <div className="flex items-center">
            <div className="flex items-center space-x-2">
              <Checkbox
                id="rememberMe"
                checked={rememberMe}
                onCheckedChange={handleRememberMeChange}
                disabled={isLoading}
              />
              <Label
                htmlFor="rememberMe"
                className="text-sm font-normal cursor-pointer"
              >
                {t('auth.rememberMe')}
              </Label>
            </div>
          </div>

          {/* 로그인 버튼 */}
          <Button
            type="submit"
            className={`w-full ${buttonBase} btn-shine glow-primary-hover`}
            disabled={isLoading}
          >
            {isLoading ? (
              <>
                <Loader2 className={`${iconSm} animate-spin`} aria-hidden="true" />
                <span>{t('auth.loggingIn')}</span>
              </>
            ) : (
              <span>{t('common.login')}</span>
            )}
          </Button>
        </form>
      </Form>
    </AuthPageLayout>
  )
}
