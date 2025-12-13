package com.whu.bookapi.schedule;

import com.whu.bookapi.model.Order;
import com.whu.bookapi.service.OrderService;
import com.whu.bookapi.service.BookService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrderScheduler {
    private final OrderService orderService;
    private final BookService bookService;

    public OrderScheduler(OrderService orderService, BookService bookService) {
        this.orderService = orderService;
        this.bookService = bookService;
    }

    @Scheduled(fixedDelay = 60000)
    public void autoCancelPending() {
        List<Order> all = orderService.listAll();
        LocalDateTime now = LocalDateTime.now();
        for (Order o : all) {
            if ("pending".equals(o.getStatus())) {
                if (o.getCreateTime() != null && o.getCreateTime().isBefore(now.minusMinutes(15))) {
                    orderService.setStatus(o.getId(), "expired");
                    bookService.releaseStock(o.getBookId());
                }
            }
        }
    }
}
