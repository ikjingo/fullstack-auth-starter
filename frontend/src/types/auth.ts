import { z } from 'zod'
import { loginFormSchema, registerFormSchema, userSchema } from '@/schemas/auth'

// Zod 스키마에서 타입 추출 (Single Source of Truth)
export type LoginFormData = z.infer<typeof loginFormSchema>
export type RegisterFormData = z.infer<typeof registerFormSchema>
export type User = z.infer<typeof userSchema>
