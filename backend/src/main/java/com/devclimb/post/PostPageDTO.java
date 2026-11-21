package com.devclimb.post;

import java.util.List;

// nextCursor là id bài cuối của trang này, gửi lại làm "before" để lấy trang kế tiếp; null nghĩa là đã hết bài.
public record PostPageDTO(List<PostDTO> posts, Integer nextCursor) {
}
