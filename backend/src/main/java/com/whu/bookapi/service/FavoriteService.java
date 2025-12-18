package com.whu.bookapi.service;

import com.whu.bookapi.model.Book;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FavoriteService {
    private final Map<String, Set<Long>> favs = new ConcurrentHashMap<>();

    public void add(String username, Long bookId) {
        favs.computeIfAbsent(username, k -> ConcurrentHashMap.newKeySet()).add(bookId);
    }

    public void remove(String username, Long bookId) {
        Set<Long> s = favs.get(username);
        if (s != null) s.remove(bookId);
    }

    public boolean isCollected(String username, Long bookId) {
        Set<Long> s = favs.get(username);
        return s != null && s.contains(bookId);
    }

    public List<Long> listIds(String username) {
        Set<Long> s = favs.getOrDefault(username, Collections.emptySet());
        return new ArrayList<>(s);
    }

    public List<Book> list(String username, Map<Long, Book> bookStore) {
        Set<Long> s = favs.getOrDefault(username, Collections.emptySet());
        List<Book> res = new ArrayList<>();
        for (Long id : s) {
            Book b = bookStore.get(id);
            if (b != null) res.add(b);
        }
        return res;
    }
}
