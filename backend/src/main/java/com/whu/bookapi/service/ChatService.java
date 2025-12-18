package com.whu.bookapi.service;

import com.whu.bookapi.model.ChatMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ChatService {
    private final ConcurrentHashMap<String, List<ChatMessage>> conv = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    private String key(String a, String b, Long bookId, Long orderId) {
        String u1 = a.compareTo(b) <= 0 ? a : b;
        String u2 = a.compareTo(b) <= 0 ? b : a;
        return (bookId != null ? "B" + bookId : "O" + (orderId == null ? 0 : orderId)) + ":" + u1 + ":" + u2;
    }

    public ChatMessage send(ChatMessage m) {
        m.setId(idGen.incrementAndGet());
        m.setCreateTime(System.currentTimeMillis());
        m.setRead(false);
        String k = key(m.getFromUser(), m.getToUser(), m.getBookId(), m.getOrderId());
        conv.computeIfAbsent(k, kk -> new ArrayList<>()).add(m);
        return m;
    }

    public void markRead(String username, String peer) {
        for (String k : conv.keySet()) {
            String[] parts = k.split(":", 3);
            if (parts.length < 3) continue;
            String u1 = parts[1];
            String u2 = parts[2];
            if ((username.equals(u1) && peer.equals(u2)) || (username.equals(u2) && peer.equals(u1))) {
                List<ChatMessage> list = conv.get(k);
                if (list != null) {
                    for (ChatMessage m : list) {
                        if (username.equals(m.getToUser()) && !m.isRead()) {
                            m.setRead(true);
                        }
                    }
                }
            }
        }
    }

    public long countTotalUnread(String username) {
        long count = 0;
        for (List<ChatMessage> list : conv.values()) {
            for (ChatMessage m : list) {
                if (username.equals(m.getToUser()) && !m.isRead()) {
                    count++;
                }
            }
        }
        return count;
    }

    public List<ChatMessage> history(String a, String b, Long bookId, Long orderId) {
        String k = key(a, b, bookId, orderId);
        return new ArrayList<>(conv.getOrDefault(k, new ArrayList<>()));
    }

    public java.util.List<java.util.Map<String, Object>> listConversations(String username) {
        java.util.List<java.util.Map<String, Object>> res = new java.util.ArrayList<>();
        for (String k : conv.keySet()) {
            String[] parts = k.split(":", 3);
            if (parts.length < 3) continue;
            String ctx = parts[0];
            String u1 = parts[1];
            String u2 = parts[2];
            if (!username.equals(u1) && !username.equals(u2)) continue;
            String peer = username.equals(u1) ? u2 : u1;
            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("peer", peer);
            if (ctx.startsWith("B")) item.put("bookId", Long.parseLong(ctx.substring(1)));
            else if (ctx.startsWith("O")) item.put("orderId", Long.parseLong(ctx.substring(1)));
            java.util.List<ChatMessage> list = conv.getOrDefault(k, new java.util.ArrayList<>());
            int unread = 0;
            if (!list.isEmpty()) {
                ChatMessage last = list.get(list.size() - 1);
                item.put("lastContent", last.getContent());
                item.put("lastTime", last.getCreateTime());
                for (ChatMessage m : list) {
                    if (username.equals(m.getToUser()) && !m.isRead()) {
                        unread++;
                    }
                }
            }
            item.put("unread", unread);
            res.add(item);
        }
        res.sort((a,b) -> Long.compare((Long)b.getOrDefault("lastTime", 0L), (Long)a.getOrDefault("lastTime", 0L)));
        return res;
    }
}
