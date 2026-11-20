import { toPuzzleCard } from '../puzzle/puzzleMapper'

// Ảnh bìa đồ họa mặc định (SVG trừu tượng) khi cuộc thi chưa thiết lập banner riêng
const DEFAULT_BANNER =
  'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 420 150" preserveAspectRatio="xMidYMid slice"><defs><linearGradient id="g1" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" stop-color="%230b1224"/><stop offset="50%" stop-color="%23131c38"/><stop offset="100%" stop-color="%23090a0f"/></linearGradient><linearGradient id="ac" x1="0%" y1="100%" x2="100%" y2="0%"><stop offset="0%" stop-color="%232554e8" stop-opacity="0.35"/><stop offset="100%" stop-color="%236f93ff" stop-opacity="0.75"/></linearGradient></defs><rect width="420" height="150" fill="url(%23g1)"/><path d="M-20 150 L110 50 L200 120 L310 30 L440 150 Z" fill="url(%23ac)"/><path d="M0 150 L150 70 L250 130 L380 40 L450 150 Z" fill="%236f93ff" fill-opacity="0.12"/></svg>'

// Định dạng thời gian đếm ngược dựa trên mốc thời gian bắt đầu hoặc kết thúc của cuộc thi
export function formatContestCountdown(contest, currentTimestamp = Date.now()) {
  if (!contest) return ''
  const status = (contest.status || '').toUpperCase()
  if (status === 'CLOSED') return ''

  const startTime = contest.startTime ? new Date(contest.startTime).getTime() : NaN
  const endTime = contest.endTime ? new Date(contest.endTime).getTime() : NaN

  if (!Number.isFinite(startTime) && !Number.isFinite(endTime)) return ''

  const now = currentTimestamp

  // Nếu hiện tại đã quá mốc kết thúc, cuộc thi kết thúc
  if (Number.isFinite(endTime) && now >= endTime) {
    return ''
  }

  const pad = (n) => String(n).padStart(2, '0')

  // Trường hợp cuộc thi chưa bắt đầu (UPCOMING)
  if (Number.isFinite(startTime) && now < startTime) {
    const diffMs = startTime - now
    const totalSeconds = Math.floor(diffMs / 1000)
    const days = Math.floor(totalSeconds / 86400)
    const hours = Math.floor((totalSeconds % 86400) / 3600)
    const minutes = Math.floor((totalSeconds % 3600) / 60)
    const seconds = totalSeconds % 60

    if (days > 0) {
      return `${pad(days)}d ${pad(hours)}:${pad(minutes)}:${pad(seconds)}`
    }
    return `${pad(hours)}:${pad(minutes)}:${pad(seconds)}`
  }

  // Trường hợp cuộc thi đang diễn ra (OPEN): đếm ngược đến endTime
  if (Number.isFinite(endTime) && now < endTime) {
    const diffMs = endTime - now
    const totalSeconds = Math.floor(diffMs / 1000)
    const days = Math.floor(totalSeconds / 86400)
    const hours = Math.floor((totalSeconds % 86400) / 3600)
    const minutes = Math.floor((totalSeconds % 3600) / 60)
    const seconds = totalSeconds % 60

    if (days > 0) {
      return `${pad(days)}d ${pad(hours)}:${pad(minutes)}:${pad(seconds)} REMAINING`
    }
    return `${pad(hours)}:${pad(minutes)}:${pad(seconds)} REMAINING`
  }

  return ''
}

// Chuyển đổi ContestDTO sang dạng props cho ContestCard.vue
export function toContestCard(contest) {
  const status = (contest.status || 'OPEN').toUpperCase()

  return {
    id: contest.id ?? null,
    title: contest.title || 'Untitled Contest',
    status,
    countdown: formatContestCountdown(contest),
    community: {
      // Để src rỗng nếu backend chưa có ảnh đại diện cộng đồng, Avatar.vue sẽ tự xử lý avatar monogram
      src: contest.communityImageUrl || '',
      name: contest.communityName || (contest.communityId != null ? `Community #${contest.communityId}` : 'Community'),
      type: 'community',
    },
    registeredLabel: 'Registered:',
    registeredCount: `${Number(contest.registeredCount || 0).toLocaleString()} devs`,
    puzzleCount: contest.puzzleCount ?? 0,
    actionLabel: status === 'CLOSED' ? 'View Standings' : status === 'OPEN' ? 'Join Contest' : 'Register Now',
    image: contest.banner || '',
  }
}

// Chuyển đổi ContestDTO sang model hiển thị chi tiết cho ContestDetailPage.vue
export function toContestDetail(contest) {
  const status = (contest.status || 'OPEN').toUpperCase()
  const puzzleCount = contest.puzzleCount ?? 0

  return {
    id: contest.id ?? null,
    title: contest.title || 'Untitled Contest',
    status,
    startTime: contest.startTime ?? null,
    endTime: contest.endTime ?? null,
    countdown: formatContestCountdown(contest),
    community: {
      // Để src rỗng nếu backend chưa có ảnh đại diện cộng đồng, Avatar.vue sẽ tự xử lý avatar monogram
      src: contest.communityImageUrl || '',
      name: contest.communityName || (contest.communityId != null ? `Community #${contest.communityId}` : 'Community'),
      type: 'community',
    },
    registeredCount: `${Number(contest.registeredCount || 0).toLocaleString()} devs`,
    puzzleCount,
    maxScore: `${puzzleCount * 100} Max Score`,
    description: contest.content || '',
    image: contest.banner || '',
  }
}

// Chuyển đổi ContestPuzzleDTO sang model hiển thị cho PuzzleCard trên ContestDetailPage
export function toContestPuzzleCard(puzzle) {
  const baseCard = toPuzzleCard(puzzle)
  const points = Number(puzzle.points || 0)
  const scoreTone = points <= 100 ? 'easy' : points <= 300 ? 'medium' : 'hard'
  const solved = Boolean(puzzle.solved)

  return {
    ...baseCard,
    points,
    scoreTone,
    scoreBadge: `${points} pts`,
    solved,
    solveLabel: solved ? 'Solved' : 'Solve',
  }
}
