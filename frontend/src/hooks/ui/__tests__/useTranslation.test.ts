import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, act } from '@testing-library/react'
import { useTranslation, getTranslation, getErrorMessage } from '../useTranslation'
import { useLanguageStore } from '@/stores/useLanguageStore'

// Mock useLanguageStore
vi.mock('@/stores/useLanguageStore', () => ({
  useLanguageStore: vi.fn(),
}))

describe('useTranslation', () => {
  const mockSetLanguage = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(useLanguageStore).mockReturnValue({
      language: 'ko',
      setLanguage: mockSetLanguage,
    })
  })

  describe('t function', () => {
    it('should translate a simple key', () => {
      const { result } = renderHook(() => useTranslation())

      // Test with a known translation key
      const translated = result.current.t('common.loading')
      expect(translated).toBe('로딩 중...')
    })

    it('should translate nested keys', () => {
      const { result } = renderHook(() => useTranslation())

      const translated = result.current.t('common.login')
      expect(translated).toBe('로그인')
    })

    it('should return the key if translation not found', () => {
      const { result } = renderHook(() => useTranslation())

      // Cast to any to test with invalid key
      const translated = result.current.t('nonexistent.key' as never)
      expect(translated).toBe('nonexistent.key')
    })
  })

  describe('tError function', () => {
    it('should translate error codes', () => {
      const { result } = renderHook(() => useTranslation())

      const translated = result.current.tError('INVALID_CREDENTIALS')
      expect(translated).toBe('이메일 또는 비밀번호가 올바르지 않습니다.')
    })

    it('should return fallback for unknown error codes', () => {
      const { result } = renderHook(() => useTranslation())

      const translated = result.current.tError('UNKNOWN_ERROR_CODE', 'Fallback message')
      expect(translated).toBe('Fallback message')
    })

    it('should return default error message when no fallback provided', () => {
      const { result } = renderHook(() => useTranslation())

      const translated = result.current.tError('UNKNOWN_ERROR_CODE')
      expect(translated).toBe('서버 오류가 발생했습니다.')
    })
  })

  describe('language switching', () => {
    it('should return current language', () => {
      const { result } = renderHook(() => useTranslation())

      expect(result.current.language).toBe('ko')
    })

    it('should provide setLanguage function', () => {
      const { result } = renderHook(() => useTranslation())

      act(() => {
        result.current.setLanguage('en')
      })

      expect(mockSetLanguage).toHaveBeenCalledWith('en')
    })

    it('should translate in English when language is en', () => {
      vi.mocked(useLanguageStore).mockReturnValue({
        language: 'en',
        setLanguage: mockSetLanguage,
      })

      const { result } = renderHook(() => useTranslation())

      const translated = result.current.t('common.loading')
      expect(translated).toBe('Loading...')
    })
  })
})

describe('getTranslation', () => {
  it('should get translation for Korean', () => {
    const result = getTranslation('ko', 'common.loading')
    expect(result).toBe('로딩 중...')
  })

  it('should get translation for English', () => {
    const result = getTranslation('en', 'common.loading')
    expect(result).toBe('Loading...')
  })

  it('should return key for non-existent translation', () => {
    const result = getTranslation('ko', 'nonexistent.key')
    expect(result).toBe('nonexistent.key')
  })
})

describe('getErrorMessage', () => {
  it('should get error message in Korean', () => {
    const result = getErrorMessage('ko', 'INVALID_CREDENTIALS')
    expect(result).toBe('이메일 또는 비밀번호가 올바르지 않습니다.')
  })

  it('should get error message in English', () => {
    const result = getErrorMessage('en', 'INVALID_CREDENTIALS')
    expect(result).toBe('Invalid email or password.')
  })

  it('should return fallback for unknown error', () => {
    const result = getErrorMessage('ko', 'UNKNOWN', 'Custom fallback')
    expect(result).toBe('Custom fallback')
  })

  it('should return default error for unknown error without fallback', () => {
    const result = getErrorMessage('ko', 'UNKNOWN')
    expect(result).toBe('서버 오류가 발생했습니다.')
  })
})
