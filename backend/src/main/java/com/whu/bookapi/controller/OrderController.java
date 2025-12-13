package com.whu.bookapi.controller;

import com.whu.bookapi.model.Book;
import com.whu.bookapi.model.Order;
import com.whu.bookapi.model.User;
import com.whu.bookapi.service.BookService;
import com.whu.bookapi.service.OrderService;
import com.whu.bookapi.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;
    private final UserService userService;
    private final BookService bookService;

    public OrderController(OrderService orderService, UserService userService, BookService bookService) {
        this.orderService = orderService;
        this.userService = userService;
        this.bookService = bookService;
    }

    @GetMapping("/list")
    public ResponseEntity<?> list(@RequestHeader(value = "token", required = false) String token) {
        User user = token == null ? null : userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<Order> orders = orderService.listByBuyer(user.getUsername());
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestHeader(value = "token", required = false) String token,
                                    @RequestParam("bookId") Long bookId) {
        User user = token == null ? null : userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Book b = bookService.get(bookId);
        if (b == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        boolean reserved = bookService.tryReserveStock(bookId);
        if (!reserved) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of("message", "库存不足"));
        }
        Order o = orderService.create(b, user.getUsername());
        return ResponseEntity.ok(o);
    }

    @PostMapping("/pay/{id}")
    public ResponseEntity<?> pay(@RequestHeader(value = "token", required = false) String token,
                                 @PathVariable("id") Long id) {
        if (userService.getByToken(token) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Order o = orderService.setStatus(id, "paid");
        if (o == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        return ResponseEntity.ok(o);
    }

    @PostMapping("/cancel/{id}")
    public ResponseEntity<?> cancel(@RequestHeader(value = "token", required = false) String token,
                                    @PathVariable("id") Long id) {
        if (userService.getByToken(token) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Order o = orderService.setStatus(id, "cancelled");
        if (o == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        bookService.releaseStock(o.getBookId());
        return ResponseEntity.ok(o);
    }

    @PostMapping("/receive/{id}")
    public ResponseEntity<?> receive(@RequestHeader(value = "token", required = false) String token,
                                     @PathVariable("id") Long id) {
        if (userService.getByToken(token) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Order o = orderService.setStatus(id, "received");
        if (o == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        return ResponseEntity.ok(o);
    }
}
