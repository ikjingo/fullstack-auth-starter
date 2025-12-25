import { describe, it, expect } from 'vitest'
import {
  isBackendGameType,
  isFrontendGameType,
  isServiceType,
  isRarity,
  isSortType,
  isUserRole,
  toFrontendGameType,
  toBackendGameType,
  safeToFrontendGameType,
  safeToBackendGameType,
  toFrontendGameTypes,
  toBackendGameTypes,
  validateRarity,
  validateSortType,
  validateUserRole,
} from '../typeGuards'
import {
  BackendGameType,
  FrontendGameType,
  Rarity,
  SortType,
  UserRole,
} from '../constants'

describe('Type Guards', () => {
  describe('isBackendGameType', () => {
    it('should return true for valid backend game types', () => {
      expect(isBackendGameType('MAPLESTORY')).toBe(true)
      expect(isBackendGameType('LOSTARK')).toBe(true)
      expect(isBackendGameType('DICTIONARY')).toBe(true)
    })

    it('should return false for invalid values', () => {
      expect(isBackendGameType('INVALID')).toBe(false)
      expect(isBackendGameType('메이플스토리')).toBe(false)
      expect(isBackendGameType(123)).toBe(false)
      expect(isBackendGameType(null)).toBe(false)
      expect(isBackendGameType(undefined)).toBe(false)
    })
  })

  describe('isFrontendGameType', () => {
    it('should return true for valid frontend game types', () => {
      expect(isFrontendGameType('메이플스토리')).toBe(true)
      expect(isFrontendGameType('로스트아크')).toBe(true)
      expect(isFrontendGameType('한글사전')).toBe(true)
    })

    it('should return false for invalid values', () => {
      expect(isFrontendGameType('MAPLESTORY')).toBe(false)
      expect(isFrontendGameType('invalid')).toBe(false)
      expect(isFrontendGameType(123)).toBe(false)
    })
  })

  describe('isServiceType', () => {
    it('should return true for valid service types', () => {
      expect(isServiceType('MAPLESTORY')).toBe(true)
      expect(isServiceType('LOSTARK')).toBe(true)
      expect(isServiceType('DICTIONARY')).toBe(true)
    })

    it('should return false for invalid values', () => {
      expect(isServiceType('invalid')).toBe(false)
      expect(isServiceType('메이플스토리')).toBe(false)
      expect(isServiceType(123)).toBe(false)
    })
  })

  describe('isRarity', () => {
    it('should return true for valid rarities', () => {
      expect(isRarity('SSS')).toBe(true)
      expect(isRarity('SS')).toBe(true)
      expect(isRarity('S')).toBe(true)
      expect(isRarity('A')).toBe(true)
      expect(isRarity('B')).toBe(true)
      expect(isRarity('C')).toBe(true)
    })

    it('should return false for invalid values', () => {
      expect(isRarity('X')).toBe(false)
      expect(isRarity('D')).toBe(false)
      expect(isRarity(123)).toBe(false)
    })
  })

  describe('isSortType', () => {
    it('should return true for valid sort types', () => {
      expect(isSortType('SCORE_DESC')).toBe(true)
      expect(isSortType('UPDATED_DESC')).toBe(true)
      expect(isSortType('UPDATED_ASC')).toBe(true)
      expect(isSortType('SCORE_DESC_UPDATED_DESC')).toBe(true)
      expect(isSortType('SCORE_DESC_UPDATED_ASC')).toBe(true)
    })

    it('should return false for invalid values', () => {
      expect(isSortType('INVALID')).toBe(false)
      expect(isSortType('SCORE_ASC')).toBe(false)
      expect(isSortType(123)).toBe(false)
    })
  })

  describe('isUserRole', () => {
    it('should return true for valid user roles', () => {
      expect(isUserRole('USER')).toBe(true)
      expect(isUserRole('ADMIN')).toBe(true)
    })

    it('should return false for invalid values', () => {
      expect(isUserRole('SUPERUSER')).toBe(false)
      expect(isUserRole('GUEST')).toBe(false)
      expect(isUserRole(123)).toBe(false)
    })
  })
})

describe('Type Conversion Functions', () => {
  describe('toFrontendGameType', () => {
    it('should convert backend to frontend game type', () => {
      expect(toFrontendGameType(BackendGameType.MAPLESTORY)).toBe(FrontendGameType.MAPLESTORY)
      expect(toFrontendGameType(BackendGameType.LOSTARK)).toBe(FrontendGameType.LOSTARK)
      expect(toFrontendGameType(BackendGameType.DICTIONARY)).toBe(FrontendGameType.DICTIONARY)
    })

    it('should throw for unknown backend game type', () => {
      expect(() => toFrontendGameType('UNKNOWN' as never)).toThrow('Unknown backend game type')
    })
  })

  describe('toBackendGameType', () => {
    it('should convert frontend to backend game type', () => {
      expect(toBackendGameType(FrontendGameType.MAPLESTORY)).toBe(BackendGameType.MAPLESTORY)
      expect(toBackendGameType(FrontendGameType.LOSTARK)).toBe(BackendGameType.LOSTARK)
      expect(toBackendGameType(FrontendGameType.DICTIONARY)).toBe(BackendGameType.DICTIONARY)
    })

    it('should throw for unknown frontend game type', () => {
      expect(() => toBackendGameType('unknown' as never)).toThrow('Unknown frontend game type')
    })
  })

  describe('safeToFrontendGameType', () => {
    it('should return frontend type for valid backend type', () => {
      expect(safeToFrontendGameType('MAPLESTORY')).toBe('메이플스토리')
      expect(safeToFrontendGameType('LOSTARK')).toBe('로스트아크')
      expect(safeToFrontendGameType('DICTIONARY')).toBe('한글사전')
    })

    it('should return null for invalid input', () => {
      expect(safeToFrontendGameType('INVALID')).toBe(null)
      expect(safeToFrontendGameType('메이플스토리')).toBe(null)
      expect(safeToFrontendGameType(123)).toBe(null)
      expect(safeToFrontendGameType(null)).toBe(null)
    })
  })

  describe('safeToBackendGameType', () => {
    it('should return backend type for valid frontend type', () => {
      expect(safeToBackendGameType('메이플스토리')).toBe('MAPLESTORY')
      expect(safeToBackendGameType('로스트아크')).toBe('LOSTARK')
      expect(safeToBackendGameType('한글사전')).toBe('DICTIONARY')
    })

    it('should return null for invalid input', () => {
      expect(safeToBackendGameType('invalid')).toBe(null)
      expect(safeToBackendGameType('MAPLESTORY')).toBe(null)
      expect(safeToBackendGameType(123)).toBe(null)
      expect(safeToBackendGameType(null)).toBe(null)
    })
  })

  describe('toFrontendGameTypes', () => {
    it('should convert array of backend types to frontend types', () => {
      const result = toFrontendGameTypes([BackendGameType.MAPLESTORY, BackendGameType.LOSTARK])
      expect(result).toEqual([FrontendGameType.MAPLESTORY, FrontendGameType.LOSTARK])
    })

    it('should handle all game types', () => {
      const result = toFrontendGameTypes([
        BackendGameType.MAPLESTORY,
        BackendGameType.LOSTARK,
        BackendGameType.DICTIONARY,
      ])
      expect(result).toEqual([
        FrontendGameType.MAPLESTORY,
        FrontendGameType.LOSTARK,
        FrontendGameType.DICTIONARY,
      ])
    })

    it('should return empty array for empty input', () => {
      expect(toFrontendGameTypes([])).toEqual([])
    })
  })

  describe('toBackendGameTypes', () => {
    it('should convert array of frontend types to backend types', () => {
      const result = toBackendGameTypes([FrontendGameType.MAPLESTORY, FrontendGameType.LOSTARK])
      expect(result).toEqual([BackendGameType.MAPLESTORY, BackendGameType.LOSTARK])
    })

    it('should handle all game types', () => {
      const result = toBackendGameTypes([
        FrontendGameType.MAPLESTORY,
        FrontendGameType.LOSTARK,
        FrontendGameType.DICTIONARY,
      ])
      expect(result).toEqual([
        BackendGameType.MAPLESTORY,
        BackendGameType.LOSTARK,
        BackendGameType.DICTIONARY,
      ])
    })

    it('should return empty array for empty input', () => {
      expect(toBackendGameTypes([])).toEqual([])
    })
  })
})

describe('Validation Functions', () => {
  describe('validateRarity', () => {
    it('should return valid rarity', () => {
      expect(validateRarity('SSS')).toBe('SSS')
      expect(validateRarity('SS')).toBe('SS')
      expect(validateRarity('S')).toBe('S')
      expect(validateRarity('A')).toBe('A')
      expect(validateRarity('B')).toBe('B')
      expect(validateRarity('C')).toBe('C')
    })

    it('should return default for invalid rarity', () => {
      expect(validateRarity('X')).toBe(Rarity.C)
      expect(validateRarity('D')).toBe(Rarity.C)
      expect(validateRarity(null)).toBe(Rarity.C)
      expect(validateRarity(undefined)).toBe(Rarity.C)
    })

    it('should use custom default', () => {
      expect(validateRarity('X', Rarity.S)).toBe(Rarity.S)
      expect(validateRarity('INVALID', Rarity.SSS)).toBe(Rarity.SSS)
    })
  })

  describe('validateSortType', () => {
    it('should return valid sort type', () => {
      expect(validateSortType('SCORE_DESC')).toBe('SCORE_DESC')
      expect(validateSortType('UPDATED_DESC')).toBe('UPDATED_DESC')
      expect(validateSortType('UPDATED_ASC')).toBe('UPDATED_ASC')
    })

    it('should return default for invalid sort type', () => {
      expect(validateSortType('INVALID')).toBe(SortType.SCORE_DESC)
      expect(validateSortType('SCORE_ASC')).toBe(SortType.SCORE_DESC)
      expect(validateSortType(null)).toBe(SortType.SCORE_DESC)
    })

    it('should use custom default', () => {
      expect(validateSortType('INVALID', SortType.UPDATED_DESC)).toBe(SortType.UPDATED_DESC)
      expect(validateSortType('X', SortType.UPDATED_ASC)).toBe(SortType.UPDATED_ASC)
    })
  })

  describe('validateUserRole', () => {
    it('should return valid user role', () => {
      expect(validateUserRole('USER')).toBe('USER')
      expect(validateUserRole('ADMIN')).toBe('ADMIN')
    })

    it('should return default for invalid user role', () => {
      expect(validateUserRole('SUPERUSER')).toBe(UserRole.USER)
      expect(validateUserRole('GUEST')).toBe(UserRole.USER)
      expect(validateUserRole(null)).toBe(UserRole.USER)
    })

    it('should use custom default', () => {
      expect(validateUserRole('INVALID', UserRole.ADMIN)).toBe(UserRole.ADMIN)
    })
  })
})
