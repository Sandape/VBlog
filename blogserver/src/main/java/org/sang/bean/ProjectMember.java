package org.sang.bean;

import lombok.Data;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;

/**
 * 项目成员表实体类
 */
@Getter
@Setter
public class ProjectMember {
    private Long id;
    private Long projectId;
    private Long userId;
    private Integer role; // 1-拥有者，2-成员
    private Timestamp joinTime;
    private Integer status;
}
