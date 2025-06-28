package com.giang.dataFAQ.service;

import com.giang.dataFAQ.exception.InvalidRequestException;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.giang.dataFAQ.model.FAQ;
import com.giang.dataFAQ.repository.FAQRepository;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.DataFormatter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class FAQService {

    @Autowired
    private FAQRepository faqRepository;

    private final DataFormatter formatter = new DataFormatter();

    /** 
     * Lấy data từ 1 cell
     */ 
    private String getCellValue(Cell cell) {
        return cell == null ? "" : formatter.formatCellValue(cell);
    }
    
    /**
     * Chuyển data từ excel sang db
     */
    public void saveFileData(InputStream file, String originalFilename) throws IOException, CsvValidationException {
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new InvalidRequestException("Không thể xác định loại file do thiếu tên file.");
        }

        if (originalFilename.toLowerCase().endsWith(".xlsx")) {
            parseExcelFile(file);
        } else if (originalFilename.toLowerCase().endsWith(".csv")) {
            parseCsvFile(file);
        } else {
            throw new InvalidRequestException("Loại file không được hỗ trợ. Vui lòng tải lên file .xlsx hoặc .csv.");
        }
    }

    private void parseExcelFile(InputStream file) throws IOException {
        List<FAQ> faqList = new ArrayList<>();
        Workbook workbook = WorkbookFactory.create(file);
        Sheet sheet = workbook.getSheetAt(0);

        Row header = sheet.getRow(0);
        if (header == null) {
            throw new InvalidRequestException("File Excel rỗng hoặc thiếu dòng tiêu đề.");
        }

        validateHeader(
            getCellValue(header.getCell(0)),
            getCellValue(header.getCell(1)),
            getCellValue(header.getCell(2)),
            getCellValue(header.getCell(3)),
            getCellValue(header.getCell(4))
        );

        // Duyệt từng dòng dữ liệu
        sheet.forEach(row -> {
            if (row.getRowNum() == 0) return; // Bỏ qua dòng tiêu đề
            processRow(
                getCellValue(row.getCell(1)), // code
                getCellValue(row.getCell(2)), // question
                getCellValue(row.getCell(3)), // answer
                getCellValue(row.getCell(4)), // note
                faqList
            );
        });

        faqRepository.saveAll(faqList);
    }

    private void parseCsvFile(InputStream file) throws IOException, CsvValidationException {
        List<FAQ> faqList = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new InputStreamReader(file))) {
            String[] header = csvReader.readNext();
            if (header == null) {
                throw new InvalidRequestException("File CSV rỗng hoặc thiếu dòng tiêu đề.");
            }
            validateHeader(header[0], header[1], header[2], header[3], header[4]);

            String[] line;
            while ((line = csvReader.readNext()) != null) {
                processRow(line[1], line[2], line[3], line[4], faqList);
            }
        }
        faqRepository.saveAll(faqList);
    }

    private void validateHeader(String hStt, String hCode, String hQuestion, String hAnswer, String hNote) {
        if (!"No.".equalsIgnoreCase(hStt.trim()) || !"Code".equalsIgnoreCase(hCode.trim()) || !"Question".equalsIgnoreCase(hQuestion.trim())
                || !"Answer".equalsIgnoreCase(hAnswer.trim()) || !"Note".equalsIgnoreCase(hNote.trim())) {
            throw new InvalidRequestException("Định dạng file không hợp lệ: Tiêu đề phải là: No. | Code | Question | Answer | Note");
        }
    }

    private String escapeNewlines(String text) {
        if (text == null) {
            return null;
        }
        // Thay thế ký tự xuống dòng (cả \r\n và \n) bằng chuỗi literal "\\n"
        return text.replaceAll("\\r?\\n", "\\\\n.");
    }

    private void processRow(String code, String question, String answer, String note, List<FAQ> faqList) {
        String trimmedCode = code != null ? code.trim() : "";
        String trimmedQuestion = question != null ? question.trim() : "";

        if (trimmedCode.isEmpty() || trimmedQuestion.isEmpty() || answer == null || answer.isEmpty()) {
            return; // Bỏ qua dòng thiếu dữ liệu bắt buộc
        }

        if (faqRepository.findByCode(trimmedCode) != null) {
            return; // Bỏ qua nếu mã code đã tồn tại trong DB
        }

        // Bỏ qua nếu mã code đã có trong danh sách đang xử lý
        if (faqList.stream().anyMatch(f -> f.getCode().equals(trimmedCode))) {
            return;
        }

        FAQ faq = new FAQ();
        faq.setCode(trimmedCode);
        faq.setQuestion(trimmedQuestion);
        // Chuyển đổi ký tự xuống dòng thành chuỗi literal "\n" để lưu thành một dòng.
        faq.setAnswer(escapeNewlines(answer));

        if (note != null && !note.isEmpty()) {
            faq.setNote(escapeNewlines(note));
        }

        faqList.add(faq);
    }

    /**
     * Thêm thủ công 1 FAQ
     * 
     * @param code      Mã câu hỏi, bắt buộc, phải duy nhất
     * @param question  Câu hỏi, bắt buộc
     * @param answer    Câu trả lời, bắt buộc
     * @param note      Ghi chú, tùy chọn
     * @return          Đối tượng FAQ đã được lưu vào database
     * @throws IllegalArgumentException nếu thiếu trường bắt buộc hoặc trùng code
     */
    public FAQ addOneFAQ(FAQ faq) {
        String code = faq.getCode();
        String question = faq.getQuestion();
        String answer = faq.getAnswer();

        // Kiểm tra ràng buộc bắt buộc
        if (code == null || code.trim().isEmpty()) {
            throw new InvalidRequestException("Bạn chưa nhập Code.");
        }
        if (question == null || question.trim().isEmpty()) {
            throw new InvalidRequestException("Bạn chưa nhập Question.");
        }
        if (answer == null || answer.isEmpty()) {
            throw new InvalidRequestException("Bạn chưa nhập Answer.");
        }

        // Check if the code already exists
        if (faqRepository.findByCode(code) != null) {
            throw new InvalidRequestException("Mã code '" + code + "' đã tồn tại.");
        }

        // Trim và định dạng lại dữ liệu trước khi lưu
        faq.setCode(code.trim());
        faq.setQuestion(question.trim());
        // Chuyển đổi ký tự xuống dòng thành chuỗi literal "\n" để lưu thành một dòng.
        faq.setAnswer(escapeNewlines(answer));

        String note = faq.getNote();
        if (note != null && !note.isEmpty()) {
            faq.setNote(escapeNewlines(note));
        } else {
            faq.setNote(null); // Đảm bảo note rỗng được lưu là null
        }

        return faqRepository.save(faq);
    }

    public List<FAQ> findAll() {
        return faqRepository.findAll();
    }
}