import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { renderHook } from '@testing-library/react'
import type { RefObject } from 'react'
import { useClickOutside } from '../'

describe('useClickOutside', () => {
  let container: HTMLDivElement
  let targetElement: HTMLDivElement
  let outsideElement: HTMLDivElement

  beforeEach(() => {
    // DOM 요소 생성
    container = document.createElement('div')
    targetElement = document.createElement('div')
    outsideElement = document.createElement('div')

    targetElement.setAttribute('data-testid', 'target')
    outsideElement.setAttribute('data-testid', 'outside')

    container.appendChild(targetElement)
    container.appendChild(outsideElement)
    document.body.appendChild(container)
  })

  afterEach(() => {
    document.body.removeChild(container)
    vi.clearAllMocks()
  })

  // ref를 생성하는 헬퍼 함수
  function createRefWithElement<T extends HTMLElement>(element: T): RefObject<T> {
    return { current: element }
  }

  it('외부 클릭 시 핸들러가 호출되어야 한다', () => {
    const handler = vi.fn()
    const ref = createRefWithElement(targetElement)

    renderHook(() => useClickOutside(ref, handler))

    // 외부 요소 클릭
    outsideElement.dispatchEvent(new MouseEvent('mousedown', { bubbles: true }))

    expect(handler).toHaveBeenCalledTimes(1)
  })

  it('내부 클릭 시 핸들러가 호출되지 않아야 한다', () => {
    const handler = vi.fn()
    const ref = createRefWithElement(targetElement)

    renderHook(() => useClickOutside(ref, handler))

    // 타겟 요소 내부 클릭
    targetElement.dispatchEvent(new MouseEvent('mousedown', { bubbles: true }))

    expect(handler).not.toHaveBeenCalled()
  })

  it('enabled가 false일 때 핸들러가 호출되지 않아야 한다', () => {
    const handler = vi.fn()
    const ref = createRefWithElement(targetElement)

    renderHook(() => useClickOutside(ref, handler, false))

    // 외부 요소 클릭
    outsideElement.dispatchEvent(new MouseEvent('mousedown', { bubbles: true }))

    expect(handler).not.toHaveBeenCalled()
  })

  it('enabled가 true일 때 핸들러가 호출되어야 한다', () => {
    const handler = vi.fn()
    const ref = createRefWithElement(targetElement)

    renderHook(() => useClickOutside(ref, handler, true))

    // 외부 요소 클릭
    outsideElement.dispatchEvent(new MouseEvent('mousedown', { bubbles: true }))

    expect(handler).toHaveBeenCalledTimes(1)
  })

  it('enabled 상태가 변경되면 리스너가 업데이트되어야 한다', () => {
    const handler = vi.fn()
    const ref = createRefWithElement(targetElement)

    const { rerender } = renderHook(
      ({ enabled }) => useClickOutside(ref, handler, enabled),
      { initialProps: { enabled: false } }
    )

    // 비활성화 상태에서 클릭
    outsideElement.dispatchEvent(new MouseEvent('mousedown', { bubbles: true }))
    expect(handler).not.toHaveBeenCalled()

    // 활성화
    rerender({ enabled: true })
    outsideElement.dispatchEvent(new MouseEvent('mousedown', { bubbles: true }))
    expect(handler).toHaveBeenCalledTimes(1)

    // 다시 비활성화
    rerender({ enabled: false })
    outsideElement.dispatchEvent(new MouseEvent('mousedown', { bubbles: true }))
    expect(handler).toHaveBeenCalledTimes(1) // 여전히 1번
  })

  it('언마운트 시 이벤트 리스너가 제거되어야 한다', () => {
    const handler = vi.fn()
    const ref = createRefWithElement(targetElement)

    const { unmount } = renderHook(() => useClickOutside(ref, handler))

    unmount()

    // 언마운트 후 클릭
    outsideElement.dispatchEvent(new MouseEvent('mousedown', { bubbles: true }))

    expect(handler).not.toHaveBeenCalled()
  })

  it('ref.current가 null일 때 핸들러가 호출되지 않아야 한다', () => {
    const handler = vi.fn()
    const ref: RefObject<HTMLDivElement | null> = { current: null }

    renderHook(() => useClickOutside(ref, handler))

    // 외부 요소 클릭
    outsideElement.dispatchEvent(new MouseEvent('mousedown', { bubbles: true }))

    expect(handler).not.toHaveBeenCalled()
  })

  it('핸들러가 변경되면 새 핸들러가 사용되어야 한다', () => {
    const handler1 = vi.fn()
    const handler2 = vi.fn()
    const ref = createRefWithElement(targetElement)

    const { rerender } = renderHook(
      ({ handler }) => useClickOutside(ref, handler),
      { initialProps: { handler: handler1 } }
    )

    outsideElement.dispatchEvent(new MouseEvent('mousedown', { bubbles: true }))
    expect(handler1).toHaveBeenCalledTimes(1)
    expect(handler2).not.toHaveBeenCalled()

    rerender({ handler: handler2 })

    outsideElement.dispatchEvent(new MouseEvent('mousedown', { bubbles: true }))
    expect(handler1).toHaveBeenCalledTimes(1)
    expect(handler2).toHaveBeenCalledTimes(1)
  })

  describe('중첩 요소', () => {
    let nestedElement: HTMLDivElement

    beforeEach(() => {
      nestedElement = document.createElement('div')
      nestedElement.setAttribute('data-testid', 'nested')
      targetElement.appendChild(nestedElement)
    })

    it('중첩된 자식 요소 클릭 시 핸들러가 호출되지 않아야 한다', () => {
      const handler = vi.fn()
      const ref = createRefWithElement(targetElement)

      renderHook(() => useClickOutside(ref, handler))

      // 중첩된 자식 요소 클릭
      nestedElement.dispatchEvent(new MouseEvent('mousedown', { bubbles: true }))

      expect(handler).not.toHaveBeenCalled()
    })
  })

  describe('다중 인스턴스', () => {
    it('여러 useClickOutside 인스턴스가 독립적으로 동작해야 한다', () => {
      const handler1 = vi.fn()
      const handler2 = vi.fn()

      const element1 = document.createElement('div')
      const element2 = document.createElement('div')
      container.appendChild(element1)
      container.appendChild(element2)

      const ref1 = createRefWithElement(element1)
      const ref2 = createRefWithElement(element2)

      renderHook(() => {
        useClickOutside(ref1, handler1)
        useClickOutside(ref2, handler2)
      })

      // element1 내부 클릭 - handler1만 호출되지 않아야 함
      element1.dispatchEvent(new MouseEvent('mousedown', { bubbles: true }))
      expect(handler1).not.toHaveBeenCalled()
      expect(handler2).toHaveBeenCalledTimes(1)
    })
  })

  describe('mousedown 이벤트', () => {
    it('mousedown 이벤트에만 반응해야 한다', () => {
      const handler = vi.fn()
      const ref = createRefWithElement(targetElement)

      renderHook(() => useClickOutside(ref, handler))

      // click 이벤트
      outsideElement.dispatchEvent(new MouseEvent('click', { bubbles: true }))
      expect(handler).not.toHaveBeenCalled()

      // mouseup 이벤트
      outsideElement.dispatchEvent(new MouseEvent('mouseup', { bubbles: true }))
      expect(handler).not.toHaveBeenCalled()

      // mousedown 이벤트
      outsideElement.dispatchEvent(new MouseEvent('mousedown', { bubbles: true }))
      expect(handler).toHaveBeenCalledTimes(1)
    })
  })
})
