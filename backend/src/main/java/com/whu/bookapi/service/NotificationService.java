package com.whu.bookapi.service;

import com.whu.bookapi.model.Notification;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService {
    private final JdbcTemplate jdbcTemplate;

    public NotificationService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Notification addToUser(String username, String type, String title, String content) {
        Notification n = new Notification();
        n.setToUser(username);
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        n.setCreateTime(System.currentTimeMillis());
        n.setRead(false);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO notifications (to_user, type, title, content, create_time, is_read) VALUES (?, ?, ?, ?, ?, 0)";
        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, username);
            ps.setString(2, type);
            ps.setString(3, title);
            ps.setString(4, content);
            ps.setLong(5, n.getCreateTime());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key != null) n.setId(key.longValue());
        return n;
    }

    public Notification add(String username, String content) {
        return addToUser(username, "system", "系统通知", content);
    }

    public Notification addBroadcast(String type, String title, String content) {
        Notification n = new Notification();
        n.setToUser("*");
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        n.setCreateTime(System.currentTimeMillis());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO notifications (to_user, type, title, content, create_time, is_read) VALUES ('*', ?, ?, ?, ?, 0)";
        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, type);
            ps.setString(2, title);
            ps.setString(3, content);
            ps.setLong(4, n.getCreateTime());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key != null) n.setId(key.longValue());
        return n;
    }

    public List<Notification> listByUser(String username) {
        if (username == null) return new ArrayList<>();
        return jdbcTemplate.query(
                "SELECT n.id, n.to_user, n.type, n.title, n.content, n.create_time, n.is_read, nr.username AS read_by " +
                        "FROM notifications n " +
                        "LEFT JOIN notification_read nr ON n.id = nr.notification_id AND nr.username = ? " +
                        "WHERE n.to_user = ? OR n.to_user = '*' " +
                        "ORDER BY n.create_time DESC",
                (rs, rowNum) -> {
                    Notification n = new Notification();
                    n.setId(rs.getLong("id"));
                    n.setToUser(rs.getString("to_user"));
                    n.setType(rs.getString("type"));
                    n.setTitle(rs.getString("title"));
                    n.setContent(rs.getString("content"));
                    n.setCreateTime(rs.getLong("create_time"));
                    boolean isRead = rs.getInt("is_read") != 0;
                    n.setRead(isRead);
                    String readBy = rs.getString("read_by");
                    if ("*".equals(n.getToUser()) && readBy != null) {
                        java.util.Set<String> s = new java.util.HashSet<>();
                        s.add(username);
                        n.setReadByUsers(s);
                    }
                    return n;
                },
                username,
                username
        );
    }

    public long countUnread(String username) {
        if (username == null) return 0;
        Long direct = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM notifications WHERE to_user = ? AND is_read = 0",
                Long.class,
                username
        );
        Long broadcast = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM notifications n LEFT JOIN notification_read nr ON n.id = nr.notification_id AND nr.username = ? WHERE n.to_user = '*' AND nr.username IS NULL",
                Long.class,
                username
        );
        long a = direct == null ? 0 : direct;
        long b = broadcast == null ? 0 : broadcast;
        return a + b;
    }

    public void markRead(Long id, String username) {
        if (id == null || username == null) return;
        String toUser;
        try {
            toUser = jdbcTemplate.queryForObject("SELECT to_user FROM notifications WHERE id = ?", String.class, id);
        } catch (EmptyResultDataAccessException e) {
            return;
        }
        if ("*".equals(toUser)) {
            jdbcTemplate.update(
                    "INSERT IGNORE INTO notification_read (notification_id, username, read_time) VALUES (?, ?, ?)",
                    id,
                    username,
                    System.currentTimeMillis()
            );
        } else {
            jdbcTemplate.update("UPDATE notifications SET is_read = 1 WHERE id = ? AND to_user = ?", id, username);
        }
    }

    public void markAllRead(String username) {
        if (username == null) return;
        jdbcTemplate.update("UPDATE notifications SET is_read = 1 WHERE to_user = ? AND is_read = 0", username);
        jdbcTemplate.update(
                "INSERT IGNORE INTO notification_read (notification_id, username, read_time) " +
                        "SELECT id, ?, ? FROM notifications WHERE to_user = '*'",
                username,
                System.currentTimeMillis()
        );
    }
}
