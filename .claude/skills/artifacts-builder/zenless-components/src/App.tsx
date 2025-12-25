import { useState } from 'react'
import { Search, Mail, Lock, Eye, EyeOff, Home, Loader2, Star, Menu, X, Github, Copy, Check } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs'
import { Separator } from '@/components/ui/separator'
import { cn } from '@/lib/utils'

// Zenless 프로젝트의 스타일 유틸리티
const styles = {
  // 페이지 레이아웃
  pageContainer: 'w-full min-h-full bg-background',
  pageContent: 'max-w-[1200px] mx-auto py-8 px-4',
  pageContentNarrow: 'max-w-[400px] mx-auto',
  pageContentMedium: 'max-w-[600px] mx-auto',
  pageMinHeight: 'min-h-[calc(100vh-3.5rem)]',

  // 카드
  card: 'bg-card rounded-lg border border-border',
  cardPadding: 'p-6',
  cardPaddingSm: 'p-4',
  cardPaddingLg: 'p-6 sm:p-8',

  // 헤더
  pageHeader: 'text-center mb-8',
  pageTitle: 'text-2xl font-bold text-foreground mb-2',
  pageSubtitle: 'text-muted-foreground',

  // 아이콘
  iconSm: 'w-4 h-4',
  iconMd: 'w-5 h-5',
  iconLg: 'w-8 h-8',

  // 플렉스
  flexCenter: 'flex items-center justify-center',
  flexBetween: 'flex items-center justify-between',
  flexRowGapSm: 'flex items-center gap-2',
  flexRowGap: 'flex items-center gap-3',
  flexRowGapLg: 'flex items-center gap-4',

  // 간격
  sectionGap: 'space-y-6',
  formGap: 'space-y-4',

  // 텍스트
  textSm: 'text-sm',
  textMuted: 'text-muted-foreground',
  textSmMuted: 'text-sm text-muted-foreground',

  // 링크
  link: 'text-primary hover:underline transition-colors',
  linkMedium: 'text-primary font-medium hover:underline transition-colors',

  // 텍스트 버튼
  textButton: 'text-sm text-muted-foreground hover:text-foreground transition-colors',
  textButtonPrimary: 'text-sm text-primary hover:text-primary/80 transition-colors',

  // Input 아이콘
  inputIconLeft: 'absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground',
  inputIconRight: 'absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors',

  // 네비게이션
  navLink: 'px-4 py-2 rounded-md text-sm font-medium no-underline transition-colors',
  navLinkActive: 'text-foreground bg-accent font-medium',
  navLinkInactive: 'text-muted-foreground hover:text-foreground hover:bg-accent/50',

  // 기타
  buttonBase: 'h-12',
  inputHeight: 'h-12',
  logoGradient: 'bg-gradient-to-br from-primary to-primary/60 rounded-lg flex items-center justify-center',
}

// 코드 블록 컴포넌트
function CodeBlock({ code }: { code: string }) {
  const [copied, setCopied] = useState(false)

  const handleCopy = () => {
    navigator.clipboard.writeText(code)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  return (
    <div className="relative group">
      <pre className="bg-zinc-950 text-zinc-100 p-4 rounded-lg text-sm overflow-x-auto">
        <code>{code}</code>
      </pre>
      <button
        onClick={handleCopy}
        className="absolute top-2 right-2 p-2 rounded-md bg-zinc-800 hover:bg-zinc-700 text-zinc-400 hover:text-zinc-100 opacity-0 group-hover:opacity-100 transition-opacity"
      >
        {copied ? <Check className="w-4 h-4" /> : <Copy className="w-4 h-4" />}
      </button>
    </div>
  )
}

// 컴포넌트 섹션 래퍼
function ComponentSection({ title, description, children }: { title: string; description: string; children: React.ReactNode }) {
  return (
    <div className="space-y-4">
      <div>
        <h3 className="text-lg font-semibold">{title}</h3>
        <p className="text-sm text-muted-foreground">{description}</p>
      </div>
      <div className="space-y-4">
        {children}
      </div>
    </div>
  )
}

// 데모 카드
function DemoCard({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <Card>
      <CardHeader className="pb-3">
        <CardTitle className="text-sm font-medium">{title}</CardTitle>
      </CardHeader>
      <CardContent>{children}</CardContent>
    </Card>
  )
}

export default function App() {
  const [showPassword, setShowPassword] = useState(false)
  const [activeNav, setActiveNav] = useState('components')

  return (
    <div className="min-h-screen bg-background">
      {/* Header */}
      <header className="sticky top-0 z-50 border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
        <div className="max-w-[1200px] mx-auto px-6 h-14 flex items-center justify-between">
          <div className={styles.flexRowGap}>
            <div className={cn(styles.logoGradient, 'w-8 h-8')}>
              <span className="text-white font-bold text-sm">Z</span>
            </div>
            <span className="font-semibold">Zenless UI</span>
          </div>
          <nav className={styles.flexRowGap}>
            {['components', 'utilities', 'examples'].map((item) => (
              <button
                key={item}
                onClick={() => setActiveNav(item)}
                className={cn(
                  styles.navLink,
                  activeNav === item ? styles.navLinkActive : styles.navLinkInactive
                )}
              >
                {item.charAt(0).toUpperCase() + item.slice(1)}
              </button>
            ))}
          </nav>
        </div>
      </header>

      {/* Main Content */}
      <main className={styles.pageContent}>
        <div className={styles.pageHeader}>
          <h1 className="text-3xl font-bold mb-2">Zenless UI Components</h1>
          <p className={styles.textMuted}>
            프로젝트에서 사용하는 UI 컴포넌트 및 스타일 유틸리티 가이드
          </p>
        </div>

        <Tabs defaultValue="buttons" className="space-y-8">
          <TabsList className="grid w-full grid-cols-5">
            <TabsTrigger value="buttons">Buttons</TabsTrigger>
            <TabsTrigger value="inputs">Inputs</TabsTrigger>
            <TabsTrigger value="cards">Cards</TabsTrigger>
            <TabsTrigger value="layout">Layout</TabsTrigger>
            <TabsTrigger value="utilities">Utilities</TabsTrigger>
          </TabsList>

          {/* Buttons */}
          <TabsContent value="buttons" className={styles.sectionGap}>
            <ComponentSection title="Button Variants" description="다양한 버튼 스타일">
              <DemoCard title="기본 버튼">
                <div className={styles.flexRowGapLg + ' flex-wrap'}>
                  <Button>Default</Button>
                  <Button variant="secondary">Secondary</Button>
                  <Button variant="outline">Outline</Button>
                  <Button variant="ghost">Ghost</Button>
                  <Button variant="destructive">Destructive</Button>
                  <Button variant="link">Link</Button>
                </div>
              </DemoCard>

              <DemoCard title="버튼 크기">
                <div className={styles.flexRowGapLg}>
                  <Button size="sm">Small</Button>
                  <Button size="default">Default</Button>
                  <Button size="lg">Large</Button>
                  <Button size="icon"><Search className={styles.iconSm} /></Button>
                </div>
              </DemoCard>

              <DemoCard title="아이콘 버튼">
                <div className={styles.flexRowGapLg + ' flex-wrap'}>
                  <Button>
                    <Mail className={styles.iconSm} />
                    이메일 보내기
                  </Button>
                  <Button variant="outline">
                    <Home className={styles.iconSm} />
                    홈으로
                  </Button>
                  <Button disabled>
                    <Loader2 className={cn(styles.iconSm, 'animate-spin')} />
                    로딩중...
                  </Button>
                </div>
              </DemoCard>

              <DemoCard title="텍스트 버튼 (인라인 액션)">
                <div className={styles.flexRowGapLg}>
                  <button className={styles.textButton}>수정</button>
                  <button className={styles.textButtonPrimary}>연동</button>
                  <span className="text-sm text-muted-foreground/50">미지원</span>
                </div>
              </DemoCard>

              <CodeBlock code={`// 기본 버튼 사용
<Button>Default</Button>
<Button variant="outline">Outline</Button>
<Button disabled>
  <Loader2 className={\`\${iconSm} animate-spin\`} />
  로딩중...
</Button>

// 텍스트 버튼 (인라인 액션)
<button className={textButton}>수정</button>
<button className={textButtonPrimary}>연동</button>`} />
            </ComponentSection>
          </TabsContent>

          {/* Inputs */}
          <TabsContent value="inputs" className={styles.sectionGap}>
            <ComponentSection title="Input Fields" description="입력 필드 컴포넌트">
              <DemoCard title="기본 입력">
                <div className={styles.formGap}>
                  <Input placeholder="기본 입력" />
                  <Input placeholder="비활성화" disabled />
                </div>
              </DemoCard>

              <DemoCard title="아이콘 입력">
                <div className={styles.formGap}>
                  <div className="relative">
                    <Mail className={cn(styles.inputIconLeft, styles.iconSm)} />
                    <Input placeholder="이메일" className="pl-10" />
                  </div>
                  <div className="relative">
                    <Search className={cn(styles.inputIconLeft, styles.iconSm)} />
                    <Input placeholder="검색어를 입력하세요" className="pl-10 pr-10" />
                    <button className={styles.inputIconRight}>
                      <X className={styles.iconSm} />
                    </button>
                  </div>
                  <div className="relative">
                    <Lock className={cn(styles.inputIconLeft, styles.iconSm)} />
                    <Input
                      type={showPassword ? 'text' : 'password'}
                      placeholder="비밀번호"
                      className="pl-10 pr-10"
                    />
                    <button
                      className={styles.inputIconRight}
                      onClick={() => setShowPassword(!showPassword)}
                    >
                      {showPassword ? <EyeOff className={styles.iconSm} /> : <Eye className={styles.iconSm} />}
                    </button>
                  </div>
                </div>
              </DemoCard>

              <CodeBlock code={`// 아이콘 입력 필드
<div className="relative">
  <Mail className={\`\${inputIconLeft} \${iconSm}\`} />
  <Input placeholder="이메일" className="pl-10" />
</div>

// 비밀번호 토글
<div className="relative">
  <Lock className={\`\${inputIconLeft} \${iconSm}\`} />
  <Input type={showPassword ? 'text' : 'password'} />
  <button className={inputIconRight}>
    {showPassword ? <EyeOff /> : <Eye />}
  </button>
</div>`} />
            </ComponentSection>
          </TabsContent>

          {/* Cards */}
          <TabsContent value="cards" className={styles.sectionGap}>
            <ComponentSection title="Card Components" description="카드 레이아웃 컴포넌트">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <Card>
                  <CardHeader>
                    <CardTitle>기본 카드</CardTitle>
                    <CardDescription>카드 설명 텍스트</CardDescription>
                  </CardHeader>
                  <CardContent>
                    <p className={styles.textSmMuted}>카드 내용이 여기에 들어갑니다.</p>
                  </CardContent>
                  <CardFooter>
                    <Button size="sm">액션</Button>
                  </CardFooter>
                </Card>

                <div className={cn(styles.card, styles.cardPadding)}>
                  <h3 className="font-semibold mb-2">스타일 유틸리티 카드</h3>
                  <p className={styles.textSmMuted}>
                    card + cardPadding 유틸리티 조합
                  </p>
                </div>
              </div>

              <DemoCard title="Badge">
                <div className={styles.flexRowGapLg + ' flex-wrap'}>
                  <Badge>Default</Badge>
                  <Badge variant="secondary">Secondary</Badge>
                  <Badge variant="outline">Outline</Badge>
                  <Badge variant="destructive">Destructive</Badge>
                </div>
              </DemoCard>

              <CodeBlock code={`// shadcn/ui Card 컴포넌트
<Card>
  <CardHeader>
    <CardTitle>제목</CardTitle>
    <CardDescription>설명</CardDescription>
  </CardHeader>
  <CardContent>내용</CardContent>
  <CardFooter><Button>액션</Button></CardFooter>
</Card>

// 스타일 유틸리티 조합
<div className={\`\${card} \${cardPadding}\`}>
  내용
</div>`} />
            </ComponentSection>
          </TabsContent>

          {/* Layout */}
          <TabsContent value="layout" className={styles.sectionGap}>
            <ComponentSection title="Page Layout" description="페이지 레이아웃 유틸리티">
              <DemoCard title="페이지 컨테이너">
                <div className="space-y-2">
                  <div className="bg-muted/50 p-4 rounded text-center text-sm">
                    <code>pageContainer</code> - w-full min-h-full bg-background
                  </div>
                  <div className="bg-muted/50 p-4 rounded text-center text-sm">
                    <code>pageContent</code> - max-w-[1200px] mx-auto py-8 px-4
                  </div>
                  <div className="bg-muted/50 p-4 rounded text-center text-sm">
                    <code>pageContentNarrow</code> - max-w-[400px] (폼, 로그인)
                  </div>
                  <div className="bg-muted/50 p-4 rounded text-center text-sm">
                    <code>pageContentMedium</code> - max-w-[600px] (생성기)
                  </div>
                </div>
              </DemoCard>

              <DemoCard title="페이지 헤더">
                <div className={styles.pageHeader}>
                  <h1 className={styles.pageTitle}>페이지 제목</h1>
                  <p className={styles.pageSubtitle}>페이지 부제목 텍스트</p>
                </div>
              </DemoCard>

              <DemoCard title="플렉스 레이아웃">
                <div className="space-y-4">
                  <div className={cn(styles.flexCenter, 'h-16 bg-muted/50 rounded')}>
                    <span className="text-sm">flexCenter</span>
                  </div>
                  <div className={cn(styles.flexBetween, 'h-12 bg-muted/50 rounded px-4')}>
                    <span className="text-sm">Left</span>
                    <span className="text-sm">flexBetween</span>
                    <span className="text-sm">Right</span>
                  </div>
                  <div className={cn(styles.flexRowGap, 'bg-muted/50 rounded p-4')}>
                    <Badge>flexRowGap</Badge>
                    <Badge variant="secondary">gap-3</Badge>
                    <Badge variant="outline">items</Badge>
                  </div>
                </div>
              </DemoCard>

              <CodeBlock code={`// 페이지 구조
<div className={pageContainer}>
  <div className={pageContent}>
    <div className={pageContentNarrow}>
      {/* 폼 내용 */}
    </div>
  </div>
</div>

// 페이지 헤더
<div className={pageHeader}>
  <h1 className={pageTitle}>제목</h1>
  <p className={pageSubtitle}>부제목</p>
</div>`} />
            </ComponentSection>
          </TabsContent>

          {/* Utilities */}
          <TabsContent value="utilities" className={styles.sectionGap}>
            <ComponentSection title="Style Utilities" description="재사용 가능한 스타일 상수">
              <DemoCard title="아이콘 크기">
                <div className={styles.flexRowGapLg + ' flex-wrap'}>
                  <div className={styles.flexRowGapSm}>
                    <Search className={styles.iconSm} />
                    <span className="text-sm">iconSm (w-4 h-4)</span>
                  </div>
                  <div className={styles.flexRowGapSm}>
                    <Menu className={styles.iconMd} />
                    <span className="text-sm">iconMd (w-5 h-5)</span>
                  </div>
                  <div className={styles.flexRowGapSm}>
                    <Star className={styles.iconLg} />
                    <span className="text-sm">iconLg (w-8 h-8)</span>
                  </div>
                </div>
              </DemoCard>

              <DemoCard title="텍스트 스타일">
                <div className="space-y-2">
                  <p className={styles.textSm}>textSm - 작은 텍스트</p>
                  <p className={styles.textMuted}>textMuted - 뮤트 텍스트</p>
                  <p className={styles.textSmMuted}>textSmMuted - 작고 뮤트된 텍스트</p>
                  <p>
                    <a href="#" className={styles.link}>link 스타일</a>
                    {' / '}
                    <a href="#" className={styles.linkMedium}>linkMedium 스타일</a>
                  </p>
                </div>
              </DemoCard>

              <DemoCard title="간격">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className={cn(styles.sectionGap, 'p-4 bg-muted/30 rounded')}>
                    <div className="h-8 bg-muted rounded" />
                    <div className="h-8 bg-muted rounded" />
                    <div className="h-8 bg-muted rounded" />
                    <span className="text-xs text-muted-foreground">sectionGap (space-y-6)</span>
                  </div>
                  <div className={cn(styles.formGap, 'p-4 bg-muted/30 rounded')}>
                    <div className="h-8 bg-muted rounded" />
                    <div className="h-8 bg-muted rounded" />
                    <div className="h-8 bg-muted rounded" />
                    <span className="text-xs text-muted-foreground">formGap (space-y-4)</span>
                  </div>
                </div>
              </DemoCard>

              <DemoCard title="네비게이션">
                <div className="flex gap-2 flex-wrap">
                  <button className={cn(styles.navLink, styles.navLinkActive)}>Active</button>
                  <button className={cn(styles.navLink, styles.navLinkInactive)}>Inactive</button>
                  <button className={cn(styles.navLink, styles.navLinkInactive)}>Another</button>
                </div>
              </DemoCard>

              <CodeBlock code={`// @/utils/classNames.ts에서 import
import {
  pageContainer,
  pageContent,
  pageContentNarrow,
  card,
  cardPadding,
  iconSm,
  iconMd,
  flexCenter,
  flexRowGap,
  textSmMuted,
  link,
  linkMedium,
} from '@/utils'

// 사용 예시
<div className={pageContainer}>
  <div className={pageContent}>
    <div className={\`\${card} \${cardPadding}\`}>
      <Search className={iconSm} />
    </div>
  </div>
</div>`} />
            </ComponentSection>
          </TabsContent>
        </Tabs>
      </main>

      {/* Footer */}
      <footer className="border-t mt-12">
        <div className="max-w-[1200px] mx-auto p-6 flex items-center justify-center gap-8">
          <div className={styles.flexRowGapSm}>
            <div className={cn(styles.logoGradient, 'w-6 h-6')}>
              <span className="text-white font-bold text-xs">Z</span>
            </div>
            <span className="text-sm font-semibold">Zenless</span>
          </div>
          <Separator orientation="vertical" className="h-4" />
          <span className={styles.textSmMuted}>© 2024 All rights reserved.</span>
          <Separator orientation="vertical" className="h-4" />
          <a href="#" className={styles.textSmMuted}>
            <Github className={styles.iconSm} />
          </a>
        </div>
      </footer>
    </div>
  )
}
