/*
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: OrderScheduler.java
 * Author: WiseBookPal Team Version: 1.0 Date: 2026-01-02
 * Description: Scheduled tasks for order management.
 *              Handles automatic cancellation of expired orders.
 * History:
 * 1. Date: 2026-01-02
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
 */
package com.whu.bookapi.schedule;

import com.whu.bookapi.model.Order;
import com.whu.bookapi.service.OrderService;
import com.whu.bookapi.service.BookService;
import com.whu.bookapi.service.NotificationService;
import com.whu.bookapi.service.ComplaintService;
import com.whu.bookapi.service.UserService;
import com.whu.bookapi.service.ReviewService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.LocalDateTime;

/**
 * Scheduled task for order maintenance.
 */
@Component
public class OrderScheduler {
    private final OrderService orderService;
    private final BookService bookService;
    private final NotificationService notificationService;
    private final ComplaintService complaintService;
    private final UserService userService;
    private final ReviewService reviewService;

    public OrderScheduler(OrderService orderService, BookService bookService, NotificationService notificationService, ComplaintService complaintService, UserService userService, ReviewService reviewService) {
        this.orderService = orderService;
        this.bookService = bookService;
        this.notificationService = notificationService;
        this.complaintService = complaintService;
        this.userService = userService;
        this.reviewService = reviewService;
    }

    /**
     * Function: autoCancelPending
     * Description: Automatically cancels pending orders that have expired.
     *              Runs every 60 seconds.
     * Logic:
     * 1. List all pending orders.
     * 2. Check if current time > expiration time.
     * 3. If expired, set status to "expired" and release book stock.
     */
    @Scheduled(fixedDelay = 60000)
    public void autoCancelPending() {
        List<Order> all = orderService.listAll();
        long now = System.currentTimeMillis();
        for (Order o : all) {
            if ("pending".equals(o.getStatus())) {
                Long expireAt = o.getExpireAt();
                if (expireAt != null && expireAt > 0 && now > expireAt) {
                    orderService.setStatus(o.getId(), "expired");
                    bookService.releaseStock(o.getBookId());
                }
            }
        }
    }

    @Scheduled(cron = "0 15 2 * * *")
    public void autoConfirmReceive() {
        List<Order> all = orderService.listAll();
        long now = System.currentTimeMillis();
        for (Order o : all) {
            if (!"paid".equals(o.getStatus())) continue;
            LocalDateTime ct = o.getCreateTime();
            if (ct == null) continue;
            long createMs = ct.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            if (now - createMs >= 7L * 24 * 60 * 60 * 1000) {
                Order updated = orderService.setStatus(o.getId(), "received");
                if (updated != null) {
                    notificationService.addToUser(
                            updated.getSellerName(),
                            "order",
                            "系统已自动确认收货",
                            "订单#" + updated.getId() + "（" + updated.getBookName() + "）交易完成超过7天，系统已自动确认收货，后续将参与每日凌晨批量结算"
                    );
                }
            }
        }
    }
    @Scheduled(cron = "0 30 2 * * *")
    public void dailySettlement() {
        List<Order> all = orderService.listAll();
        LocalDate yesterday = LocalDate.now().minusDays(1);
        long nowMs = System.currentTimeMillis();
        for (Order o : all) {
            if (!"received".equals(o.getStatus())) continue;
            LocalDateTime ct = o.getCreateTime();
            if (ct == null) continue;
            LocalDate orderDate = ct.toLocalDate();
            if (!orderDate.equals(yesterday)) continue;
            if (notificationService.hasSettlementRecord(o.getSellerName(), o.getId())) continue;
            if (!userService.isSellerAccountHealthy(o.getSellerName())) {
                notificationService.addToUser(
                        o.getSellerName(),
                        "settlement",
                        "账户异常，结算暂停",
                        "订单#" + o.getId() + "（" + o.getBookName() + "）因账户状态异常已暂停结算，请尽快更新账户信息，修复后24小时内自动补结算"
                );
                continue;
            }
            if (complaintService.hasPendingComplaint(o.getId())) {
                notificationService.addToUser(
                        o.getSellerName(),
                        "settlement",
                        "结算中止（存在未解决投诉）",
                        "订单#" + o.getId() + "存在未解决投诉，结算已中止，待纠纷处理完成后将重新核算结算金额"
                );
                continue;
            }
            double amount = o.getPrice() == null ? 0.0 : o.getPrice();
            double receivedAmount = amount; // 可在纠纷后调整
            notificationService.addSettlementVoucher(
                    o.getSellerName(),
                    o.getId(),
                    o.getBookName(),
                    amount,
                    receivedAmount,
                    nowMs
            );
        }
    }

    @Scheduled(cron = "0 5 * * * *")
    public void auditPendingWarning() {
        long now = System.currentTimeMillis();
        long limit = now - 24L * 60 * 60 * 1000;
        int booksOverdue = 0;
        int reviewsOverdue = 0;
        int complaintsOverdue = 0;

        for (com.whu.bookapi.model.Book b : bookService.listUnderReview()) {
            Long created = b.getCreatedAt();
            if (created != null && created < limit) booksOverdue++;
        }
        for (com.whu.bookapi.model.Review r : reviewService.listPending()) {
            Long ct = r.getCreateTime();
            if (ct != null && ct < limit) reviewsOverdue++;
        }
        for (com.whu.bookapi.model.Complaint c : complaintService.listAll()) {
            if (!"pending".equals(c.getStatus())) continue;
            Long ct = c.getCreateTime();
            if (ct != null && ct < limit) complaintsOverdue++;
        }

        if (booksOverdue == 0 && reviewsOverdue == 0 && complaintsOverdue == 0) return;
        String title = "待审核内容预警";
        String content = "超过24小时未处理：教材" + booksOverdue + "条、评价" + reviewsOverdue + "条、投诉" + complaintsOverdue + "条。请尽快处理。";

        for (com.whu.bookapi.model.User u : userService.listAllUsers()) {
            if (u.getRole() != null && "admin".equals(u.getRole())) {
                notificationService.addToUser(u.getUsername(), "audit_warning", title, content);
            }
        }
    }
}
