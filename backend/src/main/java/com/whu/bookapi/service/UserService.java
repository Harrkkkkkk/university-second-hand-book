package com.whu.bookapi.service;

import com.whu.bookapi.dto.LoginRequest;
import com.whu.bookapi.dto.LoginResponse;
import com.whu.bookapi.model.User;
import com.whu.bookapi.model.OperationLog;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.context.annotation.Lazy;

import java.util.UUID;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: UserService.java
 * Author: WiseBookPal Team Version: 1.1 Date: 2026-01-02
 * Description: Service for user management, including authentication, profile updates,
 *              address management, and admin user operations (blacklist, audit logs).
 * Others:
 * Function List:
 * 1. login - Authenticates user.
 * 2. logout - Invalidates session.
 * 3. getByToken - Retrieves user by token.
 * 4. updateProfile - Updates profile.
 * 5. changePassword - Changes password.
 * 6. listAddresses - Lists addresses.
 * 7. addAddress - Adds address.
 * 8. updateAddress - Updates address.
 * 9. deleteAddress - Deletes address.
 * 10. searchUsers - Searches users (U14).
 * 11. getUserDetail - Gets user details (U14).
 * 12. updateUserStatus - Updates user status (U14).
 * 13. undoBlacklist - Undoes blacklist (U14).
 * 14. getOperationLogs - Gets logs (U14).
 * 15. register - Registers a new user.
 * 16. exists - Checks if user exists.
 * 17. listAllUsers - Lists all users.
 * 18. setUserRole - Assigns role to user.
 * 19. getSellerStats - Gets seller statistics.
 * 20. deleteUser - Deletes user (Soft/Hard).
 * 21. applySeller - Handles seller application.
 * 22. approveSeller - Approves seller application.
 * 23. rejectSeller - Rejects seller application.
 * 24. listSellerApplications - Lists seller applications.
 * History:
 * 1. Date: 2024-11-20
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
 * 3. Date: 2026-01-02
 *    Author: WiseBookPal Team
 *    Modification: Added validation for register, removed payment code from seller application.
 */
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

    /**
     * Function: login
     * Description: Authenticates user credentials and generates a session token.
     * Calls: JdbcTemplate.queryForList, JdbcTemplate.update
     * Called By: UserController.login
     * Table Accessed: users, user_roles
     * Table Updated: user_token, users
     * Input: req (LoginRequest) - Credentials
     * Output: LoginResponse - Session info
     * Return: LoginResponse
     * Others:
     */
    public LoginResponse login(LoginRequest req) {
        if (req == null || req.getUsername() == null || req.getPassword() == null) return null;
        java.util.List<java.util.Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT username, password, current_role, seller_status, status, real_name, is_verified FROM users WHERE username = ?",
                req.getUsername()
        );
        if (rows.isEmpty()) return null;
        java.util.Map<String, Object> row = rows.get(0);
        
        String userStatus = (String) row.get("status");
        if (userStatus != null && "blacklist".equals(userStatus)) {
            LoginResponse resp = new LoginResponse();
            resp.setMessage("该账号已被添加至黑名单");
            return resp;
        }
        
        String pwd = (String) row.get("password");
        if (pwd == null || !pwd.equals(req.getPassword())) return null;
        String currentRole = (String) row.get("current_role");
        String sellerStatus = (String) row.get("seller_status");
        
        String realName = (String) row.get("real_name");
        Object ivObj = row.get("is_verified");
        boolean isVerified = ivObj instanceof Boolean ? (Boolean) ivObj : (ivObj instanceof Number && ((Number) ivObj).intValue() != 0);

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
        resp.setRealName(realName);
        resp.setIsVerified(isVerified);
        return resp;
    }

    /**
     * Function: logout
     * Description: Invalidates the user's session token.
     * Calls: JdbcTemplate.update
     * Called By: UserController.logout
     * Table Accessed: None
     * Table Updated: user_token
     * Input: token (String) - Session token
     * Output: None
     * Return: void
     * Others:
     */
    public void logout(String token) {
        if (token == null) return;
        jdbcTemplate.update("DELETE FROM user_token WHERE token = ?", token);
    }

    /**
     * Function: getByToken
     * Description: Retrieves user information associated with a valid token.
     * Calls: JdbcTemplate.queryForList
     * Called By: UserController methods, AdminController methods
     * Table Accessed: user_token, users, user_roles
     * Table Updated: None
     * Input: token (String) - Session token
     * Output: User - User object
     * Return: User
     * Others:
     */
    public User getByToken(String token) {
        if (token == null) return null;
        java.util.List<java.util.Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT u.username, u.current_role, u.seller_status, u.phone, u.email, u.gender, u.last_audit_time, u.real_name, u.is_verified FROM user_token t JOIN users u ON t.username = u.username WHERE t.token = ?",
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
        Object latObj = row.get("last_audit_time");
        Long lastAuditTime = latObj instanceof Number ? ((Number) latObj).longValue() : null;
        String realName = (String) row.get("real_name");
        Object ivObj = row.get("is_verified");
        boolean isVerified = ivObj instanceof Boolean ? (Boolean) ivObj : (ivObj instanceof Number && ((Number) ivObj).intValue() != 0);
        
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
        u.setLastAuditTime(lastAuditTime);
        u.setRealName(realName);
        u.setIsVerified(isVerified);
        return u;
    }

    /**
     * Function: updateProfile
     * Description: Updates user profile details.
     * Calls: JdbcTemplate.update
     * Called By: UserController.updateProfile
     * Table Accessed: None
     * Table Updated: users
     * Input: username (String), phone (String), email (String), gender (String)
     * Output: boolean - Success status
     * Return: boolean
     * Others:
     */
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

    /**
     * Function: changePassword
     * Description: Updates user password after verification.
     * Calls: JdbcTemplate.queryForList, JdbcTemplate.update
     * Called By: UserController.changePassword
     * Table Accessed: users
     * Table Updated: users
     * Input: username (String), oldPassword (String), newPassword (String)
     * Output: boolean - Success status
     * Return: boolean
     * Others:
     */
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

    /**
     * Function: listAddresses
     * Description: Retrieves all shipping addresses for a user.
     * Calls: JdbcTemplate.queryForList
     * Called By: UserController.listAddresses
     * Table Accessed: user_address
     * Table Updated: None
     * Input: username (String)
     * Output: List<Map> - List of addresses
     * Return: List<Map<String, Object>>
     * Others:
     */
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

    /**
     * Function: addAddress
     * Description: Adds a new shipping address.
     * Calls: JdbcTemplate.update
     * Called By: UserController.addAddress
     * Table Accessed: user_address
     * Table Updated: user_address
     * Input: username (String), name (String), phone (String), address (String), isDefault (Boolean)
     * Output: Long - New address ID
     * Return: Long
     * Others:
     */
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

    /**
     * Function: updateAddress
     * Description: Updates an existing address.
     * Calls: JdbcTemplate.update
     * Called By: UserController.updateAddress
     * Table Accessed: user_address
     * Table Updated: user_address
     * Input: username (String), id (Long), name (String), phone (String), address (String), isDefault (Boolean)
     * Output: boolean - Success status
     * Return: boolean
     * Others:
     */
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

    /**
     * Function: deleteAddress
     * Description: Deletes a shipping address.
     * Calls: JdbcTemplate.update
     * Called By: UserController.deleteAddress
     * Table Accessed: None
     * Table Updated: user_address
     * Input: username (String), id (Long)
     * Output: boolean - Success status
     * Return: boolean
     * Others:
     */
    public boolean deleteAddress(String username, Long id) {
        if (username == null || id == null) return false;
        int rows = jdbcTemplate.update("DELETE FROM user_address WHERE id = ? AND username = ?", id, username);
        return rows > 0;
    }

    // --- U14 User Management Methods ---

    /**
     * Function: searchUsers
     * Description: Searches for users by keyword (U14).
     * Calls: JdbcTemplate.query
     * Called By: AdminController.listUsers
     * Table Accessed: users
     * Table Updated: None
     * Input: keyword (String), page (int), size (int)
     * Output: List<User>
     * Return: List<User>
     * Others:
     */
    public List<User> searchUsers(String keyword, int page, int size) {
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

        // Pagination
        if (page < 1) page = 1;
        if (size < 1) size = 10;
        int offset = (page - 1) * size;
        sql += " LIMIT ? OFFSET ?";
        params.add(size);
        params.add(offset);

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

    /**
     * Function: searchUsers
     * Description: Overload for backward compatibility.
     */
    public List<User> searchUsers(String keyword) {
        return searchUsers(keyword, 1, 100);
    }

    /**
     * Function: getUserDetail
     * Description: Gets detailed user info (U14).
     * Calls: JdbcTemplate.queryForObject
     * Called By: AdminController.getUserDetail
     * Table Accessed: users
     * Table Updated: None
     * Input: username (String)
     * Output: User
     * Return: User
     * Others:
     */
    public User getUserDetail(String username) {
        if (username == null) return null;
        try {
            return jdbcTemplate.queryForObject(
                "SELECT username, phone, student_id, status, credit_score, created_at, last_login_time, email, gender, current_role, real_name, is_verified FROM users WHERE username = ?",
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
                    u.setRealName(rs.getString("real_name"));
                    u.setIsVerified(rs.getBoolean("is_verified"));
                    // We don't load roles here for simplicity, or we could join
                    return u;
                },
                username
            );
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Function: verifyIdentity
     * Description: Verifies user identity against simulated external DB.
     * Calls: JdbcTemplate.queryForList, JdbcTemplate.update
     * Called By: UserController.verifyIdentity
     * Table Accessed: university_students
     * Table Updated: users
     * Input: username (String), studentId (String), name (String)
     * Output: boolean - Verification success status
     * Return: boolean
     * Others:
     */
    @Transactional
    public boolean verifyIdentity(String username, String studentId, String name) {
        if (username == null || studentId == null || name == null) return false;

        // 1. Check against university_students table (Simulated external DB)
        String checkSql = "SELECT count(*) FROM university_students WHERE student_id = ? AND name = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, studentId, name);
        
        if (count == null || count == 0) {
            return false; // Not found or mismatch
        }

        // 2. Update users table
        String updateSql = "UPDATE users SET real_name = ?, is_verified = 1, student_id = ? WHERE username = ?";
        int rows = jdbcTemplate.update(updateSql, name, studentId, username);
        
        return rows > 0;
    }



    /**
     * Function: verifyAdminPassword
     * Description: Verifies admin password for double audit (U14).
     * Calls: JdbcTemplate.queryForObject
     * Called By: updateUserStatus
     * Table Accessed: users, user_roles
     * Table Updated: None
     * Input: username (String), password (String)
     * Output: boolean
     * Return: boolean
     * Others:
     */
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

    /**
     * Function: updateUserStatus
     * Description: Updates user status and logs the operation (U14).
     * Calls: verifyAdminPassword, hasUncompletedOrders, logOperation, NotificationService.addToUser
     * Called By: AdminController.updateUserStatus
     * Table Accessed: users
     * Table Updated: users
     * Input: targetUser, status, reason, operator, secondAdmin, secondAdminPwd
     * Output: Map - Result
     * Return: Map<String, Object>
     * Others:
     */
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
                 result.put("message", "Permanent blacklist requires dual admin approval");
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

    /**
     * Function: undoBlacklist
     * Description: Reverts blacklist status if within 24 hours (U14).
     * Calls: JdbcTemplate.queryForList, logOperation, NotificationService.addToUser
     * Called By: AdminController.undoBlacklist
     * Table Accessed: operation_logs, users
     * Table Updated: users, operation_logs
     * Input: targetUser, operator, reason
     * Output: boolean
     * Return: boolean
     * Others:
     */
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

    /**
     * Function: updateUserInfo
     * Description: Updates user info by admin (U14).
     * Calls: JdbcTemplate.update, logOperation
     * Called By: AdminController.updateUserInfo
     * Table Accessed: users
     * Table Updated: users, operation_logs
     * Input: user (User), operator (String)
     * Output: boolean
     * Return: boolean
     * Others:
     */
    public boolean updateUserInfo(User user, String operator) {
        if (user == null || user.getUsername() == null) return false;
        jdbcTemplate.update(
            "UPDATE users SET phone = ?, student_id = ?, credit_score = ? WHERE username = ?",
            user.getPhone(), user.getStudentId(), user.getCreditScore(), user.getUsername()
        );
        logOperation(operator, user.getUsername(), "update_info", "Updated info");
        return true;
    }

    /**
     * Function: deleteUser
     * Description: Hard deletes a user (U14).
     * Calls: deleteUser(username), logOperation
     * Called By: AdminController.deleteUser
     * Table Accessed: users
     * Table Updated: users, operation_logs
     * Input: targetUser, operator
     * Output: boolean
     * Return: boolean
     * Others:
     */
    @Transactional
    public boolean deleteUser(String targetUser, String operator) {
        logOperation(operator, targetUser, "delete_user", "User deleted (Hard Delete)");
        // We perform hard delete
        return deleteUser(targetUser);
    }

    /**
     * Function: getOperationLogs
     * Description: Retrieves logs with filters (U14).
     * Calls: JdbcTemplate.query
     * Called By: AdminController.getOperationLogs
     * Table Accessed: operation_logs
     * Table Updated: None
     * Input: keyword, targetUser, operator, startTime, endTime, action
     * Output: List<OperationLog>
     * Return: List<OperationLog>
     * Others:
     */
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

    /**
     * Function: logOperation
     * Description: Helper to insert a log entry.
     * Calls: JdbcTemplate.update
     * Called By: updateUserStatus, undoBlacklist, updateUserInfo, deleteUser
     * Table Accessed: None
     * Table Updated: operation_logs
     * Input: operator, targetUser, action, detail
     * Output: None
     * Return: void
     * Others:
     */
    private void logOperation(String operator, String targetUser, String action, String detail) {
        jdbcTemplate.update(
            "INSERT INTO operation_logs (operator, target_user, action, detail, create_time) VALUES (?, ?, ?, ?, ?)",
            operator, targetUser, action, detail, System.currentTimeMillis()
        );
    }

    /**
     * Function: hasUncompletedOrders
     * Description: Checks if user has pending/paid orders (U14).
     * Calls: OrderService.listByBuyer, OrderService.listBySeller
     * Called By: updateUserStatus
     * Table Accessed: orders
     * Table Updated: None
     * Input: username
     * Output: boolean
     * Return: boolean
     * Others:
     */
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

    /**
     * Function: register
     * Description: Registers a new user.
     * Calls: exists, JdbcTemplate.update
     * Called By: UserController.register
     * Table Accessed: users, user_roles
     * Table Updated: users, user_roles
     * Input: username, password, roles
     * Output: boolean
     * Return: boolean
     * Others:
     */
    public boolean register(String username, String password, java.util.Set<String> roles) {
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) return false;
        // Validation: Username <= 16 chars
        if (username.length() > 16) return false;
        // Validation: Password 6-25 chars, must contain letters and numbers
        if (password.length() < 6 || password.length() > 25) return false;
        if (!password.matches("^(?=.*[a-zA-Z])(?=.*[0-9]).*$")) return false;

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

    /**
     * Function: exists
     * Description: Checks if a username exists.
     * Calls: JdbcTemplate.queryForObject
     * Called By: register, setUserRole
     * Table Accessed: users
     * Table Updated: None
     * Input: username
     * Output: boolean
     * Return: boolean
     * Others:
     */
    public boolean exists(String username) {
        if (username == null) return false;
        Integer c = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM users WHERE username = ?", Integer.class, username);
        return c != null && c > 0;
    }

    /**
     * Function: listAllUsers
     * Description: Lists all users (Basic).
     * Calls: JdbcTemplate.queryForList
     * Called By: AdminController (Legacy)
     * Table Accessed: users, user_roles
     * Table Updated: None
     * Input: None
     * Output: List<User>
     * Return: List<User>
     * Others:
     */
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

    /**
     * Function: setUserRole
     * Description: Assigns a role to a user.
     * Calls: exists, JdbcTemplate.update
     * Called By: AdminController (Legacy)
     * Table Accessed: user_roles
     * Table Updated: user_roles
     * Input: username, role
     * Output: boolean
     * Return: boolean
     * Others:
     */
    public boolean setUserRole(String username, String role) {
        if (username == null || role == null) return false;
        if (!exists(username)) return false;
        jdbcTemplate.update("INSERT IGNORE INTO user_roles (username, role) VALUES (?, ?)", username, role);
        return true;
    }

    /**
     * Function: getSellerStats
     * Description: Calculates seller stats.
     * Calls: JdbcTemplate.queryForObject
     * Called By: SellerController.getStats
     * Table Accessed: reviews, orders
     * Table Updated: None
     * Input: username
     * Output: Map
     * Return: Map<String, Object>
     * Others:
     */
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

    /**
     * Function: deleteUser
     * Description: Hard deletes a user and related content.
     * Calls: JdbcTemplate.update
     * Called By: AdminController (Legacy), deleteUser(operator), deleteSelf
     * Table Accessed: None
     * Table Updated: users, books, orders, etc.
     * Input: username
     * Output: boolean
     * Return: boolean
     * Others:
     */
    @Transactional
    public boolean deleteUser(String username) {
        if (username == null) return false;

        // Manually delete related data to ensure hard delete succeeds even without CASCADE
        // Order matters: delete child records first
        try {
            // 1. Delete user_token (Login sessions)
            jdbcTemplate.update("DELETE FROM user_token WHERE username = ?", username);
            
            // 2. Delete user_roles
            jdbcTemplate.update("DELETE FROM user_roles WHERE username = ?", username);
            
            // 3. Delete user_address
            jdbcTemplate.update("DELETE FROM user_address WHERE username = ?", username);

            // 4. Delete cart_item
            jdbcTemplate.update("DELETE FROM cart_item WHERE username = ?", username);

            // 5. Delete favorites
            jdbcTemplate.update("DELETE FROM favorites WHERE username = ?", username);

            // 6. Delete review_draft
            jdbcTemplate.update("DELETE FROM review_draft WHERE username = ?", username);

            // 7. Delete reviews
            jdbcTemplate.update("DELETE FROM reviews WHERE username = ?", username);

            // 8. Delete complaints (as complainant)
            jdbcTemplate.update("DELETE FROM complaints WHERE username = ?", username);

            // 9. Delete notification_read status
            jdbcTemplate.update("DELETE FROM notification_read WHERE username = ?", username);

            // 10. Delete notifications sent to this user
            jdbcTemplate.update("DELETE FROM notifications WHERE to_user = ?", username);

            // 11. Delete chat_message (sent or received)
            jdbcTemplate.update("DELETE FROM chat_message WHERE from_user = ? OR to_user = ?", username, username);

            // 12. Delete orders (buyer or seller)
            // Note: orders reference books, so delete orders first
            jdbcTemplate.update("DELETE FROM orders WHERE seller_name = ? OR buyer_name = ?", username, username);

            // 13. Delete books (as seller)
            jdbcTemplate.update("DELETE FROM books WHERE seller_name = ?", username);

            // 14. Delete operation_logs where target_user or operator is this user
            jdbcTemplate.update("DELETE FROM operation_logs WHERE target_user = ?", username);
            jdbcTemplate.update("DELETE FROM operation_logs WHERE operator = ?", username);

        } catch (Exception e) {
            // Log error but continue to try deleting the user
            // In a transactional context, this might not be enough if the exception marks the transaction for rollback.
            // But for now we try to proceed. Ideally we should let the exception propagate to rollback.
            System.err.println("Error deleting related data for user " + username + ": " + e.getMessage());
            throw e; // Rethrow to trigger rollback if something goes wrong
        }

        // Finally, delete user
        int updated = jdbcTemplate.update("DELETE FROM users WHERE username = ?", username);
        return updated > 0;
    }

    /**
     * Function: deleteSelf
     * Description: Allows user to delete their own account.
     * Calls: deleteUser
     * Called By: UserController.deleteAccount
     * Table Accessed: None
     * Table Updated: users, etc.
     * Input: username
     * Output: boolean
     * Return: boolean
     */
    @Transactional
    public boolean deleteSelf(String username) {
        return deleteUser(username);
    }

    /**
     * Function: applySeller
     * Description: Marks user as pending seller.
     * Calls: JdbcTemplate.update
     * Called By: UserController.applySeller
     * Table Accessed: None
     * Table Updated: users
     * Input: username
     * Output: None
     * Return: void
     * Others:
     */
    public void applySeller(String username) {
        if (username == null) return;
        // Check current status and last audit time
        java.util.List<java.util.Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT seller_status, last_audit_time FROM users WHERE username = ?", username
        );
        if (!rows.isEmpty()) {
            java.util.Map<String, Object> row = rows.get(0);
            String status = (String) row.get("seller_status");
            if ("REJECTED".equals(status)) {
                Object latObj = row.get("last_audit_time");
                long lastAuditTime = latObj instanceof Number ? ((Number) latObj).longValue() : 0L;
                long now = System.currentTimeMillis();
                if (now - lastAuditTime < 20 * 60 * 1000) {
                    throw new IllegalArgumentException("申请被驳回，请在20分钟后再次尝试");
                }
            }
        }
        jdbcTemplate.update("UPDATE users SET seller_status = 'PENDING' WHERE username = ?", username);
    }

    /**
     * Function: approveSeller
     * Description: Approves seller status and grants role.
     * Calls: JdbcTemplate.update, JdbcTemplate.queryForList
     * Called By: AdminController.approveSeller
     * Table Accessed: users
     * Table Updated: users, user_roles
     * Input: username
     * Output: None
     * Return: void
     * Others:
     */
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

    /**
     * Function: rejectSeller
     * Description: Rejects seller application.
     * Calls: JdbcTemplate.update
     * Called By: AdminController.rejectSeller
     * Table Accessed: None
     * Table Updated: users
     * Input: username
     * Output: None
     * Return: void
     * Others:
     */
    public void rejectSeller(String username) {
        if (username == null) return;
        jdbcTemplate.update("UPDATE users SET seller_status = 'REJECTED', last_audit_time = ? WHERE username = ?", System.currentTimeMillis(), username);
    }

    /**
     * Function: listSellerApplications
     * Description: Lists pending seller applications.
     * Calls: JdbcTemplate.queryForList
     * Called By: AdminController.listSellerApplications
     * Table Accessed: users
     * Table Updated: None
     * Input: None
     * Output: List<User>
     * Return: List<User>
     * Others:
     */
    public java.util.List<User> listSellerApplications() {
        java.util.List<java.util.Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT username FROM users WHERE seller_status = 'PENDING' ORDER BY username"
        );
        java.util.List<User> res = new java.util.ArrayList<>();
        for (java.util.Map<String, Object> row : rows) {
            String username = (String) row.get("username");
            User u = new User();
            u.setUsername(username);
            u.setSellerStatus("PENDING");
            res.add(u);
        }
        return res;
    }

    public boolean isSellerAccountHealthy(String username) {
        if (username == null) return false;
        java.util.List<java.util.Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT seller_status, status FROM users WHERE username = ?",
                username
        );
        if (rows.isEmpty()) return false;
        java.util.Map<String, Object> row = rows.get(0);
        String sellerStatus = (String) row.get("seller_status");
        String status = (String) row.get("status");
        boolean sellerApproved = "APPROVED".equals(sellerStatus);
        boolean accountNormal = status == null || "normal".equals(status);
        return sellerApproved && accountNormal;
    }
}
