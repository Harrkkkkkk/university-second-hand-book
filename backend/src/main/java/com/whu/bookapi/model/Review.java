package com.whu.bookapi.model;

public class Review {
    private Long id;
    private Long orderId;
    private String username;
    private int scoreCondition;
    private int scoreService;
    private String comment;
    private long createTime;

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
}
