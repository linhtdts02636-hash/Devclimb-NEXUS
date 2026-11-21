// Dữ liệu mẫu cuộc thi cho DevClimb Nexus (tuân thủ nguyên tắc 5 của DOCS/DESIGN.md: "Anything invented is labelled 'sample data for illustration'").

// 5 bài toán mẫu của cuộc thi phục vụ cho PuzzleCatalog trên ContestDetailPage
export const sampleContestPuzzles = [
  {
    id: 101,
    index: '#A',
    title: 'Prefix XOR Queries',
    content:
      'Given an array of integers and multiple range XOR queries, find the cumulative parity distribution across contiguous segmented intervals without precomputation overflow.',
    difficulty: 'Easy',
    languages: ['java', 'python', 'cpp'],
    tags: ['bit-manipulation', 'prefix-sums'],
    authorName: 'Dr. K. Varma',
    createdLabel: 'Problem A',
    acceptance: '99.2%',
    community: {
      src: '',
      name: 'MIT CSAIL Coding Guild',
      type: 'community',
    },
    solveLabel: 'Solve (100 pts)',
  },
  {
    id: 102,
    index: '#B',
    title: 'Lexicographical Subsequence',
    content:
      'Construct the lexicographically minimal subsequence of length K while preserving monotonicity constraints and non-decreasing frequency requirements under Unicode alphabet rules.',
    difficulty: 'Medium',
    languages: ['java', 'python', 'cpp'],
    tags: ['greedy', 'monostack'],
    authorName: 'Alex Chen',
    createdLabel: 'Problem B',
    acceptance: '86.4%',
    community: {
      src: '',
      name: 'MIT CSAIL Coding Guild',
      type: 'community',
    },
    solveLabel: 'Solve (250 pts)',
  },
  {
    id: 103,
    index: '#C',
    title: 'Sliding Window Median Matrix',
    content:
      'Calculate dynamic running 2D medians on an N x M grid over shifting rectangular sub-matrices of dimensions R x C within strict 2.0s wall-clock CPU limits.',
    difficulty: 'Hard',
    languages: ['java', 'cpp'],
    tags: ['order-statistic-tree', 'fenwick-2d'],
    authorName: 'MIT CSAIL Algorithms Lab',
    createdLabel: 'Problem C',
    acceptance: '32.1%',
    community: {
      src: '',
      name: 'MIT CSAIL Coding Guild',
      type: 'community',
    },
    solveLabel: 'Solve (450 pts)',
  },
  {
    id: 104,
    index: '#D',
    title: 'DAG Bottleneck Cuts',
    content:
      'Find the min-cost edge set removal that guarantees reachability from source S to sink T collapses to strictly less than threshold capacity under probabilistic decay.',
    difficulty: 'Hard',
    languages: ['java', 'cpp', 'python'],
    tags: ['dinic-flow', 'min-cut'],
    authorName: 'MIT CSAIL Algorithms Lab',
    createdLabel: 'Problem D',
    acceptance: '8.9%',
    community: {
      src: '',
      name: 'MIT CSAIL Coding Guild',
      type: 'community',
    },
    solveLabel: 'Solve (700 pts)',
  },
  {
    id: 105,
    index: '#E',
    title: 'Hyperplane Quantum Lattice',
    content:
      'Count the exact number of integer coordinate points contained inside an N-dimensional affine hyperplane slice bounded by dual polyhedral hulls modulo 998,244,353 with parameter rank K ≤ 10^18.',
    difficulty: 'Hard',
    languages: ['cpp', 'java'],
    tags: ['lattice-reduction', 'generating-functions', 'ntt-convolution'],
    authorName: 'Dr. K. Varma',
    createdLabel: 'Problem E',
    acceptance: '1.4%',
    community: {
      src: '',
      name: 'MIT CSAIL Coding Guild',
      type: 'community',
    },
    solveLabel: 'Solve (850 pts)',
  },
]

// Dữ liệu xếp hạng của người dùng hiện tại
export const sampleUserStanding = {
  rank: 89,
  percentile: 'TOP 2.3%',
  handle: 'usr_904x',
  score: 350,
  penalty: '00:54:12',
  delta: '+34 Rating (-> 2444)',
}

// Bảng xếp hạng các thí sinh tham gia cuộc thi
export const sampleLeaderboard = [
  { rank: 1, handle: 'tourist', tag: '[LGM]', score: 1900, solved: '5/5 AC' },
  { rank: 2, handle: 'ecnerwala', tag: '', score: 1500, solved: '4/5 AC' },
  { rank: 3, handle: 'neal_wu', tag: '', score: 1500, solved: '4/5 AC' },
  { rank: 4, handle: 'Radewoosh', tag: '', score: 1050, solved: '3/5 AC' },
  { rank: 5, handle: 'Benq', tag: '', score: 1050, solved: '3/5 AC' },
  { rank: 89, handle: 'usr_904x', tag: 'YOU', score: 350, solved: '2/5 AC', isUser: true },
]
