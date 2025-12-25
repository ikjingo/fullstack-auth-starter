import { ko } from './ko'
import { en } from './en'
import type { TranslationKeys } from './types'

export type Language = 'ko' | 'en'

export const translations: Record<Language, TranslationKeys> = {
  ko,
  en,
}

export const languageNames: Record<Language, string> = {
  ko: '한국어',
  en: 'English',
}

export type { TranslationKeys }
export { ko, en }
