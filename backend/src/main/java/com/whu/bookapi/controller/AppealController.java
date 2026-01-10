package com.whu.bookapi.controller;

import com.whu.bookapi.dto.PageResponse;
import com.whu.bookapi.model.BlacklistAppeal;
import com.whu.bookapi.model.User;
import com.whu.bookapi.service.BlacklistAppealService;
import com.whu.bookapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/appeals")
public class AppealController {

    @Autowired
    private BlacklistAppealService appealService;

    @Autowired
    private UserService userService;

    @PostMapping("/submit")
    public ResponseEntity<?> submit(@RequestHeader("token") String token,
                                    @RequestBody Map<String, String> body) {
        User user = userService.getByToken(token);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        String reason = body.get("reason");
        String evidence = body.get("evidence");
        String proofImage = body.get("proofImage");
        
        try {
            BlacklistAppeal appeal = appealService.submit(user.getUsername(), reason, evidence, proofImage);
            return ResponseEntity.ok(appeal);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/admin/list")
    public ResponseEntity<?> list(@RequestHeader("token") String token,
                                  @RequestParam(value = "status", required = false) String status,
                                  @RequestParam(value = "page", defaultValue = "1") int page,
                                  @RequestParam(value = "size", defaultValue = "10") int size) {
        User admin = userService.getByToken(token);
        if (admin == null || !"admin".equals(admin.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Map<String, Object>> list = appealService.list(status, page, size);
        int total = appealService.count(status);
        return ResponseEntity.ok(new PageResponse<>(total, list));
    }

    @PostMapping("/admin/{id}/audit")
    public ResponseEntity<?> audit(@RequestHeader("token") String token,
                                   @PathVariable("id") Long id,
                                   @RequestBody Map<String, String> body) {
        User admin = userService.getByToken(token);
        if (admin == null || !"admin".equals(admin.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String status = body.get("status"); // approved, rejected
        String auditReason = body.get("auditReason");

        if (status == null || (!"approved".equals(status) && !"rejected".equals(status))) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid status"));
        }

        try {
            appealService.audit(id, status, auditReason, admin.getUsername());
            return ResponseEntity.ok(Map.of("message", "Audit successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
