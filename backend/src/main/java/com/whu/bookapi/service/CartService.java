package com.whu.bookapi.service;

import com.whu.bookapi.model.Book;
import com.whu.bookapi.model.CartItem;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CartService {
    private final Map<String, Map<Long, CartItem>> carts = new ConcurrentHashMap<>();
    private final BookService bookService;

    public CartService(BookService bookService) {
        this.bookService = bookService;
    }

    public void add(String username, Long bookId) {
        Book book = bookService.get(bookId);
        if (book == null) {
            throw new RuntimeException("Book not found");
        }
        
        if (username.equals(book.getSellerName())) {
            throw new RuntimeException("Cannot buy your own book");
        }
        
        Map<Long, CartItem> cart = carts.computeIfAbsent(username, k -> new ConcurrentHashMap<>());
        
        synchronized (cart) {
            cart.compute(bookId, (k, v) -> {
                int currentQty = (v == null) ? 0 : v.getQuantity();
                if (currentQty + 1 > book.getStock()) {
                    throw new RuntimeException("Inventory insufficient");
                }
                
                if (v == null) {
                    CartItem ci = new CartItem();
                    ci.setUsername(username);
                    ci.setBookId(bookId);
                    ci.setQuantity(1);
                    return ci;
                } else {
                    v.setQuantity(v.getQuantity() + 1);
                    return v;
                }
            });
        }
    }

    public void remove(String username, Long bookId) {
        Map<Long, CartItem> cart = carts.get(username);
        if (cart != null) cart.remove(bookId);
    }

    public void clear(String username) {
        Map<Long, CartItem> cart = carts.get(username);
        if (cart != null) cart.clear();
    }

    public List<CartItem> list(String username) {
        Map<Long, CartItem> cart = carts.getOrDefault(username, new ConcurrentHashMap<>());
        return new ArrayList<>(cart.values());
    }
}
