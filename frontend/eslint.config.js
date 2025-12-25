// For more info, see https://github.com/storybookjs/eslint-plugin-storybook#configuration-flat-config-format
import storybook from "eslint-plugin-storybook";

import js from '@eslint/js'
import globals from 'globals'
import reactHooks from 'eslint-plugin-react-hooks'
import reactRefresh from 'eslint-plugin-react-refresh'
import tseslint from 'typescript-eslint'
import { defineConfig, globalIgnores } from 'eslint/config'

export default defineConfig([
  globalIgnores(['dist']),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      js.configs.recommended,
      tseslint.configs.recommended,
      reactHooks.configs.flat.recommended,
      reactRefresh.configs.vite,
    ],
    languageOptions: {
      ecmaVersion: 2020,
      globals: globals.browser,
    },
    rules: {
      // console.log 경고 (production 코드에서 제거 유도)
      'no-console': ['warn', { allow: ['warn', 'error'] }],
      // 사용하지 않는 변수 경고 (언더스코어 prefix는 허용)
      '@typescript-eslint/no-unused-vars': ['warn', {
        argsIgnorePattern: '^_',
        varsIgnorePattern: '^_',
      }],
      // 빈 함수 허용 (이벤트 핸들러 등)
      '@typescript-eslint/no-empty-function': 'off',
      // any 타입 경고 (점진적 타입 안전성 개선)
      '@typescript-eslint/no-explicit-any': 'warn',
      // React hooks 의존성 배열 검사
      'react-hooks/exhaustive-deps': 'warn',
    },
  },
  // Playwright test files - disable React hooks rules
  {
    files: ['tests/**/*.{ts,tsx}'],
    rules: {
      'react-hooks/rules-of-hooks': 'off',
      'react-refresh/only-export-components': 'off',
    },
  },
  // Unit/Integration test files - relax some rules
  {
    files: ['**/*.test.{ts,tsx}', '**/__tests__/**/*.{ts,tsx}', 'src/test/**/*.{ts,tsx}'],
    rules: {
      'no-console': 'off',
      '@typescript-eslint/no-explicit-any': 'off',
    },
  },
])
