package com.whu.bookapi.service;

import com.whu.bookapi.model.Book;
import com.whu.bookapi.model.Order;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class OrderService {
    private final JdbcTemplate jdbcTemplate;

    public OrderService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Order create(Book book, String buyerName) {
        if (book == null || buyerName == null) return null;
        Order o = new Order();
        o.setBookId(book.getId());
        o.setBookName(book.getBookName());
        o.setSellerName(book.getSellerName());
        o.setPrice(book.getSellPrice());
        o.setBuyerName(buyerName);
        o.setStatus("pending");
        o.setCreateTime(LocalDateTime.now());
        o.setExpireAt(o.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() + 15L * 60 * 1000);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO orders (book_id, book_name, seller_name, price, buyer_name, status, expire_at, create_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, book.getId());
            ps.setString(2, book.getBookName());
            ps.setString(3, book.getSellerName());
            ps.setObject(4, book.getSellPrice());
            ps.setString(5, buyerName);
            ps.setString(6, o.getStatus());
            ps.setLong(7, o.getExpireAt() == null ? 0 : o.getExpireAt());
            ps.setTimestamp(8, java.sql.Timestamp.valueOf(o.getCreateTime()));
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key != null) o.setId(key.longValue());
        return o;
    }

    public List<Order> listByBuyer(String buyerName) {
        if (buyerName == null) return java.util.Collections.emptyList();
        return jdbcTemplate.query(
                "SELECT id, book_id, book_name, seller_name, price, buyer_name, status, expire_at, create_time FROM orders WHERE buyer_name = ? ORDER BY create_time DESC",
                (rs, rowNum) -> {
                    Order o = new Order();
                    o.setId(rs.getLong("id"));
                    o.setBookId(rs.getLong("book_id"));
                    o.setBookName(rs.getString("book_name"));
                    o.setSellerName(rs.getString("seller_name"));
                    o.setPrice((Double) rs.getObject("price"));
                    o.setBuyerName(rs.getString("buyer_name"));
                    o.setStatus(rs.getString("status"));
                    long expireAt = rs.getLong("expire_at");
                    if (!rs.wasNull() && expireAt > 0) o.setExpireAt(expireAt);
                    java.sql.Timestamp ts = rs.getTimestamp("create_time");
                    o.setCreateTime(ts == null ? null : ts.toLocalDateTime());
                    if (o.getExpireAt() == null && o.getCreateTime() != null) {
                        o.setExpireAt(o.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() + 15L * 60 * 1000);
                    }
                    return o;
                },
                buyerName
        );
    }

    public List<Order> listBySeller(String sellerName) {
        if (sellerName == null) return java.util.Collections.emptyList();
        return jdbcTemplate.query(
                "SELECT id, book_id, book_name, seller_name, price, buyer_name, status, expire_at, create_time FROM orders WHERE seller_name = ? ORDER BY create_time DESC",
                (rs, rowNum) -> {
                    Order o = new Order();
                    o.setId(rs.getLong("id"));
                    o.setBookId(rs.getLong("book_id"));
                    o.setBookName(rs.getString("book_name"));
                    o.setSellerName(rs.getString("seller_name"));
                    o.setPrice((Double) rs.getObject("price"));
                    o.setBuyerName(rs.getString("buyer_name"));
                    o.setStatus(rs.getString("status"));
                    long expireAt = rs.getLong("expire_at");
                    if (!rs.wasNull() && expireAt > 0) o.setExpireAt(expireAt);
                    java.sql.Timestamp ts = rs.getTimestamp("create_time");
                    o.setCreateTime(ts == null ? null : ts.toLocalDateTime());
                    if (o.getExpireAt() == null && o.getCreateTime() != null) {
                        o.setExpireAt(o.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() + 15L * 60 * 1000);
                    }
                    return o;
                },
                sellerName
        );
    }

    public Order get(Long id) {
        if (id == null) return null;
        try {
            return jdbcTemplate.queryForObject(
                "SELECT id, book_id, book_name, seller_name, price, buyer_name, status, expire_at, create_time FROM orders WHERE id = ?",
                    (rs, rowNum) -> {
                        Order o = new Order();
                        o.setId(rs.getLong("id"));
                        o.setBookId(rs.getLong("book_id"));
                        o.setBookName(rs.getString("book_name"));
                        o.setSellerName(rs.getString("seller_name"));
                        o.setPrice((Double) rs.getObject("price"));
                        o.setBuyerName(rs.getString("buyer_name"));
                        o.setStatus(rs.getString("status"));
                        long expireAt = rs.getLong("expire_at");
                        if (!rs.wasNull() && expireAt > 0) o.setExpireAt(expireAt);
                        java.sql.Timestamp ts = rs.getTimestamp("create_time");
                        o.setCreateTime(ts == null ? null : ts.toLocalDateTime());
                        if (o.getExpireAt() == null && o.getCreateTime() != null) {
                            o.setExpireAt(o.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() + 15L * 60 * 1000);
                        }
                        return o;
                    },
                    id
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Order setStatus(Long id, String status) {
        if (id == null || status == null) return null;
        jdbcTemplate.update("UPDATE orders SET status = ? WHERE id = ?", status, id);
        return get(id);
    }

    public List<Order> listAll() {
        return jdbcTemplate.query(
                "SELECT id, book_id, book_name, seller_name, price, buyer_name, status, expire_at, create_time FROM orders ORDER BY create_time DESC",
                (rs, rowNum) -> {
                    Order o = new Order();
                    o.setId(rs.getLong("id"));
                    o.setBookId(rs.getLong("book_id"));
                    o.setBookName(rs.getString("book_name"));
                    o.setSellerName(rs.getString("seller_name"));
                    o.setPrice((Double) rs.getObject("price"));
                    o.setBuyerName(rs.getString("buyer_name"));
                    o.setStatus(rs.getString("status"));
                    long expireAt = rs.getLong("expire_at");
                    if (!rs.wasNull() && expireAt > 0) o.setExpireAt(expireAt);
                    java.sql.Timestamp ts = rs.getTimestamp("create_time");
                    o.setCreateTime(ts == null ? null : ts.toLocalDateTime());
                    if (o.getExpireAt() == null && o.getCreateTime() != null) {
                        o.setExpireAt(o.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() + 15L * 60 * 1000);
                    }
                    return o;
                }
        );
    }
}
