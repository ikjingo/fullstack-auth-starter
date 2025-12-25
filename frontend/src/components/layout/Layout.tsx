import { Outlet } from 'react-router-dom'
import { Header } from './Header'
import { Footer } from './Footer'
import { skipLink } from '@/utils'

function SkipLink() {
  return (
    <a href="#main-content" className={skipLink}>
      본문으로 바로가기
    </a>
  )
}

export function Layout() {
  return (
    <div className="min-h-screen flex flex-col bg-bg-primary">
      <SkipLink />
      <Header />
      <main id="main-content" className="flex-1 pt-14" tabIndex={-1}>
        <Outlet />
      </main>
      <Footer />
    </div>
  )
}
