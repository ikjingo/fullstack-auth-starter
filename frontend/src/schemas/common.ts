import { z } from 'zod'

/**
 * 공통 유효성 검증 패턴
 *
 * 재사용 가능한 Zod 스키마 패턴을 제공합니다.
 * auth.ts, nickname.ts 등에서 import하여 사용할 수 있습니다.
 */

// 이메일 패턴
export const emailSchema = z
  .string()
  .min(1, '이메일을 입력해주세요')
  .email('올바른 이메일 형식이 아닙니다')

// 비밀번호 패턴 (8자 이상, 영문+숫자)
export const passwordSchema = z
  .string()
  .min(1, '비밀번호를 입력해주세요')
  .min(8, '비밀번호는 8자 이상이어야 합니다')
  .regex(
    /^(?=.*[a-zA-Z])(?=.*\d)/,
    '비밀번호는 영문과 숫자를 포함해야 합니다'
  )

// 간단 비밀번호 패턴 (길이만 검증)
export const simplePasswordSchema = z
  .string()
  .min(1, '비밀번호를 입력해주세요')
  .min(8, '비밀번호는 8자 이상이어야 합니다')

// 닉네임 패턴 (2-12자, 한글/영문/숫자)
export const nicknameSchema = z
  .string()
  .min(2, '닉네임은 2자 이상이어야 합니다')
  .max(12, '닉네임은 12자 이하여야 합니다')
  .regex(/^[가-힣a-zA-Z0-9]+$/, '한글, 영문, 숫자만 사용 가능합니다')

// 인증번호 패턴 (6자리 숫자)
export const verificationCodePattern = z
  .string()
  .length(6, '인증번호는 6자리입니다')
  .regex(/^\d+$/, '숫자만 입력해주세요')

// 비밀번호 확인 리파인먼트 헬퍼
// 사용 예: createPasswordConfirmSchema(baseObjectSchema, 'password', 'confirmPassword')
export function createPasswordConfirmSchema<
  T extends z.ZodObject<z.ZodRawShape>
>(
  baseSchema: T,
  passwordField: keyof z.infer<T> = 'password' as keyof z.infer<T>,
  confirmField: keyof z.infer<T> = 'confirmPassword' as keyof z.infer<T>
) {
  return baseSchema.refine(
    (data) => data[passwordField] === data[confirmField],
    {
      message: '비밀번호가 일치하지 않습니다',
      path: [confirmField as string],
    }
  )
}

// 타입 추출
export type Email = z.infer<typeof emailSchema>
export type Password = z.infer<typeof passwordSchema>
export type Nickname = z.infer<typeof nicknameSchema>
export type VerificationCode = z.infer<typeof verificationCodePattern>
