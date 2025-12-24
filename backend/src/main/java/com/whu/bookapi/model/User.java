package com.whu.bookapi.model;

public class User {
    private String username;
    private String role;
    private String token;
    private String sellerStatus; // NONE, PENDING, APPROVED, REJECTED
    private Long paymentCodeFileId;
    private String paymentCodeUrl;
    private String phone;
    private String email;
    private String gender;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSellerStatus() {
        return sellerStatus;
    }

    public void setSellerStatus(String sellerStatus) {
        this.sellerStatus = sellerStatus;
    }

    public Long getPaymentCodeFileId() {
        return paymentCodeFileId;
    }

    public void setPaymentCodeFileId(Long paymentCodeFileId) {
        this.paymentCodeFileId = paymentCodeFileId;
    }

    public String getPaymentCodeUrl() {
        return paymentCodeUrl;
    }

    public void setPaymentCodeUrl(String paymentCodeUrl) {
        this.paymentCodeUrl = paymentCodeUrl;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
