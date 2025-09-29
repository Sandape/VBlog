package org.sang.bean;

import lombok.Data;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;

/**
 * 评论表实体类
 */
@Getter
@Setter
public class Comments {
    private Integer id;
    private Integer aid;
    private String content;
    private Timestamp publishDate;
    private Integer parentId;
    private Integer uid;
}
