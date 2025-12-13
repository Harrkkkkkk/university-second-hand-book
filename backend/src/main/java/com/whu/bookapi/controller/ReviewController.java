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

    public ReviewController(ReviewService reviewService, UserService userService) {
        this.reviewService = reviewService;
        this.userService = userService;
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
}
