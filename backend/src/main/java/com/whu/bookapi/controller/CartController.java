package com.whu.bookapi.controller;

import com.whu.bookapi.model.CartItem;
import com.whu.bookapi.model.Book;
import com.whu.bookapi.model.User;
import com.whu.bookapi.service.CartService;
import com.whu.bookapi.service.UserService;
import com.whu.bookapi.service.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService;
    private final UserService userService;
    private final BookService bookService;

    public CartController(CartService cartService, UserService userService, BookService bookService) {
        this.cartService = cartService;
        this.userService = userService;
        this.bookService = bookService;
    }

    @GetMapping("/list")
    public ResponseEntity<?> list(@RequestHeader(value = "token", required = false) String token) {
        User user = token == null ? null : userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<CartItem> list = cartService.list(user.getUsername());
        java.util.List<java.util.Map<String, Object>> view = new java.util.ArrayList<>();
        for (CartItem ci : list) {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("bookId", ci.getBookId());
            m.put("quantity", ci.getQuantity());
            Book b = bookService.get(ci.getBookId());
            if (b != null) {
                m.put("bookName", b.getBookName());
                m.put("sellPrice", b.getSellPrice());
                m.put("coverUrl", b.getCoverUrl());
                m.put("sellerName", b.getSellerName());
            }
            view.add(m);
        }
        return ResponseEntity.ok(view);
    }

    @PostMapping("/add/{bookId}")
    public ResponseEntity<?> add(@RequestHeader(value = "token", required = false) String token,
                                 @PathVariable("bookId") Long bookId) {
        User user = token == null ? null : userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            cartService.add(user.getUsername(), bookId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/remove/{bookId}")
    public ResponseEntity<?> remove(@RequestHeader(value = "token", required = false) String token,
                                    @PathVariable("bookId") Long bookId,
                                    @RequestParam(value = "count", required = false) Integer count) {
        User user = token == null ? null : userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        cartService.remove(user.getUsername(), bookId, count);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/clear")
    public ResponseEntity<?> clear(@RequestHeader(value = "token", required = false) String token) {
        User user = token == null ? null : userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        cartService.clear(user.getUsername());
        return ResponseEntity.ok().build();
    }
}
