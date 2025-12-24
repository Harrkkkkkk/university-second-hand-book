package com.whu.bookapi.service;

import com.whu.bookapi.model.Book;
import com.whu.bookapi.model.CartItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {
    private final BookService bookService;
    private final JdbcTemplate jdbcTemplate;

    public CartService(BookService bookService, JdbcTemplate jdbcTemplate) {
        this.bookService = bookService;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void add(String username, Long bookId) {
        Book book = bookService.get(bookId);
        if (book == null) {
            throw new RuntimeException("Book not found");
        }
        
        if (username.equals(book.getSellerName())) {
            throw new RuntimeException("Cannot buy your own book");
        }

        Integer existingQty = null;
        try {
            existingQty = jdbcTemplate.queryForObject(
                    "SELECT quantity FROM cart_item WHERE username = ? AND book_id = ?",
                    Integer.class,
                    username,
                    bookId
            );
        } catch (org.springframework.dao.EmptyResultDataAccessException ignored) {
        }
        int currentQty = existingQty == null ? 0 : existingQty;
        int stock = book.getStock() == null ? 0 : book.getStock();
        if (currentQty + 1 > stock) {
            throw new RuntimeException("Inventory insufficient");
        }
        jdbcTemplate.update(
                "INSERT INTO cart_item (username, book_id, quantity) VALUES (?, ?, 1) ON DUPLICATE KEY UPDATE quantity = quantity + 1",
                username,
                bookId
        );
    }

    public void remove(String username, Long bookId) {
        remove(username, bookId, null);
    }

    public void remove(String username, Long bookId, Integer count) {
        if (username == null || bookId == null) return;
        if (count == null || count <= 0) {
            jdbcTemplate.update("DELETE FROM cart_item WHERE username = ? AND book_id = ?", username, bookId);
            return;
        }
        Integer existingQty = null;
        try {
            existingQty = jdbcTemplate.queryForObject(
                    "SELECT quantity FROM cart_item WHERE username = ? AND book_id = ?",
                    Integer.class,
                    username,
                    bookId
            );
        } catch (org.springframework.dao.EmptyResultDataAccessException ignored) {
        }
        if (existingQty == null) return;
        if (count >= existingQty) {
            jdbcTemplate.update("DELETE FROM cart_item WHERE username = ? AND book_id = ?", username, bookId);
        } else {
            jdbcTemplate.update(
                    "UPDATE cart_item SET quantity = quantity - ? WHERE username = ? AND book_id = ?",
                    count,
                    username,
                    bookId
            );
        }
    }

    public void clear(String username) {
        jdbcTemplate.update("DELETE FROM cart_item WHERE username = ?", username);
    }

    public List<CartItem> list(String username) {
        List<CartItem> res = new ArrayList<>();
        List<java.util.Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT book_id, quantity FROM cart_item WHERE username = ? ORDER BY book_id",
                username
        );
        for (java.util.Map<String, Object> row : rows) {
            CartItem ci = new CartItem();
            ci.setUsername(username);
            ci.setBookId(((Number) row.get("book_id")).longValue());
            ci.setQuantity(((Number) row.get("quantity")).intValue());
            res.add(ci);
        }
        return res;
    }
}
