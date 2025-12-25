import { Link } from 'react-router-dom'
import { Github } from 'lucide-react'
import { useTranslation } from '@/hooks'
import {
  footer as footerStyle,
  footerInner,
  logoGradient,
  iconSm,
  iconMd,
  flexRowGap,
  flexRowGapSm,
  footerLink,
  footerSocialLink,
} from '@/utils'

// Footer navigation links
const FOOTER_LINKS = [
  { path: '/', nameKey: 'home' },
  { path: '/mypage', nameKey: 'myPage' },
] as const

export function Footer() {
  const { t } = useTranslation()

  const getNavName = (nameKey: string) => {
    const keyMap: Record<string, string> = {
      home: t('header.nav.home'),
      myPage: t('header.nav.myPage'),
    }
    return keyMap[nameKey] || nameKey
  }

  return (
    <footer className={footerStyle}>
      <div className={footerInner}>
        {/* Logo */}
        <FooterLogo />

        <Divider />

        {/* Copyright */}
        <span className="text-muted-foreground text-xs">
          © {new Date().getFullYear()} All rights reserved.
        </span>

        <Divider />

        {/* Links */}
        <div className={flexRowGap}>
          {FOOTER_LINKS.map((link) => (
            <Link key={link.path} to={link.path} className={footerLink}>
              {getNavName(link.nameKey)}
            </Link>
          ))}
          <a
            href="https://github.com"
            target="_blank"
            rel="noopener noreferrer"
            aria-label="GitHub"
            className={footerSocialLink}
          >
            <Github className={iconSm} />
          </a>
        </div>
      </div>
    </footer>
  )
}

function FooterLogo() {
  return (
    <Link to="/" className={`${flexRowGapSm} no-underline`}>
      <div className={`${iconMd} ${logoGradient}`}>
        <span className="text-white font-bold text-xs">A</span>
      </div>
      <span className="text-sm font-semibold text-foreground">Auth Starter</span>
    </Link>
  )
}

function Divider() {
  return <span className="text-border">|</span>
}
