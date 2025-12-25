import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { Layout } from '@/components/layout'
import { ToastContainer, ErrorBoundary } from '@/components/common'
import { HomePage, MyPage, LoginPage, RegisterPage, NotFoundPage } from '@/pages'
import { useSessionExpiration, useAuthInitializer } from '@/hooks'

// 세션 만료 처리 및 인증 초기화를 위한 내부 컴포넌트 (Router 컨텍스트 내에서 훅 사용)
function AppRoutes() {
  useAuthInitializer() // 앱 시작 시 토큰 복원
  useSessionExpiration()

  return (
    <Routes>
      <Route path="/" element={<Layout />}>
        <Route index element={<HomePage />} />
        <Route path="mypage" element={<MyPage />} />
        <Route path="login" element={<LoginPage />} />
        <Route path="register" element={<RegisterPage />} />
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  )
}

function App() {
  return (
    <ErrorBoundary>
      <BrowserRouter>
        <AppRoutes />
      </BrowserRouter>
      <ToastContainer />
    </ErrorBoundary>
  )
}

export default App
