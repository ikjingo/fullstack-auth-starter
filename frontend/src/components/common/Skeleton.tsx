import { Skeleton } from '@/components/ui/skeleton'
import { cn } from '@/lib/utils'
import { card, cardPadding, tableContainer, tableHeaderRow, skeletonRow, flexRowGapLg, formGap } from '@/utils'

// 테이블 행 스켈레톤
interface TableRowSkeletonProps {
  columns?: number
  className?: string
}

export function TableRowSkeleton({ columns = 5, className }: TableRowSkeletonProps) {
  return (
    <div className={cn(skeletonRow, className)}>
      {/* 아바타 */}
      <Skeleton className="w-9 h-9 rounded-md shrink-0" />
      {/* 컬럼들 */}
      {Array.from({ length: columns - 1 }).map((_, i) => (
        <Skeleton key={i} className="h-4 flex-1" style={{ maxWidth: `${100 - i * 10}px` }} />
      ))}
    </div>
  )
}

// 닉네임 테이블 스켈레톤
interface NicknameTableSkeletonProps {
  rows?: number
}

export function NicknameTableSkeleton({ rows = 5 }: NicknameTableSkeletonProps) {
  return (
    <div className={tableContainer}>
      {/* 헤더 */}
      <div className={cn(tableHeaderRow, skeletonRow)}>
        <Skeleton className="h-4 w-24" />
        <Skeleton className="h-4 w-16" />
        <Skeleton className="h-4 w-16" />
        <Skeleton className="h-4 w-20" />
        <Skeleton className="h-4 w-24" />
      </div>
      {/* 행들 */}
      {Array.from({ length: rows }).map((_, i) => (
        <TableRowSkeleton key={i} columns={5} />
      ))}
    </div>
  )
}

// 프로필 카드 스켈레톤
export function ProfileCardSkeleton() {
  return (
    <div className={cn(card, cardPadding)}>
      <div className={flexRowGapLg}>
        <Skeleton className="w-16 h-16 rounded-full" />
        <div className="space-y-2">
          <Skeleton className="h-5 w-32" />
          <Skeleton className="h-4 w-48" />
        </div>
      </div>
    </div>
  )
}

// 계정 정보 섹션 스켈레톤
export function AccountInfoSkeleton() {
  return (
    <div className={cn(card, 'divide-y divide-border')}>
      {/* 닉네임 */}
      <div className="p-4">
        <Skeleton className="h-3 w-12 mb-2" />
        <Skeleton className="h-5 w-24" />
      </div>
      {/* 비밀번호 */}
      <div className="p-4">
        <Skeleton className="h-3 w-16 mb-2" />
        <Skeleton className="h-5 w-20" />
      </div>
    </div>
  )
}

// 카드 스켈레톤
interface CardSkeletonProps {
  lines?: number
  className?: string
}

export function CardSkeleton({ lines = 3, className }: CardSkeletonProps) {
  return (
    <div className={cn(card, cardPadding, formGap, className)}>
      <Skeleton className="h-5 w-1/3" />
      <div className="space-y-2">
        {Array.from({ length: lines }).map((_, i) => (
          <Skeleton key={i} className="h-4" style={{ width: `${100 - i * 15}%` }} />
        ))}
      </div>
    </div>
  )
}

// 버튼 스켈레톤
interface ButtonSkeletonProps {
  size?: 'sm' | 'md' | 'lg'
  className?: string
}

export function ButtonSkeleton({ size = 'md', className }: ButtonSkeletonProps) {
  const sizeClasses = {
    sm: 'h-8 w-16',
    md: 'h-10 w-24',
    lg: 'h-12 w-32',
  }

  return <Skeleton className={cn(sizeClasses[size], 'rounded-md', className)} />
}

// 입력 필드 스켈레톤
export function InputSkeleton({ className }: { className?: string }) {
  return <Skeleton className={cn('h-11 w-full rounded-md', className)} />
}

// 텍스트 스켈레톤
interface TextSkeletonProps {
  width?: string | number
  className?: string
}

export function TextSkeleton({ width = '100%', className }: TextSkeletonProps) {
  return <Skeleton className={cn('h-4', className)} style={{ width }} />
}
