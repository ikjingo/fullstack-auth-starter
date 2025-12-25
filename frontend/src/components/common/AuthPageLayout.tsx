import type { ReactNode } from 'react'
import { Link } from 'react-router-dom'
import {
  pageContainer,
  pageContent,
  pageContentNarrow,
  pageHeader,
  pageTitle,
  pageSubtitle,
  card,
  cardPadding,
  textSmMuted,
  linkMedium,
} from '@/utils'

interface AuthPageLayoutProps {
  title: string
  subtitle: string
  children: ReactNode
  /** 푸터 링크 정보 (텍스트 + 링크) */
  footerLink?: {
    text: string
    linkText: string
    to: string
  }
  /** 부제목 추가 클래스 */
  subtitleClassName?: string
  /** 카드에 hover/transition 효과 비활성화 */
  disableCardEffects?: boolean
}

export function AuthPageLayout({
  title,
  subtitle,
  children,
  footerLink,
  subtitleClassName,
  disableCardEffects = false,
}: AuthPageLayoutProps) {
  const cardClasses = disableCardEffects
    ? `${card} ${cardPadding}`
    : `${card} ${cardPadding} card-hover page-transition`

  return (
    <div className={pageContainer}>
      <div className={pageContent}>
        <div className={pageContentNarrow}>
          {/* 헤더 */}
          <div className={pageHeader}>
            <h1 className={pageTitle}>{title}</h1>
            <p className={`${pageSubtitle}${subtitleClassName ? ` ${subtitleClassName}` : ''}`}>
              {subtitle}
            </p>
          </div>

          {/* 폼 카드 */}
          <div className={cardClasses}>
            {children}
          </div>

          {/* 푸터 링크 */}
          {footerLink && (
            <p className={`text-center ${textSmMuted} mt-6`}>
              {footerLink.text}{' '}
              <Link to={footerLink.to} className={linkMedium}>
                {footerLink.linkText}
              </Link>
            </p>
          )}
        </div>
      </div>
    </div>
  )
}
