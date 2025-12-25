import { z } from 'zod'
import {
  loginPasswordSchema,
  strongPasswordSchema,
  confirmPasswordSchema,
  PASSWORD_MESSAGES,
} from './common/password'

// ============================================================================
// 공통 필드 스키마
// ============================================================================

/** 이메일 스키마 */
const emailSchema = z
  .string()
  .min(1, '이메일을 입력해주세요')
  .email('올바른 이메일 형식이 아닙니다')

/** 닉네임 스키마 */
const nicknameSchema = z
  .string()
  .min(2, '닉네임은 2자 이상이어야 합니다')
  .max(12, '닉네임은 12자 이하여야 합니다')
  .regex(/^[가-힣a-zA-Z0-9]+$/, '한글, 영문, 숫자만 사용 가능합니다')

// ============================================================================
// 인증 폼 스키마
// ============================================================================

// 로그인 폼 스키마
export const loginFormSchema = z.object({
  email: emailSchema,
  password: loginPasswordSchema,
})

// 회원가입 폼 스키마
export const registerFormSchema = z
  .object({
    email: emailSchema,
    password: strongPasswordSchema,
    confirmPassword: confirmPasswordSchema,
    nickname: nicknameSchema,
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: PASSWORD_MESSAGES.mismatch,
    path: ['confirmPassword'],
  })

// 사용자 역할 스키마
export const userRoleSchema = z.enum(['USER', 'ADMIN'])

// 사용자 정보 스키마
export const userSchema = z.object({
  id: z.string(),
  email: z.string().email(),
  nickname: z.string(),
  role: userRoleSchema,
})

// 비밀번호 찾기 폼 스키마
export const forgotPasswordFormSchema = z.object({
  email: emailSchema,
})

// 인증번호 검증 스키마
export const verificationCodeSchema = z.object({
  code: z
    .string()
    .length(6, '인증번호는 6자리입니다')
    .regex(/^\d+$/, '숫자만 입력해주세요'),
})

// 비밀번호 설정 스키마 (소셜 로그인 사용자용)
export const setPasswordFormSchema = z
  .object({
    password: strongPasswordSchema,
    confirmPassword: confirmPasswordSchema,
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: PASSWORD_MESSAGES.mismatch,
    path: ['confirmPassword'],
  })

// 닉네임 수정 스키마
export const updateNicknameFormSchema = z.object({
  nickname: z
    .string()
    .min(2, '닉네임은 2자 이상이어야 합니다')
    .max(20, '닉네임은 20자 이하여야 합니다'),
})

// 비밀번호 변경 스키마
export const changePasswordFormSchema = z
  .object({
    currentPassword: z
      .string()
      .min(1, '현재 비밀번호를 입력해주세요'),
    newPassword: strongPasswordSchema,
    confirmPassword: confirmPasswordSchema,
  })
  .refine((data) => data.newPassword === data.confirmPassword, {
    message: PASSWORD_MESSAGES.mismatch,
    path: ['confirmPassword'],
  })

// ============================================================================
// 타입 추출
// ============================================================================

export type LoginFormSchema = z.infer<typeof loginFormSchema>
export type RegisterFormSchema = z.infer<typeof registerFormSchema>
export type UserSchema = z.infer<typeof userSchema>
export type ForgotPasswordFormSchema = z.infer<typeof forgotPasswordFormSchema>
export type VerificationCodeSchema = z.infer<typeof verificationCodeSchema>
export type SetPasswordFormSchema = z.infer<typeof setPasswordFormSchema>
export type UpdateNicknameFormSchema = z.infer<typeof updateNicknameFormSchema>
export type ChangePasswordFormSchema = z.infer<typeof changePasswordFormSchema>
