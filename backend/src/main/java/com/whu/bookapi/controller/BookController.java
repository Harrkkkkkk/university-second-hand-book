package com.whu.bookapi.controller;

import com.whu.bookapi.dto.PageResponse;
import com.whu.bookapi.model.Book;
import com.whu.bookapi.model.User;
import com.whu.bookapi.service.BookService;
import com.whu.bookapi.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
public class BookController {
    private final BookService bookService;
    private final UserService userService;

    public BookController(BookService bookService, UserService userService) {
        this.bookService = bookService;
        this.userService = userService;
    }

    @GetMapping("/page")
    public ResponseEntity<?> page(@RequestParam(value = "bookName", required = false) String bookName,
                                  @RequestParam(value = "minPrice", required = false) Double minPrice,
                                  @RequestParam(value = "maxPrice", required = false) Double maxPrice,
                                  @RequestParam(value = "conditionLevel", required = false) String conditionLevel,
                                  @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                  @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                  @RequestParam(value = "sortBy", required = false) String sortBy) {
        List<Book> records = bookService.page(bookName, minPrice, maxPrice, conditionLevel, pageNum, pageSize, sortBy);
        long total = bookService.count(bookName, minPrice, maxPrice);
        return ResponseEntity.ok(new PageResponse<>(total, records));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detail(@PathVariable("id") Long id) {
        Book b = bookService.get(id);
        if (b == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        return ResponseEntity.ok(b);
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestHeader(value = "token", required = false) String token,
                                 @RequestBody Book book) {
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userService.getByToken(token);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Book created = bookService.add(book, user.getUsername());
        return ResponseEntity.ok(created);
    }

    @GetMapping("/owner/list")
    public ResponseEntity<?> ownerList(@RequestHeader(value = "token", required = false) String token) {
        if (token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        User user = userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        java.util.List<Book> list = bookService.listBySeller(user.getUsername());
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@RequestHeader(value = "token", required = false) String token,
                                    @PathVariable("id") Long id,
                                    @RequestBody Book book) {
        if (token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        User user = userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        book.setId(id);
        Book updated = bookService.update(book, user.getUsername());
        if (updated == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/offline/{id}")
    public ResponseEntity<?> offline(@RequestHeader(value = "token", required = false) String token,
                                     @PathVariable("id") Long id) {
        if (token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        User user = userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        boolean ok = bookService.offline(id, user.getUsername());
        if (!ok) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@RequestHeader(value = "token", required = false) String token,
                                    @PathVariable("id") Long id) {
        if (token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        User user = userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        boolean ok = bookService.delete(id, user.getUsername());
        if (!ok) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/hot")
    public ResponseEntity<?> hot(@RequestParam(value = "limit", defaultValue = "6") int limit) {
        return ResponseEntity.ok(bookService.getHotBooks(limit));
    }

    @GetMapping("/suggestions")
    public ResponseEntity<?> suggestions(@RequestParam("keyword") String keyword) {
        return ResponseEntity.ok(bookService.getSuggestions(keyword));
    }
}
