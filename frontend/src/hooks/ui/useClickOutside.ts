import { useEffect, type RefObject } from 'react'

/**
 * Custom hook to detect clicks outside of a referenced element
 * @param ref - Reference to the element to monitor
 * @param handler - Callback function to execute when click is detected outside
 * @param enabled - Optional flag to enable/disable the hook (default: true)
 */
export function useClickOutside<T extends HTMLElement>(
  ref: RefObject<T | null>,
  handler: () => void,
  enabled: boolean = true
): void {
  useEffect(() => {
    if (!enabled) return

    function handleClickOutside(event: MouseEvent) {
      if (ref.current && !ref.current.contains(event.target as Node)) {
        handler()
      }
    }

    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [ref, handler, enabled])
}
