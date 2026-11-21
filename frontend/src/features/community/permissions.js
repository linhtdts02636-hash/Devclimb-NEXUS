// Id trùng với tên enum CommunityPermission ở backend, nơi là nguồn dữ liệu chuẩn.
export const PERMISSIONS = [
  { id: 'PIN_POSTS', label: 'Pin posts', hint: 'Pin and unpin posts at the top of the community.' },
  { id: 'REMOVE_CONTENT', label: 'Remove posts and comments', hint: 'Take down content that breaks the rules.' },
  { id: 'CREATE_PUZZLES', label: 'Create puzzles', hint: 'Add problems to the community library.' },
  { id: 'RUN_CONTESTS', label: 'Run contests', hint: 'Create and edit contests.' },
  { id: 'MANAGE_MEMBERS', label: 'Manage members', hint: 'Assign roles and remove members.' },
  { id: 'ATTACH_FILES', label: 'Attach files', hint: 'Attach files to posts.', comingSoon: true },
  { id: 'COMMENT', label: 'Comment', hint: 'Comment on posts.', comingSoon: true },
]

export function describePermission(permissionId) {
  return PERMISSIONS.find((permission) => permission.id === permissionId)?.label ?? permissionId
}
