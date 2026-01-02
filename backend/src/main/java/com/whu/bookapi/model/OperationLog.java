package com.whu.bookapi.model;

public class OperationLog {
    private Long id;
    private String operator;
    private String targetUser;
    private String action; // blacklist, unblacklist, update_info, delete_user
    private String detail;
    private long createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public String getTargetUser() { return targetUser; }
    public void setTargetUser(String targetUser) { this.targetUser = targetUser; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public long getCreateTime() { return createTime; }
    public void setCreateTime(long createTime) { this.createTime = createTime; }
}
