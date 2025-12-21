package com.whu.bookapi.service;

import com.whu.bookapi.model.Complaint;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ComplaintService {
    private final JdbcTemplate jdbcTemplate;

    public ComplaintService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Complaint add(Complaint c) {
        if (c == null || c.getOrderId() == null || c.getUsername() == null) return null;
        c.setCreateTime(System.currentTimeMillis());
        c.setStatus("pending");
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO complaints (order_id, username, type, detail, create_time, status) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, c.getOrderId());
            ps.setString(2, c.getUsername());
            ps.setString(3, c.getType());
            ps.setString(4, c.getDetail());
            ps.setLong(5, c.getCreateTime());
            ps.setString(6, c.getStatus());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key != null) c.setId(key.longValue());
        return c;
    }

    public List<Complaint> listByUser(String username) {
        List<Complaint> res = new ArrayList<>();
        if (username == null) return res;
        return jdbcTemplate.query(
                "SELECT id, order_id, username, type, detail, create_time, status FROM complaints WHERE username = ? ORDER BY create_time DESC",
                (rs, rowNum) -> {
                    Complaint c = new Complaint();
                    c.setId(rs.getLong("id"));
                    c.setOrderId(rs.getLong("order_id"));
                    c.setUsername(rs.getString("username"));
                    c.setType(rs.getString("type"));
                    c.setDetail(rs.getString("detail"));
                    c.setCreateTime(rs.getLong("create_time"));
                    c.setStatus(rs.getString("status"));
                    return c;
                },
                username
        );
    }

    public List<Complaint> listAll() {
        return jdbcTemplate.query(
                "SELECT id, order_id, username, type, detail, create_time, status FROM complaints ORDER BY create_time DESC",
                (rs, rowNum) -> {
                    Complaint c = new Complaint();
                    c.setId(rs.getLong("id"));
                    c.setOrderId(rs.getLong("order_id"));
                    c.setUsername(rs.getString("username"));
                    c.setType(rs.getString("type"));
                    c.setDetail(rs.getString("detail"));
                    c.setCreateTime(rs.getLong("create_time"));
                    c.setStatus(rs.getString("status"));
                    return c;
                }
        );
    }

    public boolean setStatus(Long id, String status) {
        if (id == null || status == null) return false;
        int updated = jdbcTemplate.update("UPDATE complaints SET status = ? WHERE id = ?", status, id);
        return updated > 0;
    }
}
