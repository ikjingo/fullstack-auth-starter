import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { renderHook, act } from '@testing-library/react'
import { useDebounce, useDebouncedCallback } from '../'

describe('useDebounce', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  describe('useDebounce hook', () => {
    it('초기값을 즉시 반환해야 한다', () => {
      const { result } = renderHook(() => useDebounce('initial', 500))
      expect(result.current).toBe('initial')
    })

    it('지연 시간 후에 값이 업데이트되어야 한다', () => {
      const { result, rerender } = renderHook(
        ({ value, delay }) => useDebounce(value, delay),
        { initialProps: { value: 'initial', delay: 500 } }
      )

      expect(result.current).toBe('initial')

      rerender({ value: 'updated', delay: 500 })
      expect(result.current).toBe('initial')

      act(() => {
        vi.advanceTimersByTime(500)
      })
      expect(result.current).toBe('updated')
    })

    it('지연 시간 전에 값이 변경되면 타이머가 리셋되어야 한다', () => {
      const { result, rerender } = renderHook(
        ({ value, delay }) => useDebounce(value, delay),
        { initialProps: { value: 'initial', delay: 500 } }
      )

      rerender({ value: 'first', delay: 500 })
      act(() => {
        vi.advanceTimersByTime(300)
      })
      expect(result.current).toBe('initial')

      rerender({ value: 'second', delay: 500 })
      act(() => {
        vi.advanceTimersByTime(300)
      })
      expect(result.current).toBe('initial')

      act(() => {
        vi.advanceTimersByTime(200)
      })
      expect(result.current).toBe('second')
    })

    it('숫자 타입도 처리해야 한다', () => {
      const { result, rerender } = renderHook(
        ({ value, delay }) => useDebounce(value, delay),
        { initialProps: { value: 0, delay: 300 } }
      )

      expect(result.current).toBe(0)

      rerender({ value: 42, delay: 300 })
      act(() => {
        vi.advanceTimersByTime(300)
      })
      expect(result.current).toBe(42)
    })

    it('객체 타입도 처리해야 한다', () => {
      const initialObj = { name: 'test' }
      const updatedObj = { name: 'updated' }

      const { result, rerender } = renderHook(
        ({ value, delay }) => useDebounce(value, delay),
        { initialProps: { value: initialObj, delay: 300 } }
      )

      expect(result.current).toEqual(initialObj)

      rerender({ value: updatedObj, delay: 300 })
      act(() => {
        vi.advanceTimersByTime(300)
      })
      expect(result.current).toEqual(updatedObj)
    })

    it('지연 시간이 변경되면 새 지연 시간을 사용해야 한다', () => {
      const { result, rerender } = renderHook(
        ({ value, delay }) => useDebounce(value, delay),
        { initialProps: { value: 'initial', delay: 500 } }
      )

      rerender({ value: 'updated', delay: 1000 })
      act(() => {
        vi.advanceTimersByTime(500)
      })
      expect(result.current).toBe('initial')

      act(() => {
        vi.advanceTimersByTime(500)
      })
      expect(result.current).toBe('updated')
    })

    it('0ms 지연도 처리해야 한다', () => {
      const { result, rerender } = renderHook(
        ({ value, delay }) => useDebounce(value, delay),
        { initialProps: { value: 'initial', delay: 0 } }
      )

      rerender({ value: 'updated', delay: 0 })
      act(() => {
        vi.advanceTimersByTime(0)
      })
      expect(result.current).toBe('updated')
    })
  })

  describe('useDebouncedCallback hook', () => {
    it('콜백이 지연 후에 호출되어야 한다', () => {
      const callback = vi.fn()
      const { result } = renderHook(() => useDebouncedCallback(callback, 500))

      result.current('arg1')
      expect(callback).not.toHaveBeenCalled()

      act(() => {
        vi.advanceTimersByTime(500)
      })
      expect(callback).toHaveBeenCalledWith('arg1')
      expect(callback).toHaveBeenCalledTimes(1)
    })

    it('연속 호출 시 마지막 호출만 실행되어야 한다', () => {
      const callback = vi.fn()
      const { result } = renderHook(() => useDebouncedCallback(callback, 500))

      result.current('first')
      act(() => {
        vi.advanceTimersByTime(200)
      })

      result.current('second')
      act(() => {
        vi.advanceTimersByTime(200)
      })

      result.current('third')
      act(() => {
        vi.advanceTimersByTime(500)
      })

      expect(callback).toHaveBeenCalledTimes(1)
      expect(callback).toHaveBeenCalledWith('third')
    })

    it('여러 인자를 전달할 수 있어야 한다', () => {
      const callback = vi.fn()
      const { result } = renderHook(() => useDebouncedCallback(callback, 300))

      result.current('arg1', 'arg2', 123)
      act(() => {
        vi.advanceTimersByTime(300)
      })

      expect(callback).toHaveBeenCalledWith('arg1', 'arg2', 123)
    })

    it('언마운트 시 타이머가 정리되어야 한다', () => {
      const callback = vi.fn()
      const { result, unmount } = renderHook(() => useDebouncedCallback(callback, 500))

      result.current('arg')
      unmount()

      act(() => {
        vi.advanceTimersByTime(500)
      })

      expect(callback).not.toHaveBeenCalled()
    })

    it('콜백이 변경되면 새 콜백으로 다시 호출해야 한다', () => {
      const callback1 = vi.fn()
      const callback2 = vi.fn()

      const { result, rerender } = renderHook(
        ({ callback, delay }) => useDebouncedCallback(callback, delay),
        { initialProps: { callback: callback1, delay: 500 } }
      )

      // 첫 번째 콜백으로 호출
      result.current('arg1')

      // 콜백 변경 (useCallback이 새 함수를 생성, 이전 타이머는 old callback 참조)
      rerender({ callback: callback2, delay: 500 })

      // 새 콜백으로 다시 호출
      result.current('arg2')

      act(() => {
        vi.advanceTimersByTime(500)
      })

      // callback2가 마지막 호출 인자로 호출됨
      expect(callback2).toHaveBeenCalledWith('arg2')
      expect(callback2).toHaveBeenCalledTimes(1)
    })

    it('지연 시간 0ms도 처리해야 한다', () => {
      const callback = vi.fn()
      const { result } = renderHook(() => useDebouncedCallback(callback, 0))

      result.current()
      act(() => {
        vi.advanceTimersByTime(0)
      })

      expect(callback).toHaveBeenCalled()
    })
  })

  describe('엣지 케이스', () => {
    it('null 값도 디바운스해야 한다', () => {
      const { result, rerender } = renderHook(
        ({ value, delay }) => useDebounce(value, delay),
        { initialProps: { value: 'initial' as string | null, delay: 300 } }
      )

      rerender({ value: null, delay: 300 })
      act(() => {
        vi.advanceTimersByTime(300)
      })
      expect(result.current).toBeNull()
    })

    it('undefined 값도 디바운스해야 한다', () => {
      const { result, rerender } = renderHook(
        ({ value, delay }) => useDebounce(value, delay),
        { initialProps: { value: 'initial' as string | undefined, delay: 300 } }
      )

      rerender({ value: undefined, delay: 300 })
      act(() => {
        vi.advanceTimersByTime(300)
      })
      expect(result.current).toBeUndefined()
    })

    it('빈 문자열도 디바운스해야 한다', () => {
      const { result, rerender } = renderHook(
        ({ value, delay }) => useDebounce(value, delay),
        { initialProps: { value: 'initial', delay: 300 } }
      )

      rerender({ value: '', delay: 300 })
      act(() => {
        vi.advanceTimersByTime(300)
      })
      expect(result.current).toBe('')
    })
  })
})
