package com.whu.bookapi.controller;

import com.whu.bookapi.model.Review;
import com.whu.bookapi.model.User;
import com.whu.bookapi.service.ReviewService;
import com.whu.bookapi.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;
    private final UserService userService;
    private final com.whu.bookapi.service.OrderService orderService;

    public ReviewController(ReviewService reviewService, UserService userService, com.whu.bookapi.service.OrderService orderService) {
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

    @GetMapping("/draft/my")
    public ResponseEntity<?> getDraft(@RequestHeader(value = "token", required = false) String token,
                                      @RequestParam("orderId") Long orderId) {
        User u = userService.getByToken(token);
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Review r = reviewService.getDraft(u.getUsername(), orderId);
        if (r == null) return ResponseEntity.ok().build();
        return ResponseEntity.ok(r);
    }

    @GetMapping("/received")
    public ResponseEntity<?> received(@RequestHeader(value = "token", required = false) String token) {
        User u = userService.getByToken(token);
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        java.util.List<Review> all = reviewService.listAll();
        java.util.List<java.util.Map<String, Object>> res = new java.util.ArrayList<>();
        for (Review r : all) {
            com.whu.bookapi.model.Order o = orderService.get(r.getOrderId());
            if (o != null && u.getUsername().equals(o.getSellerName())) {
                java.util.Map<String, Object> m = new java.util.HashMap<>();
                m.put("id", r.getId());
                m.put("orderId", r.getOrderId());
                m.put("buyerName", r.getUsername());
                m.put("scoreCondition", r.getScoreCondition());
                m.put("scoreService", r.getScoreService());
                m.put("comment", r.getComment());
                m.put("createTime", r.getCreateTime());
                m.put("tags", r.getTags());
                res.add(m);
            }
        }
        return ResponseEntity.ok(res);
    }

    @GetMapping("/stats/good-rate")
    public ResponseEntity<?> goodRate(@RequestHeader(value = "token", required = false) String token) {
        User u = userService.getByToken(token);
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        int total = 0;
        int positive = 0;
        for (Review r : reviewService.listAll()) {
            com.whu.bookapi.model.Order o = orderService.get(r.getOrderId());
            if (o != null && u.getUsername().equals(o.getSellerName())) {
                total++;
                double avg = (r.getScoreCondition() + r.getScoreService()) / 2.0;
                if (avg >= 4.0) positive++;
            }
        }
        double rate = total == 0 ? 0.0 : (positive * 100.0 / total);
        java.util.Map<String, Object> res = new java.util.HashMap<>();
        res.put("totalReviews", total);
        res.put("positiveReviews", positive);
        res.put("goodRate", rate);
        return ResponseEntity.ok(res);
    }
}
