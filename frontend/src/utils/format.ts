/**
 * 날짜 문자열을 'yyyy-MM-dd hh:mm' 형식으로 변환
 */
export function formatDate(dateStr: string): string {
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

/**
 * 숫자를 한글 단위(억, 만)로 변환
 */
export function formatNumber(num: number): string {
  if (num >= 100000000) {
    return (num / 100000000).toFixed(1) + '억'
  } else if (num >= 10000) {
    return (num / 10000).toFixed(0) + '만'
  }
  return num.toLocaleString()
}
