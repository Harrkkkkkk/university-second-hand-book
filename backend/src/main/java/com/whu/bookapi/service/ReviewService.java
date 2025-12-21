package com.whu.bookapi.service;

import com.whu.bookapi.model.Review;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReviewService {
    private final JdbcTemplate jdbcTemplate;

    private static final java.util.Set<String> SENSITIVE = new java.util.HashSet<>(java.util.Arrays.asList("辱骂", "骚扰", "黄赌毒", "垃圾", "骗子"));

    public ReviewService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private String filter(String s) {
        if (s == null) return null;
        String r = s;
        for (String w : SENSITIVE) {
            r = r.replace(w, "**");
        }
        return r;
    }

    private static String tagsToText(java.util.List<String> tags) {
        if (tags == null || tags.isEmpty()) return null;
        java.util.List<String> cleaned = new java.util.ArrayList<>();
        for (String t : tags) {
            if (t == null) continue;
            String tt = t.trim();
            if (!tt.isEmpty()) cleaned.add(tt);
        }
        if (cleaned.isEmpty()) return null;
        return String.join(",", cleaned);
    }

    private static java.util.List<String> textToTags(String text) {
        if (text == null || text.isEmpty()) return null;
        String[] parts = text.split(",");
        java.util.List<String> tags = new java.util.ArrayList<>();
        for (String p : parts) {
            if (p == null) continue;
            String t = p.trim();
            if (!t.isEmpty()) tags.add(t);
        }
        return tags.isEmpty() ? null : tags;
    }

    public Review add(Review r) {
        if (r == null || r.getOrderId() == null || r.getUsername() == null) return null;
        r.setCreateTime(System.currentTimeMillis());
        r.setComment(filter(r.getComment()));
        String tags = tagsToText(r.getTags());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO reviews (order_id, username, score_condition, score_service, comment, tags, create_time) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, r.getOrderId());
            ps.setString(2, r.getUsername());
            ps.setInt(3, r.getScoreCondition());
            ps.setInt(4, r.getScoreService());
            ps.setString(5, r.getComment());
            ps.setString(6, tags);
            ps.setLong(7, r.getCreateTime());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key != null) r.setId(key.longValue());
        return r;
    }

    public List<Review> listByUser(String username) {
        if (username == null) return new ArrayList<>();
        return jdbcTemplate.query(
                "SELECT id, order_id, username, score_condition, score_service, comment, tags, create_time FROM reviews WHERE username = ? ORDER BY create_time DESC",
                (rs, rowNum) -> {
                    Review r = new Review();
                    r.setId(rs.getLong("id"));
                    r.setOrderId(rs.getLong("order_id"));
                    r.setUsername(rs.getString("username"));
                    r.setScoreCondition(rs.getInt("score_condition"));
                    r.setScoreService(rs.getInt("score_service"));
                    r.setComment(rs.getString("comment"));
                    r.setTags(textToTags(rs.getString("tags")));
                    r.setCreateTime(rs.getLong("create_time"));
                    return r;
                },
                username
        );
    }

    public Review saveDraft(String username, Review r) {
        if (r.getOrderId() == null) return null;
        Review copy = new Review();
        copy.setOrderId(r.getOrderId());
        copy.setUsername(username);
        copy.setScoreCondition(r.getScoreCondition());
        copy.setScoreService(r.getScoreService());
        copy.setComment(r.getComment());
        copy.setTags(r.getTags());
        copy.setCreateTime(System.currentTimeMillis());
        jdbcTemplate.update(
                "INSERT INTO review_draft (username, order_id, score_condition, score_service, comment, tags, create_time) VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE score_condition=VALUES(score_condition), score_service=VALUES(score_service), comment=VALUES(comment), tags=VALUES(tags), create_time=VALUES(create_time)",
                username,
                r.getOrderId(),
                r.getScoreCondition(),
                r.getScoreService(),
                r.getComment(),
                tagsToText(r.getTags()),
                copy.getCreateTime()
        );
        return copy;
    }

    public Review getDraft(String username, Long orderId) {
        if (orderId == null) return null;
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT username, order_id, score_condition, score_service, comment, tags, create_time FROM review_draft WHERE username = ? AND order_id = ?",
                    (rs, rowNum) -> {
                        Review r = new Review();
                        r.setOrderId(rs.getLong("order_id"));
                        r.setUsername(rs.getString("username"));
                        r.setScoreCondition(rs.getInt("score_condition"));
                        r.setScoreService(rs.getInt("score_service"));
                        r.setComment(rs.getString("comment"));
                        r.setTags(textToTags(rs.getString("tags")));
                        r.setCreateTime(rs.getLong("create_time"));
                        return r;
                    },
                    username,
                    orderId
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public java.util.List<Review> listAll() {
        return jdbcTemplate.query(
                "SELECT id, order_id, username, score_condition, score_service, comment, tags, create_time FROM reviews ORDER BY create_time DESC",
                (rs, rowNum) -> {
                    Review r = new Review();
                    r.setId(rs.getLong("id"));
                    r.setOrderId(rs.getLong("order_id"));
                    r.setUsername(rs.getString("username"));
                    r.setScoreCondition(rs.getInt("score_condition"));
                    r.setScoreService(rs.getInt("score_service"));
                    r.setComment(rs.getString("comment"));
                    r.setTags(textToTags(rs.getString("tags")));
                    r.setCreateTime(rs.getLong("create_time"));
                    return r;
                }
        );
    }
}
