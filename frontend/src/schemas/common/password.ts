/**
 * 공통 비밀번호 스키마
 *
 * 비밀번호 관련 유효성 검증 스키마를 중앙화합니다.
 * @see backend/api/auth-api/.../validation/StrongPasswordValidator.kt
 */
import { z } from 'zod'

/**
 * 비밀번호 유효성 검증 정규식 (백엔드와 동기화)
 */
export const PASSWORD_PATTERNS = {
  uppercase: /[A-Z]/,
  lowercase: /[a-z]/,
  digit: /[0-9]/,
  special: /[!@#$%^&*()_+\-=\[\]{}|;':",./<>?`~\\]/,
} as const

/**
 * 비밀번호 에러 메시지
 */
export const PASSWORD_MESSAGES = {
  required: '비밀번호를 입력해주세요',
  minLength: '비밀번호는 8자 이상이어야 합니다',
  uppercase: '대문자를 1개 이상 포함해야 합니다',
  lowercase: '소문자를 1개 이상 포함해야 합니다',
  digit: '숫자를 1개 이상 포함해야 합니다',
  special: '특수문자를 1개 이상 포함해야 합니다',
  confirmRequired: '비밀번호 확인을 입력해주세요',
  mismatch: '비밀번호가 일치하지 않습니다',
  // 레거시 호환성
  pattern: '비밀번호는 대문자, 소문자, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다',
} as const

/**
 * 기본 비밀번호 스키마 (로그인용 - 패턴 검증 없음)
 */
export const loginPasswordSchema = z
  .string()
  .min(1, PASSWORD_MESSAGES.required)
  .min(8, PASSWORD_MESSAGES.minLength)

/**
 * 강화된 비밀번호 스키마 (회원가입/변경용 - 패턴 검증 포함)
 * 백엔드 StrongPasswordValidator와 동일한 검증 규칙 적용
 */
export const strongPasswordSchema = z
  .string()
  .min(1, PASSWORD_MESSAGES.required)
  .min(8, PASSWORD_MESSAGES.minLength)
  .refine((val) => PASSWORD_PATTERNS.uppercase.test(val), {
    message: PASSWORD_MESSAGES.uppercase,
  })
  .refine((val) => PASSWORD_PATTERNS.lowercase.test(val), {
    message: PASSWORD_MESSAGES.lowercase,
  })
  .refine((val) => PASSWORD_PATTERNS.digit.test(val), {
    message: PASSWORD_MESSAGES.digit,
  })
  .refine((val) => PASSWORD_PATTERNS.special.test(val), {
    message: PASSWORD_MESSAGES.special,
  })

/**
 * 비밀번호 확인 스키마
 */
export const confirmPasswordSchema = z
  .string()
  .min(1, PASSWORD_MESSAGES.confirmRequired)

/**
 * 비밀번호 + 확인 필드를 포함하는 스키마 생성 헬퍼
 *
 * @param passwordFieldName 비밀번호 필드명 (기본값: 'password')
 * @param confirmFieldName 확인 필드명 (기본값: 'confirmPassword')
 *
 * @example
 * const schema = z.object({
 *   email: emailSchema,
 *   ...createPasswordWithConfirmFields(),
 * }).pipe(withPasswordConfirmRefinement())
 */
export function createPasswordWithConfirmFields(
  passwordFieldName = 'password',
  confirmFieldName = 'confirmPassword'
) {
  return {
    [passwordFieldName]: strongPasswordSchema,
    [confirmFieldName]: confirmPasswordSchema,
  }
}

/**
 * 비밀번호 일치 검증 refinement 생성
 *
 * @param passwordField 비밀번호 필드명
 * @param confirmField 확인 필드명
 */
export function createPasswordMatchRefinement<T extends Record<string, unknown>>(
  passwordField: keyof T = 'password' as keyof T,
  confirmField: keyof T = 'confirmPassword' as keyof T
) {
  return (data: T) => data[passwordField] === data[confirmField]
}

/**
 * 비밀번호 일치 검증 refinement 옵션
 */
export const passwordMatchRefinementOptions = {
  message: PASSWORD_MESSAGES.mismatch,
  path: ['confirmPassword'],
} as const

/**
 * 새 비밀번호 일치 검증 refinement 옵션 (비밀번호 변경용)
 */
export const newPasswordMatchRefinementOptions = {
  message: PASSWORD_MESSAGES.mismatch,
  path: ['confirmPassword'],
} as const
