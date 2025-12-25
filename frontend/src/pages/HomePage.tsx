import { Link } from 'react-router-dom'
import { LogIn, UserPlus, User } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { useUserStore } from '@/stores/useUserStore'
import { useTranslation } from '@/hooks'
import {
  pageContainer,
  pageContent,
  flexCenter,
  flexColCenter,
  spacingYLg,
  textCenter,
  iconMd,
} from '@/utils'

export function HomePage() {
  const { t } = useTranslation()
  const { user, isAuthenticated } = useUserStore()

  return (
    <div className={`${pageContainer} bg-pattern`}>
      <div className={pageContent}>
        <div className={`${flexColCenter} gap-8 ${spacingYLg}`}>
          <div className={textCenter}>
            <h1 className="text-4xl font-bold tracking-tight mb-4">
              Fullstack Auth Starter
            </h1>
            <p className="text-lg text-muted-foreground">
              Kotlin + Spring Boot & React + TypeScript
            </p>
          </div>

          {isAuthenticated && user ? (
            <div className={`${flexColCenter} gap-4`}>
              <div className={`${flexCenter} gap-2`}>
                <User className={iconMd} />
                <span className="text-lg">
                  {t('common.welcome')}, <strong>{user.nickname}</strong>!
                </span>
              </div>
              <Link to="/mypage">
                <Button variant="outline">
                  {t('nav.mypage')}
                </Button>
              </Link>
            </div>
          ) : (
            <div className={`${flexCenter} gap-4`}>
              <Link to="/login">
                <Button variant="default" className="gap-2">
                  <LogIn className="w-4 h-4" />
                  {t('common.login')}
                </Button>
              </Link>
              <Link to="/register">
                <Button variant="outline" className="gap-2">
                  <UserPlus className="w-4 h-4" />
                  {t('common.register')}
                </Button>
              </Link>
            </div>
          )}

          <div className="mt-8 p-6 rounded-lg border bg-card">
            <h2 className="text-xl font-semibold mb-4">Features</h2>
            <ul className="space-y-2 text-muted-foreground">
              <li>- JWT-based authentication (Access + Refresh Token)</li>
              <li>- User registration and login</li>
              <li>- Password change functionality</li>
              <li>- Session management with remember me</li>
              <li>- React 19 + TypeScript + Vite</li>
              <li>- Kotlin + Spring Boot 3.5</li>
              <li>- PostgreSQL database</li>
              <li>- Docker Compose setup</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  )
}
