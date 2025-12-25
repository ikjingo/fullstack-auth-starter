---
name: PR Workflow
description: 현재 작업 내용을 커밋하고 PR을 생성한 후 코드 리뷰를 수행하고 피드백을 반영하여 머지하는 전체 워크플로우를 자동화합니다. 사용자가 "PR 만들어 줘", "커밋하고 PR 생성해 줘", "코드 리뷰 후 머지해 줘" 등의 요청을 할 때 사용합니다.
---

# PR Workflow

현재 작업 내용을 커밋 → PR 생성 → 코드 리뷰 → 피드백 반영 → 머지하는 전체 워크플로우를 자동화하는 스킬입니다.

## 워크플로우 단계

### 1단계: 변경사항 분석 및 커밋

> **💡 Tip:** 커밋 작성 시 `zenless-commit` 스킬을 사용하면 기능별, 영역별로 구조화된 커밋을 자동 생성할 수 있습니다.

```bash
# 변경된 파일 확인
git status

# 변경 내용 상세 확인
git diff

# 최근 커밋 메시지 스타일 확인
git log --oneline -10
```

#### Atomic Commit (기능별 커밋) 원칙

**중요: 모든 변경사항을 한 번에 커밋하지 않고, 논리적 단위로 분리하여 커밋합니다.**

**좋은 예시:**
```bash
# 1. 백엔드 엔티티 변경
git add backend/storage/db-core/src/main/kotlin/.../UserEntity.kt
git add backend/storage/db-core/src/main/kotlin/.../UserRepository.kt
git commit -m "[feature] [BE] UserEntity에 OAuth provider 필드 추가

변경 내용:
- UserEntity에 oauthProvider, oauthId 필드 추가
- UserRepository에 findByOAuthId 메서드 추가

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>"

# 2. 백엔드 서비스 로직
git add backend/api/auth-api/src/main/kotlin/.../GoogleOAuthService.kt
git add backend/api/auth-api/src/main/kotlin/.../AuthService.kt
git commit -m "[feature] [BE] Google OAuth 인증 서비스 구현

변경 내용:
- GoogleOAuthService 클래스 생성
- AuthService에 OAuth 로그인 로직 통합

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>"

# 3. 백엔드 컨트롤러
git add backend/api/auth-api/src/main/kotlin/.../AuthController.kt
git add backend/api/auth-api/src/main/kotlin/.../GoogleLoginRequest.kt
git commit -m "[feature] [BE] Google 로그인 API 엔드포인트 추가

변경 내용:
- AuthController에 POST /api/auth/google 엔드포인트 추가
- GoogleLoginRequest DTO 생성

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>"

# 4. 프론트엔드 변경
git add frontend/src/pages/LoginPage.tsx
git add frontend/src/pages/RegisterPage.tsx
git add frontend/src/api/auth.ts
git commit -m "[feature] [FE] Google 로그인 버튼 및 연동 구현

변경 내용:
- LoginPage에 Google 로그인 버튼 추가
- RegisterPage에 OAuth 옵션 추가
- auth.ts에 googleLogin API 함수 추가

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>"
```

**나쁜 예시:**
```bash
# 모든 변경사항을 한 번에 커밋 (피해야 함)
git add .
git commit -m "[feature] Google 로그인 기능 구현"
```

**기능별 커밋의 장점:**
- 코드 리뷰가 쉬워짐 (변경 단위가 명확)
- 문제 발생 시 특정 커밋만 되돌리기 가능
- Git 히스토리가 깔끔해짐
- 협업 시 충돌 해결이 용이

**커밋 분리 기준:**
1. **레이어별**: 엔티티 → 서비스 → 컨트롤러 → 프론트엔드
2. **기능별**: 인증 로직 → API 엔드포인트 → UI 컴포넌트
3. **관심사별**: 비즈니스 로직 → 설정 변경 → 테스트 코드

#### 커밋 메시지 형식

**커밋 메시지 형식 (CLAUDE.md + zenless-commit 기준):**
- 한글로 작성
- 형식: `[타입] [영역] 설명`
- 타입: `[feature]`, `[fix]`, `[docs]`, `[refactor]`, `[test]`, `[chore]`
- 영역 태그:
  - `[BE]` - 백엔드 (Kotlin/Spring Boot)
  - `[FE]` - 프론트엔드 (React/TypeScript)
  - `[INFRA]` - 인프라/설정 (Docker, Gradle, npm 등)
  - `[FULL]` - 풀스택 (프론트+백엔드 동시 변경)

**커밋 실행:**
```bash
# 특정 파일만 스테이징 (권장)
git add <관련된 파일들>
git commit -m "[타입] 변경 내용 설명

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>"
```

#### 선택적 스테이징

```bash
# 파일 단위로 스테이징
git add path/to/specific/file.kt

# 디렉토리 단위로 스테이징
git add backend/api/auth-api/src/

# 변경 내용 일부만 스테이징 (인터랙티브)
git add -p

# 스테이징된 내용 확인
git diff --staged
```

### 2단계: 브랜치 확인 및 푸시

```bash
# 현재 브랜치 확인
git branch --show-current

# 리모트 브랜치 존재 여부 확인
git ls-remote --heads origin $(git branch --show-current)

# 브랜치 푸시 (필요시 -u 옵션으로 upstream 설정)
git push -u origin $(git branch --show-current)
```

### 3단계: 기존 PR 확인 및 처리

**PR 생성 전에 현재 브랜치에 이미 열려있는 PR이 있는지 확인합니다.**

```bash
# 현재 브랜치의 기존 PR 확인
gh pr list --head $(git branch --show-current) --state open
```

또는 MCP 도구 사용:
```
mcp__github__list_pull_requests(owner, repo, head="owner:branch-name", state="open")
```

**기존 PR이 있는 경우 처리 방법:**

#### 옵션 A: 기존 PR에 커밋 추가 (기본 권장)
현재 작업이 기존 PR과 동일한 기능/목적인 경우:
- 새 커밋을 push하면 자동으로 기존 PR에 반영됨
- PR 생성 단계를 건너뛰고 바로 코드 리뷰 단계로 진행

```bash
# 커밋 후 push만 하면 기존 PR에 자동 반영
git push
```

#### 옵션 B: 기존 PR 머지 후 새 PR 생성
기존 PR이 완료되었고 새로운 작업을 별도 PR로 만들고 싶은 경우:

```bash
# 1. 기존 PR 머지
gh pr merge <PR_NUMBER> --squash --delete-branch

# 또는 MCP 사용
mcp__github__merge_pull_request(owner, repo, pull_number, merge_method="squash")

# 2. main 브랜치로 이동 및 최신화
git checkout main
git pull origin main

# 3. 새 feature 브랜치 생성
git checkout -b feature/new-feature-name

# 4. 새 PR 생성 진행
```

#### 옵션 C: 기존 PR 닫고 새 PR 생성
기존 PR을 폐기하고 새로 시작하고 싶은 경우:

```bash
# 기존 PR 닫기
gh pr close <PR_NUMBER>

# 새 PR 생성
gh pr create --title "..." --body "..."
```

**자동 처리 로직:**
1. 기존 PR 확인
2. 기존 PR이 있으면:
   - 현재 커밋이 기존 PR과 관련된 작업이면 → **옵션 A** (push하여 기존 PR에 추가)
   - 기존 PR이 완료 상태이고 머지 가능하면 → **옵션 B** (머지 후 새 PR)
   - 사용자가 별도 PR을 원하면 → 사용자에게 확인 후 진행

### 4단계: PR 생성 (기존 PR이 없는 경우)

**PR 제목 형식 (CLAUDE.md 기준):**
- 커밋 메시지와 동일한 형식: `[타입] 설명`

**PR 본문 형식:**
```markdown
## Summary
- 변경 사항 요약 (bullet points)

## Test plan
- [ ] 테스트 항목 체크리스트

🤖 Generated with [Claude Code](https://claude.com/claude-code)
```

**MCP 도구를 사용한 PR 생성 (권장):**

프로젝트에는 `github`와 `github-reviewer` 두 개의 MCP 서버가 설정되어 있습니다.
- **PR 생성**: `mcp__github__` 도구 사용
- **코드 리뷰**: `mcp__github-reviewer__` 도구 사용

```
# PR 생성 (github MCP 사용)
mcp__github__create_pull_request(
    owner="ikjingo",
    repo="zenless",
    title="[타입] PR 제목",
    head="feature/branch-name",
    base="main",
    body="## Summary\n- 변경 사항\n\n## Test plan\n- [ ] 테스트 항목\n\n🤖 Generated with [Claude Code](https://claude.com/claude-code)"
)
```

**gh CLI를 사용한 PR 생성 (대체):**
```bash
gh pr create --title "[타입] PR 제목" --body "$(cat <<'EOF'
## Summary
- 변경 사항 1
- 변경 사항 2

## Test plan
- [ ] 테스트 항목 1
- [ ] 테스트 항목 2

🤖 Generated with [Claude Code](https://claude.com/claude-code)
EOF
)"
```

### 5단계: 코드 리뷰

PR이 생성되면 자동으로 코드 리뷰를 수행합니다.
**중요: PR 생성자와 다른 계정으로 리뷰하기 위해 `github-reviewer` MCP를 사용합니다.**

**리뷰 포인트 (CLAUDE.md 기준):**
- 보안 이슈 (민감 정보 노출, console.log 등)
- 코드 중복 및 재사용성
- 타입 안정성
- 에러 처리
- 네이밍 컨벤션

**MCP 도구를 사용한 코드 리뷰 (권장):**

```
# 1. PR 정보 확인 (github-reviewer MCP 사용)
mcp__github-reviewer__get_pull_request(owner="ikjingo", repo="zenless", pull_number=<PR_NUMBER>)

# 2. PR 변경 파일 확인
mcp__github-reviewer__get_pull_request_files(owner="ikjingo", repo="zenless", pull_number=<PR_NUMBER>)

# 3. PR 리뷰 작성 - 승인
mcp__github-reviewer__create_pull_request_review(
    owner="ikjingo",
    repo="zenless",
    pull_number=<PR_NUMBER>,
    body="코드 리뷰 완료. LGTM! 👍",
    event="APPROVE"
)

# 4. PR 리뷰 작성 - 변경 요청
mcp__github-reviewer__create_pull_request_review(
    owner="ikjingo",
    repo="zenless",
    pull_number=<PR_NUMBER>,
    body="다음 사항을 수정해 주세요:\n- 수정 사항 1\n- 수정 사항 2",
    event="REQUEST_CHANGES"
)

# 5. PR 리뷰 작성 - 코멘트만
mcp__github-reviewer__create_pull_request_review(
    owner="ikjingo",
    repo="zenless",
    pull_number=<PR_NUMBER>,
    body="리뷰 내용",
    event="COMMENT"
)
```

**gh CLI를 사용한 리뷰 (대체):**
```bash
# PR 파일 목록 확인
gh pr diff <PR_NUMBER>

# PR 상세 정보 확인
gh pr view <PR_NUMBER>

# 승인
gh pr review <PR_NUMBER> --approve --body "LGTM! 코드가 깔끔합니다."

# 변경 요청
gh pr review <PR_NUMBER> --request-changes --body "다음 사항을 수정해 주세요:
- 수정 사항 1
- 수정 사항 2"
```

### 6단계: 피드백 반영 (필요시)

코드 리뷰에서 변경 요청이 있는 경우:

```bash
# 코드 수정 후 (영역에 맞게 태그 사용)
git add backend/api/auth-api/...
git commit -m "[fix] [BE] 코드 리뷰 피드백 반영: 예외 처리 개선

변경 내용:
- GoogleOAuthService에 null 체크 추가
- AuthController에 적절한 HTTP 상태 코드 반환

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>"

git push
```

### 7단계: Test Plan 확인 및 실행

**PR 머지 전에 Test plan 항목이 있다면 반드시 확인하고 실행합니다.**

```bash
# PR 본문에서 Test plan 확인
gh pr view <PR_NUMBER> --json body --jq '.body'
```

**Test plan 항목별 확인 절차:**

1. **빌드 확인 항목**: 실제로 빌드를 실행하여 성공 여부 확인
   ```bash
   # 백엔드 빌드
   cd backend && ./gradlew build

   # 프론트엔드 빌드
   cd frontend && npm run build
   ```

2. **기능 테스트 항목**: 해당 기능을 실제로 테스트
   - 개발 서버 실행 후 브라우저에서 확인
   - Playwright 등 테스트 도구 활용
   - API 엔드포인트 직접 호출하여 확인

3. **단위/통합 테스트 항목**: 테스트 스위트 실행
   ```bash
   # 백엔드 테스트
   cd backend && ./gradlew test

   # 프론트엔드 테스트
   cd frontend && npm run test
   ```

**Test plan 완료 후:**
- PR 본문의 체크리스트 항목을 체크 완료 상태로 업데이트
- 테스트 결과를 PR 코멘트로 남기기 (선택)

```bash
# PR 코멘트로 테스트 결과 공유
gh pr comment <PR_NUMBER> --body "## Test Results
- [x] 백엔드 빌드 성공
- [x] 프론트엔드 빌드 성공
- [x] 기능 테스트 완료

모든 테스트가 통과했습니다. ✅"
```

### 8단계: PR 머지

**MCP 도구를 사용한 머지 (권장):**

```
# Squash merge (권장)
mcp__github__merge_pull_request(
    owner="ikjingo",
    repo="zenless",
    pull_number=<PR_NUMBER>,
    merge_method="squash"
)

# 또는 일반 merge
mcp__github__merge_pull_request(
    owner="ikjingo",
    repo="zenless",
    pull_number=<PR_NUMBER>,
    merge_method="merge"
)

# 또는 rebase merge
mcp__github__merge_pull_request(
    owner="ikjingo",
    repo="zenless",
    pull_number=<PR_NUMBER>,
    merge_method="rebase"
)
```

**gh CLI를 사용한 머지 (대체):**
```bash
# Squash merge (권장)
gh pr merge <PR_NUMBER> --squash --delete-branch

# 또는 일반 merge
gh pr merge <PR_NUMBER> --merge --delete-branch

# 또는 rebase merge
gh pr merge <PR_NUMBER> --rebase --delete-branch
```

**머지 후 로컬 정리:**
```bash
git checkout main
git pull origin main
git branch -d <feature-branch-name>
```

## 전체 워크플로우 예시

사용자가 "현재 작업 커밋하고 PR 만들어 줘"라고 요청하면:

1. `git status`로 변경사항 확인
2. `git diff`로 변경 내용 분석
3. 적절한 커밋 메시지 작성 후 커밋 (Atomic Commit 원칙)
4. `git push`로 리모트에 푸시
5. **기존 PR 확인** (`mcp__github__list_pull_requests` 또는 `gh pr list`)
   - 기존 PR이 있으면 → 옵션 A/B/C 중 선택하여 처리
   - 기존 PR이 없으면 → 새 PR 생성 진행
6. `gh pr create` 또는 `mcp__github__create_pull_request`로 PR 생성
7. 자동으로 코드 리뷰 수행
8. 리뷰 결과에 따라 승인 또는 피드백 제공
9. 피드백이 있으면 수정 후 재푸시
10. **PR의 Test plan 항목이 있다면 테스트 수행 및 확인**
11. 최종 승인 후 `gh pr merge --squash` 또는 `mcp__github__merge_pull_request`로 머지

## 주의사항

- **main/master 브랜치에 직접 커밋하지 않기**: 항상 feature 브랜치에서 작업
- **커밋 전 빌드/테스트 확인**: 코드가 정상 동작하는지 확인
- **민감 정보 커밋 금지**: .env, credentials.json 등 제외
- **force push 주의**: 협업 브랜치에서는 force push 금지
- **리뷰 없이 머지 금지**: 최소 1명 이상의 리뷰 후 머지

## GitHub MCP 도구 사용 가이드

이 프로젝트는 `.mcp.json`에 두 개의 GitHub MCP 서버가 설정되어 있어 PR 생성과 코드 리뷰를 서로 다른 계정으로 수행할 수 있습니다.

### MCP 서버 구성

| MCP 서버 | 용도 | 토큰 |
|---------|------|------|
| `github` | PR 생성, 파일 수정, 머지 | PR 생성자 계정 |
| `github-reviewer` | 코드 리뷰, 리뷰 코멘트 | 리뷰어 계정 |

### 작업별 사용 도구

| 작업 | MCP 서버 | 도구 |
|------|----------|------|
| 기존 PR 확인 | `github` | `mcp__github__list_pull_requests` |
| PR 생성 | `github` | `mcp__github__create_pull_request` |
| PR 정보 조회 | `github-reviewer` | `mcp__github-reviewer__get_pull_request` |
| PR 파일 목록 | `github-reviewer` | `mcp__github-reviewer__get_pull_request_files` |
| 코드 리뷰 작성 | `github-reviewer` | `mcp__github-reviewer__create_pull_request_review` |
| PR 머지 | `github` | `mcp__github__merge_pull_request` |

### 전체 워크플로우에서의 MCP 도구 사용 순서

```
1. 기존 PR 확인
   → mcp__github__list_pull_requests(owner, repo, head="owner:branch", state="open")

2. PR 생성 (기존 PR 없을 때)
   → mcp__github__create_pull_request(owner, repo, title, head, base, body)

3. 코드 리뷰 수행
   → mcp__github-reviewer__get_pull_request_files(owner, repo, pull_number)
   → mcp__github-reviewer__create_pull_request_review(owner, repo, pull_number, body, event)

4. PR 머지
   → mcp__github__merge_pull_request(owner, repo, pull_number, merge_method="squash")
```

### 토큰 권한 요구사항

| 용도 | 필요한 Scope |
|------|-------------|
| PR 생성/머지 | `repo` (전체) |
| 코드 리뷰 | `repo` 또는 `public_repo` |

**참고:**
- 설정 변경 후 Claude Code 재시작 필요
- Fine-grained token 사용 시 해당 레포지토리에 대한 Pull requests 권한 필요

## 트러블슈팅

**PR 생성 실패 시:**
```bash
# GitHub CLI 인증 상태 확인
gh auth status

# 재인증
gh auth login
```

**충돌 발생 시:**
```bash
git fetch origin main
git rebase origin/main
# 충돌 해결 후
git rebase --continue
git push --force-with-lease
```

**실수로 main에 커밋한 경우:**
```bash
# 마지막 커밋을 새 브랜치로 이동
git branch feature/new-branch
git reset --hard HEAD~1
git checkout feature/new-branch
```
