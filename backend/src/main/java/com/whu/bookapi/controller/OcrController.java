package com.whu.bookapi.controller;

import com.whu.bookapi.service.BaiduOcrService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ocr")
public class OcrController {

    private final BaiduOcrService baiduOcrService;

    public OcrController(BaiduOcrService baiduOcrService) {
        this.baiduOcrService = baiduOcrService;
    }

    @PostMapping("/recognize")
    public ResponseEntity<?> recognize(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }
            List<String> results = baiduOcrService.recognizeText(file.getBytes());
            return ResponseEntity.ok(Map.of("lines", results));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error reading file");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("OCR failed: " + e.getMessage());
        }
    }
}
