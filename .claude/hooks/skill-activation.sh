#!/bin/bash
# skill-activation.sh - UserPromptSubmit Hook: 스킬 자동 활성화 제안
#
# 이 Hook은 사용자 프롬프트 제출 시 실행되어:
# 1. 프롬프트 내용을 분석
# 2. 관련 스킬을 자동으로 제안
# 3. Claude가 적절한 스킬을 사용하도록 유도

# stdin에서 프롬프트 내용 읽기
PROMPT=$(cat)

# 프롬프트가 비어있으면 종료
if [ -z "$PROMPT" ]; then
    exit 0
fi

# 소문자로 변환하여 검색
PROMPT_LOWER=$(echo "$PROMPT" | tr '[:upper:]' '[:lower:]')

SUGGESTIONS=""

# 백엔드 테스트 관련 키워드
if echo "$PROMPT_LOWER" | grep -qE "(백엔드.*테스트|서비스.*테스트|컨트롤러.*테스트|test.*backend|backend.*test|junit|mockk|테스트.*작성|테스트.*만들|테스트.*추가|단위.*테스트|통합.*테스트|unit.*test|integration.*test)"; then
    SUGGESTIONS="${SUGGESTIONS}💡 backend-test-generator 스킬 사용을 권장합니다.\n"
fi

# 코드 리팩토링 관련 키워드
if echo "$PROMPT_LOWER" | grep -qE "(리팩토링|리펙토링|refactor|코드.*개선|코드.*정리|품질.*개선|클린.*코드|clean.*code|구조.*개선|중복.*제거|최적화|optimize)"; then
    SUGGESTIONS="${SUGGESTIONS}💡 refactor-code 스킬 사용을 권장합니다.\n"
fi

# PR/커밋 관련 키워드
if echo "$PROMPT_LOWER" | grep -qE "(pr.*(만들|올려|생성|올리|띄워)|pull.*request|커밋.*하고|머지|merge|pr.*마무리|pr.*완료|브랜치.*머지)"; then
    SUGGESTIONS="${SUGGESTIONS}💡 pr-workflow 스킬 사용을 권장합니다.\n"
fi

# 프로젝트 시작/중지 관련 키워드
if echo "$PROMPT_LOWER" | grep -qE "(프로젝트.*시작|서버.*시작|개발.*환경.*시작|start.*project|run.*server|서버.*실행|서버.*켜|서버.*띄워|docker.*up|개발.*시작)"; then
    SUGGESTIONS="${SUGGESTIONS}💡 start-project 스킬 사용을 권장합니다.\n"
fi

if echo "$PROMPT_LOWER" | grep -qE "(프로젝트.*중지|서버.*중지|개발.*환경.*중지|stop.*project|stop.*server|서버.*종료|서버.*끄|서버.*내려|docker.*down|개발.*중지)"; then
    SUGGESTIONS="${SUGGESTIONS}💡 stop-project 스킬 사용을 권장합니다.\n"
fi

# 커밋 메시지 관련 키워드
if echo "$PROMPT_LOWER" | grep -qE "(커밋.*메시지|commit.*message|git.*commit|커밋.*작성|커밋해|커밋.*해줘|변경.*커밋)"; then
    SUGGESTIONS="${SUGGESTIONS}💡 git-commit-helper 스킬 사용을 권장합니다.\n"
fi

# 웹앱 테스트 관련 키워드
if echo "$PROMPT_LOWER" | grep -qE "(playwright|브라우저.*테스트|e2e.*테스트|스크린샷|웹.*테스트|ui.*테스트|화면.*테스트|화면.*확인|페이지.*확인|브라우저.*확인|화면.*캡처)"; then
    SUGGESTIONS="${SUGGESTIONS}💡 webapp-testing 스킬 사용을 권장합니다.\n"
fi

# 아티팩트 빌드 관련 키워드
if echo "$PROMPT_LOWER" | grep -qE "(artifact|아티팩트|html.*artifact|컴포넌트.*미리보기|미리보기.*만들|프리뷰|preview.*component|인터랙티브.*html)"; then
    SUGGESTIONS="${SUGGESTIONS}💡 artifacts-builder 스킬 사용을 권장합니다.\n"
fi

# 문서 생성 관련 키워드
if echo "$PROMPT_LOWER" | grep -qE "(docx|워드.*문서|word.*document|문서.*생성|문서.*만들|\.docx|워드.*파일|word.*file)"; then
    SUGGESTIONS="${SUGGESTIONS}💡 docx 스킬 사용을 권장합니다.\n"
fi

# 프론트엔드 개발 관련 키워드
if echo "$PROMPT_LOWER" | grep -qE "(프론트엔드.*개발|프론트.*구현|frontend.*develop|react.*구현|컴포넌트.*만들|component.*create|ui.*구현|페이지.*만들|tailwind|화면.*개발|뷰.*만들|view.*create|폼.*만들|form.*create)"; then
    SUGGESTIONS="${SUGGESTIONS}💡 frontend-dev-guidelines 스킬 사용을 권장합니다.\n"
fi

# 백엔드 개발 관련 키워드
if echo "$PROMPT_LOWER" | grep -qE "(백엔드.*개발|백엔드.*구현|backend.*develop|api.*구현|api.*만들|컨트롤러.*만들|controller.*create|서비스.*구현|service.*create|엔드포인트|endpoint|rest.*api|dto.*만들|entity.*만들)"; then
    SUGGESTIONS="${SUGGESTIONS}💡 backend-dev-guidelines 스킬 사용을 권장합니다.\n"
fi

# 코드 리뷰 관련 키워드
if echo "$PROMPT_LOWER" | grep -qE "(코드.*리뷰|code.*review|리뷰.*해|review.*this|코드.*검토|코드.*봐줘|코드.*확인|pr.*리뷰|변경.*리뷰)"; then
    SUGGESTIONS="${SUGGESTIONS}💡 /code-review 커맨드 사용을 권장합니다.\n"
fi

# 빌드 에러 관련 키워드
if echo "$PROMPT_LOWER" | grep -qE "(빌드.*에러|build.*error|타입.*에러|type.*error|컴파일.*에러|compile.*error|빌드.*실패|build.*fail|에러.*수정|에러.*고쳐|fix.*error|tsc.*error|gradle.*error)"; then
    SUGGESTIONS="${SUGGESTIONS}💡 /build-and-fix 커맨드 사용을 권장합니다.\n"
fi

# 제안이 있으면 출력
if [ -n "$SUGGESTIONS" ]; then
    echo ""
    echo "═══════════════════════════════════════════════════════════════"
    echo "🎯 스킬 활성화 제안"
    echo "═══════════════════════════════════════════════════════════════"
    echo -e "$SUGGESTIONS"
    echo "스킬을 사용하려면 해당 스킬 이름을 언급하거나 자동으로 활성화됩니다."
    echo "═══════════════════════════════════════════════════════════════"
fi

exit 0
