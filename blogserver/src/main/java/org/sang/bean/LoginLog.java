package org.sang.bean;

import lombok.Data;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;

/**
 * 登录日志表实体类
 */
@Getter
@Setter
public class LoginLog {
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

    public LoginLog(Long userId, String username, String nickname, String ipAddress, String loginStatus) {
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.ipAddress = ipAddress;
        this.loginStatus = loginStatus;
    }

    public LoginLog(Long id, Long userId, String username, String nickname, String ipAddress,
        String userAgent, String loginStatus, Timestamp loginTime) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.loginStatus = loginStatus;
        this.loginTime = loginTime;
    }

    public LoginLog(Long id, Long userId, String username, String ipAddress,
        String userAgent, String loginStatus, Timestamp loginTime) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.loginStatus = loginStatus;
        this.loginTime = loginTime;
    }
}
