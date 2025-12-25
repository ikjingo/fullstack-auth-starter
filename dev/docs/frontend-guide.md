# Frontend Development Guide

## Project Structure

```
frontend/src/
├── api/                      # API integration
│   ├── client/              # HTTP client & token management
│   │   ├── client.ts        # Fetch-based API client
│   │   ├── errors.ts        # Error handling
│   │   ├── tokenManager.ts  # Token storage
│   │   └── session.ts       # Session events
│   ├── auth.ts              # Auth API endpoints
│   └── password.ts          # Password API
│
├── components/              # UI components
│   ├── ui/                  # shadcn/ui base components
│   ├── common/              # Reusable components
│   └── layout/              # Layout components
│
├── pages/                   # Page components
│   ├── HomePage.tsx
│   ├── LoginPage.tsx
│   ├── RegisterPage.tsx
│   ├── MyPage.tsx
│   └── NotFoundPage.tsx
│
├── hooks/                   # Custom hooks
│   ├── auth/                # Authentication hooks
│   ├── common/              # Shared hooks
│   └── ui/                  # UI-specific hooks
│
├── stores/                  # Zustand stores
│   ├── useUserStore.ts      # User auth state
│   ├── useThemeStore.ts     # Theme state
│   ├── useLanguageStore.ts  # i18n state
│   └── useToastStore.ts     # Notifications
│
├── schemas/                 # Zod validation
│   ├── auth.ts              # Auth schemas
│   └── common/password.ts   # Password validation
│
├── locales/                 # i18n translations
│   ├── types.ts             # Translation types
│   ├── ko.ts                # Korean
│   └── en.ts                # English
│
└── utils/                   # Utilities
    └── styles/              # Tailwind utilities
```

## Key Components

### API Client

#### Basic Usage

```typescript
import { apiClient } from '@/api/client'

// GET request
const user = await apiClient.get<UserResponse>('/auth/me')

// POST request
const result = await apiClient.post<AuthResponse>('/auth/signin', {
  email: 'user@example.com',
  password: 'Password123!'
})
```

#### Token Management

```typescript
import { tokenManager } from '@/api/client'

// Set tokens after login
tokenManager.setTokens(accessToken, refreshToken)

// Get current access token
const token = tokenManager.getAccessToken()

// Clear tokens on logout
tokenManager.clearTokens()
```

### State Management (Zustand)

#### useUserStore

```typescript
import { useUserStore } from '@/stores'

function MyComponent() {
  const { user, isAuthenticated, login, logout } = useUserStore()

  const handleLogin = async (data: LoginData) => {
    const response = await authApi.login(data)
    login(response.user, response.token, response.refreshToken)
  }

  const handleLogout = async () => {
    await authApi.logout()
    logout()
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" />
  }

  return <div>Welcome, {user?.nickname}</div>
}
```

#### useThemeStore

```typescript
import { useThemeStore } from '@/stores'

function ThemeToggle() {
  const { theme, toggleTheme } = useThemeStore()

  return (
    <button onClick={toggleTheme}>
      {theme === 'dark' ? '🌙' : '☀️'}
    </button>
  )
}
```

#### useLanguageStore

```typescript
import { useLanguageStore } from '@/stores'

function LanguageToggle() {
  const { language, setLanguage } = useLanguageStore()

  return (
    <button onClick={() => setLanguage(language === 'ko' ? 'en' : 'ko')}>
      {language.toUpperCase()}
    </button>
  )
}
```

### Form Validation (Zod + React Hook Form)

#### Schema Definition

```typescript
// schemas/auth.ts
import { z } from 'zod'
import { strongPasswordSchema } from './common/password'

export const registerFormSchema = z.object({
  email: z.string().email('유효한 이메일을 입력해주세요'),
  password: strongPasswordSchema,
  confirmPassword: z.string().min(1, '비밀번호 확인을 입력해주세요'),
  nickname: z.string()
    .min(2, '닉네임은 2자 이상이어야 합니다')
    .max(12, '닉네임은 12자 이하여야 합니다'),
}).refine((data) => data.password === data.confirmPassword, {
  message: '비밀번호가 일치하지 않습니다',
  path: ['confirmPassword'],
})

export type RegisterFormSchema = z.infer<typeof registerFormSchema>
```

#### Password Schema (Synced with Backend)

```typescript
// schemas/common/password.ts
export const PASSWORD_PATTERNS = {
  uppercase: /[A-Z]/,
  lowercase: /[a-z]/,
  digit: /[0-9]/,
  special: /[!@#$%^&*()_+\-=\[\]{}|;':",./<>?`~\\]/,
} as const

export const strongPasswordSchema = z
  .string()
  .min(1, '비밀번호를 입력해주세요')
  .min(8, '비밀번호는 8자 이상이어야 합니다')
  .refine((val) => PASSWORD_PATTERNS.uppercase.test(val), {
    message: '대문자를 1개 이상 포함해야 합니다',
  })
  .refine((val) => PASSWORD_PATTERNS.lowercase.test(val), {
    message: '소문자를 1개 이상 포함해야 합니다',
  })
  .refine((val) => PASSWORD_PATTERNS.digit.test(val), {
    message: '숫자를 1개 이상 포함해야 합니다',
  })
  .refine((val) => PASSWORD_PATTERNS.special.test(val), {
    message: '특수문자를 1개 이상 포함해야 합니다',
  })
```

#### Form Component

```typescript
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { registerFormSchema, type RegisterFormSchema } from '@/schemas'

function RegisterPage() {
  const form = useForm<RegisterFormSchema>({
    resolver: zodResolver(registerFormSchema),
    defaultValues: {
      email: '',
      password: '',
      confirmPassword: '',
      nickname: '',
    },
  })

  const onSubmit = async (data: RegisterFormSchema) => {
    try {
      const response = await authApi.register(data)
      login(response.user, response.token, response.refreshToken)
      navigate('/')
    } catch (error) {
      const apiError = toApiError(error)

      // Handle server validation errors
      if (apiError.isValidationError() && apiError.data) {
        const fieldErrors = apiError.data as Record<string, string>
        Object.entries(fieldErrors).forEach(([field, message]) => {
          form.setError(field as keyof RegisterFormSchema, {
            type: 'server',
            message,
          })
        })
      }

      toast.error(t('register.failed'), tError(apiError.errorType))
    }
  }

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)}>
        <FormField
          control={form.control}
          name="email"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Email</FormLabel>
              <FormControl>
                <Input {...field} type="email" />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />
        {/* More fields... */}
      </form>
    </Form>
  )
}
```

### Internationalization (i18n)

#### Translation Types

```typescript
// locales/types.ts
export interface TranslationKeys {
  common: {
    login: string
    register: string
    logout: string
    // ...
  }
  auth: {
    email: string
    password: string
    // ...
  }
  errors: {
    INVALID_REQUEST: string
    DUPLICATE_EMAIL: string
    UNAUTHORIZED: string
    // ...
  }
}
```

#### useTranslation Hook

```typescript
import { useTranslation } from '@/hooks'

function LoginPage() {
  const { t, tError, language, setLanguage } = useTranslation()

  return (
    <div>
      <h1>{t('login.title')}</h1>
      <p>{t('login.subtitle')}</p>

      {/* On error */}
      {error && (
        <p>{tError(error.code)}</p>
      )}
    </div>
  )
}
```

### Error Handling

#### ApiError Class

```typescript
// api/client/errors.ts
export class ApiError extends Error {
  code: string
  status: number
  data?: unknown
  errorType?: ErrorTypeValue

  isAuthError(): boolean {
    return this.status === 401
  }

  isValidationError(): boolean {
    return this.code === 'INVALID_REQUEST' && !!this.data
  }

  isServerError(): boolean {
    return this.status >= 500
  }
}

export function toApiError(error: unknown): ApiError {
  if (error instanceof ApiError) return error
  return new ApiError('An error occurred', 'DEFAULT_ERROR', 500)
}
```

#### Error Handling Pattern

```typescript
try {
  await someApiCall()
} catch (error) {
  const apiError = toApiError(error)

  // Translate error message
  const message = apiError.errorType
    ? tError(apiError.errorType)
    : t('common.tryAgain')

  // Show toast notification
  toast.error(t('operation.failed'), message)

  // Handle specific error types
  if (apiError.isAuthError()) {
    logout()
    navigate('/login')
  }
}
```

### Styling with Tailwind CSS

#### Style Utilities

```typescript
// utils/styles/index.ts
export * from './layout'
export * from './flex'
export * from './typography'
export * from './components'
export * from './icons'
```

#### Usage Example

```typescript
import {
  pageContainer,
  pageContent,
  flexCenter,
  buttonBase,
  inputIconLeft,
  iconSm,
} from '@/utils'

function MyPage() {
  return (
    <div className={pageContainer}>
      <div className={pageContent}>
        <div className={flexCenter}>
          <div className="relative">
            <Mail className={`${inputIconLeft} ${iconSm}`} />
            <Input className="pl-10" />
          </div>
          <Button className={buttonBase}>Submit</Button>
        </div>
      </div>
    </div>
  )
}
```

### Common Components

#### PasswordInput

```typescript
import { PasswordInput } from '@/components/common'

<FormField
  control={form.control}
  name="password"
  render={({ field }) => (
    <FormItem>
      <FormLabel>Password</FormLabel>
      <FormControl>
        <PasswordInput
          field={field}
          placeholder="Enter password"
          showPassword={showPassword}
          onToggleVisibility={() => setShowPassword(!showPassword)}
        />
      </FormControl>
      <FormMessage />
    </FormItem>
  )}
/>
```

#### AuthPageLayout

```typescript
import { AuthPageLayout } from '@/components/common'

function LoginPage() {
  return (
    <AuthPageLayout
      title={t('login.title')}
      subtitle={t('login.subtitle')}
      footerLink={{
        text: t('auth.noAccount'),
        linkText: t('common.register'),
        to: '/register',
      }}
    >
      <Form {...form}>
        {/* Form content */}
      </Form>
    </AuthPageLayout>
  )
}
```

#### Toast Notifications

```typescript
import { toast } from '@/stores/useToastStore'

// Success
toast.success('Success', 'Operation completed')

// Error
toast.error('Error', 'Something went wrong')

// Info
toast.info('Info', 'Please note...')
```

## Routes

### Route Configuration

```typescript
// App.tsx
function App() {
  return (
    <Routes>
      <Route path="/" element={<Layout />}>
        <Route index element={<HomePage />} />
        <Route path="login" element={<LoginPage />} />
        <Route path="register" element={<RegisterPage />} />
        <Route path="mypage" element={<MyPage />} />
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  )
}
```

### Protected Routes

```typescript
function MyPage() {
  const { user, isAuthenticated } = useUserStore()
  const { t } = useTranslation()

  if (!isAuthenticated || !user) {
    return (
      <div>
        <h1>{t('myPage.loginRequired')}</h1>
        <Link to="/login">{t('myPage.goToLogin')}</Link>
      </div>
    )
  }

  return (
    <div>
      <h1>{t('myPage.title')}</h1>
      {/* Page content */}
    </div>
  )
}
```

## Development

### Running the Dev Server

```bash
cd frontend
npm install
npm run dev
```

### Building for Production

```bash
npm run build
npm run preview  # Preview production build
```

### Linting

```bash
npm run lint
```

### Testing

```bash
npm run test
npm run test:coverage
```

## Configuration

### Vite Config

```typescript
// vite.config.ts
export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
```

### TypeScript Config

```json
// tsconfig.json
{
  "compilerOptions": {
    "target": "ES2020",
    "module": "ESNext",
    "strict": true,
    "paths": {
      "@/*": ["./src/*"]
    }
  }
}
```
