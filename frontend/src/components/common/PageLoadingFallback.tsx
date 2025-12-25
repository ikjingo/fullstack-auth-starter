import { Loader2 } from 'lucide-react'
import { flexCenter, pageContainer, iconLg } from '@/utils'

/**
 * 페이지 Lazy Loading 시 표시되는 폴백 컴포넌트
 * React.lazy + Suspense와 함께 사용
 */
export function PageLoadingFallback() {
  return (
    <div className={`${pageContainer} ${flexCenter} min-h-[50vh]`}>
      <Loader2 className={`${iconLg} animate-spin text-muted-foreground`} />
    </div>
  )
}
