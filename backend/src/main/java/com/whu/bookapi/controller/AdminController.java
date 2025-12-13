package com.whu.bookapi.controller;

import com.whu.bookapi.model.Book;
import com.whu.bookapi.model.User;
import com.whu.bookapi.model.Complaint;
import com.whu.bookapi.service.BookService;
import com.whu.bookapi.service.ComplaintService;
import com.whu.bookapi.service.UserService;
import com.whu.bookapi.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final UserService userService;
    private final BookService bookService;
    private final ComplaintService complaintService;
    private final NotificationService notificationService;

    public AdminController(UserService userService, BookService bookService, ComplaintService complaintService, NotificationService notificationService) {
        this.userService = userService;
        this.bookService = bookService;
        this.complaintService = complaintService;
        this.notificationService = notificationService;
    }

    private boolean isAdmin(User u) {
        return u != null && "admin".equals(u.getRole());
    }

    @GetMapping("/review/books")
    public ResponseEntity<?> listUnderReview(@RequestHeader(value = "token", required = false) String token) {
        User u = userService.getByToken(token);
        if (!isAdmin(u)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<Book> list = bookService.listUnderReview();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/review/books/{id}/approve")
    public ResponseEntity<?> approve(@RequestHeader(value = "token", required = false) String token,
                                     @PathVariable("id") Long id) {
        User u = userService.getByToken(token);
        if (!isAdmin(u)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        boolean ok = bookService.approve(id);
        if (!ok) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        // 通知卖家审核结果
        com.whu.bookapi.model.Book b = bookService.get(id);
        if (b != null) {
            notificationService.addToUser(b.getSellerName(), "audit", "教材审核通过", "您的教材《" + b.getBookName() + "》已通过审核");
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/review/books/{id}/reject")
    public ResponseEntity<?> reject(@RequestHeader(value = "token", required = false) String token,
                                    @PathVariable("id") Long id) {
        User u = userService.getByToken(token);
        if (!isAdmin(u)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        boolean ok = bookService.reject(id);
        if (!ok) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        com.whu.bookapi.model.Book b = bookService.get(id);
        if (b != null) {
            notificationService.addToUser(b.getSellerName(), "audit", "教材审核未通过", "您的教材《" + b.getBookName() + "》未通过审核");
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users")
    public ResponseEntity<?> listUsers(@RequestHeader(value = "token", required = false) String token) {
        User u = userService.getByToken(token);
        if (!isAdmin(u)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(userService.listAllUsers());
    }

    @PostMapping("/users/{username}/role")
    public ResponseEntity<?> setRole(@RequestHeader(value = "token", required = false) String token,
                                     @PathVariable("username") String username,
                                     @RequestParam("role") String role) {
        User u = userService.getByToken(token);
        if (!isAdmin(u)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        boolean ok = userService.setUserRole(username, role);
        if (!ok) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users/{username}")
    public ResponseEntity<?> deleteUser(@RequestHeader(value = "token", required = false) String token,
                                        @PathVariable("username") String username) {
        User u = userService.getByToken(token);
        if (!isAdmin(u)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        boolean ok = userService.deleteUser(username);
        if (!ok) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/complaints")
    public ResponseEntity<?> listComplaints(@RequestHeader(value = "token", required = false) String token) {
        User u = userService.getByToken(token);
        if (!isAdmin(u)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        java.util.List<Complaint> list = complaintService.listAll();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/complaints/{id}/approve")
    public ResponseEntity<?> approveComplaint(@RequestHeader(value = "token", required = false) String token,
                                              @PathVariable("id") Long id) {
        User u = userService.getByToken(token);
        if (!isAdmin(u)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        boolean ok = complaintService.setStatus(id, "approved");
        if (!ok) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        Complaint c = null;
        for (Complaint cc : complaintService.listAll()) if (cc.getId().equals(id)) { c = cc; break; }
        if (c != null) {
            notificationService.addToUser(c.getUsername(), "complaint", "投诉审核通过", "您对订单" + c.getOrderId() + "的投诉已通过");
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/complaints/{id}/reject")
    public ResponseEntity<?> rejectComplaint(@RequestHeader(value = "token", required = false) String token,
                                             @PathVariable("id") Long id) {
        User u = userService.getByToken(token);
        if (!isAdmin(u)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        boolean ok = complaintService.setStatus(id, "rejected");
        if (!ok) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        Complaint c = null;
        for (Complaint cc : complaintService.listAll()) if (cc.getId().equals(id)) { c = cc; break; }
        if (c != null) {
            notificationService.addToUser(c.getUsername(), "complaint", "投诉审核未通过", "您对订单" + c.getOrderId() + "的投诉未通过");
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/announce")
    public ResponseEntity<?> announce(@RequestHeader(value = "token", required = false) String token,
                                      @RequestBody java.util.Map<String, String> body) {
        User u = userService.getByToken(token);
        if (!isAdmin(u)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String title = body.getOrDefault("title", "平台公告");
        String content = body.getOrDefault("content", "");
        notificationService.addBroadcast("announcement", title, content);
        return ResponseEntity.ok(java.util.Map.of("success", true));
    }
}
