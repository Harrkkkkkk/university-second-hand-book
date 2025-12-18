package com.whu.bookapi.controller;

import com.whu.bookapi.model.Order;
import com.whu.bookapi.model.Review;
import com.whu.bookapi.model.User;
import com.whu.bookapi.service.OrderService;
import com.whu.bookapi.service.ReviewService;
import com.whu.bookapi.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;
    private final UserService userService;
    private final OrderService orderService;

    public ReviewController(ReviewService reviewService, UserService userService, OrderService orderService) {
        this.reviewService = reviewService;
        this.userService = userService;
        this.orderService = orderService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestHeader(value = "token", required = false) String token,
                                 @RequestBody Review review) {
        User u = userService.getByToken(token);
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        review.setUsername(u.getUsername());
        Review r = reviewService.add(review);
        return ResponseEntity.ok(r);
    }

    @GetMapping("/my")
    public ResponseEntity<?> my(@RequestHeader(value = "token", required = false) String token) {
        User u = userService.getByToken(token);
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(reviewService.listByUser(u.getUsername()));
    }

    @PostMapping("/draft/save")
    public ResponseEntity<?> saveDraft(@RequestHeader(value = "token", required = false) String token,
                                       @RequestBody Review review) {
        User u = userService.getByToken(token);
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        review.setUsername(u.getUsername());
        Review r = reviewService.saveDraft(u.getUsername(), review);
        if (r == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        return ResponseEntity.ok(r);
    }

    @GetMapping("/received")
    public ResponseEntity<?> received(@RequestHeader(value = "token", required = false) String token) {
        User u = userService.getByToken(token);
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<Review> all = reviewService.listAll();
        List<Map<String, Object>> res = new ArrayList<>();
        for (Review r : all) {
            if (r.getOrderId() == null) continue;
            Order o = orderService.get(r.getOrderId());
            if (o != null && u.getUsername().equals(o.getSellerName())) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", r.getId());
                m.put("orderId", r.getOrderId());
                m.put("buyerName", r.getUsername());
                m.put("scoreCondition", r.getScoreCondition());
                m.put("scoreService", r.getScoreService());
                m.put("comment", r.getComment());
                m.put("createTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(r.getCreateTime())));
                m.put("tags", r.getTags());
                res.add(m);
            }
        }
        return ResponseEntity.ok(res);
    }

    @GetMapping("/seller/{sellerName}")
    public ResponseEntity<?> listBySeller(@PathVariable String sellerName) {
        List<Review> all = reviewService.listAll();
        List<Map<String, Object>> res = new ArrayList<>();
        for (Review r : all) {
            if (r.getOrderId() == null) continue;
            Order o = orderService.get(r.getOrderId());
            if (o != null && sellerName.equals(o.getSellerName())) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", r.getId());
                m.put("orderId", r.getOrderId());
                m.put("buyerName", r.getUsername());
                m.put("scoreCondition", r.getScoreCondition());
                m.put("scoreService", r.getScoreService());
                m.put("comment", r.getComment());
                m.put("createTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(r.getCreateTime())));
                m.put("tags", r.getTags());
                res.add(m);
            }
        }
        return ResponseEntity.ok(res);
    }
}
