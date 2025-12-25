#!/usr/bin/env npx tsx
/**
 * skill-activation-prompt.ts - UserPromptSubmit Hook
 *
 * 사용자 프롬프트 제출 시 실행되어 관련 스킬을 자동으로 제안합니다.
 * skill-rules.json의 규칙에 따라 키워드 및 정규식 패턴 매칭을 수행합니다.
 */

import { readFileSync } from 'fs';
import { join } from 'path';

interface HookInput {
    session_id: string;
    transcript_path: string;
    cwd: string;
    permission_mode: string;
    prompt: string;
}

interface PromptTriggers {
    keywords?: string[];
    intentPatterns?: string[];
}

interface FileTriggers {
    pathPatterns?: string[];
    pathExclusions?: string[];
    contentPatterns?: string[];
}

interface SkillRule {
    type: 'guardrail' | 'domain';
    enforcement: 'block' | 'suggest' | 'warn';
    priority: 'critical' | 'high' | 'medium' | 'low';
    description?: string;
    promptTriggers?: PromptTriggers;
    fileTriggers?: FileTriggers;
}

interface SkillRules {
    version: string;
    description?: string;
    skills: Record<string, SkillRule>;
}

interface MatchedSkill {
    name: string;
    matchType: 'keyword' | 'intent';
    config: SkillRule;
}

async function main() {
    try {
        // stdin에서 입력 읽기
        const input = readFileSync(0, 'utf-8');
        const data: HookInput = JSON.parse(input);
        const prompt = data.prompt.toLowerCase();

        // skill-rules.json 로드
        const projectDir = process.env.CLAUDE_PROJECT_DIR || process.cwd();
        const rulesPath = join(projectDir, '.claude', 'skills', 'skill-rules.json');

        let rules: SkillRules;
        try {
            rules = JSON.parse(readFileSync(rulesPath, 'utf-8'));
        } catch {
            // skill-rules.json이 없으면 조용히 종료
            process.exit(0);
        }

        const matchedSkills: MatchedSkill[] = [];

        // 각 스킬에 대해 매칭 검사
        for (const [skillName, config] of Object.entries(rules.skills)) {
            const triggers = config.promptTriggers;
            if (!triggers) {
                continue;
            }

            // 키워드 매칭
            if (triggers.keywords) {
                const keywordMatch = triggers.keywords.some(kw =>
                    prompt.includes(kw.toLowerCase())
                );
                if (keywordMatch) {
                    matchedSkills.push({ name: skillName, matchType: 'keyword', config });
                    continue;
                }
            }

            // 의도 패턴 매칭 (정규식)
            if (triggers.intentPatterns) {
                const intentMatch = triggers.intentPatterns.some(pattern => {
                    try {
                        const regex = new RegExp(pattern, 'i');
                        return regex.test(prompt);
                    } catch {
                        return false;
                    }
                });
                if (intentMatch) {
                    matchedSkills.push({ name: skillName, matchType: 'intent', config });
                }
            }
        }

        // 매칭된 스킬이 있으면 출력
        if (matchedSkills.length > 0) {
            let output = '\n';
            output += '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n';
            output += '🎯 스킬 자동 활성화 제안\n';
            output += '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n';

            // 우선순위별 그룹화
            const critical = matchedSkills.filter(s => s.config.priority === 'critical');
            const high = matchedSkills.filter(s => s.config.priority === 'high');
            const medium = matchedSkills.filter(s => s.config.priority === 'medium');
            const low = matchedSkills.filter(s => s.config.priority === 'low');

            if (critical.length > 0) {
                output += '⚠️  필수 스킬 (CRITICAL):\n';
                critical.forEach(s => {
                    output += `    → ${s.name}`;
                    if (s.config.description) output += ` - ${s.config.description}`;
                    output += '\n';
                });
                output += '\n';
            }

            if (high.length > 0) {
                output += '📚 권장 스킬 (HIGH):\n';
                high.forEach(s => {
                    output += `    → ${s.name}`;
                    if (s.config.description) output += ` - ${s.config.description}`;
                    output += '\n';
                });
                output += '\n';
            }

            if (medium.length > 0) {
                output += '💡 제안 스킬 (MEDIUM):\n';
                medium.forEach(s => {
                    output += `    → ${s.name}`;
                    if (s.config.description) output += ` - ${s.config.description}`;
                    output += '\n';
                });
                output += '\n';
            }

            if (low.length > 0) {
                output += '📌 선택적 스킬 (LOW):\n';
                low.forEach(s => {
                    output += `    → ${s.name}`;
                    if (s.config.description) output += ` - ${s.config.description}`;
                    output += '\n';
                });
                output += '\n';
            }

            output += '💬 위 스킬을 사용하려면 Skill 도구를 호출하세요.\n';
            output += '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n';

            console.log(output);
        }

        process.exit(0);
    } catch (err) {
        // 에러가 발생해도 hook이 Claude 실행을 방해하지 않도록 조용히 종료
        process.exit(0);
    }
}

main().catch(() => {
    process.exit(0);
});
