package com.whu.bookapi.service;

import com.whu.bookapi.model.ChatMessage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {
    private final JdbcTemplate jdbcTemplate;

    public ChatService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private String key(String a, String b, Long bookId, Long orderId) {
        String u1 = a.compareTo(b) <= 0 ? a : b;
        String u2 = a.compareTo(b) <= 0 ? b : a;
        return (bookId != null ? "B" + bookId : "O" + (orderId == null ? 0 : orderId)) + ":" + u1 + ":" + u2;
    }

    public ChatMessage send(ChatMessage m) {
        if (m == null || m.getFromUser() == null || m.getToUser() == null) return null;
        m.setCreateTime(System.currentTimeMillis());
        m.setRead(false);
        String k = key(m.getFromUser(), m.getToUser(), m.getBookId(), m.getOrderId());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO chat_message (conv_key, from_user, to_user, book_id, order_id, content, create_time, is_read) VALUES (?, ?, ?, ?, ?, ?, ?, 0)";
        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, k);
            ps.setString(2, m.getFromUser());
            ps.setString(3, m.getToUser());
            if (m.getBookId() == null) ps.setObject(4, null);
            else ps.setLong(4, m.getBookId());
            if (m.getOrderId() == null) ps.setObject(5, null);
            else ps.setLong(5, m.getOrderId());
            ps.setString(6, m.getContent());
            ps.setLong(7, m.getCreateTime());
            return ps;
        }, keyHolder);
        Number id = keyHolder.getKey();
        if (id != null) m.setId(id.longValue());
        return m;
    }

    public void markRead(String username, String peer) {
        if (username == null || peer == null) return;
        jdbcTemplate.update(
                "UPDATE chat_message SET is_read = 1 WHERE to_user = ? AND from_user = ? AND is_read = 0",
                username,
                peer
        );
    }

    public long countTotalUnread(String username) {
        if (username == null) return 0;
        Long c = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM chat_message WHERE to_user = ? AND is_read = 0",
                Long.class,
                username
        );
        return c == null ? 0 : c;
    }

    public List<ChatMessage> history(String a, String b, Long bookId, Long orderId) {
        String k = key(a, b, bookId, orderId);
        return jdbcTemplate.query(
                "SELECT id, from_user, to_user, book_id, order_id, content, create_time, is_read FROM chat_message WHERE conv_key = ? ORDER BY create_time ASC, id ASC",
                (rs, rowNum) -> {
                    ChatMessage m = new ChatMessage();
                    m.setId(rs.getLong("id"));
                    m.setFromUser(rs.getString("from_user"));
                    m.setToUser(rs.getString("to_user"));
                    Object bid = rs.getObject("book_id");
                    if (bid != null) m.setBookId(((Number) bid).longValue());
                    Object oid = rs.getObject("order_id");
                    if (oid != null) m.setOrderId(((Number) oid).longValue());
                    m.setContent(rs.getString("content"));
                    m.setCreateTime(rs.getLong("create_time"));
                    m.setRead(rs.getInt("is_read") != 0);
                    return m;
                },
                k
        );
    }

    public java.util.List<java.util.Map<String, Object>> listConversations(String username) {
        if (username == null) return new java.util.ArrayList<>();
        java.util.List<java.util.Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT agg.conv_key, agg.last_id, agg.last_time, agg.unread, m.content AS last_content " +
                        "FROM (" +
                        "  SELECT conv_key, MAX(id) AS last_id, MAX(create_time) AS last_time, " +
                        "         SUM(CASE WHEN to_user = ? AND is_read = 0 THEN 1 ELSE 0 END) AS unread " +
                        "  FROM chat_message " +
                        "  WHERE from_user = ? OR to_user = ? " +
                        "  GROUP BY conv_key" +
                        ") agg " +
                        "JOIN chat_message m ON m.id = agg.last_id " +
                        "ORDER BY agg.last_time DESC",
                username,
                username,
                username
        );

        java.util.List<java.util.Map<String, Object>> res = new java.util.ArrayList<>();
        for (java.util.Map<String, Object> row : rows) {
            String k = (String) row.get("conv_key");
            String[] parts = k == null ? null : k.split(":", 3);
            if (parts == null || parts.length < 3) continue;
            String ctx = parts[0];
            String u1 = parts[1];
            String u2 = parts[2];
            if (!username.equals(u1) && !username.equals(u2)) continue;
            String peer = username.equals(u1) ? u2 : u1;
            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("peer", peer);
            if (ctx.startsWith("B")) item.put("bookId", Long.parseLong(ctx.substring(1)));
            else if (ctx.startsWith("O")) item.put("orderId", Long.parseLong(ctx.substring(1)));
            item.put("lastContent", row.get("last_content"));
            item.put("lastTime", ((Number) row.getOrDefault("last_time", 0L)).longValue());
            item.put("unread", ((Number) row.getOrDefault("unread", 0)).intValue());
            res.add(item);
        }
        return res;
    }
}
