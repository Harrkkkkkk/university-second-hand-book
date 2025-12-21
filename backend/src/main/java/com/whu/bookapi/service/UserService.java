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
                "SELECT u.username, u.current_role, u.seller_status FROM user_token t JOIN users u ON t.username = u.username WHERE t.token = ?",
                token
        );
        if (rows.isEmpty()) return null;
        java.util.Map<String, Object> row = rows.get(0);
        String username = (String) row.get("username");
        String currentRole = (String) row.get("current_role");
        String sellerStatus = (String) row.get("seller_status");
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
        return u;
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
        java.util.List<String> usernames = jdbcTemplate.queryForList(
                "SELECT username FROM users WHERE seller_status = 'PENDING' ORDER BY username",
                String.class
        );
        java.util.List<User> res = new java.util.ArrayList<>();
        for (String username : usernames) {
            User u = new User();
            u.setUsername(username);
            u.setSellerStatus("PENDING");
            res.add(u);
        }
        return res;
    }
}
