package com.whu.bookapi.model;

public class Notification {
    private Long id;
    private String toUser; // 具体用户或"*"广播
    private String type;   // announcement, audit, complaint, system
    private String title;
    private String content;
    private long createTime;
    private boolean read;
    private java.util.Set<String> readByUsers = new java.util.HashSet<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getToUser() { return toUser; }
    public void setToUser(String toUser) { this.toUser = toUser; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public long getCreateTime() { return createTime; }
    public void setCreateTime(long createTime) { this.createTime = createTime; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public java.util.Set<String> getReadByUsers() { return readByUsers; }
    public void setReadByUsers(java.util.Set<String> readByUsers) { this.readByUsers = readByUsers; }
    
    // Helper to check if read by a specific user
    public boolean isReadBy(String username) {
        if ("*".equals(toUser)) {
            return readByUsers.contains(username);
        }
        return read;
    }

    public void markReadBy(String username) {
        if ("*".equals(toUser)) {
            readByUsers.add(username);
        } else {
            this.read = true;
        }
    }
}
