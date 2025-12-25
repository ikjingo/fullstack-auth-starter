# 프론트엔드 리팩토링 패턴 (React/TypeScript)

## 스타일 유틸리티 적용 (필수)

`@/utils/classNames.ts`의 유틸리티를 **반드시** 사용해야 합니다.

### 페이지 레이아웃

```tsx
import { pageContainer, pageContent, pageContentNarrow, pageContentMedium } from '@/utils'

// 페이지 최상위 컨테이너
<div className={pageContainer}>

// 기본 컨텐츠 영역 (max-w-[1200px])
<div className={pageContent}>

// 좁은 영역 - 폼, 로그인 등 (max-w-[400px])
<div className={pageContentNarrow}>

// 중간 영역 - 생성기 등 (max-w-[600px])
<div className={pageContentMedium}>
```

### 카드 스타일

```tsx
import { card, cardPadding, cardPaddingSm, cardPaddingLg, cardRoundedLg } from '@/utils'

// 기본 카드
<div className={`${card} ${cardPadding}`}>

// 큰 카드 (반응형)
<div className={`${cardRoundedLg} ${cardPaddingLg}`}>

// 작은 패딩 카드
<div className={`${card} ${cardPaddingSm}`}>
```

### 페이지 헤더

```tsx
import { pageHeader, pageTitle, pageSubtitle } from '@/utils'

<div className={pageHeader}>
  <h1 className={pageTitle}>{title}</h1>
  <p className={pageSubtitle}>{subtitle}</p>
</div>
```

### 아이콘 크기

```tsx
import { iconSm, iconMd, iconLg } from '@/utils'

<Search className={iconSm} />   // w-4 h-4 (폼 내부, 인라인)
<Menu className={iconMd} />     // w-5 h-5 (버튼, 네비게이션)
<AlertCircle className={iconLg} /> // w-8 h-8 (헤더, 강조)
```

### 플렉스 레이아웃

```tsx
import { flexCenter, flexBetween, flexRowGapSm, flexRowGap, flexRowGapLg } from '@/utils'

<div className={flexCenter}>      // 중앙 정렬
<div className={flexBetween}>     // 양쪽 정렬
<div className={flexRowGapSm}>    // gap-2
<div className={flexRowGap}>      // gap-3
<div className={flexRowGapLg}>    // gap-4
```

### 간격 및 갭

```tsx
import { sectionGap, formGap } from '@/utils'

<div className={sectionGap}>  // space-y-6 (섹션 간)
<form className={formGap}>    // space-y-4 (폼 필드 간)
```

### 텍스트 버튼 (인라인 액션)

```tsx
import { textButton, textButtonPrimary, textButtonDisabled } from '@/utils'

<button className={textButton}>수정</button>           // muted -> foreground
<button className={textButtonPrimary}>연동</button>    // primary
<span className={textButtonDisabled}>미지원</span>     // 비활성화
```

### 테이블 스타일

```tsx
import { tableContainer, tableHeaderRow, tableDataRow } from '@/utils'

<div className={tableContainer}>
  <TableHeader>
    <TableRow className={tableHeaderRow}>
  <TableBody>
    <TableRow className={tableDataRow}>
```

---

## shadcn/ui 버튼 사용 가이드

### Primary Action (폼 제출, CTA)
```tsx
import { Button } from '@/components/ui/button'

<Button type="submit" disabled={isLoading}>제출</Button>
<Button asChild size="lg"><Link to="/login">로그인</Link></Button>
<Button variant="destructive">삭제</Button>
<Button variant="outline">취소</Button>
```

### Inline Text Action (수정, 연동, 해제 등)
```tsx
// 순수 <button> + Tailwind 사용
<button className="text-sm text-muted-foreground hover:text-foreground transition-colors">
  수정
</button>

// 또는 스타일 유틸리티 사용
<button className={textButton}>수정</button>
<button className={textButtonPrimary}>연동</button>
```

### Disabled/Unavailable
```tsx
<span className="text-sm text-muted-foreground/50">미지원</span>

// 또는 스타일 유틸리티
<span className={textButtonDisabled}>미지원</span>
```

### 주의사항
- shadcn/ui `<Button>`에 과도한 `className` 오버라이드 금지
- `variant`, `size` props로 스타일 조정
- 기본 호버 스타일과 충돌하는 커스텀 스타일 사용 금지

---

## 상태 관리 패턴

### 서버 상태: TanStack Query

**데이터 조회**
```tsx
import { useQuery } from '@tanstack/react-query'
import { userApi } from '@/api/user'

function UserProfile({ userId }: { userId: number }) {
  const { data: user, isLoading, error } = useQuery({
    queryKey: ['users', userId],
    queryFn: () => userApi.getUser(userId),
  })

  if (isLoading) return <Skeleton />
  if (error) return <ErrorMessage error={error} />

  return <div>{user.nickname}</div>
}
```

**데이터 변경**
```tsx
import { useMutation, useQueryClient } from '@tanstack/react-query'

function EditUserForm({ userId }: { userId: number }) {
  const queryClient = useQueryClient()

  const mutation = useMutation({
    mutationFn: userApi.updateUser,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] })
      toast.success('저장되었습니다')
    },
    onError: (error) => {
      toast.error(error.message)
    },
  })

  const handleSubmit = (data: UserFormData) => {
    mutation.mutate({ id: userId, ...data })
  }
}
```

**리스트 조회**
```tsx
const { data: users, isLoading } = useQuery({
  queryKey: ['users', { page, limit, search }],
  queryFn: () => userApi.getUsers({ page, limit, search }),
})
```

### 클라이언트 상태: Zustand

```tsx
// stores/useAuthStore.ts
import { create } from 'zustand'

interface AuthState {
  user: User | null
  isAuthenticated: boolean
  setUser: (user: User | null) => void
  logout: () => void
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  isAuthenticated: false,
  setUser: (user) => set({ user, isAuthenticated: !!user }),
  logout: () => set({ user: null, isAuthenticated: false }),
}))

// 사용
function Header() {
  const { user, logout } = useAuthStore()

  return (
    <div>
      {user?.nickname}
      <button onClick={logout}>로그아웃</button>
    </div>
  )
}
```

---

## 폼 처리 패턴

### react-hook-form + Zod 조합

**스키마 정의**
```tsx
// schemas/userSchema.ts
import { z } from 'zod'

export const userSchema = z.object({
  email: z
    .string()
    .min(1, '이메일을 입력하세요')
    .email('올바른 이메일 형식이 아닙니다'),
  nickname: z
    .string()
    .min(2, '닉네임은 2자 이상이어야 합니다')
    .max(20, '닉네임은 20자 이하여야 합니다'),
  password: z
    .string()
    .min(8, '비밀번호는 8자 이상이어야 합니다')
    .regex(/[A-Z]/, '대문자를 포함해야 합니다')
    .regex(/[0-9]/, '숫자를 포함해야 합니다'),
})

export type UserFormData = z.infer<typeof userSchema>
```

**폼 컴포넌트**
```tsx
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { userSchema, UserFormData } from '@/schemas/userSchema'

function UserForm() {
  const form = useForm<UserFormData>({
    resolver: zodResolver(userSchema),
    defaultValues: {
      email: '',
      nickname: '',
      password: '',
    },
    mode: 'onChange', // 실시간 검증
  })

  const {
    register,
    handleSubmit,
    formState: { errors, isValid, isSubmitting },
  } = form

  const onSubmit = async (data: UserFormData) => {
    // 제출 처리
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className={formGap}>
      <div>
        <Input {...register('email')} placeholder="이메일" />
        {errors.email && (
          <p className="text-sm text-destructive">{errors.email.message}</p>
        )}
      </div>
      {/* 나머지 필드들 */}
      <Button type="submit" disabled={!isValid || isSubmitting}>
        저장
      </Button>
    </form>
  )
}
```

---

## API 클라이언트 패턴

### 공통 클라이언트
```tsx
// api/client.ts
class ApiClient {
  private baseUrl: string

  constructor(baseUrl: string) {
    this.baseUrl = baseUrl
  }

  async get<T>(path: string): Promise<T> {
    const response = await fetch(`${this.baseUrl}${path}`, {
      credentials: 'include',
    })
    if (!response.ok) throw new ApiError(response)
    return response.json()
  }

  async post<T>(path: string, data: unknown): Promise<T> {
    const response = await fetch(`${this.baseUrl}${path}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
      credentials: 'include',
    })
    if (!response.ok) throw new ApiError(response)
    return response.json()
  }

  // put, delete 등...
}

export const apiClient = new ApiClient(import.meta.env.VITE_API_URL)
```

### 도메인별 API
```tsx
// api/user.ts
import { apiClient } from './client'

export const userApi = {
  getUser: async (id: number): Promise<User> => {
    return apiClient.get(`/users/${id}`)
  },

  getUsers: async (params: GetUsersParams): Promise<PaginatedResponse<User>> => {
    const query = new URLSearchParams(params as any).toString()
    return apiClient.get(`/users?${query}`)
  },

  updateUser: async (data: UpdateUserRequest): Promise<User> => {
    return apiClient.put(`/users/${data.id}`, data)
  },

  deleteUser: async (id: number): Promise<void> => {
    return apiClient.delete(`/users/${id}`)
  },
}
```

---

## 컴포넌트 분리 기준

### 분리가 필요한 경우

1. **100줄 이상의 컴포넌트**
2. **재사용 가능한 UI 패턴**
3. **독립적인 상태 관리 필요**
4. **복잡한 조건부 렌더링**

### 분리 방법

**커스텀 훅으로 로직 분리**
```tsx
// hooks/useUserForm.ts
export function useUserForm() {
  const form = useForm<UserFormData>({...})
  const mutation = useMutation({...})

  const handleSubmit = form.handleSubmit((data) => {
    mutation.mutate(data)
  })

  return {
    form,
    handleSubmit,
    isLoading: mutation.isPending,
  }
}

// components/UserForm.tsx
function UserForm() {
  const { form, handleSubmit, isLoading } = useUserForm()
  // UI 렌더링만 담당
}
```

**UI 컴포넌트 분리**
```tsx
// components/UserCard.tsx
function UserCard({ user }: { user: User }) {
  return (
    <div className={`${card} ${cardPadding}`}>
      <Avatar src={user.avatar} />
      <div>{user.nickname}</div>
    </div>
  )
}

// pages/UsersPage.tsx
function UsersPage() {
  const { data: users } = useQuery({...})
  return (
    <div className={pageContainer}>
      {users?.map(user => <UserCard key={user.id} user={user} />)}
    </div>
  )
}
```

---

## 타입 정의 패턴

```tsx
// types/user.ts
export interface User {
  id: number
  email: string
  nickname: string
  createdAt: string
}

export interface CreateUserRequest {
  email: string
  nickname: string
  password: string
}

export interface UpdateUserRequest {
  id: number
  nickname?: string
}

export interface PaginatedResponse<T> {
  items: T[]
  total: number
  page: number
  limit: number
}
```
