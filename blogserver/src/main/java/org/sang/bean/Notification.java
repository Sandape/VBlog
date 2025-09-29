package org.sang.bean;

import lombok.Data;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;

/**
 * 通知表实体类
 */
@Getter
@Setter
public class Notification {
    private Long id;
    private Long userId;
    private String type;
    private String title;
    private String content;
    private Boolean isRead;
    private Long projectId;
    private String tableName;
    private Timestamp createTime;

  public Notification(Long userId, String parseCompleted, String title, String s, Long projectId, String tableName) {
      this.userId = userId;
      this.type = parseCompleted;
      this.title = title;
      this.content = s;
      this.projectId = projectId;
      this.tableName = tableName;
  }
}
