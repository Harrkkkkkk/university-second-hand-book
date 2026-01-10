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

/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: ReviewController.java
 * Author: WiseBookPal Team Version: 1.0 Date: 2024-11-20
 * Description: Controller for handling book reviews and seller evaluations.
 * Others:
 * Function List:
 * 1. add - Submit a review
 * 2. my - List my reviews
 * 3. saveDraft - Save a review draft
 * 4. received - List reviews received (for seller)
 * 5. listBySeller - List reviews for a specific seller
 * History:
 * 1. Date: 2024-11-20
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
 */
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

    /**
     * Function: add
     * Description: Submits a new review for an order.
     * Calls: UserService.getByToken, ReviewService.add
     * Called By: Frontend Evaluate Page
     * Table Accessed: user_token, users, reviews
     * Table Updated: reviews
     * Input: token (String) - User token
     *        review (Review) - Review details
     * Output: Review - Created review
     * Return: ResponseEntity<?>
     * Others:
     */
    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestHeader(value = "token", required = false) String token,
                                 @RequestBody Review review) {
        User u = userService.getByToken(token);
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        
        if ("blacklist".equals(u.getStatus())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(java.util.Map.of("message", "账号已黑名单，无法评论。原因：" + (u.getBlacklistReason() != null ? u.getBlacklistReason() : "无")));
        }

        review.setUsername(u.getUsername());
        Review r = reviewService.add(review);
        return ResponseEntity.ok(r);
    }

    /**
     * Function: my
     * Description: Lists reviews submitted by the current user.
     * Calls: UserService.getByToken, ReviewService.listByUser
     * Called By: Frontend User Profile
     * Table Accessed: user_token, users, reviews
     * Table Updated: None
     * Input: token (String) - User token
     * Output: List<Review> - List of reviews
     * Return: ResponseEntity<?>
     * Others:
     */
    @GetMapping("/my")
    public ResponseEntity<?> my(@RequestHeader(value = "token", required = false) String token) {
        User u = userService.getByToken(token);
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(reviewService.listByUser(u.getUsername()));
    }

    /**
     * Function: saveDraft
     * Description: Saves a draft of a review.
     * Calls: UserService.getByToken, ReviewService.saveDraft
     * Called By: Frontend Evaluate Page
     * Table Accessed: user_token, users, reviews
     * Table Updated: reviews
     * Input: token (String) - User token
     *        review (Review) - Draft review
     * Output: Review - Saved draft
     * Return: ResponseEntity<?>
     * Others:
     */
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

    /**
     * Function: received
     * Description: Lists reviews received by the current user (as a seller).
     * Calls: UserService.getByToken, ReviewService.listAll, OrderService.get
     * Called By: Frontend Seller Center
     * Table Accessed: user_token, users, reviews, orders
     * Table Updated: None
     * Input: token (String) - User token
     * Output: List<Map> - List of received reviews
     * Return: ResponseEntity<?>
     * Others: Filters approved reviews linked to orders where the user is the seller.
     */
    @GetMapping("/received")
    public ResponseEntity<?> received(@RequestHeader(value = "token", required = false) String token) {
        User u = userService.getByToken(token);
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<Review> reviews = reviewService.listApprovedBySeller(u.getUsername());
        List<Map<String, Object>> res = new ArrayList<>();
        for (Review r : reviews) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            m.put("orderId", r.getOrderId());
            m.put("buyerName", r.getUsername());
            m.put("scoreCondition", r.getScoreCondition());
            m.put("scoreService", r.getScoreService());
            m.put("comment", r.getComment());
            m.put("createTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(r.getCreateTime())));
            m.put("tags", r.getTags());
            m.put("images", r.getImages());
            res.add(m);
        }
        return ResponseEntity.ok(res);
    }

    /**
     * Function: listBySeller
     * Description: Lists reviews for a specific seller (public view).
     * Calls: ReviewService.listApprovedBySeller
     * Called By: Frontend Seller Detail Page
     * Table Accessed: reviews, orders
     * Table Updated: None
     * Input: sellerName (String) - Seller's username
     * Output: List<Map> - List of reviews
     * Return: ResponseEntity<?>
     * Others:
     */
    @GetMapping("/seller/{sellerName}")
    public ResponseEntity<?> listBySeller(@PathVariable("sellerName") String sellerName) {
        try {
            List<Review> reviews = reviewService.listApprovedBySeller(sellerName);
            List<Map<String, Object>> res = new ArrayList<>();
            for (Review r : reviews) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", r.getId());
                m.put("orderId", r.getOrderId());
                m.put("buyerName", r.getUsername());
                m.put("scoreCondition", r.getScoreCondition());
                m.put("scoreService", r.getScoreService());
                m.put("comment", r.getComment());
                m.put("createTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(r.getCreateTime())));
                m.put("tags", r.getTags());
                m.put("images", r.getImages());
                res.add(m);
            }
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    /**
     * Function: goodRate
     * Description: Calculates seller good review rate where average score >= 4 (4/5 stars).
     * Calls: UserService.getByToken, ReviewService.listAll, OrderService.get
     * Called By: Frontend Seller Center
     * Table Accessed: user_token, users, reviews, orders
     * Table Updated: None
     * Input: token (String)
     * Output: Map - { goodRate, totalReviews, positiveReviews }
     * Return: ResponseEntity<?>
     * Others:
     */
    @GetMapping("/stats/good-rate")
    public ResponseEntity<?> goodRate(@RequestHeader(value = "token", required = false) String token) {
        User u = userService.getByToken(token);
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<Review> reviews = reviewService.listApprovedBySeller(u.getUsername());
        int total = reviews.size();
        int positive = 0;
        for (Review r : reviews) {
            double avg = (r.getScoreCondition() + r.getScoreService()) / 2.0;
            if (avg >= 4.0) positive++;
        }
        double rate = total == 0 ? 0.0 : (positive * 100.0 / total);
        Map<String, Object> res = new HashMap<>();
        res.put("goodRate", Math.round(rate * 10.0) / 10.0);
        res.put("totalReviews", total);
        res.put("positiveReviews", positive);
        return ResponseEntity.ok(res);
    }
}
