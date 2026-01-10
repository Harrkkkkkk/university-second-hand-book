package com.whu.bookapi.service;

import com.whu.bookapi.model.BlacklistAppeal;
import com.whu.bookapi.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

@Service
public class BlacklistAppealService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    public BlacklistAppeal submit(String username, String reason, String evidence, String proofImage) {
        // Check if user is blacklisted
        User user = userService.getUserDetail(username);
        if (user == null || !"blacklist".equals(user.getStatus())) {
            throw new IllegalArgumentException("Only blacklisted users can submit appeals.");
        }

        // Check if there is a pending appeal
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM blacklist_appeals WHERE username = ? AND status = 'pending'",
                Integer.class, username
        );
        if (count != null && count > 0) {
            throw new IllegalStateException("You already have a pending appeal.");
        }

        BlacklistAppeal appeal = new BlacklistAppeal();
        appeal.setUsername(username);
        appeal.setReason(reason);
        appeal.setEvidence(evidence);
        appeal.setProofImage(proofImage);
        appeal.setStatus("pending");
        appeal.setCreateTime(System.currentTimeMillis());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO blacklist_appeals (username, reason, evidence, proof_image, status, create_time) VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, appeal.getUsername());
            ps.setString(2, appeal.getReason());
            ps.setString(3, appeal.getEvidence());
            ps.setString(4, appeal.getProofImage());
            ps.setString(5, appeal.getStatus());
            ps.setLong(6, appeal.getCreateTime());
            return ps;
        }, keyHolder);

        appeal.setId(keyHolder.getKey().longValue());
        
        // Notify admins (optional, but good for real-world)
        // notificationService.notifyAdmins("New Blacklist Appeal", "User " + username + " submitted an appeal.");
        
        return appeal;
    }

    public List<Map<String, Object>> list(String status, int page, int size) {
        int offset = (page - 1) * size;
        String sql = "SELECT id, username, reason, evidence, proof_image AS proofImage, status, create_time AS createTime, audit_reason AS auditReason, audit_time AS auditTime, auditor FROM blacklist_appeals";
        if (status != null && !status.isEmpty()) {
            sql += " WHERE status = ?";
            sql += " ORDER BY create_time DESC LIMIT ? OFFSET ?";
            return jdbcTemplate.queryForList(sql, status, size, offset);
        } else {
            sql += " ORDER BY create_time DESC LIMIT ? OFFSET ?";
            return jdbcTemplate.queryForList(sql, size, offset);
        }
    }

    public int count(String status) {
        String sql = "SELECT COUNT(*) FROM blacklist_appeals";
        if (status != null && !status.isEmpty()) {
            sql += " WHERE status = ?";
            return jdbcTemplate.queryForObject(sql, Integer.class, status);
        } else {
            return jdbcTemplate.queryForObject(sql, Integer.class);
        }
    }

    @Transactional
    public void audit(Long id, String status, String auditReason, String auditor) {
        BlacklistAppeal appeal = get(id);
        if (appeal == null) {
            throw new IllegalArgumentException("Appeal not found");
        }
        if (!"pending".equals(appeal.getStatus())) {
            throw new IllegalStateException("Appeal already processed");
        }

        long now = System.currentTimeMillis();
        jdbcTemplate.update(
                "UPDATE blacklist_appeals SET status = ?, audit_reason = ?, audit_time = ?, auditor = ? WHERE id = ?",
                status, auditReason, now, auditor, id
        );

        if ("approved".equals(status)) {
            // Restore user status to normal
            userService.updateUserStatus(appeal.getUsername(), "normal", "Appeal approved: " + auditReason, auditor, null, null);
            
            // Send notification
            notificationService.addToUser(
                    appeal.getUsername(),
                    "system",
                    "申诉通过，黑名单已解除",
                    "您的申诉已通过审核，黑名单限制已解除。审核意见：" + auditReason
            );
        } else if ("rejected".equals(status)) {
            // Send notification
            notificationService.addToUser(
                    appeal.getUsername(),
                    "system",
                    "申诉未通过",
                    "很遗憾，您的申诉未通过审核。原因：" + auditReason
            );
        }
    }

    public BlacklistAppeal get(Long id) {
        List<BlacklistAppeal> list = jdbcTemplate.query(
                "SELECT * FROM blacklist_appeals WHERE id = ?",
                (rs, rowNum) -> {
                    BlacklistAppeal a = new BlacklistAppeal();
                    a.setId(rs.getLong("id"));
                    a.setUsername(rs.getString("username"));
                    a.setReason(rs.getString("reason"));
                    a.setEvidence(rs.getString("evidence"));
                    a.setStatus(rs.getString("status"));
                    a.setCreateTime(rs.getLong("create_time"));
                    a.setAuditReason(rs.getString("audit_reason"));
                    a.setAuditTime(rs.getLong("audit_time"));
                    a.setAuditor(rs.getString("auditor"));
                    return a;
                },
                id
        );
        return list.isEmpty() ? null : list.get(0);
    }
}
