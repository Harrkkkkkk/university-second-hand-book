package com.whu.bookapi.controller;

import com.whu.bookapi.model.Book;
import com.whu.bookapi.model.User;
import com.whu.bookapi.service.BookService;
import com.whu.bookapi.service.FavoriteService;
import com.whu.bookapi.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/favorites")
public class FavoriteController {
    private final FavoriteService favoriteService;
    private final UserService userService;
    private final BookService bookService;

    public FavoriteController(FavoriteService favoriteService, UserService userService, BookService bookService) {
        this.favoriteService = favoriteService;
        this.userService = userService;
        this.bookService = bookService;
    }

    @GetMapping("/list")
    public ResponseEntity<?> list(@RequestHeader(value = "token", required = false) String token) {
        User user = token == null ? null : userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<Book> list = favoriteService.list(user.getUsername(), bookServiceStore());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/check/{bookId}")
    public ResponseEntity<?> check(@RequestHeader(value = "token", required = false) String token,
                                   @PathVariable("bookId") Long bookId) {
        User user = token == null ? null : userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        boolean collected = favoriteService.isCollected(user.getUsername(), bookId);
        return ResponseEntity.ok(Collections.singletonMap("collected", collected));
    }

    @GetMapping("/ids")
    public ResponseEntity<?> listIds(@RequestHeader(value = "token", required = false) String token) {
        User user = token == null ? null : userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<Long> ids = favoriteService.listIds(user.getUsername());
        return ResponseEntity.ok(ids);
    }

    @PostMapping("/add/{bookId}")
    public ResponseEntity<?> add(@RequestHeader(value = "token", required = false) String token,
                                 @PathVariable("bookId") Long bookId) {
        User user = token == null ? null : userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        favoriteService.add(user.getUsername(), bookId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/remove/{bookId}")
    public ResponseEntity<?> remove(@RequestHeader(value = "token", required = false) String token,
                                    @PathVariable("bookId") Long bookId) {
        User user = token == null ? null : userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        favoriteService.remove(user.getUsername(), bookId);
        return ResponseEntity.ok().build();
    }

    private java.util.Map<Long, Book> bookServiceStore() {
        java.util.List<Book> all = bookService.page(null, null, null, null, 1, Integer.MAX_VALUE, null);
        java.util.Map<Long, Book> m = new java.util.HashMap<>();
        for (Book b : all) m.put(b.getId(), b);
        return m;
    }
}
