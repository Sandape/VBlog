package org.sang.bean;

import java.util.Date;

/**
 * 通知消息类
 * 用于存储用户通知信息
 *
 * @author sang
 * @date 2024
 */
public class Notification {

    /**
     * 通知ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 通知类型：PARSE_COMPLETED-解析完成，PARSE_FAILED-解析失败
     */
    private String type;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 是否已读：0-未读，1-已读
     */
    private Integer isRead;

    /**
     * 关联项目ID
     */
    private Long projectId;

    /**
     * 关联表名
     */
    private String tableName;

    /**
     * 创建时间
     */
    private Date createTime;

    public Notification() {
    }

    public Notification(Long userId, String type, String title, String content, Long projectId, String tableName) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.content = content;
        this.isRead = 0;
        this.projectId = projectId;
        this.tableName = tableName;
        this.createTime = new Date();
    }

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getIsRead() {
        return isRead;
    }

    public void setIsRead(Integer isRead) {
        this.isRead = isRead;
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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
