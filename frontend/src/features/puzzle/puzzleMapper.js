const DIFFICULTY_LABELS = {
  EASY: 'Easy',
  MEDIUM: 'Medium',
  HARD: 'Hard',
}

function formatDate(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  return new Intl.DateTimeFormat(undefined, {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  }).format(date)
}

export function toPuzzleCard(puzzle) {
  return {
    id: puzzle.id ?? null,
    index: puzzle.id != null ? `#${puzzle.id}` : '',
    title: puzzle.title || puzzle.functionName || 'Untitled puzzle',
    content: puzzle.content ?? '',
    difficulty: DIFFICULTY_LABELS[puzzle.difficulty] ?? 'Medium',
    languages: (puzzle.languages ?? [])
      .map((language) => language.languageName)
      .filter(Boolean),
    tags: puzzle.tags ?? [],
    authorName: puzzle.authorName ?? '',
    createdLabel: formatDate(puzzle.createdAt),
    acceptance: '',
    showProof: false,
    community: {
      src: '/favicon.svg',
      name: puzzle.communityId != null ? `Community #${puzzle.communityId}` : 'Community',
      type: 'community',
    },
  }
}
