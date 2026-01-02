package com.whu.bookapi.model;

public class Complaint {
    private Long id;
    private Long orderId;
    private String username;
    private String type;
    private String detail;
    private long createTime;
    private String status; // pending, handling, resolved, rejected
    private String auditReason;
    private Long auditTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public long getCreateTime() { return createTime; }
    public void setCreateTime(long createTime) { this.createTime = createTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAuditReason() { return auditReason; }
    public void setAuditReason(String auditReason) { this.auditReason = auditReason; }
    public Long getAuditTime() { return auditTime; }
    public void setAuditTime(Long auditTime) { this.auditTime = auditTime; }
}
