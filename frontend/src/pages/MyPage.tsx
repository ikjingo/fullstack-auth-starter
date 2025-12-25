import { Link, useNavigate } from 'react-router-dom'
import { User, Mail, Calendar, LogOut } from 'lucide-react'
import { useUserStore } from '@/stores/useUserStore'
import { useTranslation } from '@/hooks'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { toast } from '@/stores/useToastStore'
import {
  pageContainer,
  pageContent,
  flexColCenter,
  iconMd,
} from '@/utils'

export function MyPage() {
  const navigate = useNavigate()
  const { t } = useTranslation()
  const { user, isAuthenticated, logout } = useUserStore()

  const handleLogout = () => {
    logout()
    toast.success(t('common.logout'), t('logout.successMessage'))
    navigate('/login')
  }

  if (!isAuthenticated || !user) {
    return (
      <div className={`${pageContainer} bg-pattern`}>
        <div className={pageContent}>
          <div className={`${flexColCenter} gap-4`}>
            <p className="text-muted-foreground">{t('myPage.loginRequired')}</p>
            <Link to="/login">
              <Button>{t('common.login')}</Button>
            </Link>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className={`${pageContainer} bg-pattern`}>
      <div className={pageContent}>
        <Card className="max-w-md mx-auto">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <User className={iconMd} />
              {t('nav.mypage')}
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex items-center gap-3">
              <User className="w-5 h-5 text-muted-foreground" />
              <div>
                <p className="text-sm text-muted-foreground">{t('auth.nickname')}</p>
                <p className="font-medium">{user.nickname}</p>
              </div>
            </div>

            <div className="flex items-center gap-3">
              <Mail className="w-5 h-5 text-muted-foreground" />
              <div>
                <p className="text-sm text-muted-foreground">{t('auth.email')}</p>
                <p className="font-medium">{user.email}</p>
              </div>
            </div>

            <div className="flex items-center gap-3">
              <Calendar className="w-5 h-5 text-muted-foreground" />
              <div>
                <p className="text-sm text-muted-foreground">{t('myPage.role')}</p>
                <p className="font-medium">{user.role}</p>
              </div>
            </div>

            <div className="pt-4 border-t">
              <Button
                variant="outline"
                className="w-full gap-2"
                onClick={handleLogout}
              >
                <LogOut className="w-4 h-4" />
                {t('common.logout')}
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
