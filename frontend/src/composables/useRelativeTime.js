const DATE_FORMAT_OPTIONS = { month: 'short', day: 'numeric', year: 'numeric' }

export function formatRelativeTime(value) {
  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return ''
  }

  const diffSeconds = Math.max(0, Math.floor((Date.now() - date.getTime()) / 1000))

  if (diffSeconds < 60) {
    return 'now'
  }

  const minutes = Math.floor(diffSeconds / 60)
  if (minutes < 60) {
    return `${minutes}m`
  }

  const hours = Math.floor(minutes / 60)
  if (hours < 24) {
    return `${hours}h`
  }

  const days = Math.floor(hours / 24)
  if (days < 7) {
    return `${days}d`
  }

  const weeks = Math.floor(days / 7)
  if (weeks < 5) {
    return `${weeks}w`
  }

  return new Intl.DateTimeFormat('en-US', DATE_FORMAT_OPTIONS).format(date)
}

export function formatFullDate(value) {
  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return ''
  }

  return new Intl.DateTimeFormat('en-US', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(date)
}
