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

    public void add(String username, Long bookId) {
        Map<Long, CartItem> cart = carts.computeIfAbsent(username, k -> new ConcurrentHashMap<>());
        cart.compute(bookId, (k, v) -> {
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
