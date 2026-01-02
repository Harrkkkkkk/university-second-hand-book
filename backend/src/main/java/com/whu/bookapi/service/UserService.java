package com.whu.bookapi.service;

import com.whu.bookapi.dto.LoginRequest;
import com.whu.bookapi.dto.LoginResponse;
import com.whu.bookapi.model.User;
import com.whu.bookapi.model.OperationLog;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.context.annotation.Lazy;

import java.util.UUID;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@Service
public class UserService {
    private final JdbcTemplate jdbcTemplate;
    private final NotificationService notificationService;
    private final OrderService orderService;

    public UserService(JdbcTemplate jdbcTemplate, @Lazy NotificationService notificationService, @Lazy OrderService orderService) {
        this.jdbcTemplate = jdbcTemplate;
        this.notificationService = notificationService;
        this.orderService = orderService;
    }

    private static String resolveRole(String currentRole, java.util.Set<String> roles, boolean loginPriority) {
        if (currentRole != null && !currentRole.isBlank()) return currentRole;
        if (roles == null || roles.isEmpty()) return null;
        if (loginPriority) {
            if (roles.contains("admin")) return "admin";
            if (roles.contains("seller")) return "seller";
            if (roles.contains("buyer")) return "buyer";
            return null;
        }
        if (roles.contains("buyer")) return "buyer";
        if (roles.contains("seller")) return "seller";
        if (roles.contains("admin")) return "admin";
        return null;
    }

    public LoginResponse login(LoginRequest req) {
        if (req == null || req.getUsername() == null || req.getPassword() == null) return null;
        java.util.List<java.util.Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT username, password, current_role, seller_status FROM users WHERE username = ?",
                req.getUsername()
        );
        if (rows.isEmpty()) return null;
        java.util.Map<String, Object> row = rows.get(0);
        String pwd = (String) row.get("password");
        if (pwd == null || !pwd.equals(req.getPassword())) return null;
        String currentRole = (String) row.get("current_role");
        String sellerStatus = (String) row.get("seller_status");
        java.util.Set<String> roles = new java.util.HashSet<>(jdbcTemplate.queryForList(
                "SELECT role FROM user_roles WHERE username = ?",
                String.class,
                req.getUsername()
        ));
        String role = resolveRole(currentRole, roles, true);
        if (role == null) return null;

        String token = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();
        jdbcTemplate.update(
                "INSERT INTO user_token (token, username, created_at) VALUES (?, ?, ?)",
                token,
                req.getUsername(),
                now
        );
        jdbcTemplate.update("UPDATE users SET last_login_time = ? WHERE username = ?", now, req.getUsername());

        LoginResponse resp = new LoginResponse();
        resp.setToken(token);
        resp.setUsername(req.getUsername());
        resp.setRole(role);
        resp.setSellerStatus(sellerStatus == null ? "NONE" : sellerStatus);
        return resp;
    }

    public void logout(String token) {
        if (token == null) return;
        jdbcTemplate.update("DELETE FROM user_token WHERE token = ?", token);
    }

    public User getByToken(String token) {
        if (token == null) return null;
        java.util.List<java.util.Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT u.username, u.current_role, u.seller_status, u.phone, u.email, u.gender FROM user_token t JOIN users u ON t.username = u.username WHERE t.token = ?",
                token
        );
        if (rows.isEmpty()) return null;
        java.util.Map<String, Object> row = rows.get(0);
        String username = (String) row.get("username");
        String currentRole = (String) row.get("current_role");
        String sellerStatus = (String) row.get("seller_status");
        String phone = row.get("phone") == null ? null : row.get("phone").toString();
        String email = row.get("email") == null ? null : row.get("email").toString();
        String gender = row.get("gender") == null ? null : row.get("gender").toString();
        java.util.Set<String> roles = new java.util.HashSet<>(jdbcTemplate.queryForList(
                "SELECT role FROM user_roles WHERE username = ?",
                String.class,
                username
        ));
        String role = resolveRole(currentRole, roles, true);
        if (role == null) return null;
        User u = new User();
        u.setUsername(username);
        u.setRole(role);
        u.setToken(token);
        u.setSellerStatus(sellerStatus == null ? "NONE" : sellerStatus);
        u.setPhone(phone);
        u.setEmail(email);
        u.setGender(gender == null ? "secret" : gender);
        return u;
    }

    public boolean updateProfile(String username, String phone, String email, String gender) {
        if (username == null) return false;
        String g = gender;
        if (!"male".equals(g) && !"female".equals(g) && !"secret".equals(g)) {
            g = "secret";
        }
        jdbcTemplate.update(
                "UPDATE users SET phone = ?, email = ?, gender = ? WHERE username = ?",
                phone == null || phone.isBlank() ? null : phone,
                email == null || email.isBlank() ? null : email,
                g,
                username
        );
        return true;
    }

    public boolean changePassword(String username, String oldPassword, String newPassword) {
        if (username == null || oldPassword == null || newPassword == null) return false;
        if (newPassword.isBlank()) return false;
        java.util.List<String> pwds = jdbcTemplate.queryForList(
                "SELECT password FROM users WHERE username = ?",
                String.class,
                username
        );
        if (pwds.isEmpty()) return false;
        String pwd = pwds.get(0);
        if (pwd == null || !pwd.equals(oldPassword)) return false;
        int updated = jdbcTemplate.update(
                "UPDATE users SET password = ? WHERE username = ?",
                newPassword,
                username
        );
        return updated > 0;
    }

    public java.util.List<java.util.Map<String, Object>> listAddresses(String username) {
        if (username == null) return new java.util.ArrayList<>();
        java.util.List<java.util.Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, receiver_name, phone, address, is_default FROM user_address WHERE username = ? ORDER BY is_default DESC, updated_at DESC, id DESC",
                username
        );
        java.util.List<java.util.Map<String, Object>> res = new java.util.ArrayList<>();
        for (java.util.Map<String, Object> row : rows) {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            Object idObj = row.get("id");
            Long id = idObj instanceof Number ? ((Number) idObj).longValue() : null;
            m.put("id", id);
            m.put("name", row.get("receiver_name"));
            m.put("phone", row.get("phone"));
            m.put("address", row.get("address"));
            Object defObj = row.get("is_default");
            boolean isDefault = defObj instanceof Boolean ? (Boolean) defObj : (defObj instanceof Number && ((Number) defObj).intValue() != 0);
            m.put("isDefault", isDefault);
            res.add(m);
        }
        return res;
    }

    public Long addAddress(String username, String name, String phone, String address, Boolean isDefault) {
        if (username == null) return null;
        if (name == null || name.isBlank()) return null;
        if (address == null || address.isBlank()) return null;
        boolean makeDefault = Boolean.TRUE.equals(isDefault);
        Long cnt = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM user_address WHERE username = ?",
                Long.class,
                username
        );
        if (cnt != null && cnt == 0) {
            makeDefault = true;
        }
        final boolean def = makeDefault;
        if (def) {
            jdbcTemplate.update("UPDATE user_address SET is_default = 0 WHERE username = ?", username);
        }
        long now = System.currentTimeMillis();
        org.springframework.jdbc.support.KeyHolder keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
        String sql = "INSERT INTO user_address (username, receiver_name, phone, address, is_default, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, username);
            ps.setString(2, name);
            ps.setString(3, phone == null || phone.isBlank() ? null : phone);
            ps.setString(4, address);
            ps.setInt(5, def ? 1 : 0);
            ps.setLong(6, now);
            ps.setLong(7, now);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? null : key.longValue();
    }

    public boolean updateAddress(String username, Long id, String name, String phone, String address, Boolean isDefault) {
        if (username == null || id == null) return false;
        if (name == null || name.isBlank()) return false;
        if (address == null || address.isBlank()) return false;
        boolean makeDefault = Boolean.TRUE.equals(isDefault);
        
        // check ownership
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM user_address WHERE id = ? AND username = ?", Long.class, id, username);
        if (count == null || count == 0) return false;

        if (makeDefault) {
            jdbcTemplate.update("UPDATE user_address SET is_default = 0 WHERE username = ?", username);
        }

        jdbcTemplate.update(
                "UPDATE user_address SET receiver_name = ?, phone = ?, address = ?, is_default = ?, updated_at = ? WHERE id = ?",
                name,
                phone == null || phone.isBlank() ? null : phone,
                address,
                makeDefault ? 1 : 0,
                System.currentTimeMillis(),
                id
        );
        return true;
    }

    public boolean deleteAddress(String username, Long id) {
        if (username == null || id == null) return false;
        int rows = jdbcTemplate.update("DELETE FROM user_address WHERE id = ? AND username = ?", id, username);
        return rows > 0;
    }

    // --- U14 User Management Methods ---

    public List<User> searchUsers(String keyword) {
        String sql = "SELECT username, phone, student_id, status, credit_score, created_at, last_login_time FROM users";
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql += " WHERE username LIKE ? OR phone LIKE ? OR student_id LIKE ?";
            String k = "%" + keyword + "%";
            params.add(k);
            params.add(k);
            params.add(k);
        }
        sql += " ORDER BY created_at DESC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            User u = new User();
            u.setUsername(rs.getString("username"));
            u.setPhone(rs.getString("phone"));
            u.setStudentId(rs.getString("student_id"));
            u.setStatus(rs.getString("status"));
            u.setCreditScore(rs.getInt("credit_score"));
            u.setCreatedAt(rs.getLong("created_at"));
            u.setLastLoginTime(rs.getLong("last_login_time"));
            return u;
        }, params.toArray());
    }

    public User getUserDetail(String username) {
        if (username == null) return null;
        try {
            return jdbcTemplate.queryForObject(
                "SELECT username, phone, student_id, status, credit_score, created_at, last_login_time, email, gender, current_role FROM users WHERE username = ?",
                (rs, rowNum) -> {
                    User u = new User();
                    u.setUsername(rs.getString("username"));
                    u.setPhone(rs.getString("phone"));
                    u.setStudentId(rs.getString("student_id"));
                    u.setStatus(rs.getString("status"));
                    u.setCreditScore(rs.getInt("credit_score"));
                    u.setCreatedAt(rs.getLong("created_at"));
                    u.setLastLoginTime(rs.getLong("last_login_time"));
                    u.setEmail(rs.getString("email"));
                    u.setGender(rs.getString("gender"));
                    // We don't load roles here for simplicity, or we could join
                    return u;
                },
                username
            );
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    private boolean verifyAdminPassword(String username, String password) {
        if (username == null || password == null) return false;
        try {
            String pwd = jdbcTemplate.queryForObject("SELECT password FROM users WHERE username = ?", String.class, username);
            if (pwd == null || !pwd.equals(password)) return false;
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM user_roles WHERE username = ? AND role = 'admin'", Integer.class, username);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, Object> updateUserStatus(String targetUser, String status, String reason, String operator, String secondAdmin, String secondAdminPwd) {
        Map<String, Object> result = new HashMap<>();
        User user = getUserDetail(targetUser);
        if (user == null) {
            result.put("success", false);
            result.put("message", "User not found");
            return result;
        }

        if ("permanent_blacklist".equals(status)) {
             if (secondAdmin == null || secondAdminPwd == null || secondAdmin.isBlank() || secondAdminPwd.isBlank()) {
                 result.put("success", false);
                 result.put("message", "Double audit required: Second admin credentials missing");
                 return result;
             }
             if (secondAdmin.equals(operator)) {
                 result.put("success", false);
                 result.put("message", "Double audit required: Second admin must be different");
                 return result;
             }
             if (!verifyAdminPassword(secondAdmin, secondAdminPwd)) {
                 result.put("success", false);
                 result.put("message", "Second admin verification failed");
                 return result;
             }
             // Treat as blacklist in DB, but log as permanent
             status = "blacklist";
             reason = "[Permanent] " + reason;
        }

        if ("blacklist".equals(status)) {
            // Check uncompleted orders
            if (hasUncompletedOrders(targetUser)) {
                result.put("riskWarning", "该用户有未完成订单，建议优先处理订单。");
                // We proceed, but return warning
            }
        }

        jdbcTemplate.update("UPDATE users SET status = ? WHERE username = ?", status, targetUser);
        
        logOperation(operator, targetUser, "update_status", "Status changed to " + status + ". Reason: " + reason);
        
        // Notification
        String title = "Account Status Updated";
        String content = "Your account status has been updated to: " + status;
        if (reason != null && !reason.isBlank()) {
            content += ". Reason: " + reason;
        }
        notificationService.addToUser(targetUser, "system", title, content);

        result.put("success", true);
        return result;
    }

    public boolean undoBlacklist(String targetUser, String operator, String reason) {
        // Check if last operation was blacklist and within 24h
        // This is a simplification. Ideally we find the last blacklist log.
        List<Map<String, Object>> logs = jdbcTemplate.queryForList(
            "SELECT create_time FROM operation_logs WHERE target_user = ? AND action = 'update_status' AND detail LIKE 'Status changed to blacklist%' ORDER BY create_time DESC LIMIT 1",
            targetUser
        );
        
        if (logs.isEmpty()) return false;
        Long time = (Long) logs.get(0).get("create_time");
        if (System.currentTimeMillis() - time > 24 * 3600 * 1000) {
            return false; // Time expired
        }

        jdbcTemplate.update("UPDATE users SET status = 'normal' WHERE username = ?", targetUser);
        logOperation(operator, targetUser, "undo_blacklist", "Undo blacklist. Reason: " + (reason == null ? "No reason provided" : reason));
        notificationService.addToUser(targetUser, "system", "Blacklist Removed", "Your blacklist status has been revoked.");
        return true;
    }

    public boolean updateUserInfo(User user, String operator) {
        if (user == null || user.getUsername() == null) return false;
        jdbcTemplate.update(
            "UPDATE users SET phone = ?, student_id = ?, credit_score = ? WHERE username = ?",
            user.getPhone(), user.getStudentId(), user.getCreditScore(), user.getUsername()
        );
        logOperation(operator, user.getUsername(), "update_info", "Updated info");
        return true;
    }

    public boolean deleteUser(String targetUser, String operator) {
        jdbcTemplate.update("UPDATE users SET status = 'deleted' WHERE username = ?", targetUser);
        logOperation(operator, targetUser, "delete_user", "User deleted");
        notificationService.addToUser(targetUser, "system", "Account Deleted", "Your account has been deleted.");
        return true;
    }

    public List<OperationLog> getOperationLogs(String keyword, String targetUser, String operator, Long startTime, Long endTime, String action) {
        StringBuilder sql = new StringBuilder("SELECT * FROM operation_logs WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (target_user LIKE ? OR operator LIKE ? OR action LIKE ? OR detail LIKE ?)");
            String k = "%" + keyword + "%";
            params.add(k);
            params.add(k);
            params.add(k);
            params.add(k);
        }
        if (targetUser != null && !targetUser.isBlank()) {
            sql.append(" AND target_user = ?");
            params.add(targetUser);
        }
        if (operator != null && !operator.isBlank()) {
            sql.append(" AND operator = ?");
            params.add(operator);
        }
        if (startTime != null) {
            sql.append(" AND create_time >= ?");
            params.add(startTime);
        }
        if (endTime != null) {
            sql.append(" AND create_time <= ?");
            params.add(endTime);
        }
        if (action != null && !action.isBlank()) {
            sql.append(" AND action = ?");
            params.add(action);
        }
        sql.append(" ORDER BY create_time DESC");

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
            OperationLog log = new OperationLog();
            log.setId(rs.getLong("id"));
            log.setOperator(rs.getString("operator"));
            log.setTargetUser(rs.getString("target_user"));
            log.setAction(rs.getString("action"));
            log.setDetail(rs.getString("detail"));
            log.setCreateTime(rs.getLong("create_time"));
            return log;
        }, params.toArray());
    }

    private void logOperation(String operator, String targetUser, String action, String detail) {
        jdbcTemplate.update(
            "INSERT INTO operation_logs (operator, target_user, action, detail, create_time) VALUES (?, ?, ?, ?, ?)",
            operator, targetUser, action, detail, System.currentTimeMillis()
        );
    }

    public boolean hasUncompletedOrders(String username) {
        // Check as buyer
        List<com.whu.bookapi.model.Order> buyerOrders = orderService.listByBuyer(username);
        for (com.whu.bookapi.model.Order o : buyerOrders) {
            if ("pending".equals(o.getStatus()) || "paid".equals(o.getStatus())) return true;
        }
        // Check as seller
        List<com.whu.bookapi.model.Order> sellerOrders = orderService.listBySeller(username);
        for (com.whu.bookapi.model.Order o : sellerOrders) {
            if ("pending".equals(o.getStatus()) || "paid".equals(o.getStatus())) return true;
        }
        return false;
    }

    public boolean register(String username, String password, java.util.Set<String> roles) {
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) return false;
        if (exists(username)) return false;
        long now = System.currentTimeMillis();
        jdbcTemplate.update(
                "INSERT INTO users (username, password, current_role, seller_status, created_at) VALUES (?, ?, ?, ?, ?)",
                username,
                password,
                "buyer",
                "NONE",
                now
        );
        java.util.Set<String> toInsert = new java.util.HashSet<>();
        toInsert.add("buyer");
        if (roles != null) toInsert.addAll(roles);
        for (String r : toInsert) {
            if (r == null || r.isBlank()) continue;
            jdbcTemplate.update("INSERT IGNORE INTO user_roles (username, role) VALUES (?, ?)", username, r);
        }
        return true;
    }

    public boolean exists(String username) {
        if (username == null) return false;
        Integer c = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM users WHERE username = ?", Integer.class, username);
        return c != null && c > 0;
    }

    public java.util.List<User> listAllUsers() {
        java.util.List<java.util.Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT username, current_role FROM users");
        java.util.List<User> res = new java.util.ArrayList<>();
        for (java.util.Map<String, Object> row : rows) {
            String username = (String) row.get("username");
            String currentRole = (String) row.get("current_role");
            java.util.Set<String> roles = new java.util.HashSet<>(jdbcTemplate.queryForList(
                    "SELECT role FROM user_roles WHERE username = ?",
                    String.class,
                    username
            ));
            String role = resolveRole(currentRole, roles, false);
            User u = new User();
            u.setUsername(username);
            u.setRole(role);
            res.add(u);
        }
        res.sort(java.util.Comparator.comparing(User::getUsername));
        return res;
    }

    public boolean setUserRole(String username, String role) {
        if (username == null || role == null) return false;
        if (!exists(username)) return false;
        jdbcTemplate.update("INSERT IGNORE INTO user_roles (username, role) VALUES (?, ?)", username, role);
        return true;
    }

    public java.util.Map<String, Object> getSellerStats(String username) {
        if (username == null) return null;
        if (!exists(username)) return null;

        // Calculate average score from reviews
        String sqlScore = "SELECT AVG((r.score_service + r.score_condition) / 2.0) FROM reviews r JOIN orders o ON r.order_id = o.id WHERE o.seller_name = ?";
        Double score = jdbcTemplate.queryForObject(sqlScore, Double.class, username);

        // Calculate sold count (orders with status 'received')
        String sqlCount = "SELECT COUNT(1) FROM orders WHERE seller_name = ? AND status = 'received'";
        Long count = jdbcTemplate.queryForObject(sqlCount, Long.class, username);

        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("username", username);
        stats.put("score", score == null ? 5.0 : Math.round(score * 10.0) / 10.0); // Default 5.0 if no reviews, round to 1 decimal
        stats.put("soldCount", count == null ? 0 : count);
        
        return stats;
    }

    public boolean deleteUser(String username) {
        if (username == null) return false;
        int updated = jdbcTemplate.update("DELETE FROM users WHERE username = ?", username);
        return updated > 0;
    }

    public void applySeller(String username) {
        if (username == null) return;
        jdbcTemplate.update("UPDATE users SET seller_status = 'PENDING' WHERE username = ?", username);
    }

    public boolean applySeller(String username, Long paymentCodeFileId) {
        if (username == null || paymentCodeFileId == null) return false;
        java.util.List<java.util.Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT uploader FROM stored_file WHERE id = ?",
                paymentCodeFileId
        );
        if (rows.isEmpty()) return false;
        Object uploaderObj = rows.get(0).get("uploader");
        String uploader = uploaderObj == null ? null : uploaderObj.toString();
        if (uploader == null || !uploader.equals(username)) return false;
        jdbcTemplate.update(
                "UPDATE users SET seller_status = 'PENDING', payment_code_file_id = ? WHERE username = ?",
                paymentCodeFileId,
                username
        );
        return true;
    }

    public void approveSeller(String username) {
        if (username == null) return;
        jdbcTemplate.update("UPDATE users SET seller_status = 'APPROVED' WHERE username = ?", username);
        jdbcTemplate.update("INSERT IGNORE INTO user_roles (username, role) VALUES (?, 'buyer')", username);
        jdbcTemplate.update("INSERT IGNORE INTO user_roles (username, role) VALUES (?, 'seller')", username);
        java.util.List<String> roles = jdbcTemplate.queryForList(
                "SELECT current_role FROM users WHERE username = ?",
                String.class,
                username
        );
        String currentRole = roles.isEmpty() ? null : roles.get(0);
        if (!"admin".equals(currentRole)) {
            jdbcTemplate.update("UPDATE users SET current_role = 'seller' WHERE username = ?", username);
        }
    }

    public void rejectSeller(String username) {
        if (username == null) return;
        jdbcTemplate.update("UPDATE users SET seller_status = 'REJECTED' WHERE username = ?", username);
    }

    public java.util.List<User> listSellerApplications() {
        java.util.List<java.util.Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT username, payment_code_file_id FROM users WHERE seller_status = 'PENDING' ORDER BY username"
        );
        java.util.List<User> res = new java.util.ArrayList<>();
        for (java.util.Map<String, Object> row : rows) {
            String username = (String) row.get("username");
            Long paymentCodeFileId = row.get("payment_code_file_id") == null ? null : ((Number) row.get("payment_code_file_id")).longValue();
            User u = new User();
            u.setUsername(username);
            u.setSellerStatus("PENDING");
            u.setPaymentCodeFileId(paymentCodeFileId);
            if (paymentCodeFileId != null) {
                u.setPaymentCodeUrl("/book-api/files/" + paymentCodeFileId + "/raw");
            }
            res.add(u);
        }
        return res;
    }
}
