package com.giang.dataFAQ.controller;

import com.giang.dataFAQ.exception.InvalidRequestException;
import com.giang.dataFAQ.model.FAQ;
import com.giang.dataFAQ.service.FAQService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/faqs")
public class FAQController {

    @Autowired
    private FAQService faqService;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, String>> saveFileData(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "File không hợp lệ hoặc rỗng."));
            }

            faqService.saveFileData(file.getInputStream(), file.getOriginalFilename());
            return ResponseEntity.ok(Collections.singletonMap("message", "Dữ liệu từ file Excel đã được lưu vào database."));

        } catch (InvalidRequestException e) { // Lỗi về định dạng file hoặc header
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace(); // Xem log cụ thể trong console
            String errorMessage = "Đã xảy ra lỗi khi xử lý file: " + e.getMessage();
            return ResponseEntity.internalServerError().body(Collections.singletonMap("error", errorMessage));
        }
    }


    @GetMapping("/read-data")
    public ResponseEntity<List<FAQ>> findAll(){
        return ResponseEntity.ok(faqService.findAll());
    }


    /**
     * Thêm thủ công một FAQ (code, question, answer là bắt buộc; note là tùy chọn)
     */
    @PostMapping("/add")
    public ResponseEntity<Map<String, String>> addFAQ(@RequestBody FAQ faq) {
        try {
            faqService.addOneFAQ(faq);
            return ResponseEntity.ok(Collections.singletonMap("message", "Thêm FAQ thành công."));

        } catch (InvalidRequestException e) {  // Bắt lỗi dữ liệu đầu vào không hợp lệ
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));

        } catch (Exception e) { // mọi lỗi còn lại
            return ResponseEntity.internalServerError().body(Collections.singletonMap("error", "Lỗi khi thêm FAQ: " + e.getMessage()));
        }
    }
}
