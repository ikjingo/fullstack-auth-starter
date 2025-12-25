import { useState, useRef, useCallback } from 'react'
import { ChevronDown, Check } from 'lucide-react'
import { useClickOutside } from '@/hooks'
import { iconSm } from '@/utils'

// Base dropdown props
interface BaseDropdownProps<T extends string> {
  options: T[]
  icon: React.ReactNode
  getLabel: (value: T) => string
  className?: string
}

// Single select props
interface SingleSelectDropdownProps<T extends string> extends BaseDropdownProps<T> {
  mode: 'single'
  selected: T
  onChange: (value: T) => void
}

// Multi select props
interface MultiSelectDropdownProps<T extends string> extends BaseDropdownProps<T> {
  mode: 'multi'
  selected: T[]
  onChange: (values: T[]) => void
  placeholder: string
}

type FilterDropdownProps<T extends string> =
  | SingleSelectDropdownProps<T>
  | MultiSelectDropdownProps<T>

export function FilterDropdown<T extends string>(props: FilterDropdownProps<T>) {
  const [isOpen, setIsOpen] = useState(false)
  const dropdownRef = useRef<HTMLDivElement>(null)

  const closeDropdown = useCallback(() => setIsOpen(false), [])
  useClickOutside(dropdownRef, closeDropdown, isOpen)

  const { options, icon, getLabel, className = '' } = props

  // Determine display text
  const displayText = props.mode === 'single'
    ? getLabel(props.selected)
    : props.selected.length === 0
      ? props.placeholder
      : props.selected.length === 1
        ? getLabel(props.selected[0])
        : `${getLabel(props.selected[0])} 외 ${props.selected.length - 1}개`

  // Check if option is selected
  const isSelected = (option: T): boolean => {
    return props.mode === 'single'
      ? props.selected === option
      : props.selected.includes(option)
  }

  // Handle option selection
  const handleSelect = (option: T) => {
    if (props.mode === 'single') {
      props.onChange(option)
      setIsOpen(false)
    } else {
      const newSelected = props.selected.includes(option)
        ? props.selected.filter(item => item !== option)
        : [...props.selected, option]
      props.onChange(newSelected)
    }
  }

  const showPlaceholderStyle = props.mode === 'multi' && props.selected.length === 0

  return (
    <div ref={dropdownRef} className={`relative ${className}`}>
      <button
        type="button"
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center gap-2 h-9 px-3 rounded-md bg-muted/50 hover:bg-muted/80 transition-colors text-sm"
      >
        {icon}
        <span className={showPlaceholderStyle ? 'text-muted-foreground' : ''}>
          {displayText}
        </span>
        <ChevronDown
          className={`${iconSm} text-muted-foreground transition-transform ${isOpen ? 'rotate-180' : ''}`}
        />
      </button>

      {isOpen && (
        <div className="absolute top-full left-0 mt-1 z-50 min-w-[140px] bg-popover border rounded-md shadow-md p-1">
          {options.map((option) => (
            <button
              key={option}
              type="button"
              onClick={() => handleSelect(option)}
              className="flex items-center justify-between w-full px-3 py-2 text-sm rounded hover:bg-muted/50 transition-colors"
            >
              <span>{getLabel(option)}</span>
              {isSelected(option) && (
                <Check className={`${iconSm} text-primary`} />
              )}
            </button>
          ))}
        </div>
      )}
    </div>
  )
}

// Convenience exports for common use cases
export function SingleSelectDropdown<T extends string>(
  props: Omit<SingleSelectDropdownProps<T>, 'mode'>
) {
  return <FilterDropdown {...props} mode="single" />
}

export function MultiSelectDropdown<T extends string>(
  props: Omit<MultiSelectDropdownProps<T>, 'mode'>
) {
  return <FilterDropdown {...props} mode="multi" />
}
