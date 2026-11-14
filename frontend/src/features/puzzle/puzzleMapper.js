const DIFFICULTY_LABELS = {
  EASY: 'Easy',
  MEDIUM: 'Medium',
  HARD: 'Hard',
}

export function toPuzzleCard(puzzle) {
  return {
    index: puzzle.id != null ? `#${puzzle.id}` : '',
    title: puzzle.title || puzzle.functionName || 'Untitled puzzle',
    difficulty: DIFFICULTY_LABELS[puzzle.difficulty] ?? 'Medium',
    languages: [],
    tags: [],
    acceptance: '',
    showProof: false,
    community: {
      src: '/favicon.svg',
      name: puzzle.communityId != null ? `Community #${puzzle.communityId}` : 'Community',
      type: 'community',
    },
  }
}
