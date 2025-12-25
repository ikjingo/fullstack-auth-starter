import { useLanguageStore } from '@/stores/useLanguageStore'
import { translations, type Language, type TranslationKeys } from '@/locales'

type NestedKeyOf<T> = T extends object
  ? {
      [K in keyof T]: K extends string
        ? T[K] extends object
          ? `${K}.${NestedKeyOf<T[K]>}`
          : K
        : never
    }[keyof T]
  : never

type TranslationKey = NestedKeyOf<TranslationKeys>

function getNestedValue(obj: unknown, path: string): string {
  const keys = path.split('.')
  let result: unknown = obj

  for (const key of keys) {
    if (result && typeof result === 'object' && key in result) {
      result = (result as Record<string, unknown>)[key]
    } else {
      return path // 키를 찾지 못하면 경로 반환
    }
  }

  return typeof result === 'string' ? result : path
}

export function useTranslation() {
  const { language, setLanguage } = useLanguageStore()

  const t = (key: TranslationKey): string => {
    return getNestedValue(translations[language], key)
  }

  // 에러 코드를 번역된 메시지로 변환
  const tError = (errorCode: string, fallback?: string): string => {
    const errorKey = `errors.${errorCode}` as TranslationKey
    const translated = getNestedValue(translations[language], errorKey)

    // 번역이 없으면 fallback 또는 기본 메시지 반환
    if (translated === errorKey) {
      return fallback || translations[language].errors.DEFAULT_ERROR
    }
    return translated
  }

  return {
    t,
    tError,
    language,
    setLanguage,
  }
}

// 컴포넌트 외부에서 사용할 수 있는 함수
export function getTranslation(language: Language, key: string): string {
  return getNestedValue(translations[language], key)
}

// 에러 코드 번역 (컴포넌트 외부용)
export function getErrorMessage(language: Language, errorCode: string, fallback?: string): string {
  const errorKey = `errors.${errorCode}`
  const translated = getNestedValue(translations[language], errorKey)

  if (translated === errorKey) {
    return fallback || translations[language].errors.DEFAULT_ERROR
  }
  return translated
}
