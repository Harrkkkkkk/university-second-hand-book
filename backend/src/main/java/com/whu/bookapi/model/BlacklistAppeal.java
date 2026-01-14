package com.whu.bookapi.model;

import lombok.Data;

@Data
public class BlacklistAppeal {
    private Long id;
    private String username;
    private String reason;
    private String evidence;
    private String proofImage; // URL of the proof image
    private String status; // pending, approved, rejected
    private Long createTime;
    private String auditReason;
    private Long auditTime;
    private String auditor;
}
