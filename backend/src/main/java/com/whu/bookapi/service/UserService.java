package com.whu.bookapi.service;

import com.whu.bookapi.dto.LoginRequest;
import com.whu.bookapi.dto.LoginResponse;
import com.whu.bookapi.model.User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {
    private final Map<String, User> tokenStore = new ConcurrentHashMap<>();
    private final Map<String, String> passwordStore = new ConcurrentHashMap<>();
    private final Map<String, String> roleStore = new ConcurrentHashMap<>();
    private final Map<String, java.util.Set<String>> roleSets = new ConcurrentHashMap<>();
    private final Map<String, String> sellerStatusStore = new ConcurrentHashMap<>();

    public UserService() {
        passwordStore.put("buyer1", "123456");
        roleStore.put("buyer1", "buyer");
        sellerStatusStore.put("buyer1", "NONE");

        passwordStore.put("seller1", "123456");
        roleStore.put("seller1", "seller");
        sellerStatusStore.put("seller1", "APPROVED");

        passwordStore.put("admin1", "123456");
        roleStore.put("admin1", "admin");
        sellerStatusStore.put("admin1", "APPROVED"); // Admin has access? Doesn't matter much.

        passwordStore.put("user1", "123456");
        roleSets.put("user1", new java.util.HashSet<>(java.util.Arrays.asList("buyer", "seller")));
        sellerStatusStore.put("user1", "APPROVED");
    }

    public LoginResponse login(LoginRequest req) {
        String pwd = passwordStore.get(req.getUsername());
        if (pwd == null || !pwd.equals(req.getPassword())) {
            return null;
        }
        // Determine role automatically
        String role = roleStore.get(req.getUsername());
        java.util.Set<String> roles = roleSets.get(req.getUsername());
        
        // Priority: Admin > Seller > Buyer (if multiple roles exist, pick highest)
        // Or just return the primary role stored in roleStore.
        // If roleSets exists, we might need logic. For now, trust roleStore or pick one from set.
        if (role == null && roles != null && !roles.isEmpty()) {
            if (roles.contains("admin")) role = "admin";
            else if (roles.contains("seller")) role = "seller";
            else role = "buyer";
        }
        if (role == null) return null; // No role assigned?

        String token = UUID.randomUUID().toString();
        User user = new User();
        user.setUsername(req.getUsername());
        user.setRole(role);
        user.setToken(token);
        String ss = sellerStatusStore.getOrDefault(req.getUsername(), "NONE");
        user.setSellerStatus(ss);
        tokenStore.put(token, user);
        LoginResponse resp = new LoginResponse();
        resp.setToken(token);
        resp.setUsername(user.getUsername());
        resp.setRole(user.getRole());
        resp.setSellerStatus(ss);
        return resp;
    }

    public void logout(String token) {
        tokenStore.remove(token);
    }

    public User getByToken(String token) {
        User u = tokenStore.get(token);
        if (u != null) {
            u.setSellerStatus(sellerStatusStore.getOrDefault(u.getUsername(), "NONE"));
        }
        return u;
    }

    public boolean register(String username, String password, java.util.Set<String> roles) {
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) return false;
        if (passwordStore.containsKey(username)) return false;
        passwordStore.put(username, password);
        // Default role is buyer
        roleStore.put(username, "buyer");
        sellerStatusStore.put(username, "NONE");
        return true;
    }

    public boolean exists(String username) {
        return passwordStore.containsKey(username);
    }

    public java.util.List<User> listAllUsers() {
        java.util.List<User> res = new java.util.ArrayList<>();
        for (Map.Entry<String, String> e : roleStore.entrySet()) {
            String username = e.getKey();
            String role = e.getValue();
            User u = new User();
            u.setUsername(username);
            u.setRole(role);
            res.add(u);
        }
        for (Map.Entry<String, java.util.Set<String>> e : roleSets.entrySet()) {
            String username = e.getKey();
            java.util.Set<String> roles = e.getValue();
            String role = roles.contains("buyer") ? "buyer" : (roles.contains("seller") ? "seller" : (roles.contains("admin") ? "admin" : "buyer"));
            role = roleStore.getOrDefault(username, role);
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
        if (!passwordStore.containsKey(username)) return false;
        roleStore.put(username, role);
        roleSets.remove(username);
        return true;
    }

    public boolean deleteUser(String username) {
        if (username == null) return false;
        if (!passwordStore.containsKey(username)) return false;
        passwordStore.remove(username);
        roleStore.remove(username);
        roleSets.remove(username);
        java.util.List<String> tokensToRemove = new java.util.ArrayList<>();
        for (Map.Entry<String, User> e : tokenStore.entrySet()) {
            if (username.equals(e.getValue().getUsername())) tokensToRemove.add(e.getKey());
        }
        for (String t : tokensToRemove) tokenStore.remove(t);
        return true;
    }

    public void applySeller(String username) {
        sellerStatusStore.put(username, "PENDING");
    }

    public void approveSeller(String username) {
        sellerStatusStore.put(username, "APPROVED");
        // 如果用户角色还不是 seller，则更新为 seller（保留原角色可能更好，但这里简单起见直接升级）
        // 或者应该支持多角色，但 current logic seems to pick one.
        // Let's check roleSets
        java.util.Set<String> roles = roleSets.get(username);
        if (roles == null) {
            roles = new java.util.HashSet<>();
            roles.add("buyer");
        }
        roles.add("seller");
        roleSets.put(username, roles);
        
        // 同时更新当前的主要角色为 seller，以便用户不需要重新登录就能获得权限
        // 注意：这取决于 login 逻辑中的优先级，Admin > Seller > Buyer
        // 如果已经是 Admin，则不需要降级。如果只是 Buyer，则升级为 Seller。
        String currentRole = roleStore.get(username);
        if (!"admin".equals(currentRole)) {
            roleStore.put(username, "seller");
        }
    }

    public void rejectSeller(String username) {
        sellerStatusStore.put(username, "REJECTED");
    }

    public java.util.List<User> listSellerApplications() {
        java.util.List<User> res = new java.util.ArrayList<>();
        for (Map.Entry<String, String> e : sellerStatusStore.entrySet()) {
            if ("PENDING".equals(e.getValue())) {
                User u = new User();
                u.setUsername(e.getKey());
                u.setSellerStatus("PENDING");
                res.add(u);
            }
        }
        return res;
    }
}
