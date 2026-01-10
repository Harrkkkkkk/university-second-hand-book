package com.whu.bookapi.controller;

import com.whu.bookapi.model.User;
import com.whu.bookapi.service.StatisticsService;
import com.whu.bookapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/stats")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardStats(@RequestHeader("token") String token,
                                               @RequestParam(value = "days", defaultValue = "7") int days) {
        User admin = userService.getByToken(token);
        if (admin == null || !"admin".equals(admin.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (days > 30) days = 30; // Limit to 30 days
        if (days < 1) days = 7;

        return ResponseEntity.ok(statisticsService.getStats(days));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportStats(@RequestHeader("token") String token,
                                              @RequestParam(value = "days", defaultValue = "7") int days) {
        User admin = userService.getByToken(token);
        if (admin == null || !"admin".equals(admin.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (days > 90) days = 90; // Allow larger range for export

        List<Map<String, Object>> stats = statisticsService.getStats(days);
        
        StringBuilder csv = new StringBuilder();
        csv.append("日期,日活跃用户(DAU),交易笔数,交易总额\n");
        
        for (Map<String, Object> row : stats) {
            csv.append(row.get("date")).append(",");
            csv.append(row.get("dau")).append(",");
            csv.append(row.get("transactionCount")).append(",");
            csv.append(row.get("transactionAmount")).append("\n");
        }

        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);
        // Add BOM for Excel to recognize UTF-8
        byte[] bom = new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF};
        byte[] result = new byte[bom.length + bytes.length];
        System.arraycopy(bom, 0, result, 0, bom.length);
        System.arraycopy(bytes, 0, result, bom.length, bytes.length);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "statistics.csv");

        return new ResponseEntity<>(result, headers, HttpStatus.OK);
    }
}
