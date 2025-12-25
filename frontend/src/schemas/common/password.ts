/**
 * 공통 비밀번호 스키마
 *
 * 비밀번호 관련 유효성 검증 스키마를 중앙화합니다.
 */
import { z } from 'zod'

/**
 * 비밀번호 유효성 검증 정규식
 * - 최소 하나의 영문자
 * - 최소 하나의 숫자
 */
export const PASSWORD_REGEX = /^(?=.*[a-zA-Z])(?=.*\d)/

/**
 * 비밀번호 에러 메시지
 */
export const PASSWORD_MESSAGES = {
  required: '비밀번호를 입력해주세요',
  minLength: '비밀번호는 8자 이상이어야 합니다',
  pattern: '비밀번호는 영문과 숫자를 포함해야 합니다',
  confirmRequired: '비밀번호 확인을 입력해주세요',
  mismatch: '비밀번호가 일치하지 않습니다',
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
 */
export const strongPasswordSchema = z
  .string()
  .min(1, PASSWORD_MESSAGES.required)
  .min(8, PASSWORD_MESSAGES.minLength)
  .regex(PASSWORD_REGEX, PASSWORD_MESSAGES.pattern)

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
