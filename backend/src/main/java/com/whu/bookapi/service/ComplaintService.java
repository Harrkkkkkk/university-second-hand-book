package com.whu.bookapi.service;

import com.whu.bookapi.model.Complaint;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: ComplaintService.java
 * Author: WiseBookPal Team Version: 1.0 Date: 2024-11-20
 * Description: Service class for managing user complaints.
 *              Handles complaint submission, retrieval, and status updates (resolution).
 * History:
 * <author>          <time>          <version>          <desc>
 * WiseBookPal Team  2024-11-20      1.0                Initial implementation.
 */
@Service
public class ComplaintService {
    private final JdbcTemplate jdbcTemplate;

    public ComplaintService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static String listToText(java.util.List<String> list) {
        if (list == null || list.isEmpty()) return null;
        java.util.List<String> cleaned = new ArrayList<>();
        for (String t : list) {
            if (t == null) continue;
            String tt = t.trim();
            if (!tt.isEmpty()) cleaned.add(tt);
        }
        if (cleaned.isEmpty()) return null;
        return String.join(",", cleaned);
    }

    private static java.util.List<String> textToList(String text) {
        if (text == null || text.isEmpty()) return null;
        String[] parts = text.split(",");
        java.util.List<String> list = new ArrayList<>();
        for (String p : parts) {
            if (p == null) continue;
            String t = p.trim();
            if (!t.isEmpty()) list.add(t);
        }
        return list.isEmpty() ? null : list;
    }

    /**
     * Function: add
     * Description: Submits a new complaint for an order.
     *              Sets initial status to 'pending'.
     * Called By: ComplaintController.add
     * Table Accessed: complaints
     * Table Updated: complaints
     * Input: c (Complaint) - The complaint object containing order ID, type, and details
     * Output: Complaint - The created complaint object with generated ID
     * Return: Complaint
     */
    public Complaint add(Complaint c) {
        if (c == null || c.getOrderId() == null || c.getUsername() == null) return null;
        c.setCreateTime(System.currentTimeMillis());
        c.setStatus("pending");
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO complaints (order_id, username, type, detail, images, create_time, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, c.getOrderId());
            ps.setString(2, c.getUsername());
            ps.setString(3, c.getType());
            ps.setString(4, c.getDetail());
            ps.setString(5, listToText(c.getImages()));
            ps.setLong(6, c.getCreateTime());
            ps.setString(7, c.getStatus());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key != null) c.setId(key.longValue());
        return c;
    }

    /**
     * Function: listByUser
     * Description: Retrieves all complaints submitted by a specific user.
     * Called By: ComplaintController.listByUser
     * Table Accessed: complaints
     * Input: username (String) - The username of the complainant
     * Output: List<Complaint> - List of complaints
     * Return: List<Complaint>
     */
    public List<Complaint> listByUser(String username) {
        List<Complaint> res = new ArrayList<>();
        if (username == null) return res;
        return jdbcTemplate.query(
                "SELECT id, order_id, username, type, detail, images, create_time, status FROM complaints WHERE username = ? ORDER BY create_time DESC",
                (rs, rowNum) -> {
                    Complaint c = new Complaint();
                    c.setId(rs.getLong("id"));
                    c.setOrderId(rs.getLong("order_id"));
                    c.setUsername(rs.getString("username"));
                    c.setType(rs.getString("type"));
                    c.setDetail(rs.getString("detail"));
                    c.setImages(textToList(rs.getString("images")));
                    c.setCreateTime(rs.getLong("create_time"));
                    c.setStatus(rs.getString("status"));
                    return c;
                },
                username
        );
    }

    /**
     * Function: listAll
     * Description: Retrieves all complaints in the system.
     *              Used for administrative review.
     * Called By: AdminController.listComplaints
     * Table Accessed: complaints
     * Input: None
     * Output: List<Complaint> - List of all complaints
     * Return: List<Complaint>
     */
    public List<Complaint> listAll() {
        return jdbcTemplate.query(
                "SELECT id, order_id, username, type, detail, images, create_time, status, audit_reason, audit_time FROM complaints ORDER BY create_time DESC",
                (rs, rowNum) -> {
                    Complaint c = new Complaint();
                    c.setId(rs.getLong("id"));
                    c.setOrderId(rs.getLong("order_id"));
                    c.setUsername(rs.getString("username"));
                    c.setType(rs.getString("type"));
                    c.setDetail(rs.getString("detail"));
                    c.setImages(textToList(rs.getString("images")));
                    c.setCreateTime(rs.getLong("create_time"));
                    c.setStatus(rs.getString("status"));
                    c.setAuditReason(rs.getString("audit_reason"));
                    c.setAuditTime(rs.getObject("audit_time") == null ? null : rs.getLong("audit_time"));
                    return c;
                }
        );
    }

    /**
     * Function: setStatus
     * Description: Updates the status of a complaint (e.g., to 'resolved').
     * Called By: AdminController.resolveComplaint
     * Table Accessed: complaints
     * Table Updated: complaints
     * Input: id (Long) - The ID of the complaint
     *        status (String) - The new status
     * Output: boolean - True if successful
     * Return: boolean
     */
    public boolean setStatus(Long id, String status) {
        if (id == null || status == null) return false;
        int updated = jdbcTemplate.update("UPDATE complaints SET status = ? WHERE id = ?", status, id);
        return updated > 0;
    }

    public boolean hasPendingComplaint(Long orderId) {
        if (orderId == null) return false;
        Integer cnt = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM complaints WHERE order_id = ? AND status <> 'resolved'",
                Integer.class,
                orderId
        );
        return cnt != null && cnt > 0;
    }

    /**
     * Function: audit
     * Description: Updates complaint status with audit metadata.
     * Input: id, status, reason
     * Return: boolean
     */
    public boolean audit(Long id, String status, String reason) {
        if (id == null || status == null) return false;
        long now = System.currentTimeMillis();
        int updated = jdbcTemplate.update(
                "UPDATE complaints SET status = ?, audit_reason = ?, audit_time = ? WHERE id = ?",
                status, reason, now, id
        );
        return updated > 0;
    }

    /**
     * Function: undoAudit
     * Description: Reverts complaint audit within 24 hours.
     * Return: boolean
     */
    public boolean undoAudit(Long id) {
        if (id == null) return false;
        long now = System.currentTimeMillis();
        long limit = now - 24L * 60 * 60 * 1000;
        int updated = jdbcTemplate.update(
                "UPDATE complaints SET status = 'pending', audit_reason = NULL, audit_time = NULL WHERE id = ? AND audit_time IS NOT NULL AND audit_time >= ?",
                id, limit
        );
        return updated > 0;
    }
}
