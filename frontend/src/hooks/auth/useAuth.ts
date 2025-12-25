import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { authApi } from '@/api'
import { queryKeys } from '@/api/client'
import { useUserStore } from '@/stores'
import type { LoginFormSchema, RegisterFormSchema } from '@/schemas'

/**
 * Query Keys (통합 queryKeys 사용)
 * @deprecated queryKeys.auth 사용 권장
 */
export const authKeys = {
  user: queryKeys.auth.user(),
}

/**
 * 현재 로그인된 사용자 정보 조회
 */
export function useCurrentUser() {
  const { isAuthenticated, setUser } = useUserStore()

  return useQuery({
    queryKey: authKeys.user,
    queryFn: async () => {
      const response = await authApi.getMe()
      const user = {
        id: String(response.id),
        email: response.email,
        nickname: response.nickname,
        role: response.role as 'USER' | 'ADMIN',
        hasPassword: response.hasPassword,
        profileImageUrl: response.profileImageUrl,
      }
      setUser(user)
      return user
    },
    enabled: isAuthenticated,
    staleTime: 1000 * 60 * 10, // 10분 (세션 중 사용자 정보는 거의 변경되지 않음)
    retry: false,
  })
}

/**
 * 로그인 뮤테이션 훅
 */
export function useLogin() {
  const queryClient = useQueryClient()
  const { setUser } = useUserStore()

  return useMutation({
    mutationFn: (data: LoginFormSchema) => authApi.login(data),
    onSuccess: (response) => {
      setUser(response.user)
      queryClient.setQueryData(authKeys.user, response.user)
    },
  })
}

/**
 * 회원가입 뮤테이션 훅
 */
export function useRegister() {
  const queryClient = useQueryClient()
  const { setUser } = useUserStore()

  return useMutation({
    mutationFn: (data: RegisterFormSchema) => {
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      const { confirmPassword, ...registerData } = data
      return authApi.register(registerData)
    },
    onSuccess: (response) => {
      setUser(response.user)
      queryClient.setQueryData(authKeys.user, response.user)
    },
  })
}

/**
 * 로그아웃 훅
 */
export function useLogout() {
  const queryClient = useQueryClient()
  const { logout } = useUserStore()

  return useMutation({
    mutationFn: async () => {
      authApi.logout()
    },
    onSuccess: () => {
      logout()
      queryClient.removeQueries({ queryKey: authKeys.user })
      queryClient.clear()
    },
  })
}
