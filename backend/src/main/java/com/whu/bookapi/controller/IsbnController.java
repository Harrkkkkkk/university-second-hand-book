package com.whu.bookapi.controller;

import com.whu.bookapi.service.IsbnService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/isbn")
public class IsbnController {

    private final IsbnService isbnService;

    public IsbnController(IsbnService isbnService) {
        this.isbnService = isbnService;
    }

    @GetMapping("/info")
    public ResponseEntity<?> getBookInfo(@RequestParam("isbn") String isbn) {
        try {
            Map<String, String> info = isbnService.getBookInfo(isbn);
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get book info: " + e.getMessage());
        }
    }
}
