package com.whu.bookapi.service;

import com.whu.bookapi.model.Book;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {
    private final JdbcTemplate jdbcTemplate;

    public BookService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Book add(Book book, String sellerName) {
        if (book == null) return null;
        book.setSellerName(sellerName);
        if (book.getConditionLevel() == null) book.setConditionLevel("九成新");
        if (book.getStock() == null) book.setStock(1);
        book.setStatus("under_review");
        book.setCreatedAt(System.currentTimeMillis());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO books (book_name, author, original_price, sell_price, description, seller_name, cover_url, isbn, publisher, publish_date, condition_level, stock, status, created_at, seller_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, book.getBookName());
            ps.setString(2, book.getAuthor());
            ps.setObject(3, book.getOriginalPrice());
            ps.setObject(4, book.getSellPrice());
            ps.setString(5, book.getDescription());
            ps.setString(6, book.getSellerName());
            ps.setString(7, book.getCoverUrl());
            ps.setString(8, book.getIsbn());
            ps.setString(9, book.getPublisher());
            ps.setString(10, book.getPublishDate());
            ps.setString(11, book.getConditionLevel());
            ps.setInt(12, book.getStock() == null ? 1 : book.getStock());
            ps.setString(13, book.getStatus());
            ps.setLong(14, book.getCreatedAt() == null ? System.currentTimeMillis() : book.getCreatedAt());
            ps.setString(15, book.getSellerType());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key != null) book.setId(key.longValue());
        return book;
    }

    public Book get(Long id) {
        if (id == null) return null;
        java.util.List<Book> list = jdbcTemplate.query(
                "SELECT id, book_name, author, original_price, sell_price, description, seller_name, cover_url, isbn, publisher, publish_date, condition_level, stock, status, created_at, seller_type FROM books WHERE id = ?",
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
                id
        );
        return list.isEmpty() ? null : list.get(0);
    }

    public boolean tryReserveStock(Long id) {
        if (id == null) return false;
        int updated = jdbcTemplate.update("UPDATE books SET stock = stock - 1 WHERE id = ? AND stock > 0", id);
        return updated > 0;
    }

    public void releaseStock(Long id) {
        if (id == null) return;
        jdbcTemplate.update("UPDATE books SET stock = stock + 1 WHERE id = ?", id);
    }

    public List<Book> page(String bookName, Double minPrice, Double maxPrice, String conditionLevel, int pageNum, int pageSize, String sortBy) {
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 200);
        java.util.List<Object> params = new java.util.ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT id, book_name, author, original_price, sell_price, description, seller_name, cover_url, isbn, publisher, publish_date, condition_level, stock, status, created_at, seller_type FROM books WHERE status = 'on_sale' AND stock > 0");
        if (bookName != null && !bookName.isEmpty()) {
            sb.append(" AND (LOWER(book_name) LIKE ? OR LOWER(author) LIKE ?)");
            String like = "%" + bookName.toLowerCase() + "%";
            params.add(like);
            params.add(like);
        }
        if (minPrice != null) {
            sb.append(" AND sell_price IS NOT NULL AND sell_price >= ?");
            params.add(minPrice);
        }
        if (maxPrice != null) {
            sb.append(" AND sell_price IS NOT NULL AND sell_price <= ?");
            params.add(maxPrice);
        }
        if (conditionLevel != null && !conditionLevel.isEmpty()) {
            sb.append(" AND condition_level = ?");
            params.add(conditionLevel);
        }
        if ("price_asc".equals(sortBy)) sb.append(" ORDER BY sell_price ASC");
        else if ("price_desc".equals(sortBy)) sb.append(" ORDER BY sell_price DESC");
        else if ("created_desc".equals(sortBy)) sb.append(" ORDER BY created_at DESC");
        else sb.append(" ORDER BY id ASC");
        sb.append(" LIMIT ? OFFSET ?");
        params.add(safePageSize);
        params.add((safePageNum - 1) * safePageSize);
        return jdbcTemplate.query(
                sb.toString(),
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
                params.toArray()
        );
    }

    public long count(String bookName, Double minPrice, Double maxPrice) {
        java.util.List<Object> params = new java.util.ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT COUNT(1) FROM books WHERE status = 'on_sale' AND stock > 0");
        if (bookName != null && !bookName.isEmpty()) {
            sb.append(" AND (LOWER(book_name) LIKE ? OR LOWER(author) LIKE ?)");
            String like = "%" + bookName.toLowerCase() + "%";
            params.add(like);
            params.add(like);
        }
        if (minPrice != null) {
            sb.append(" AND sell_price IS NOT NULL AND sell_price >= ?");
            params.add(minPrice);
        }
        if (maxPrice != null) {
            sb.append(" AND sell_price IS NOT NULL AND sell_price <= ?");
            params.add(maxPrice);
        }
        Long c = jdbcTemplate.queryForObject(sb.toString(), Long.class, params.toArray());
        return c == null ? 0 : c;
    }

    public List<Book> listHot(int limit) {
        int n = Math.min(Math.max(limit, 1), 50);
        return jdbcTemplate.query(
                "SELECT id, book_name, author, original_price, sell_price, description, seller_name, cover_url, isbn, publisher, publish_date, condition_level, stock, status, created_at, seller_type FROM books WHERE status = 'on_sale' ORDER BY created_at DESC LIMIT ?",
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
                n
        );
    }

    public Book update(Book incoming, String operator) {
        if (incoming == null || incoming.getId() == null) return null;
        Book origin = get(incoming.getId());
        if (origin == null) return null;
        if (operator == null || !operator.equals(origin.getSellerName())) return null;
        if (incoming.getBookName() != null) origin.setBookName(incoming.getBookName());
        if (incoming.getAuthor() != null) origin.setAuthor(incoming.getAuthor());
        if (incoming.getOriginalPrice() != null) origin.setOriginalPrice(incoming.getOriginalPrice());
        if (incoming.getSellPrice() != null) origin.setSellPrice(incoming.getSellPrice());
        if (incoming.getDescription() != null) origin.setDescription(incoming.getDescription());
        if (incoming.getCoverUrl() != null) origin.setCoverUrl(incoming.getCoverUrl());
        if (incoming.getIsbn() != null) origin.setIsbn(incoming.getIsbn());
        if (incoming.getPublisher() != null) origin.setPublisher(incoming.getPublisher());
        if (incoming.getPublishDate() != null) origin.setPublishDate(incoming.getPublishDate());
        if (incoming.getConditionLevel() != null) origin.setConditionLevel(incoming.getConditionLevel());
        if (incoming.getStock() != null) origin.setStock(incoming.getStock());
        origin.setStatus("under_review");
        jdbcTemplate.update(
                "UPDATE books SET book_name=?, author=?, original_price=?, sell_price=?, description=?, cover_url=?, isbn=?, publisher=?, publish_date=?, condition_level=?, stock=?, status=?, seller_type=? WHERE id=? AND seller_name=?",
                origin.getBookName(),
                origin.getAuthor(),
                origin.getOriginalPrice(),
                origin.getSellPrice(),
                origin.getDescription(),
                origin.getCoverUrl(),
                origin.getIsbn(),
                origin.getPublisher(),
                origin.getPublishDate(),
                origin.getConditionLevel(),
                origin.getStock(),
                origin.getStatus(),
                origin.getSellerType(),
                origin.getId(),
                operator
        );
        return origin;
    }

    public boolean offline(Long id, String operator) {
        if (id == null || operator == null) return false;
        int updated = jdbcTemplate.update("UPDATE books SET status = 'offline' WHERE id = ? AND seller_name = ?", id, operator);
        return updated > 0;
    }

    public boolean delete(Long id, String operator) {
        if (id == null || operator == null) return false;
        int updated = jdbcTemplate.update("DELETE FROM books WHERE id = ? AND seller_name = ?", id, operator);
        return updated > 0;
    }

    public java.util.List<Book> listBySeller(String sellerName) {
        if (sellerName == null) return new java.util.ArrayList<>();
        return jdbcTemplate.query(
                "SELECT id, book_name, author, original_price, sell_price, description, seller_name, cover_url, isbn, publisher, publish_date, condition_level, stock, status, created_at, seller_type FROM books WHERE seller_name = ? ORDER BY id",
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
                sellerName
        );
    }

    public List<Book> listUnderReview() {
        return jdbcTemplate.query(
                "SELECT id, book_name, author, original_price, sell_price, description, seller_name, cover_url, isbn, publisher, publish_date, condition_level, stock, status, created_at, seller_type FROM books WHERE status = 'under_review' ORDER BY id",
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
                }
        );
    }

    public boolean approve(Long id) {
        if (id == null) return false;
        int updated = jdbcTemplate.update("UPDATE books SET status = 'on_sale' WHERE id = ?", id);
        return updated > 0;
    }

    public boolean reject(Long id) {
        if (id == null) return false;
        int updated = jdbcTemplate.update("UPDATE books SET status = 'rejected' WHERE id = ?", id);
        return updated > 0;
    }
}
