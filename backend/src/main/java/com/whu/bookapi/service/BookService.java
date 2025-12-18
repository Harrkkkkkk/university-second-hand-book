package com.whu.bookapi.service;

import com.whu.bookapi.model.Book;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class BookService {
    private final ConcurrentHashMap<Long, Book> store = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(0);
    private final java.util.concurrent.ConcurrentHashMap<Long, java.util.concurrent.locks.ReentrantLock> locks = new java.util.concurrent.ConcurrentHashMap<>();

    public BookService() {
        addSample("Java编程思想", "Bruce Eckel", 108d, 30d, "卖家1");
        addSample("Python入门到精通", "张三", 89d, 25d, "卖家1");
        addSample("红楼梦", "曹雪芹", 45d, 15d, "卖家2");
        addSample("经济学原理", "曼昆", 68d, 20d, "卖家2");
        addSample("数据结构与算法", "李四", 79d, 20d, "卖家1");
        addSample("西游记", "吴承恩", 38d, 12d, "卖家2");
        addSample("MySQL实战", "王五", 99d, 28d, "卖家1");
        addSample("财务管理", "赵六", 59d, 18d, "卖家2");
    }

    private void addSample(String name, String author, Double original, Double sell, String seller) {
        Book b = new Book();
        b.setId(idGen.incrementAndGet());
        b.setBookName(name);
        b.setAuthor(author);
        b.setOriginalPrice(original);
        b.setSellPrice(sell);
        b.setSellerName(seller);
        b.setConditionLevel("九成新");
        b.setStock(10);
        b.setStatus("on_sale");
        b.setCreatedAt(System.currentTimeMillis());
        store.put(b.getId(), b);
        locks.put(b.getId(), new java.util.concurrent.locks.ReentrantLock());
    }

    public Book add(Book book, String sellerName) {
        long id = idGen.incrementAndGet();
        book.setId(id);
        book.setSellerName(sellerName);
        if (book.getConditionLevel() == null) book.setConditionLevel("九成新");
        if (book.getStock() == null) book.setStock(1);
        book.setStatus("under_review");
        book.setCreatedAt(System.currentTimeMillis());
        store.put(id, book);
        locks.put(id, new java.util.concurrent.locks.ReentrantLock());
        return book;
    }

    public Book get(Long id) {
        return store.get(id);
    }

    public boolean tryReserveStock(Long id) {
        Book b = store.get(id);
        if (b == null) return false;
        java.util.concurrent.locks.ReentrantLock lock = locks.computeIfAbsent(id, k -> new java.util.concurrent.locks.ReentrantLock());
        lock.lock();
        try {
            Integer stock = b.getStock();
            if (stock == null) stock = 0;
            if (stock <= 0) return false;
            b.setStock(stock - 1);
            return true;
        } finally {
            lock.unlock();
        }
    }

    public void releaseStock(Long id) {
        Book b = store.get(id);
        if (b == null) return;
        java.util.concurrent.locks.ReentrantLock lock = locks.computeIfAbsent(id, k -> new java.util.concurrent.locks.ReentrantLock());
        lock.lock();
        try {
            Integer stock = b.getStock();
            if (stock == null) stock = 0;
            b.setStock(stock + 1);
        } finally {
            lock.unlock();
        }
    }

    public List<Book> page(String bookName, Double minPrice, Double maxPrice, String conditionLevel, int pageNum, int pageSize, String sortBy) {
        List<Book> all = new ArrayList<>(store.values());
        if ("price_asc".equals(sortBy)) {
            all.sort(Comparator.comparingDouble(b -> b.getSellPrice() == null ? Double.MAX_VALUE : b.getSellPrice()));
        } else if ("price_desc".equals(sortBy)) {
            all.sort((a, b) -> Double.compare(b.getSellPrice() == null ? 0 : b.getSellPrice(), a.getSellPrice() == null ? 0 : a.getSellPrice()));
        } else if ("created_desc".equals(sortBy)) {
            all.sort((a, b) -> Long.compare(b.getCreatedAt() == null ? 0 : b.getCreatedAt(), a.getCreatedAt() == null ? 0 : a.getCreatedAt()));
        } else {
            all.sort(Comparator.comparingLong(Book::getId));
        }
        List<Book> filtered = new ArrayList<>();
        for (Book b : all) {
            boolean ok = true;
            if (bookName != null && !bookName.isEmpty()) {
                String key = bookName.toLowerCase();
                String name = b.getBookName() == null ? "" : b.getBookName().toLowerCase();
                String author = b.getAuthor() == null ? "" : b.getAuthor().toLowerCase();
                ok = name.contains(key) || author.contains(key);
            }
            if (ok && minPrice != null) {
                ok = b.getSellPrice() != null && b.getSellPrice() >= minPrice;
            }
            if (ok && maxPrice != null) {
                ok = b.getSellPrice() != null && b.getSellPrice() <= maxPrice;
            }
            if (ok && conditionLevel != null && !conditionLevel.isEmpty()) {
                ok = conditionLevel.equals(b.getConditionLevel());
            }
            if (ok) {
                // 默认只展示在售与审核通过的商品
                ok = "on_sale".equals(b.getStatus()) || "under_review".equals(b.getStatus());
            }
            if (ok) filtered.add(b);
        }
        int from = Math.max(0, (pageNum - 1) * pageSize);
        int to = Math.min(filtered.size(), from + pageSize);
        if (from >= to) return new ArrayList<>();
        return filtered.subList(from, to);
    }

    public long count(String bookName, Double minPrice, Double maxPrice) {
        long c = 0;
        for (Book b : store.values()) {
            boolean ok = true;
            if (bookName != null && !bookName.isEmpty()) {
                String key = bookName.toLowerCase();
                String name = b.getBookName() == null ? "" : b.getBookName().toLowerCase();
                String author = b.getAuthor() == null ? "" : b.getAuthor().toLowerCase();
                ok = name.contains(key) || author.contains(key);
            }
            if (ok && minPrice != null) {
                ok = b.getSellPrice() != null && b.getSellPrice() >= minPrice;
            }
            if (ok && maxPrice != null) {
                ok = b.getSellPrice() != null && b.getSellPrice() <= maxPrice;
            }
            if (ok) {
                ok = "on_sale".equals(b.getStatus()) || "under_review".equals(b.getStatus());
            }
            if (ok) c++;
        }
        return c;
    }

    public List<Book> listHot(int limit) {
        List<Book> all = new ArrayList<>(store.values());
        List<Book> onSale = new ArrayList<>();
        for (Book b : all) {
            if ("on_sale".equals(b.getStatus())) onSale.add(b);
        }
        onSale.sort((a, b) -> Long.compare(b.getCreatedAt() == null ? 0 : b.getCreatedAt(), a.getCreatedAt() == null ? 0 : a.getCreatedAt()));
        int n = Math.min(Math.max(limit, 1), onSale.size());
        return onSale.subList(0, n);
    }

    public Book update(Book incoming, String operator) {
        Book origin = store.get(incoming.getId());
        if (origin == null) return null;
        if (!operator.equals(origin.getSellerName())) return null;
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
        origin.setStatus("under_review"); // 编辑后进入待审核
        return origin;
    }

    public boolean offline(Long id, String operator) {
        Book b = store.get(id);
        if (b == null) return false;
        if (!operator.equals(b.getSellerName())) return false;
        b.setStatus("offline");
        return true;
    }

    public boolean delete(Long id, String operator) {
        Book b = store.get(id);
        if (b == null) return false;
        if (!operator.equals(b.getSellerName())) return false;
        store.remove(id);
        return true;
    }

    public java.util.List<Book> listBySeller(String sellerName) {
        java.util.List<Book> res = new java.util.ArrayList<>();
        for (Book b : store.values()) {
            if (sellerName.equals(b.getSellerName())) res.add(b);
        }
        res.sort(Comparator.comparingLong(Book::getId));
        return res;
    }

    public List<Book> listUnderReview() {
        List<Book> res = new ArrayList<>();
        for (Book b : store.values()) {
            if ("under_review".equals(b.getStatus())) res.add(b);
        }
        res.sort(Comparator.comparingLong(Book::getId));
        return res;
    }

    public boolean approve(Long id) {
        Book b = store.get(id);
        if (b == null) return false;
        b.setStatus("on_sale");
        return true;
    }

    public boolean reject(Long id) {
        Book b = store.get(id);
        if (b == null) return false;
        b.setStatus("rejected");
        return true;
    }
}
