#!/bin/bash
# post-tool-use-tracker.sh - PostToolUse Hook
#
# Zenless 프로젝트용 파일 편집 추적 훅
# 파일 편집 도구(Edit, MultiEdit, Write) 사용 후 실행되어
# 변경된 파일과 영향받는 모듈을 추적합니다.
#
# 프로젝트 구조:
# - backend/ (멀티모듈 Gradle: api/*, core/*, storage/*)
# - frontend/ (React + TypeScript + Vite)
#
# 이 정보는 build-check.sh에서 사용됩니다.

set -e

# stdin에서 도구 정보 읽기
tool_info=$(cat)

# 관련 데이터 추출
tool_name=$(echo "$tool_info" | jq -r '.tool_name // empty')
file_path=$(echo "$tool_info" | jq -r '.tool_input.file_path // empty')
session_id=$(echo "$tool_info" | jq -r '.session_id // empty')

# 편집 도구가 아니거나 파일 경로가 없으면 종료
if [[ ! "$tool_name" =~ ^(Edit|MultiEdit|Write)$ ]] || [[ -z "$file_path" ]]; then
    exit 0
fi

# 빌드 체크 대상이 아닌 파일 무시
# - 마크다운 파일
# - .claude 디렉토리 (훅, 스킬, 설정 등)
# - 설정 파일 (json, yaml, toml 등)
if [[ "$file_path" =~ \.(md|markdown)$ ]]; then
    exit 0
fi
if [[ "$file_path" =~ \.claude/ ]]; then
    exit 0
fi
if [[ "$file_path" =~ \.(json|yaml|yml|toml|properties)$ ]]; then
    exit 0
fi

# 캐시 디렉토리 생성
cache_dir="$CLAUDE_PROJECT_DIR/.claude/build-cache/${session_id:-default}"
mkdir -p "$cache_dir"

# 파일 경로에서 모듈 감지
# Zenless 프로젝트 구조:
# - frontend/src/**/*.{ts,tsx} → frontend
# - backend/api/**/*.kt → backend (api 모듈)
# - backend/core/**/*.kt → backend (core 모듈)
# - backend/storage/**/*.kt → backend (storage 모듈)
detect_module() {
    local file="$1"
    local project_root="$CLAUDE_PROJECT_DIR"

    # 프로젝트 루트 경로 제거
    local relative_path="${file#$project_root/}"

    # 첫 번째 디렉토리 컴포넌트 추출
    local top_level=$(echo "$relative_path" | cut -d'/' -f1)

    case "$top_level" in
        frontend)
            # TypeScript/JavaScript 파일만 타입체크 대상
            if [[ "$relative_path" =~ \.(ts|tsx|js|jsx)$ ]]; then
                echo "frontend"
            else
                echo "other"
            fi
            ;;
        backend)
            # Kotlin 파일만 컴파일 대상
            if [[ "$relative_path" =~ \.kt$ ]]; then
                echo "backend"
            else
                echo "other"
            fi
            ;;
        *)
            echo "other"
            ;;
    esac
}

# 모듈별 빌드 명령 반환
get_build_command() {
    local module="$1"
    local project_root="$CLAUDE_PROJECT_DIR"

    case "$module" in
        frontend)
            echo "cd $project_root/frontend && npm run build"
            ;;
        backend)
            echo "cd $project_root/backend && ./gradlew compileKotlin"
            ;;
        *)
            echo ""
            ;;
    esac
}

# 모듈별 타입 체크 명령 반환
get_typecheck_command() {
    local module="$1"
    local project_root="$CLAUDE_PROJECT_DIR"

    case "$module" in
        frontend)
            echo "cd $project_root/frontend && npx tsc --noEmit"
            ;;
        backend)
            echo "cd $project_root/backend && ./gradlew compileKotlin"
            ;;
        *)
            echo ""
            ;;
    esac
}

# 모듈 감지
module=$(detect_module "$file_path")

# 알 수 없는 모듈이면 종료
if [[ "$module" == "other" ]] || [[ -z "$module" ]]; then
    exit 0
fi

# 편집된 파일 로그
echo "$(date +%s):$file_path:$module" >> "$cache_dir/edited-files.log"

# 영향받는 모듈 목록 업데이트
if ! grep -q "^$module$" "$cache_dir/affected-modules.txt" 2>/dev/null; then
    echo "$module" >> "$cache_dir/affected-modules.txt"
fi

# 빌드 명령 저장
build_cmd=$(get_build_command "$module")
typecheck_cmd=$(get_typecheck_command "$module")

if [[ -n "$build_cmd" ]]; then
    echo "$module:build:$build_cmd" >> "$cache_dir/commands.txt.tmp"
fi

if [[ -n "$typecheck_cmd" ]]; then
    echo "$module:typecheck:$typecheck_cmd" >> "$cache_dir/commands.txt.tmp"
fi

# 중복 제거
if [[ -f "$cache_dir/commands.txt.tmp" ]]; then
    sort -u "$cache_dir/commands.txt.tmp" > "$cache_dir/commands.txt"
    rm -f "$cache_dir/commands.txt.tmp"
fi

exit 0
