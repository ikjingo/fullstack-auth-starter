import { useMutation, type UseMutationOptions } from '@tanstack/react-query'
import { toast } from '@/stores/useToastStore'
import { toApiError, type ApiError } from '@/api/client/errors'
import { useTranslation } from '@/hooks/ui/useTranslation'

/**
 * API mutation 옵션
 */
export interface UseApiMutationOptions<TData, TVariables> {
  /**
   * 성공 시 표시할 토스트 메시지
   */
  successMessage?: {
    title: string
    description: string
  }
  /**
   * 에러 토스트의 제목 (기본값: '오류')
   */
  errorTitle?: string
  /**
   * 기존 TanStack Query onSuccess 콜백
   */
  onSuccess?: (data: TData, variables: TVariables) => void
  /**
   * 기존 TanStack Query onError 콜백
   */
  onError?: (error: ApiError, variables: TVariables) => void
  /**
   * 기존 TanStack Query onSettled 콜백
   */
  onSettled?: (data: TData | undefined, error: ApiError | null, variables: TVariables) => void
  /**
   * 에러 메시지 표시 여부 (기본값: true)
   */
  showErrorToast?: boolean
  /**
   * 성공 메시지 표시 여부 (기본값: true, successMessage가 있을 때만)
   */
  showSuccessToast?: boolean
}

/**
 * API mutation을 위한 공통 훅
 *
 * 주요 기능:
 * - 자동 에러 변환 (toApiError)
 * - 성공/에러 토스트 자동 표시
 * - 에러 코드 기반 다국어 메시지
 *
 * @example
 * ```typescript
 * const { mutate, isPending } = useApiMutation(
 *   (data: UpdateNicknameRequest) => profileApi.updateNickname(data),
 *   {
 *     successMessage: {
 *       title: t('myPage.nicknameUpdated'),
 *       description: t('myPage.nicknameUpdatedMessage')
 *     },
 *     errorTitle: t('myPage.nicknameUpdateFailed'),
 *     onSuccess: (response) => {
 *       login(response.user, response.token)
 *     }
 *   }
 * )
 * ```
 */
export function useApiMutation<TData, TVariables = void>(
  mutationFn: (variables: TVariables) => Promise<TData>,
  options?: UseApiMutationOptions<TData, TVariables>
) {
  const { t, tError } = useTranslation()
  const {
    successMessage,
    errorTitle = t('common.error'),
    onSuccess,
    onError,
    onSettled,
    showErrorToast = true,
    showSuccessToast = true,
  } = options ?? {}

  return useMutation<TData, ApiError, TVariables>({
    mutationFn,
    onSuccess: (data, variables) => {
      if (showSuccessToast && successMessage) {
        toast.success(successMessage.title, successMessage.description)
      }
      onSuccess?.(data, variables)
    },
    onError: (error, variables) => {
      const apiError = toApiError(error)
      if (showErrorToast) {
        const message = apiError.errorType
          ? tError(apiError.errorType)
          : apiError.message || t('common.tryAgain')
        toast.error(errorTitle, message)
      }
      onError?.(apiError, variables)
    },
    onSettled: (data, error, variables) => {
      const apiError = error ? toApiError(error) : null
      onSettled?.(data, apiError, variables)
    },
  } as UseMutationOptions<TData, ApiError, TVariables>)
}
