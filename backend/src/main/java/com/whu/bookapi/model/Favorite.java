package com.whu.bookapi.model;

public class Favorite {
    private Long bookId;
    private String username;

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
