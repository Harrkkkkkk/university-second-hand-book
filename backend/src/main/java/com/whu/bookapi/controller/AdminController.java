package com.whu.bookapi.controller;

import com.whu.bookapi.model.Book;
import com.whu.bookapi.model.User;
import com.whu.bookapi.model.Complaint;
import com.whu.bookapi.model.OperationLog;
import com.whu.bookapi.service.BookService;
import com.whu.bookapi.service.ComplaintService;
import com.whu.bookapi.service.ReviewService;
import com.whu.bookapi.service.UserService;
import com.whu.bookapi.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final UserService userService;
    private final BookService bookService;
    private final ComplaintService complaintService;
    private final NotificationService notificationService;
    private final ReviewService reviewService;

    public AdminController(UserService userService, BookService bookService, ComplaintService complaintService, NotificationService notificationService, ReviewService reviewService) {
        this.userService = userService;
        this.bookService = bookService;
        this.complaintService = complaintService;
        this.notificationService = notificationService;
        this.reviewService = reviewService;
    }

    @GetMapping("/seller-applications")
    public ResponseEntity<?> listSellerApplications(@RequestHeader(value = "token", required = false) String token) {
        User u = userService.getByToken(token);
        if (u == null || !"admin".equals(u.getRole())) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(userService.listSellerApplications());
    }

    @PostMapping("/approve-seller/{username}")
    public ResponseEntity<?> approveSeller(@RequestHeader(value = "token", required = false) String token,
                                           @PathVariable("username") String username) {
        User u = userService.getByToken(token);
        if (u == null || !"admin".equals(u.getRole())) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        userService.approveSeller(username);
        notificationService.add(username, "恭喜，您的卖家资质申请已通过！");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reject-seller/{username}")
    public ResponseEntity<?> rejectSeller(@RequestHeader(value = "token", required = false) String token,
                                          @PathVariable("username") String username) {
        User u = userService.getByToken(token);
        if (u == null || !"admin".equals(u.getRole())) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        userService.rejectSeller(username);
        notificationService.add(username, "很遗憾，您的卖家资质申请未通过。");
        return ResponseEntity.ok().build();
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
    public ResponseEntity<?> listUsers(@RequestHeader(value = "token", required = false) String token,
                                       @RequestParam(value = "keyword", required = false) String keyword) {
        User u = userService.getByToken(token);
        if (!isAdmin(u)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(userService.searchUsers(keyword));
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
        boolean ok = userService.deleteUser(username, u.getUsername());
        if (!ok) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users/{username}")
    public ResponseEntity<?> getUserDetail(@RequestHeader(value = "token", required = false) String token,
                                           @PathVariable("username") String username) {
        User u = userService.getByToken(token);
        if (!isAdmin(u)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        User detail = userService.getUserDetail(username);
        if (detail == null) return ResponseEntity.notFound().build();
        // Enrich with addresses?
        List<Map<String, Object>> addresses = userService.listAddresses(username);
        Map<String, Object> res = new java.util.HashMap<>();
        res.put("user", detail);
        res.put("addresses", addresses);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/users/{username}/status")
    public ResponseEntity<?> updateUserStatus(@RequestHeader(value = "token", required = false) String token,
                                              @PathVariable("username") String username,
                                              @RequestBody Map<String, String> body) {
        User u = userService.getByToken(token);
        if (!isAdmin(u)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String status = body.get("status");
        String reason = body.get("reason");
        String secondAdmin = body.get("secondAdmin");
        String secondAdminPwd = body.get("secondAdminPwd");
        
        Map<String, Object> result = userService.updateUserStatus(username, status, reason, u.getUsername(), secondAdmin, secondAdminPwd);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/users/{username}/undo-blacklist")
    public ResponseEntity<?> undoBlacklist(@RequestHeader(value = "token", required = false) String token,
                                           @PathVariable("username") String username,
                                           @RequestBody(required = false) Map<String, String> body) {
        User u = userService.getByToken(token);
        if (!isAdmin(u)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String reason = body == null ? null : body.get("reason");
        boolean ok = userService.undoBlacklist(username, u.getUsername(), reason);
        if (!ok) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Undo failed or time expired"));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{username}/update")
    public ResponseEntity<?> updateUserInfo(@RequestHeader(value = "token", required = false) String token,
                                            @PathVariable("username") String username,
                                            @RequestBody User body) {
        User u = userService.getByToken(token);
        if (!isAdmin(u)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        body.setUsername(username); // ensure username matches path
        boolean ok = userService.updateUserInfo(body, u.getUsername());
        if (!ok) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users/logs")
    public ResponseEntity<?> getOperationLogs(@RequestHeader(value = "token", required = false) String token,
                                              @RequestParam(value = "keyword", required = false) String keyword,
                                              @RequestParam(value = "targetUser", required = false) String targetUser,
                                              @RequestParam(value = "operator", required = false) String operator,
                                              @RequestParam(value = "startTime", required = false) Long startTime,
                                              @RequestParam(value = "endTime", required = false) Long endTime,
                                              @RequestParam(value = "action", required = false) String action) {
        User u = userService.getByToken(token);
        if (!isAdmin(u)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<OperationLog> logs = userService.getOperationLogs(keyword, targetUser, operator, startTime, endTime, action);
        return ResponseEntity.ok(logs);
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

    @GetMapping("/reviews/pending")
    public ResponseEntity<?> listPendingReviews(@RequestHeader(value = "token", required = false) String token) {
        User u = userService.getByToken(token);
        if (!isAdmin(u)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(reviewService.listPending());
    }

    @PostMapping("/reviews/{id}/audit")
    public ResponseEntity<?> auditReview(@RequestHeader(value = "token", required = false) String token,
                                         @PathVariable("id") Long id,
                                         @RequestBody java.util.Map<String, String> body) {
        User u = userService.getByToken(token);
        if (!isAdmin(u)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String status = body.get("status");
        String reason = body.get("reason");
        boolean ok = reviewService.audit(id, status, reason);
        if (!ok) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reviews/{id}/undo")
    public ResponseEntity<?> undoAuditReview(@RequestHeader(value = "token", required = false) String token,
                                             @PathVariable("id") Long id) {
        User u = userService.getByToken(token);
        if (!isAdmin(u)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        boolean ok = reviewService.undoAudit(id);
        if (!ok) return ResponseEntity.badRequest().body("Undo failed");
        return ResponseEntity.ok().build();
    }
}
