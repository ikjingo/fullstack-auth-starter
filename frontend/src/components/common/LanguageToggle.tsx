import { Globe } from 'lucide-react'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { useLanguageStore } from '@/stores/useLanguageStore'
import { languageNames, type Language } from '@/locales'
import { iconMd, hoverBg } from '@/utils'

export function LanguageToggle() {
  const { language, setLanguage } = useLanguageStore()

  const languages: Language[] = ['ko', 'en']

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button
          variant="ghost"
          size="icon"
          className={`text-muted-foreground hover:text-foreground ${hoverBg}`}
          title={languageNames[language]}
        >
          <Globe className={iconMd} />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end">
        {languages.map((lang) => (
          <DropdownMenuItem
            key={lang}
            onClick={() => setLanguage(lang)}
            className={language === lang ? 'bg-muted font-medium' : ''}
          >
            {languageNames[lang]}
          </DropdownMenuItem>
        ))}
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
