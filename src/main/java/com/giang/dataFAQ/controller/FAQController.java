package com.giang.dataFAQ.controller;

import com.giang.dataFAQ.exception.InvalidRequestException;
import com.giang.dataFAQ.model.FAQ;
import com.giang.dataFAQ.service.FAQService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/faqs")
public class FAQController {

    @Autowired
    private FAQService faqService;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, String>> saveFileData(@RequestParam("file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new InvalidRequestException("File không hợp lệ hoặc rỗng.");
        }
        faqService.saveFileData(file.getInputStream(), file.getOriginalFilename());
        return ResponseEntity.ok(Collections.singletonMap("message", "Dữ liệu từ file Excel đã được lưu vào database."));
    }

    /**
     * Thêm thủ công một FAQ (code, question, answer là bắt buộc; note là tùy chọn)
     */
    @PostMapping("/add")
    public ResponseEntity<Map<String, String>> addFAQ(@RequestBody FAQ faq) {
        faqService.addOneFAQ(faq);
        return ResponseEntity.ok(Collections.singletonMap("message", "Thêm FAQ thành công."));
    }
}
