package org.sang.bean;

import java.sql.Timestamp;

/**
 * 项目实体类
 */
public class Project {
    private Long id;
    private String projectName;
    private String projectCode;
    private String developmentSpec;
    private String apiKey;
    private String apiUrl;
    private String modelName;
    private String exampleMapperPath;
    private String exampleEntityPath;
    private String exampleInterfacePath;
    private Long ownerId;
    private Timestamp createTime;
    private Timestamp updateTime;
    private Integer status;
    
    // 关联字段
    private String ownerNickname; // 拥有者昵称
    private Integer memberCount; // 成员数量
    private Integer userRole; // 当前用户在项目中的角色：1-拥有者，2-成员，0-非成员

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public String getDevelopmentSpec() {
        return developmentSpec;
    }

    public void setDevelopmentSpec(String developmentSpec) {
        this.developmentSpec = developmentSpec;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getExampleMapperPath() {
        return exampleMapperPath;
    }

    public void setExampleMapperPath(String exampleMapperPath) {
        this.exampleMapperPath = exampleMapperPath;
    }

    public String getExampleEntityPath() {
        return exampleEntityPath;
    }

    public void setExampleEntityPath(String exampleEntityPath) {
        this.exampleEntityPath = exampleEntityPath;
    }

    public String getExampleInterfacePath() {
        return exampleInterfacePath;
    }

    public void setExampleInterfacePath(String exampleInterfacePath) {
        this.exampleInterfacePath = exampleInterfacePath;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getOwnerNickname() {
        return ownerNickname;
    }

    public void setOwnerNickname(String ownerNickname) {
        this.ownerNickname = ownerNickname;
    }

    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }

    public Integer getUserRole() {
        return userRole;
    }

    public void setUserRole(Integer userRole) {
        this.userRole = userRole;
    }
}
