package com.whu.bookapi.service;

import com.whu.bookapi.dto.LoginRequest;
import com.whu.bookapi.dto.LoginResponse;
import com.whu.bookapi.model.User;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

@Service
public class UserService {
    private final JdbcTemplate jdbcTemplate;

    public UserService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
        jdbcTemplate.update(
                "INSERT INTO user_token (token, username, created_at) VALUES (?, ?, ?)",
                token,
                req.getUsername(),
                System.currentTimeMillis()
        );

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
        boolean def = Boolean.TRUE.equals(isDefault);
        if (def) {
            jdbcTemplate.update("UPDATE user_address SET is_default = 0 WHERE username = ?", username);
        }
        long now = System.currentTimeMillis();
        int updated = jdbcTemplate.update(
                "UPDATE user_address SET receiver_name = ?, phone = ?, address = ?, is_default = ?, updated_at = ? WHERE id = ? AND username = ?",
                name,
                phone == null || phone.isBlank() ? null : phone,
                address,
                def ? 1 : 0,
                now,
                id,
                username
        );
        return updated > 0;
    }

    public boolean deleteAddress(String username, Long id) {
        if (username == null || id == null) return false;
        java.util.List<java.util.Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT is_default FROM user_address WHERE id = ? AND username = ?",
                id,
                username
        );
        boolean wasDefault = false;
        if (!rows.isEmpty()) {
            Object defObj = rows.get(0).get("is_default");
            wasDefault = defObj instanceof Boolean ? (Boolean) defObj : (defObj instanceof Number && ((Number) defObj).intValue() != 0);
        }
        int deleted = jdbcTemplate.update("DELETE FROM user_address WHERE id = ? AND username = ?", id, username);
        if (deleted <= 0) return false;
        if (wasDefault) {
            java.util.List<Long> ids = jdbcTemplate.queryForList(
                    "SELECT id FROM user_address WHERE username = ? ORDER BY updated_at DESC, id DESC LIMIT 1",
                    Long.class,
                    username
            );
            if (!ids.isEmpty()) {
                jdbcTemplate.update("UPDATE user_address SET is_default = 1 WHERE id = ? AND username = ?", ids.get(0), username);
            }
        }
        return true;
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
        jdbcTemplate.update("UPDATE users SET current_role = ? WHERE username = ?", role, username);
        jdbcTemplate.update("DELETE FROM user_roles WHERE username = ?", username);
        jdbcTemplate.update("INSERT IGNORE INTO user_roles (username, role) VALUES (?, ?)", username, role);
        return true;
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
