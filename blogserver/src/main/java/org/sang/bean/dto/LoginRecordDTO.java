package org.sang.bean.dto;

import java.util.Date;

/**
 * 登录记录DTO
 * 用于封装登录记录列表数据
 */
public class LoginRecordDTO {
    private Long id;
    private Long userId;
    private String username;
    private String nickname;
    private String ipAddress;
    private String loginStatus; // SUCCESS-成功，FAILED-失败
    private Date loginTime;

    public LoginRecordDTO() {
    }

    public LoginRecordDTO(Long id, Long userId, String username, String nickname, 
                         String ipAddress, String loginStatus, Date loginTime) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.ipAddress = ipAddress;
        this.loginStatus = loginStatus;
        this.loginTime = loginTime;
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
