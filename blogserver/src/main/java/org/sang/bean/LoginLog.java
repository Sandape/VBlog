package org.sang.bean;

import java.util.Date;

/**
 * 登录日志实体类
 * Created by sang on 2017/12/25.
 */
public class LoginLog {
    private Long id;
    private Long userId;
    private String username;
    private String nickname;
    private String ipAddress;
    private String loginStatus; // 登录状态：SUCCESS-成功，FAILED-失败
    private Date loginTime;

    public LoginLog() {
    }

    public LoginLog(Long userId, String username, String nickname, String ipAddress, String loginStatus) {
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.ipAddress = ipAddress;
        this.loginStatus = loginStatus;
        this.loginTime = new Date();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getLoginStatus() {
        return loginStatus;
    }

    public void setLoginStatus(String loginStatus) {
        this.loginStatus = loginStatus;
    }

    public Date getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Date loginTime) {
        this.loginTime = loginTime;
    }
}
