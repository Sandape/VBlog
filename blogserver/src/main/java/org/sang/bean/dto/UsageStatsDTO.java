package org.sang.bean.dto;

/**
 * 使用统计DTO
 * 用于封装用户使用统计数据
 */
public class UsageStatsDTO {
    private Long userId;
    private String username;
    private String nickname;
    private Integer usageCount;
    private String lastUsageTime;
    private String mostUsedProject;

    public UsageStatsDTO() {
    }

    public UsageStatsDTO(Long userId, String username, String nickname, Integer usageCount, 
                        String lastUsageTime, String mostUsedProject) {
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.usageCount = usageCount;
        this.lastUsageTime = lastUsageTime;
        this.mostUsedProject = mostUsedProject;
    }

    // Getters and Setters
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

    public Integer getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(Integer usageCount) {
        this.usageCount = usageCount;
    }

    public String getLastUsageTime() {
        return lastUsageTime;
    }

    public void setLastUsageTime(String lastUsageTime) {
        this.lastUsageTime = lastUsageTime;
    }

    public String getMostUsedProject() {
        return mostUsedProject;
    }

    public void setMostUsedProject(String mostUsedProject) {
        this.mostUsedProject = mostUsedProject;
    }
}
