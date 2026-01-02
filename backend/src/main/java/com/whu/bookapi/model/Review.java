package com.whu.bookapi.model;

public class Review {
    private Long id;
    private Long orderId;
    private String username;
    private int scoreCondition;
    private int scoreService;
    private String comment;
    private long createTime;
    private java.util.List<String> tags;
    private String status; // pending, approved, rejected
    private String auditReason;
    private Long auditTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public int getScoreCondition() { return scoreCondition; }
    public void setScoreCondition(int scoreCondition) { this.scoreCondition = scoreCondition; }
    public int getScoreService() { return scoreService; }
    public void setScoreService(int scoreService) { this.scoreService = scoreService; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public long getCreateTime() { return createTime; }
    public void setCreateTime(long createTime) { this.createTime = createTime; }
    public java.util.List<String> getTags() { return tags; }
    public void setTags(java.util.List<String> tags) { this.tags = tags; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAuditReason() { return auditReason; }
    public void setAuditReason(String auditReason) { this.auditReason = auditReason; }
    public Long getAuditTime() { return auditTime; }
    public void setAuditTime(Long auditTime) { this.auditTime = auditTime; }
}
