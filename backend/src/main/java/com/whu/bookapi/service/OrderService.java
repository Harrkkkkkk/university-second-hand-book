package com.whu.bookapi.service;

import com.whu.bookapi.model.Book;
import com.whu.bookapi.model.Order;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderService {
    private final Map<Long, Order> store = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(100);

    public Order create(Book book, String buyerName) {
        Order o = new Order();
        o.setId(idGen.incrementAndGet());
        o.setBookId(book.getId());
        o.setBookName(book.getBookName());
        o.setSellerName(book.getSellerName());
        o.setPrice(book.getSellPrice());
        o.setBuyerName(buyerName);
        o.setStatus("pending");
        o.setCreateTime(LocalDateTime.now());
        store.put(o.getId(), o);
        return o;
    }

    public List<Order> listByBuyer(String buyerName) {
        List<Order> res = new ArrayList<>();
        for (Order o : store.values()) {
            if (buyerName.equals(o.getBuyerName())) res.add(o);
        }
        return res;
    }

    public Order get(Long id) { return store.get(id); }

    public Order setStatus(Long id, String status) {
        Order o = store.get(id);
        if (o != null) o.setStatus(status);
        return o;
    }

    public List<Order> listAll() {
        return new ArrayList<>(store.values());
    }
}
