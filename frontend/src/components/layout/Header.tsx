import { Link, useNavigate } from 'react-router-dom'
import { Menu, X, User, LogOut } from 'lucide-react'
import { useState } from 'react'
import { ThemeToggle } from '@/components/common/ThemeToggle'
import { LanguageToggle } from '@/components/common/LanguageToggle'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { useUserStore } from '@/stores/useUserStore'
import { toast } from '@/stores/useToastStore'
import { useTranslation } from '@/hooks'
import {
  iconSm,
  iconMd,
  iconLg,
  iconWithText,
  hoverBg,
  hiddenMobile,
  hiddenDesktop,
  logoGradient,
  header as headerStyle,
  headerInner,
  mobileMenu,
  flexRowGapSm,
  navLink,
  navLinkActive,
  navLinkInactive,
  mobileMenuItem,
  mobileMenuItemActive,
  avatarSm,
  textGroupHover,
} from '@/utils'

// Navigation items for the starter template
const NAV_ITEMS = [
  { path: '/', nameKey: 'home' },
  { path: '/mypage', nameKey: 'myPage' },
]

export function Header() {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)
  const { t } = useTranslation()

  const isActivePath = (path: string) => {
    if (typeof window === 'undefined') return false
    return window.location.pathname === path
  }

  const getNavName = (nameKey: string) => {
    const keyMap: Record<string, string> = {
      home: t('header.nav.home'),
      myPage: t('header.nav.myPage'),
    }
    return keyMap[nameKey] || nameKey
  }

  return (
    <header className={headerStyle}>
      <div className={headerInner}>
        {/* Logo */}
        <Logo />

        {/* Desktop Navigation */}
        <nav className={`hidden md:${flexRowGapSm}`}>
          {NAV_ITEMS.map((item) => (
            <NavLink key={item.path} to={item.path} isActive={isActivePath(item.path)}>
              {getNavName(item.nameKey)}
            </NavLink>
          ))}
        </nav>

        {/* Right Section */}
        <div className={flexRowGapSm}>
          <LanguageToggle />
          <ThemeToggle />
          <UserMenu />

          {/* Mobile Menu Button */}
          <Button
            variant="ghost"
            size="icon"
            className={`${hiddenDesktop} text-muted-foreground hover:text-foreground ${hoverBg}`}
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            aria-label={t('header.menu')}
          >
            {mobileMenuOpen ? <X className={iconMd} /> : <Menu className={iconMd} />}
          </Button>
        </div>
      </div>

      {/* Mobile Navigation */}
      {mobileMenuOpen && <MobileMenu onClose={() => setMobileMenuOpen(false)} isActivePath={isActivePath} />}
    </header>
  )
}

function Logo() {
  return (
    <Link to="/" className="flex items-center gap-2.5 no-underline">
      <div className={`${iconLg} ${logoGradient}`}>
        <span className="text-white font-bold text-base">A</span>
      </div>
      <span className="text-lg font-bold text-foreground">Auth Starter</span>
    </Link>
  )
}

interface NavLinkProps {
  to: string
  isActive: boolean
  children: React.ReactNode
}

function NavLink({ to, isActive, children }: NavLinkProps) {
  return (
    <Link
      to={to}
      className={`${navLink} ${isActive ? navLinkActive : navLinkInactive}`}
    >
      {children}
    </Link>
  )
}

function UserMenu() {
  const navigate = useNavigate()
  const { user, isAuthenticated, logout } = useUserStore()
  const { t } = useTranslation()

  const handleLogout = () => {
    logout()
    toast.success(t('header.logoutSuccess'), t('header.logoutMessage'))
    navigate('/')
  }

  if (!isAuthenticated || !user) {
    return (
      <Button asChild className="hidden md:flex">
        <Link to="/login">
          <User className={iconSm} />
          <span>{t('header.login')}</span>
        </Link>
      </Button>
    )
  }

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" className={`${hiddenMobile} items-center gap-2 ${hoverBg} group`}>
          <div className={avatarSm}>
            <User className={`${iconSm} ${textGroupHover}`} />
          </div>
          <span className={`text-sm font-medium ${textGroupHover}`}>{user.nickname}</span>
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end" className="w-48">
        <div className="px-2 py-1.5">
          <p className="text-sm font-medium">{user.nickname}</p>
          <p className="text-xs text-muted-foreground">{user.email}</p>
        </div>
        <DropdownMenuSeparator />
        <DropdownMenuItem asChild>
          <Link to="/mypage" className="cursor-pointer">
            <User className={iconWithText} />
            {t('header.nav.myPage')}
          </Link>
        </DropdownMenuItem>
        <DropdownMenuSeparator />
        <DropdownMenuItem onClick={handleLogout} className="text-destructive cursor-pointer">
          <LogOut className={iconWithText} />
          {t('header.logout')}
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}

interface MobileMenuProps {
  onClose: () => void
  isActivePath: (path: string) => boolean
}

function MobileMenu({ onClose, isActivePath }: MobileMenuProps) {
  const navigate = useNavigate()
  const { user, isAuthenticated, logout } = useUserStore()
  const { t } = useTranslation()

  const handleLogout = () => {
    logout()
    toast.success(t('header.logoutSuccess'), t('header.logoutMessage'))
    onClose()
    navigate('/')
  }

  const getNavName = (nameKey: string) => {
    const keyMap: Record<string, string> = {
      home: t('header.nav.home'),
      myPage: t('header.nav.myPage'),
    }
    return keyMap[nameKey] || nameKey
  }

  return (
    <div className={mobileMenu} role="menu" aria-label="모바일 메뉴">
      <nav className="flex flex-col p-3 gap-1" role="none">
        {NAV_ITEMS.map((item) => (
          <Link
            key={item.path}
            to={item.path}
            onClick={onClose}
            role="menuitem"
            className={isActivePath(item.path) ? mobileMenuItemActive : mobileMenuItem}
          >
            {getNavName(item.nameKey)}
          </Link>
        ))}

        {isAuthenticated && user ? (
          <>
            <div className="border-t my-2" />
            <Link to="/mypage" onClick={onClose} className={mobileMenuItem}>
              {t('header.nav.myPage')}
            </Link>
            <Button
              variant="ghost"
              className="w-full justify-start text-destructive"
              onClick={handleLogout}
            >
              <LogOut className={iconWithText} />
              {t('header.logout')}
            </Button>
          </>
        ) : (
          <Button asChild className="mt-2">
            <Link to="/login" onClick={onClose}>
              <User className={iconSm} />
              <span>{t('header.login')}</span>
            </Link>
          </Button>
        )}
      </nav>
    </div>
  )
}
