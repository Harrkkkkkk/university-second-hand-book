package com.whu.bookapi.controller;

import com.whu.bookapi.model.Book;
import com.whu.bookapi.model.Order;
import com.whu.bookapi.model.User;
import com.whu.bookapi.service.BookService;
import com.whu.bookapi.service.NotificationService;
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
    private final com.whu.bookapi.service.CartService cartService;
    private final NotificationService notificationService;

    public OrderController(OrderService orderService, UserService userService, BookService bookService, com.whu.bookapi.service.CartService cartService, NotificationService notificationService) {
        this.orderService = orderService;
        this.userService = userService;
        this.bookService = bookService;
        this.cartService = cartService;
        this.notificationService = notificationService;
    }

    @GetMapping("/list")
    public ResponseEntity<?> list(@RequestHeader(value = "token", required = false) String token) {
        User user = token == null ? null : userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<Order> orders = orderService.listByBuyer(user.getUsername());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/seller/list")
    public ResponseEntity<?> listForSeller(@RequestHeader(value = "token", required = false) String token) {
        User user = token == null ? null : userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<Order> orders = orderService.listBySeller(user.getUsername());
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestHeader(value = "token", required = false) String token,
                                    @RequestParam("bookId") Long bookId) {
        User user = token == null ? null : userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Book b = bookService.get(bookId);
        if (b == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        if (!"on_sale".equals(b.getStatus())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of("message", "该教材不可购买"));
        }
        
        if (user.getUsername().equals(b.getSellerName())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("message", "Cannot buy your own book"));
        }

        boolean reserved = bookService.tryReserveStock(bookId);
        if (!reserved) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of("message", "库存不足"));
        }
        Order o = orderService.create(b, user.getUsername());
        cartService.remove(user.getUsername(), bookId);
        return ResponseEntity.ok(o);
    }

    @PostMapping("/pay/{id}")
    public ResponseEntity<?> pay(@RequestHeader(value = "token", required = false) String token,
                                 @PathVariable("id") Long id) {
        User user = token == null ? null : userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Order existing = orderService.get(id);
        if (existing == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        if (!user.getUsername().equals(existing.getBuyerName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if ("paid".equals(existing.getStatus())) return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of("message", "订单已支付"));
        if (!"pending".equals(existing.getStatus())) return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of("message", "订单状态不可支付"));
        if (existing.getExpireAt() != null && System.currentTimeMillis() > existing.getExpireAt()) {
            orderService.setStatus(id, "expired");
            bookService.releaseStock(existing.getBookId());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of("message", "订单已超时"));
        }
        Order o = orderService.setStatus(id, "paid");
        if (o == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        notificationService.addToUser(
                o.getSellerName(),
                "order",
                "订单已付款",
                "订单#" + o.getId() + "（" + o.getBookName() + "）已付款，金额已暂存至平台，买家确认收货后自动结算至您的账户"
        );
        return ResponseEntity.ok(o);
    }

    @PostMapping("/cancel/{id}")
    public ResponseEntity<?> cancel(@RequestHeader(value = "token", required = false) String token,
                                    @PathVariable("id") Long id) {
        User user = token == null ? null : userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Order existing = orderService.get(id);
        if (existing == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        if (!user.getUsername().equals(existing.getBuyerName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (!"pending".equals(existing.getStatus())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of("message", "订单状态不可取消"));
        }
        Order o = orderService.setStatus(id, "cancelled");
        bookService.releaseStock(existing.getBookId());
        return ResponseEntity.ok(o);
    }

    @PostMapping("/receive/{id}")
    public ResponseEntity<?> receive(@RequestHeader(value = "token", required = false) String token,
                                     @PathVariable("id") Long id) {
        User user = token == null ? null : userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Order existing = orderService.get(id);
        if (existing == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        if (!user.getUsername().equals(existing.getBuyerName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (!"paid".equals(existing.getStatus())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of("message", "订单状态不可确认收货"));
        }
        Order o = orderService.setStatus(id, "received");
        if (o == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        notificationService.addToUser(
                o.getSellerName(),
                "order",
                "买家已确认收货",
                "订单#" + o.getId() + "（" + o.getBookName() + "）买家已确认收货，金额已打入您的账户，请注意查收"
        );
        return ResponseEntity.ok(o);
    }
}
