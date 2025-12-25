#!/bin/bash
# build-check.sh - Stop Hook: 빌드 체크 실행
#
# Zenless 프로젝트용 빌드 체크 훅
# Claude가 응답을 완료한 후 실행되어 변경된 코드를 검증합니다.
#
# 프로젝트 구조:
# - backend/ (멀티모듈 Gradle: api/*, core/*, storage/*)
#   → ./gradlew compileKotlin으로 전체 모듈 컴파일
# - frontend/ (React + TypeScript + Vite)
#   → npx tsc --noEmit으로 타입 체크
#
# 동작 흐름:
# 1. post-tool-use-tracker.sh가 캐시한 변경 정보 확인
# 2. 캐시가 없으면 git diff로 폴백 (*.kt, *.ts, *.tsx만 대상)
# 3. backend/ 변경 시 Kotlin 컴파일 체크
# 4. frontend/ 변경 시 Prettier 포맷팅 + TypeScript 타입 체크
# 5. 에러 발생 시 Claude에게 알림
# 6. 완료 후 캐시 정리

set -e

PROJECT_DIR="${CLAUDE_PROJECT_DIR:-$(pwd)}"
cd "$PROJECT_DIR"

# 캐시 디렉토리 확인 (post-tool-use-tracker.sh에서 생성)
CACHE_BASE="$PROJECT_DIR/.claude/build-cache"
BACKEND_CHANGED=false
FRONTEND_CHANGED=false

# 캐시에서 영향받은 모듈 확인
if [ -d "$CACHE_BASE" ]; then
    for session_dir in "$CACHE_BASE"/*/; do
        if [ -f "${session_dir}affected-modules.txt" ]; then
            while IFS= read -r module; do
                case "$module" in
                    backend) BACKEND_CHANGED=true ;;
                    frontend) FRONTEND_CHANGED=true ;;
                esac
            done < "${session_dir}affected-modules.txt"
        fi
    done
fi

# 캐시가 비어있으면 git diff로 폴백
if [ "$BACKEND_CHANGED" = false ] && [ "$FRONTEND_CHANGED" = false ]; then
    CHANGED_FILES=$(git diff --name-only HEAD 2>/dev/null || git diff --name-only 2>/dev/null || echo "")

    if [ -z "$CHANGED_FILES" ]; then
        # 캐시 정리 후 종료
        rm -rf "$CACHE_BASE" 2>/dev/null || true
        exit 0
    fi

    while IFS= read -r file; do
        # backend/*.kt 파일만 컴파일 대상
        if [[ "$file" == backend/*.kt ]] || [[ "$file" == backend/**/*.kt ]]; then
            BACKEND_CHANGED=true
        # frontend/*.ts, *.tsx 파일만 타입체크 대상
        elif [[ "$file" == frontend/*.ts ]] || [[ "$file" == frontend/**/*.ts ]] || \
             [[ "$file" == frontend/*.tsx ]] || [[ "$file" == frontend/**/*.tsx ]]; then
            FRONTEND_CHANGED=true
        fi
    done <<< "$CHANGED_FILES"
fi

ERRORS=""
WARNINGS=""
RISK_PATTERNS=""

# Frontend Prettier 포맷 (빌드 전 실행)
if [ "$FRONTEND_CHANGED" = true ]; then
    if [ -f "$PROJECT_DIR/frontend/package.json" ]; then
        # Prettier가 설치되어 있으면 포맷팅 실행
        if [ -f "$PROJECT_DIR/frontend/node_modules/.bin/prettier" ]; then
            echo "✨ Prettier 포맷팅 중..."
            cd "$PROJECT_DIR/frontend"
            npx prettier --write "src/**/*.{ts,tsx}" --log-level error 2>/dev/null || true
            cd "$PROJECT_DIR"
        fi
    fi
fi

# Backend 빌드 체크 (Kotlin)
if [ "$BACKEND_CHANGED" = true ]; then
    if [ -f "$PROJECT_DIR/backend/gradlew" ]; then
        echo "🔍 Backend 컴파일 체크 중..."

        cd "$PROJECT_DIR/backend"
        BUILD_OUTPUT=$(./gradlew compileKotlin --quiet 2>&1) || {
            ERROR_COUNT=$(echo "$BUILD_OUTPUT" | grep -c "error:" || echo "0")
            if [ "$ERROR_COUNT" -gt 0 ]; then
                ERRORS="${ERRORS}\n\n📦 Backend 컴파일 에러 (${ERROR_COUNT}개):\n"
                # 처음 5개 에러만 표시
                ERRORS="${ERRORS}$(echo "$BUILD_OUTPUT" | grep -A 2 "error:" | head -20)"
            fi
        }
        cd "$PROJECT_DIR"
    fi
fi

# Frontend 빌드 체크 (TypeScript)
if [ "$FRONTEND_CHANGED" = true ]; then
    if [ -f "$PROJECT_DIR/frontend/package.json" ]; then
        echo "🔍 Frontend 타입 체크 중..."

        cd "$PROJECT_DIR/frontend"

        # TypeScript 타입 체크만 실행 (빌드보다 빠름)
        TSC_OUTPUT=$(npx tsc --noEmit 2>&1) || {
            ERROR_COUNT=$(echo "$TSC_OUTPUT" | grep -c "error TS" || echo "0")
            if [ "$ERROR_COUNT" -gt 0 ]; then
                ERRORS="${ERRORS}\n\n🎨 Frontend 타입 에러 (${ERROR_COUNT}개):\n"
                # 처음 5개 에러만 표시
                ERRORS="${ERRORS}$(echo "$TSC_OUTPUT" | grep "error TS" | head -5)"
            fi
        }
        cd "$PROJECT_DIR"
    fi
fi

# 결과 출력
if [ -n "$ERRORS" ]; then
    echo ""
    echo "═══════════════════════════════════════════════════════════════"
    echo "⚠️  빌드 체크 결과: 에러 발견"
    echo "═══════════════════════════════════════════════════════════════"
    echo -e "$ERRORS"
    echo ""
    echo "💡 위 에러를 수정해주세요."
    echo "═══════════════════════════════════════════════════════════════"
else
    if [ "$BACKEND_CHANGED" = true ] || [ "$FRONTEND_CHANGED" = true ]; then
        echo "✅ 빌드 체크 통과"
    fi
fi

# 캐시 정리: 현재 세션 캐시 삭제
rm -rf "$CACHE_BASE" 2>/dev/null || true

exit 0
