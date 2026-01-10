package com.whu.bookapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: StatisticsService.java
 * Author: WiseBookPal Team Version: 1.0 Date: 2026-01-11
 * Description: Service for handling system statistics (DAU, Transactions).
 */
@Service
public class StatisticsService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Records a user's activity for today.
     * Idempotent: safe to call multiple times.
     */
    public void recordLogin(String username) {
        if (username == null) return;
        String sql = "INSERT IGNORE INTO user_daily_activity (username, activity_date) VALUES (?, ?)";
        jdbcTemplate.update(sql, username, LocalDate.now());
    }

    /**
     * Retrieves statistics for the last N days.
     * Returns a list of maps, each containing date, dau, orderCount, orderAmount.
     */
    public List<Map<String, Object>> getStats(int days) {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        // Prepare the date list
        List<LocalDate> dates = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            dates.add(startDate.plusDays(i));
        }

        // Query DAU
        String dauSql = "SELECT activity_date, COUNT(DISTINCT username) as cnt FROM user_daily_activity WHERE activity_date >= ? AND activity_date <= ? GROUP BY activity_date";
        Map<String, Long> dauMap = new HashMap<>();
        jdbcTemplate.query(dauSql, (rs) -> {
            dauMap.put(rs.getDate("activity_date").toString(), rs.getLong("cnt"));
        }, java.sql.Date.valueOf(startDate), java.sql.Date.valueOf(endDate));

        // Query Orders
        // Assuming orders table has create_time (timestamp/datetime)
        String orderSql = "SELECT DATE(create_time) as date_val, COUNT(id) as cnt, SUM(price) as amount FROM orders WHERE create_time >= ? AND create_time <= ? GROUP BY DATE(create_time)";
        Map<String, Map<String, Object>> orderMap = new HashMap<>();
        // Start/End for orders need to be timestamps
        // But for DATE() comparison in SQL, we can pass dates if the driver handles it, 
        // or better pass strings/timestamps covering the full range.
        // Simple way: cast create_time to DATE in SQL.
        jdbcTemplate.query(orderSql, (rs) -> {
            Map<String, Object> m = new HashMap<>();
            m.put("count", rs.getLong("cnt"));
            m.put("amount", rs.getDouble("amount"));
            orderMap.put(rs.getDate("date_val").toString(), m);
        }, java.sql.Date.valueOf(startDate), java.sql.Date.valueOf(endDate.plusDays(1))); // +1 to cover today fully if using <

        // Merge data
        for (LocalDate date : dates) {
            String dateStr = date.toString();
            Map<String, Object> entry = new HashMap<>();
            entry.put("date", dateStr);
            entry.put("dau", dauMap.getOrDefault(dateStr, 0L));
            
            Map<String, Object> o = orderMap.get(dateStr);
            if (o != null) {
                entry.put("transactionCount", o.get("count"));
                entry.put("transactionAmount", o.get("amount"));
            } else {
                entry.put("transactionCount", 0L);
                entry.put("transactionAmount", 0.0);
            }
            result.add(entry);
        }

        return result;
    }
}
