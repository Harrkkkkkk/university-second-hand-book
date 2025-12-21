package com.whu.bookapi.service;

import com.whu.bookapi.model.Book;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FavoriteService {
    private final JdbcTemplate jdbcTemplate;

    public FavoriteService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void add(String username, Long bookId) {
        if (username == null || bookId == null) return;
        jdbcTemplate.update(
                "INSERT IGNORE INTO favorites (username, book_id, created_at) VALUES (?, ?, ?)",
                username,
                bookId,
                System.currentTimeMillis()
        );
    }

    public void remove(String username, Long bookId) {
        if (username == null || bookId == null) return;
        jdbcTemplate.update("DELETE FROM favorites WHERE username = ? AND book_id = ?", username, bookId);
    }

    public boolean isCollected(String username, Long bookId) {
        if (username == null || bookId == null) return false;
        Integer c = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM favorites WHERE username = ? AND book_id = ?",
                Integer.class,
                username,
                bookId
        );
        return c != null && c > 0;
    }

    public List<Long> listIds(String username) {
        if (username == null) return new ArrayList<>();
        return jdbcTemplate.queryForList(
                "SELECT book_id FROM favorites WHERE username = ? ORDER BY created_at DESC",
                Long.class,
                username
        );
    }

    public List<Book> list(String username) {
        if (username == null) return new ArrayList<>();
        return jdbcTemplate.query(
                "SELECT b.id, b.book_name, b.author, b.original_price, b.sell_price, b.description, b.seller_name, b.cover_url, b.isbn, b.publisher, b.publish_date, b.condition_level, b.stock, b.status, b.created_at, b.seller_type " +
                        "FROM favorites f JOIN books b ON f.book_id = b.id " +
                        "WHERE f.username = ? AND b.status = 'on_sale' AND b.stock > 0 " +
                        "ORDER BY f.created_at DESC",
                (rs, rowNum) -> {
                    Book b = new Book();
                    b.setId(rs.getLong("id"));
                    b.setBookName(rs.getString("book_name"));
                    b.setAuthor(rs.getString("author"));
                    b.setOriginalPrice((Double) rs.getObject("original_price"));
                    b.setSellPrice((Double) rs.getObject("sell_price"));
                    b.setDescription(rs.getString("description"));
                    b.setSellerName(rs.getString("seller_name"));
                    b.setCoverUrl(rs.getString("cover_url"));
                    b.setIsbn(rs.getString("isbn"));
                    b.setPublisher(rs.getString("publisher"));
                    b.setPublishDate(rs.getString("publish_date"));
                    b.setConditionLevel(rs.getString("condition_level"));
                    b.setStock((Integer) rs.getObject("stock"));
                    b.setStatus(rs.getString("status"));
                    b.setCreatedAt(rs.getLong("created_at"));
                    b.setSellerType(rs.getString("seller_type"));
                    return b;
                },
                username
        );
    }
}
