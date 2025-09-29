package org.sang.bean.dto;

/**
 * 登录统计DTO
 * 用于封装用户登录统计数据
 */
public class LoginStatsDTO {
    private String username;
    private String nickname;
    private Integer loginCount;
    private String lastLoginTime;
    private String lastLoginIp;

    public LoginStatsDTO() {
    }

    public LoginStatsDTO(String username, String nickname, Integer loginCount, 
                        String lastLoginTime, String lastLoginIp) {
        this.username = username;
        this.nickname = nickname;
        this.loginCount = loginCount;
        this.lastLoginTime = lastLoginTime;
        this.lastLoginIp = lastLoginIp;
    }

    // Getters and Setters
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

    public Integer getLoginCount() {
        return loginCount;
    }

    public void setLoginCount(Integer loginCount) {
        this.loginCount = loginCount;
    }

    public String getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(String lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }
}


