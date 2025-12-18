package com.whu.bookapi.model;

public class ChatMessage {
    private Long id;
    private String fromUser;
    private String toUser;
    private Long bookId;
    private Long orderId;
    private String content;
    private long createTime;
    private boolean read;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFromUser() { return fromUser; }
    public void setFromUser(String fromUser) { this.fromUser = fromUser; }
    public String getToUser() { return toUser; }
    public void setToUser(String toUser) { this.toUser = toUser; }
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public long getCreateTime() { return createTime; }
    public void setCreateTime(long createTime) { this.createTime = createTime; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
}
