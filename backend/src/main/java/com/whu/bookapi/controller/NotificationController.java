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
    private final com.whu.bookapi.service.ChatService chatService;

    public NotificationController(NotificationService notificationService, UserService userService, com.whu.bookapi.service.ChatService chatService) {
        this.notificationService = notificationService;
        this.userService = userService;
        this.chatService = chatService;
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
        
        // Use broadcast mechanism instead of iterating users
        notificationService.addBroadcast("SYSTEM", title, content);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list")
    public ResponseEntity<?> list(@RequestHeader(value = "token", required = false) String token) {
        User u = userService.getByToken(token);
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<Notification> list = notificationService.listByUser(u.getUsername());
        
        List<Map<String, Object>> dtos = new java.util.ArrayList<>();
        for (Notification n : list) {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", n.getId());
            map.put("type", n.getType());
            map.put("title", n.getTitle());
            map.put("content", n.getContent());
            map.put("createTime", n.getCreateTime());
            map.put("read", n.isReadBy(u.getUsername()));
            dtos.add(map);
        }
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> unreadCount(@RequestHeader(value = "token", required = false) String token) {
        User u = userService.getByToken(token);
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        long notifCount = notificationService.countUnread(u.getUsername());
        long chatCount = chatService.countTotalUnread(u.getUsername());
        Map<String, Object> m = new HashMap<>();
        m.put("count", notifCount + chatCount);
        return ResponseEntity.ok(m);
    }

    @PostMapping("/read/{id}")
    public ResponseEntity<?> markRead(@RequestHeader(value = "token", required = false) String token,
                                      @PathVariable Long id) {
        User u = userService.getByToken(token);
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        notificationService.markRead(id, u.getUsername());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<?> markAllRead(@RequestHeader(value = "token", required = false) String token) {
        User u = userService.getByToken(token);
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        notificationService.markAllRead(u.getUsername());
        return ResponseEntity.ok().build();
    }
}
