// Placeholder dùng chung cho trang tạo puzzle và trang giải puzzle khi API
// chưa trả về pula_functionheader. Nội dung cố tình trung tính, không chứa
// sample "two sum" để tránh người dùng tưởng đó là đề bài thật.
export const DEFAULT_FUNCTION_HEADERS = {
  python: 'def functionHeader(arg1, arg2):',
  java: `import java.util.*;

class Solution {
    public static int functionHeader(String arg1, String arg2) {
        throw new UnsupportedOperationException("Not implemented");
    }
}`,
  javascript: 'function functionHeader(arg1, arg2) {',
}
