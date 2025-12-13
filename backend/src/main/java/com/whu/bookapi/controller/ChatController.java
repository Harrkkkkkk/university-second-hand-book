package com.whu.bookapi.controller;

import com.whu.bookapi.model.ChatMessage;
import com.whu.bookapi.model.User;
import com.whu.bookapi.service.ChatService;
import com.whu.bookapi.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService;
    private final UserService userService;

    public ChatController(ChatService chatService, UserService userService) {
        this.chatService = chatService;
        this.userService = userService;
    }

    @PostMapping("/send")
    public ResponseEntity<?> send(@RequestHeader(value = "token", required = false) String token,
                                  @RequestBody ChatMessage msg) {
        User u = userService.getByToken(token);
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        msg.setFromUser(u.getUsername());
        return ResponseEntity.ok(chatService.send(msg));
    }

    @GetMapping("/history")
    public ResponseEntity<?> history(@RequestHeader(value = "token", required = false) String token,
                                     @RequestParam("peer") String peer,
                                     @RequestParam(value = "bookId", required = false) Long bookId,
                                     @RequestParam(value = "orderId", required = false) Long orderId) {
        User u = userService.getByToken(token);
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<ChatMessage> list = chatService.history(u.getUsername(), peer, bookId, orderId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/conversations")
    public ResponseEntity<?> conversations(@RequestHeader(value = "token", required = false) String token) {
        User u = userService.getByToken(token);
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        java.util.List<java.util.Map<String, Object>> list = chatService.listConversations(u.getUsername());
        return ResponseEntity.ok(list);
    }
}
