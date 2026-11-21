// Dữ liệu minh hoạ cho các phần chưa có backend (bài viết, vai trò, cuộc thi, bảng xếp hạng, chủ đề).
// Mọi nơi dùng file này phải hiển thị nhãn "sample data for illustration".

function sampleImage(title, hue) {
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="960" height="540" viewBox="0 0 960 540"><rect width="960" height="540" fill="hsl(${hue} 28% 14%)"/><rect x="48" y="48" width="864" height="444" rx="16" fill="hsl(${hue} 24% 10%)" stroke="hsl(${hue} 30% 26%)"/><g fill="hsl(${hue} 60% 70%)" opacity=".9"><rect x="88" y="96" width="260" height="14" rx="7"/><rect x="88" y="132" width="420" height="10" rx="5" opacity=".5"/><rect x="120" y="164" width="360" height="10" rx="5" opacity=".5"/><rect x="120" y="196" width="300" height="10" rx="5" opacity=".5"/><rect x="88" y="228" width="200" height="10" rx="5" opacity=".5"/></g><text x="88" y="330" font-family="monospace" font-size="22" fill="hsl(${hue} 70% 75%)">${title}</text><rect x="88" y="380" width="200" height="40" rx="10" fill="hsl(145 50% 40%)"/><text x="108" y="407" font-family="monospace" font-size="18" fill="#fff">ACCEPTED 5/5</text></svg>`
  return `data:image/svg+xml;utf8,${encodeURIComponent(svg)}`
}

export const samplePosts = [
  {
    id: 1,
    author: 'khoa.nexus',
    role: 'Owner',
    time: '3d',
    isPinned: true,
    title: 'Read first: how this community works',
    text: 'Be kind, show your reasoning, and mark spoilers. Puzzles in review are not for sharing outside the club. Contests run on Saturdays at 20:00.',
    images: [],
    files: [{ name: 'club-guidelines.pdf', size: '212 KB' }],
    upvotes: 41,
    replies: 8,
  },
  {
    id: 2,
    author: 'mai.pham',
    role: 'Moderator',
    time: '1w',
    isPinned: true,
    title: 'Contest schedule for the term',
    text: 'Round 12 is this Saturday. Sign up on the right before Friday midnight so we can balance the rooms.',
    images: [],
    files: [],
    upvotes: 29,
    replies: 3,
  },
  {
    id: 3,
    author: 'minh.tran',
    role: 'Moderator',
    time: '2h',
    isPinned: false,
    title: 'Why does my two-pointer solution time out on test 12?',
    text: 'Passes the first 11 tests, then Time Limit on the big one. I think I am re-scanning the array inside the loop. Screenshot of the verdict below.',
    images: [sampleImage('two_pointer.py', 215)],
    files: [],
    upvotes: 24,
    replies: 12,
  },
  {
    id: 4,
    author: 'linh_cp',
    role: 'Problem setter',
    time: '5h',
    isPinned: false,
    title: 'New puzzle in review: Merge Intervals II',
    text: "Looking for two reviewers before it goes live for Saturday's round. Test data and statement draft attached.",
    images: [sampleImage('merge_intervals_ii', 150), sampleImage('edge_cases.txt', 30)],
    files: [{ name: 'merge-intervals-ii-tests.zip', size: '148 KB' }],
    upvotes: 18,
    replies: 7,
  },
  {
    id: 5,
    author: 'byte_me',
    role: 'Member',
    time: '1d',
    isPinned: false,
    title: 'My notes on monotonic stacks',
    text: 'Wrote these while studying for the round. Corrections welcome.',
    images: [],
    files: [
      { name: 'monotonic-stack.md', size: '9 KB' },
      { name: 'practice-set.py', size: '3 KB' },
    ],
    upvotes: 9,
    replies: 2,
  },
]

export const sampleSolvers = [
  { name: 'minh.tran', solves: 41 },
  { name: 'linh_cp', solves: 38 },
  { name: 'hung.dev', solves: 33 },
  { name: 'khoa.nexus', solves: 29 },
  { name: 'byte_me', solves: 24 },
]

export const sampleTopics = ['dynamic programming', 'graphs', 'binary search', 'hash map', 'two pointers', 'greedy']
