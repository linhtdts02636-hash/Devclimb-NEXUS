<script setup>
/**
 * FILE TEST GIAO DIỆN — CHỈ DÙNG Ở DEVELOPMENT.
 *
 * Trang này dùng chính component Post.vue với dữ liệu giả để kiểm tra layout ảnh.
 * Không gọi API, không upload, không lưu database.
 *
 * Cách dọn: xoá file này và route `/dev/post-mock-gallery` trong `src/router/index.js`.
 */
import Post from '../../components/post/Post.vue'

const MOCK_AUTHOR = {
  id: 6,
  name: 'hung Mock',
  avatarUrl: '',
}

function mockImage({ label, width, height, from, to }) {
  const shortSide = Math.min(width, height)
  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${height}" viewBox="0 0 ${width} ${height}">
      <defs>
        <linearGradient id="mock" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0" stop-color="${from}" />
          <stop offset="1" stop-color="${to}" />
        </linearGradient>
      </defs>
      <rect width="100%" height="100%" fill="url(#mock)" />
      <circle cx="${width * 0.84}" cy="${height * 0.16}" r="${shortSide * 0.13}" fill="rgba(255,255,255,0.16)" />
      <circle cx="${width * 0.12}" cy="${height * 0.9}" r="${shortSide * 0.18}" fill="rgba(0,0,0,0.12)" />
      <text x="${width / 2}" y="${height / 2}" text-anchor="middle" dominant-baseline="middle"
        font-family="Arial, sans-serif" font-size="${shortSide * 0.085}" font-weight="700" fill="#ffffff">
        ${label}
      </text>
      <text x="${width / 2}" y="${height / 2 + shortSide * 0.095}" text-anchor="middle"
        font-family="Arial, sans-serif" font-size="${shortSide * 0.042}" fill="rgba(255,255,255,0.85)">
        ${width} × ${height}
      </text>
    </svg>
  `

  return `data:image/svg+xml;charset=utf-8,${encodeURIComponent(svg)}`
}

const mockImages = {
  landscape: mockImage({
    label: 'Ảnh ngang',
    width: 1200,
    height: 800,
    from: '#2f6fed',
    to: '#7c3aed',
  }),
  portrait: mockImage({
    label: 'Ảnh dọc',
    width: 700,
    height: 1000,
    from: '#0f9f7a',
    to: '#0ea5e9',
  }),
  square: mockImage({
    label: 'Ảnh vuông',
    width: 1000,
    height: 1000,
    from: '#f59e0b',
    to: '#ef4444',
  }),
  wide: mockImage({
    label: 'Ảnh rộng',
    width: 1600,
    height: 700,
    from: '#db2777',
    to: '#7c3aed',
  }),
  tall: mockImage({
    label: 'Ảnh cao',
    width: 600,
    height: 1200,
    from: '#0ea5e9',
    to: '#1d4ed8',
  }),
}

const baseTime = Date.parse('2026-11-18T02:00:00.000Z')

const mockPosts = [
  {
    id: 9001,
    author: MOCK_AUTHOR,
    title: 'Bài viết không có ảnh',
    content:
      'Bài này kiểm tra trường hợp không có ảnh.\nTiêu đề và nội dung xuống dòng bình thường, không có khoảng trống thừa phía dưới.',
    createdAt: new Date(baseTime).toISOString(),
    updatedAt: null,
    images: [],
  },
  {
    id: 9002,
    author: MOCK_AUTHOR,
    title: 'Bài viết có 1 ảnh',
    content:
      'Một ảnh ngang giữ nguyên tỷ lệ 3:2, không bị kéo méo và không tràn khỏi khung bài viết.',
    createdAt: new Date(baseTime - 60 * 1000).toISOString(),
    updatedAt: null,
    images: [mockImages.landscape],
  },
  {
    id: 9003,
    author: MOCK_AUTHOR,
    title: 'Bài viết có 2 ảnh',
    content: 'Hai ảnh hiển thị thành 2 cột đều nhau.',
    createdAt: new Date(baseTime - 2 * 60 * 1000).toISOString(),
    updatedAt: new Date(baseTime - 60 * 1000).toISOString(),
    images: [mockImages.square, mockImages.portrait],
  },
  {
    id: 9004,
    author: MOCK_AUTHOR,
    title: 'Bài viết có 3 ảnh',
    content:
      'Ba ảnh hiển thị thành 3 cột trên desktop. Trên mobile, ảnh thứ ba chiếm toàn bộ chiều ngang hàng dưới.',
    createdAt: new Date(baseTime - 3 * 60 * 1000).toISOString(),
    updatedAt: null,
    images: [mockImages.landscape, mockImages.portrait, mockImages.square],
  },
  {
    id: 9005,
    author: MOCK_AUTHOR,
    title: 'Bài viết có 4 ảnh',
    content: 'Bốn ảnh hiển thị dạng lưới 2×2.',
    createdAt: new Date(baseTime - 4 * 60 * 1000).toISOString(),
    updatedAt: null,
    images: [
      mockImages.square,
      mockImages.landscape,
      mockImages.portrait,
      mockImages.wide,
    ],
  },
  {
    id: 9006,
    author: MOCK_AUTHOR,
    title: 'Bài viết có 5 ảnh (overlay +1)',
    content:
      'Chỉ hiển thị 4 ảnh đầu; ảnh thứ tư có overlay +1 cho biết còn ảnh khác.',
    createdAt: new Date(baseTime - 5 * 60 * 1000).toISOString(),
    updatedAt: null,
    images: [
      mockImages.square,
      mockImages.landscape,
      mockImages.tall,
      mockImages.portrait,
      mockImages.wide,
    ],
  },
  {
    id: 9007,
    author: MOCK_AUTHOR,
    title: `Tiêu đề rất dài không có khoảng trắng ${'T'.repeat(90)}`,
    content: `https://example.com/${'duong-dan-rat-dai-khong-co-khoang-trang-'.repeat(5)}`,
    createdAt: new Date(baseTime - 6 * 60 * 1000).toISOString(),
    updatedAt: new Date(baseTime - 30 * 1000).toISOString(),
    images: [],
  },
]
</script>

<template>
  <main class="gallery-page">
    <div class="gallery-page__inner">
      <header class="gallery-header">
        <p class="gallery-eyebrow">DEV ONLY</p>
        <h1 class="gallery-title">Post Mock Gallery</h1>
        <p class="gallery-description">
          Kiểm tra giao diện <code>Post.vue</code> với dữ liệu ảnh giả. Trang chỉ được
          đăng ký ở môi trường development, không gọi API và không lưu database.
        </p>
        <div class="gallery-badges">
          <span class="gallery-badge">DỮ LIỆU MẪU</span>
          <span class="gallery-badge gallery-badge--muted">KHÔNG GỌI API</span>
        </div>
      </header>

      <div class="gallery-list">
        <Post
          v-for="post in mockPosts"
          :key="post.id"
          :post="post"
          :images="post.images"
          :current-user-id="MOCK_AUTHOR.id"
        />
      </div>

      <footer class="gallery-note">
        Các nút Sửa/Xoá chỉ hiển thị để kiểm tra bố cục, không thao tác trong trang
        mock. Muốn kiểm tra thật, dùng Community Feed với tài khoản đã đăng nhập.
      </footer>
    </div>
  </main>
</template>

<style scoped>
.gallery-page {
  min-height: 100vh;
  padding: 40px 20px 72px;
  background:
    radial-gradient(circle at 50% 0, var(--ac-soft), transparent 34rem),
    var(--bg);
}

.gallery-page__inner {
  width: min(720px, 100%);
  margin: 0 auto;
}

.gallery-header {
  display: grid;
  gap: 8px;
  margin-bottom: 24px;
}

.gallery-eyebrow {
  color: var(--tx2);
  font-family: var(--mono);
  font-size: 11px;
  font-weight: 500;
  letter-spacing: 0.06em;
}

.gallery-title {
  font-size: 26px;
  font-weight: 600;
  letter-spacing: -0.03em;
  line-height: 1.15;
}

.gallery-description {
  max-width: 60ch;
  color: var(--tx2);
  font-size: 14px;
  line-height: 1.6;
}

.gallery-description code {
  font-family: var(--mono);
  font-size: 0.92em;
}

.gallery-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 4px;
}

.gallery-badge {
  padding: 4px 8px;
  border-radius: 999px;
  background: var(--ac-soft);
  color: var(--ac);
  font-family: var(--mono);
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.06em;
}

.gallery-badge--muted {
  background: var(--s2);
  color: var(--tx2);
}

.gallery-list {
  display: grid;
  gap: 12px;
}

.gallery-note {
  margin-top: 20px;
  padding: 12px 14px;
  border: 1px dashed var(--line2);
  border-radius: var(--r-sm);
  color: var(--tx2);
  font-size: 12px;
  line-height: 1.55;
}

@media (max-width: 560px) {
  .gallery-page {
    padding: 24px 14px 56px;
  }

  .gallery-title {
    font-size: 22px;
  }
}
</style>
