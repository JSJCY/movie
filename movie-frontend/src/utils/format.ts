export function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return d.toLocaleDateString('zh-CN')
}

export function formatDateTime(dateStr: string): string {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return d.toLocaleString('zh-CN')
}

export function getWatchStatusText(status: string): string {
  const map: Record<string, string> = {
    WANT_TO_WATCH: '想看',
    WATCHING: '在看',
    WATCHED: '看过',
  }
  return map[status] || status
}

export function getWatchStatusType(status: string): 'primary' | 'success' | 'warning' {
  const map: Record<string, 'primary' | 'success' | 'warning'> = {
    WANT_TO_WATCH: 'primary',
    WATCHING: 'warning',
    WATCHED: 'success',
  }
  return map[status] || 'primary'
}
