package com.whu.bookapi.service;

import com.whu.bookapi.model.Notification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class NotificationService {
    private final ConcurrentHashMap<Long, Notification> store = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    public Notification addToUser(String username, String type, String title, String content) {
        Notification n = new Notification();
        n.setId(idGen.incrementAndGet());
        n.setToUser(username);
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        n.setCreateTime(System.currentTimeMillis());
        store.put(n.getId(), n);
        return n;
    }

    public Notification addBroadcast(String type, String title, String content) {
        Notification n = new Notification();
        n.setId(idGen.incrementAndGet());
        n.setToUser("*");
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        n.setCreateTime(System.currentTimeMillis());
        store.put(n.getId(), n);
        return n;
    }

    public List<Notification> listByUser(String username) {
        List<Notification> res = new ArrayList<>();
        for (Notification n : store.values()) {
            if (username.equals(n.getToUser()) || "*".equals(n.getToUser())) res.add(n);
        }
        res.sort((a,b) -> Long.compare(b.getCreateTime(), a.getCreateTime()));
        return res;
    }
}
