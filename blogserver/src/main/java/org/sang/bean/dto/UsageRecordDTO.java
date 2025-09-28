package org.sang.bean.dto;

import java.sql.Timestamp;

/**
 * 使用记录DTO
 * 用于封装用户使用记录列表数据
 */
public class UsageRecordDTO {
    private Long id;
    private Long userId;
    private String username;
    private String nickname;
    private Long projectId;
    private String projectName;
    private String apiName;
    private String apiPath;
    private String apiDesc;
    private Timestamp createTime;

    public UsageRecordDTO() {
    }

    public UsageRecordDTO(Long id, Long userId, String username, String nickname, 
                         Long projectId, String projectName, String apiName, 
                         String apiPath, String apiDesc, Timestamp createTime) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.projectId = projectId;
        this.projectName = projectName;
        this.apiName = apiName;
        this.apiPath = apiPath;
        this.apiDesc = apiDesc;
        this.createTime = createTime;
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

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getApiPath() {
        return apiPath;
    }

    public void setApiPath(String apiPath) {
        this.apiPath = apiPath;
    }

    public String getApiDesc() {
        return apiDesc;
    }

    public void setApiDesc(String apiDesc) {
        this.apiDesc = apiDesc;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }
}
