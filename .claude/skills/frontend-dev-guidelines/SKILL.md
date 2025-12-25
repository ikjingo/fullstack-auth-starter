# Frontend Development Guidelines

프론트엔드(React/TypeScript) 개발 가이드라인 스킬. 사용자가 "프론트엔드 개발", "React 컴포넌트", "UI 구현" 등의 요청을 할 때 사용합니다. (project)

## 디렉토리 구조

```
frontend/src/
├── api/           # API 클라이언트 (authApi, userApi 등)
├── components/    # 재사용 컴포넌트
│   ├── ui/        # shadcn/ui 컴포넌트
│   ├── common/    # 공통 컴포넌트 (ToastContainer 등)
│   └── layout/    # 레이아웃 컴포넌트
├── pages/         # 페이지 컴포넌트
├── schemas/       # Zod 유효성 검증 스키마
├── stores/        # 상태 관리 (Zustand)
└── lib/           # 유틸리티 함수
```

## 핵심 기술 스택

- **React 19** + **TypeScript 5**
- **Vite 7** - 빌드 도구
- **Tailwind CSS** - 스타일링
- **shadcn/ui** - UI 컴포넌트
- **Zustand** - 전역 상태 관리
- **TanStack Query** - 서버 상태 관리
- **React Hook Form + Zod** - 폼 처리

## 페이지 컴포넌트 패턴

파일명: `PascalCase` (예: `LoginPage.tsx`)

```typescript
export function PageName() {
  const navigate = useNavigate()
  const [isLoading, setIsLoading] = useState(false)

  return (
    <div className="w-full min-h-full bg-background">
      {/* 페이지 내용 */}
    </div>
  )
}
```

`pages/index.ts`에서 반드시 export 추가.

## 폼 처리 패턴

**react-hook-form + Zod** 조합 필수:

```typescript
const form = useForm<FormSchema>({
  resolver: zodResolver(formSchema),
  defaultValues: { ... },
  mode: 'onChange',  // 실시간 유효성 검증
})
```

스키마는 `schemas/` 디렉토리에 정의.

## API 클라이언트 패턴

```typescript
// api/auth.ts
export const authApi = {
  login: async (data) => { ... },
  register: async (data) => { ... },
}
```

도메인별 파일 분리 (auth.ts, user.ts 등).

## 스타일 유틸리티 (필수 사용)

**하드코딩 금지** - `@/utils/classNames.ts` 유틸리티 사용:

```typescript
import { pageContainer, pageContent, card, cardPadding } from '@/utils'
import { flexCenter, flexBetween, flexRowGap } from '@/utils'
import { iconSm, iconMd, iconLg } from '@/utils'
```

### 주요 유틸리티

| 카테고리 | 유틸리티 | 용도 |
|---------|---------|------|
| 레이아웃 | `pageContainer`, `pageContent` | 페이지 구조 |
| 카드 | `card`, `cardPadding` | 카드 스타일 |
| 플렉스 | `flexCenter`, `flexBetween` | 정렬 |
| 아이콘 | `iconSm`, `iconMd`, `iconLg` | 아이콘 크기 |
| 간격 | `sectionGap`, `formGap` | 요소 간 간격 |

## shadcn/ui 버튼 가이드

- **Primary Action**: shadcn/ui `<Button>` 사용
- **Inline Text Action**: 순수 `<button>` + Tailwind

```tsx
// Primary (폼 제출, CTA)
<Button type="submit">제출</Button>

// Inline (수정, 연동 등)
<button className="text-sm text-muted-foreground hover:text-foreground">
  수정
</button>
```

## 네이밍 컨벤션

| 대상 | 규칙 | 예시 |
|-----|------|------|
| 컴포넌트 | PascalCase | `LoginPage`, `UserCard` |
| 함수/변수 | camelCase | `handleSubmit`, `userName` |
| 이벤트 핸들러 | handle 접두사 | `handleClick`, `handleChange` |
| 불리언 상태 | is 접두사 | `isLoading`, `isEmailSent` |

## 상태 관리

- **로딩**: `useState`로 `isLoading` 관리
- **알림**: `toast.success()`, `toast.error()`
- **페이지 이동**: `useNavigate()` 훅

## 주의사항

1. shadcn/ui `<Button>`에 과도한 className 오버라이드 금지
2. 3회 이상 반복 패턴은 `classNames.ts`에 추가
3. 애니메이션은 기능적인 것만 (로딩 스피너 등)
4. `variant`, `size` props로 버튼 스타일 조정
