import { describe, it, expect } from 'vitest'
import { isUserRole, validateUserRole } from '../typeGuards'
import { UserRole } from '../constants'

describe('Type Guards', () => {
  describe('isUserRole', () => {
    it('should return true for valid user roles', () => {
      expect(isUserRole('USER')).toBe(true)
      expect(isUserRole('ADMIN')).toBe(true)
    })

    it('should return false for invalid values', () => {
      expect(isUserRole('SUPERUSER')).toBe(false)
      expect(isUserRole('GUEST')).toBe(false)
      expect(isUserRole(123)).toBe(false)
      expect(isUserRole(null)).toBe(false)
      expect(isUserRole(undefined)).toBe(false)
    })
  })
})

describe('Validation Functions', () => {
  describe('validateUserRole', () => {
    it('should return valid user role', () => {
      expect(validateUserRole('USER')).toBe('USER')
      expect(validateUserRole('ADMIN')).toBe('ADMIN')
    })

    it('should return default for invalid user role', () => {
      expect(validateUserRole('SUPERUSER')).toBe(UserRole.USER)
      expect(validateUserRole('GUEST')).toBe(UserRole.USER)
      expect(validateUserRole(null)).toBe(UserRole.USER)
      expect(validateUserRole(undefined)).toBe(UserRole.USER)
    })

    it('should use custom default', () => {
      expect(validateUserRole('INVALID', UserRole.ADMIN)).toBe(UserRole.ADMIN)
    })
  })
})
