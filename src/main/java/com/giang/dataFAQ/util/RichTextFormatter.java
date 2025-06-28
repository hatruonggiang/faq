package com.giang.dataFAQ.util;

public class RichTextFormatter {
    /**
     * Chuyển đổi văn bản thô có chứa ký tự xuống dòng ("\n")
     * sang định dạng HTML bằng cách thay thế chúng bằng thẻ <br />.
     * Hàm này cũng sẽ escape các ký tự HTML đặc biệt để đảm bảo an toàn.
     *
     * @param rawText Văn bản thô đầu vào.
     * @return Chuỗi HTML đã được định dạng chỉ với các thẻ <br />.
     */
    public static String convertToRichHtml(String rawText) {
        if (rawText == null || rawText.trim().isEmpty()) {
            return "";
        }
 
        // 1. Escape các ký tự HTML đặc biệt để tránh lỗi hiển thị và tấn công XSS.
        String escapedText = escapeHtml(rawText);
 
        // 2. Thay thế các ký tự xuống dòng (\n hoặc \r\n) bằng thẻ <br /> của HTML.
        return escapedText.replaceAll("\\r?\\n", "<br />");
    }
 
    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
