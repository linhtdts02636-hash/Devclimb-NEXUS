package com.devclimb.ptag;

// Số lượng puzzle đang dùng tag này được tính bằng một query aggregate duy nhất.
public record TagDTO(String name, long puzzleCount) {
}
