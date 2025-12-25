import { create } from 'zustand'
import { persist } from 'zustand/middleware'

type Theme = 'light' | 'dark'

interface ThemeState {
  theme: Theme
  setTheme: (theme: Theme) => void
  toggleTheme: () => void
}

// DOM에 테마 클래스 적용
const applyTheme = (theme: Theme) => {
  const root = window.document.documentElement
  root.classList.remove('light', 'dark')
  root.classList.add(theme)
}

export const useThemeStore = create<ThemeState>()(
  persist(
    (set, get) => ({
      theme: 'dark',
      setTheme: (theme) => {
        applyTheme(theme)
        set({ theme })
      },
      toggleTheme: () => {
        const newTheme = get().theme === 'dark' ? 'light' : 'dark'
        applyTheme(newTheme)
        set({ theme: newTheme })
      },
    }),
    {
      name: 'zenless-theme',
      onRehydrateStorage: () => (state) => {
        // 스토어가 복원될 때 DOM에 테마 적용
        if (state?.theme) {
          applyTheme(state.theme)
        }
      },
    }
  )
)

// 기존 useTheme 훅과 호환성을 위한 래퍼
export const useTheme = () => {
  const { theme, setTheme, toggleTheme } = useThemeStore()
  return { theme, setTheme, toggleTheme }
}
