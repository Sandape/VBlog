package org.sang.bean.dto;

import lombok.Data;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;

/**
 * 通知DTO
 * 用于通知的查询和展示
 */
@Getter
@Setter
public class NotificationDTO {
    private Long id;
    private Long userId;
    private String type;
    private String title;
    private String content;
    private Boolean isRead;
    private Long projectId;
    private String tableName;
    private Timestamp createTime;
}
