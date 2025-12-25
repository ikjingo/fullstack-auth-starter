import { Moon, Sun } from 'lucide-react'
import { useTheme } from '@/stores'
import { Button } from '@/components/ui/button'
import { iconMd, hoverBg } from '@/utils'

export function ThemeToggle() {
  const { theme, toggleTheme } = useTheme()

  return (
    <Button
      variant="ghost"
      size="icon"
      className={`text-muted-foreground hover:text-foreground ${hoverBg}`}
      onClick={toggleTheme}
      title={theme === 'dark' ? '라이트 모드로 전환' : '다크 모드로 전환'}
    >
      {theme === 'dark' ? <Moon className={iconMd} /> : <Sun className={iconMd} />}
    </Button>
  )
}
