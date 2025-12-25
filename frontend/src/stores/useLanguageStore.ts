import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { Language } from '@/locales'

interface LanguageState {
  language: Language
  setLanguage: (language: Language) => void
}

// 브라우저 언어 감지
const detectBrowserLanguage = (): Language => {
  const browserLang = navigator.language.split('-')[0]
  return browserLang === 'ko' ? 'ko' : 'en'
}

export const useLanguageStore = create<LanguageState>()(
  persist(
    (set) => ({
      language: detectBrowserLanguage(),
      setLanguage: (language) => set({ language }),
    }),
    {
      name: 'zenless-language',
    }
  )
)
