package com.whu.bookapi.service;

import com.whu.bookapi.model.Complaint;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ComplaintService {
    private final ConcurrentHashMap<Long, Complaint> store = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    public Complaint add(Complaint c) {
        c.setId(idGen.incrementAndGet());
        c.setCreateTime(System.currentTimeMillis());
        c.setStatus("pending");
        store.put(c.getId(), c);
        return c;
    }

    public List<Complaint> listByUser(String username) {
        List<Complaint> res = new ArrayList<>();
        for (Complaint c : store.values()) if (username.equals(c.getUsername())) res.add(c);
        return res;
    }

    public List<Complaint> listAll() { return new ArrayList<>(store.values()); }

    public boolean setStatus(Long id, String status) {
        Complaint c = store.get(id);
        if (c == null) return false;
        c.setStatus(status);
        return true;
    }
}
