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

    public UserService() {
        passwordStore.put("buyer1", "123456");
        roleStore.put("buyer1", "buyer");
        passwordStore.put("seller1", "123456");
        roleStore.put("seller1", "seller");
        passwordStore.put("admin1", "123456");
        roleStore.put("admin1", "admin");
        passwordStore.put("user1", "123456");
        roleSets.put("user1", new java.util.HashSet<>(java.util.Arrays.asList("buyer", "seller")));
    }

    public LoginResponse login(LoginRequest req) {
        String pwd = passwordStore.get(req.getUsername());
        String role = roleStore.get(req.getUsername());
        if (pwd == null || !pwd.equals(req.getPassword())) {
            return null;
        }
        java.util.Set<String> roles = roleSets.get(req.getUsername());
        if (roles != null) {
            if (!roles.contains(req.getRole())) {
                return null;
            }
        } else {
            if (role == null || !role.equals(req.getRole())) {
                return null;
            }
        }
        String token = UUID.randomUUID().toString();
        User user = new User();
        user.setUsername(req.getUsername());
        user.setRole(req.getRole());
        user.setToken(token);
        tokenStore.put(token, user);
        LoginResponse resp = new LoginResponse();
        resp.setToken(token);
        resp.setUsername(user.getUsername());
        resp.setRole(user.getRole());
        return resp;
    }

    public void logout(String token) {
        tokenStore.remove(token);
    }

    public User getByToken(String token) {
        return tokenStore.get(token);
    }

    public boolean register(String username, String password, java.util.Set<String> roles) {
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) return false;
        if (passwordStore.containsKey(username)) return false;
        passwordStore.put(username, password);
        if (roles == null || roles.isEmpty()) {
            roleStore.put(username, "buyer");
        } else {
            roleSets.put(username, roles);
        }
        return true;
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
}
