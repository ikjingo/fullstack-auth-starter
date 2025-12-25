import { useEffect, useCallback, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import { SESSION_EXPIRED_EVENT } from '@/api'
import { useUserStore } from '@/stores/useUserStore'
import { toast } from '@/stores/useToastStore'

/**
 * 세션 만료 이벤트를 감지하고 처리하는 훅
 * App 컴포넌트에서 한 번만 사용해야 함
 */
export function useSessionExpiration() {
  const navigate = useNavigate()
  const logout = useUserStore((state) => state.logout)
  const isAuthenticated = useUserStore((state) => state.isAuthenticated)

  // 중복 처리 방지를 위한 ref
  const isProcessingRef = useRef(false)

  const handleSessionExpired = useCallback(() => {
    // 이미 처리 중이거나 로그인되지 않은 상태면 무시
    if (isProcessingRef.current || !isAuthenticated) {
      return
    }

    isProcessingRef.current = true

    // 로그아웃 처리
    logout()

    // 토스트 메시지 표시
    toast.warning('세션 만료', '로그인이 만료되었습니다. 다시 로그인해 주세요.')

    // 로그인 페이지로 이동 (현재 경로를 state로 전달하여 로그인 후 복귀 가능)
    const currentPath = window.location.pathname
    if (currentPath !== '/login' && currentPath !== '/register') {
      navigate('/login', { state: { from: currentPath } })
    }

    // 처리 완료 후 플래그 리셋 (다음 세션 만료를 위해)
    setTimeout(() => {
      isProcessingRef.current = false
    }, 1000)
  }, [logout, navigate, isAuthenticated])

  useEffect(() => {
    window.addEventListener(SESSION_EXPIRED_EVENT, handleSessionExpired)

    return () => {
      window.removeEventListener(SESSION_EXPIRED_EVENT, handleSessionExpired)
    }
  }, [handleSessionExpired])
}
