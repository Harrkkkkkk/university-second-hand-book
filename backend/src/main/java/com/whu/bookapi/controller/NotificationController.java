package com.whu.bookapi.controller;

import com.whu.bookapi.model.Notification;
import com.whu.bookapi.model.User;
import com.whu.bookapi.service.NotificationService;
import com.whu.bookapi.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @PostMapping("/announce")
    public ResponseEntity<?> announce(@RequestHeader(value = "token", required = false) String token,
                                      @RequestBody Map<String, String> body) {
        User u = userService.getByToken(token);
        if (u == null || !"admin".equals(u.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String title = body.get("title");
        String content = body.get("content");
        if (title == null || content == null) return ResponseEntity.badRequest().build();
        
        // Notify all users (naive implementation: iterate all known users)
        List<User> all = userService.listAllUsers();
        for (User user : all) {
            notificationService.addToUser(user.getUsername(), "SYSTEM", title, content);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list")
    public ResponseEntity<?> list(@RequestHeader(value = "token", required = false) String token) {
        User u = userService.getByToken(token);
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<Notification> list = notificationService.listByUser(u.getUsername());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> unreadCount(@RequestHeader(value = "token", required = false) String token) {
        User u = userService.getByToken(token);
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        long count = notificationService.countUnread(u.getUsername());
        Map<String, Object> m = new HashMap<>();
        m.put("count", count);
        return ResponseEntity.ok(m);
    }

    @PostMapping("/read/{id}")
    public ResponseEntity<?> markRead(@RequestHeader(value = "token", required = false) String token,
                                      @PathVariable("id") Long id) {
        User u = userService.getByToken(token);
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        notificationService.markRead(id);
        return ResponseEntity.ok().build();
    }
}
