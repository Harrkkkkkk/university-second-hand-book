package com.whu.bookapi.controller;

import com.whu.bookapi.dto.LoginRequest;
import com.whu.bookapi.dto.LoginResponse;
import com.whu.bookapi.model.User;
import com.whu.bookapi.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        LoginResponse resp = userService.login(req);
        if (resp == null) {
            Map<String, Object> m = new HashMap<>();
            m.put("message", "用户名或密码错误");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(m);
        }
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "token", required = false) String token) {
        if (token != null) {
            userService.logout(token);
        }
        Map<String, Object> m = new HashMap<>();
        m.put("success", true);
        return ResponseEntity.ok(m);
    }

    @GetMapping("/info")
    public ResponseEntity<?> info(@RequestHeader(value = "token", required = false) String token) {
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userService.getByToken(token);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Map<String, Object> m = new HashMap<>();
        m.put("username", user.getUsername());
        m.put("role", user.getRole());
        m.put("sellerStatus", user.getSellerStatus());
        return ResponseEntity.ok(m);
    }

    @PostMapping("/apply-seller")
    public ResponseEntity<?> applySeller(@RequestHeader(value = "token", required = false) String token) {
        User user = token == null ? null : userService.getByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        userService.applySeller(user.getUsername());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> body) {
        String username = (String) body.get("username");
        String password = (String) body.get("password");
        Object rolesObj = body.get("roles");
        Set<String> roles = null;
        if (rolesObj instanceof java.util.List) {
            roles = new java.util.HashSet<>((java.util.List<String>) rolesObj);
        }
        boolean ok = userService.register(username, password, roles);
        Map<String, Object> m = new HashMap<>();
        m.put("success", ok);
        if (!ok) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(m);
        return ResponseEntity.ok(m);
    }
}
