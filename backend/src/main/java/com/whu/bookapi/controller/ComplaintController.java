package com.whu.bookapi.controller;

import com.whu.bookapi.model.Complaint;
import com.whu.bookapi.model.User;
import com.whu.bookapi.service.ComplaintService;
import com.whu.bookapi.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/complaints")
public class ComplaintController {
    private final ComplaintService complaintService;
    private final UserService userService;
    private final com.whu.bookapi.service.OrderService orderService;

    public ComplaintController(ComplaintService complaintService, UserService userService, com.whu.bookapi.service.OrderService orderService) {
        this.complaintService = complaintService;
        this.userService = userService;
        this.orderService = orderService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestHeader(value = "token", required = false) String token,
                                 @RequestBody Complaint complaint) {
        User u = userService.getByToken(token);
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        complaint.setUsername(u.getUsername());
        return ResponseEntity.ok(complaintService.add(complaint));
    }

    @GetMapping("/my")
    public ResponseEntity<?> my(@RequestHeader(value = "token", required = false) String token) {
        User u = userService.getByToken(token);
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(complaintService.listByUser(u.getUsername()));
    }

    @GetMapping("/received")
    public ResponseEntity<?> received(@RequestHeader(value = "token", required = false) String token) {
        User u = userService.getByToken(token);
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        java.util.List<Complaint> all = complaintService.listAll();
        java.util.List<java.util.Map<String, Object>> res = new java.util.ArrayList<>();
        for (Complaint c : all) {
            com.whu.bookapi.model.Order o = orderService.get(c.getOrderId());
            if (o != null && u.getUsername().equals(o.getSellerName())) {
                java.util.Map<String, Object> m = new java.util.HashMap<>();
                m.put("id", c.getId());
                m.put("orderId", c.getOrderId());
                m.put("buyerName", c.getUsername());
                m.put("type", c.getType());
                m.put("detail", c.getDetail());
                m.put("createTime", c.getCreateTime());
                m.put("status", c.getStatus());
                res.add(m);
            }
        }
        return ResponseEntity.ok(res);
    }
}
