import { Link } from 'react-router-dom'
import { Home } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { useTranslation } from '@/hooks'
import { iconSm, flexCenter, pageMinHeight } from '@/utils'

export function NotFoundPage() {
  const { t } = useTranslation()
  return (
    <div className={`${pageMinHeight} ${flexCenter} px-4`}>
      <div className="text-center">
        <h1 className="text-[6rem] font-bold text-primary mb-4">404</h1>
        <h2 className="text-2xl font-semibold text-foreground mb-2">
          {t('notFound.title')}
        </h2>
        <p className="text-muted-foreground mb-8">
          {t('notFound.subtitle')}
        </p>
        <Button asChild size="lg">
          <Link to="/">
            <Home className={iconSm} />
            {t('notFound.backToHome')}
          </Link>
        </Button>
      </div>
    </div>
  )
}
