import { useState } from 'react'
import type { ImgHTMLAttributes } from 'react'
import { cn } from '@/lib/utils'

interface OptimizedImageProps extends Omit<ImgHTMLAttributes<HTMLImageElement>, 'src'> {
  src: string
  fallbackSrc?: string
  aspectRatio?: 'square' | 'video' | 'auto' | string
  objectFit?: 'contain' | 'cover' | 'fill' | 'none' | 'scale-down'
  lazy?: boolean
  blur?: boolean
}

const aspectRatioClasses: Record<string, string> = {
  square: 'aspect-square',
  video: 'aspect-video',
  auto: '',
}

/**
 * 최적화된 이미지 컴포넌트
 *
 * 특징:
 * - Lazy loading 기본 지원
 * - WebP 포맷 자동 폴백
 * - 로딩 중 blur placeholder
 * - 에러 시 fallback 이미지
 * - 반응형 이미지 지원
 *
 * 사용법:
 * ```tsx
 * <OptimizedImage
 *   src="/images/hero.jpg"
 *   alt="Hero image"
 *   aspectRatio="video"
 *   className="rounded-lg"
 * />
 * ```
 *
 * vite-imagetools와 함께 사용:
 * ```tsx
 * import heroWebp from '@/assets/hero.jpg?w=800&format=webp'
 * import heroFallback from '@/assets/hero.jpg?w=800'
 *
 * <OptimizedImage
 *   src={heroWebp}
 *   fallbackSrc={heroFallback}
 *   alt="Hero image"
 * />
 * ```
 */
export function OptimizedImage({
  src,
  fallbackSrc,
  alt = '',
  aspectRatio = 'auto',
  objectFit = 'cover',
  lazy = true,
  blur = true,
  className,
  onLoad,
  onError,
  ...props
}: OptimizedImageProps) {
  const [isLoading, setIsLoading] = useState(true)
  const [hasError, setHasError] = useState(false)
  const [currentSrc, setCurrentSrc] = useState(src)

  const handleLoad = (e: React.SyntheticEvent<HTMLImageElement>) => {
    setIsLoading(false)
    onLoad?.(e)
  }

  const handleError = (e: React.SyntheticEvent<HTMLImageElement>) => {
    if (fallbackSrc && currentSrc !== fallbackSrc) {
      setCurrentSrc(fallbackSrc)
    } else {
      setHasError(true)
    }
    onError?.(e)
  }

  const aspectClass = aspectRatioClasses[aspectRatio] || ''
  const customAspectStyle = !aspectRatioClasses[aspectRatio] && aspectRatio !== 'auto'
    ? { aspectRatio }
    : undefined

  if (hasError) {
    return (
      <div
        className={cn(
          'flex items-center justify-center bg-muted text-muted-foreground',
          aspectClass,
          className
        )}
        style={customAspectStyle}
        role="img"
        aria-label={alt || 'Image failed to load'}
      >
        <span className="text-sm">이미지를 불러올 수 없습니다</span>
      </div>
    )
  }

  return (
    <div
      className={cn('relative overflow-hidden', aspectClass, className)}
      style={customAspectStyle}
    >
      {isLoading && blur && (
        <div className="absolute inset-0 bg-muted animate-pulse" />
      )}
      <img
        src={currentSrc}
        alt={alt}
        loading={lazy ? 'lazy' : 'eager'}
        decoding="async"
        onLoad={handleLoad}
        onError={handleError}
        className={cn(
          'w-full h-full transition-opacity duration-300',
          objectFit === 'contain' && 'object-contain',
          objectFit === 'cover' && 'object-cover',
          objectFit === 'fill' && 'object-fill',
          objectFit === 'none' && 'object-none',
          objectFit === 'scale-down' && 'object-scale-down',
          isLoading && blur ? 'opacity-0' : 'opacity-100'
        )}
        {...props}
      />
    </div>
  )
}

/**
 * picture 요소를 사용한 반응형 이미지 컴포넌트
 * WebP/AVIF 자동 지원
 */
interface ResponsiveImageProps extends OptimizedImageProps {
  webpSrc?: string
  avifSrc?: string
  sizes?: string
}

export function ResponsiveImage({
  src,
  webpSrc,
  avifSrc,
  sizes = '100vw',
  alt = '',
  className,
  ...props
}: ResponsiveImageProps) {
  return (
    <picture>
      {avifSrc && <source srcSet={avifSrc} type="image/avif" sizes={sizes} />}
      {webpSrc && <source srcSet={webpSrc} type="image/webp" sizes={sizes} />}
      <OptimizedImage
        src={src}
        alt={alt}
        className={className}
        {...props}
      />
    </picture>
  )
}
