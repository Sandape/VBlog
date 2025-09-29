package org.sang.bean.dto;

import lombok.Data;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;

/**
 * 登录日志DTO
 * 用于登录日志的查询和展示
 */
@Getter
@Setter
public class LoginLogDTO {
    private Long id;
    private Long userId;
    private String username;
    private String nickname;
    private String ipAddress;
    private String userAgent;
    private String loginStatus;
    private String failureReason;
    private Timestamp loginTime;
    private Timestamp logoutTime;
    private Long sessionDuration;
}
