package org.sang.bean;

/**
 * 表元数据解析任务DTO
 * 用于传递异步解析任务所需的所有信息
 *
 * @author sang
 * @date 2024
 */
public class TableMetaParseTask {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 表名
     */
    private String tableName;

    /**
     * AI API密钥
     */
    private String apiKey;

    /**
     * AI API地址
     */
    private String apiUrl;

    /**
     * AI模型名称
     */
    private String modelName;

    /**
     * SQL语句
     */
    private String sql;

    public TableMetaParseTask() {
    }

    public TableMetaParseTask(Long userId, Long projectId, String tableName, String apiKey, String apiUrl, String modelName, String sql) {
        this.userId = userId;
        this.projectId = projectId;
        this.tableName = tableName;
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.modelName = modelName;
        this.sql = sql;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
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

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }
}
