package com.whu.bookapi.service;

import com.whu.bookapi.model.Review;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ReviewService {
    private final ConcurrentHashMap<Long, Review> store = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);
    private final ConcurrentHashMap<String, Review> drafts = new ConcurrentHashMap<>();

    private static final java.util.Set<String> SENSITIVE = new java.util.HashSet<>(java.util.Arrays.asList("辱骂", "骚扰", "黄赌毒", "垃圾", "骗子"));

    private String filter(String s) {
        if (s == null) return null;
        String r = s;
        for (String w : SENSITIVE) {
            r = r.replace(w, "**");
        }
        return r;
    }

    public Review add(Review r) {
        r.setId(idGen.incrementAndGet());
        r.setCreateTime(System.currentTimeMillis());
        r.setComment(filter(r.getComment()));
        store.put(r.getId(), r);
        return r;
    }

    public List<Review> listByUser(String username) {
        List<Review> res = new ArrayList<>();
        for (Review r : store.values()) if (username.equals(r.getUsername())) res.add(r);
        return res;
    }

    public Review saveDraft(String username, Review r) {
        if (r.getOrderId() == null) return null;
        String key = username + ":" + r.getOrderId();
        Review copy = new Review();
        copy.setOrderId(r.getOrderId());
        copy.setUsername(username);
        copy.setScoreCondition(r.getScoreCondition());
        copy.setScoreService(r.getScoreService());
        copy.setComment(r.getComment());
        copy.setCreateTime(System.currentTimeMillis());
        drafts.put(key, copy);
        return copy;
    }

    public Review getDraft(String username, Long orderId) {
        if (orderId == null) return null;
        String key = username + ":" + orderId;
        return drafts.get(key);
    }
}
