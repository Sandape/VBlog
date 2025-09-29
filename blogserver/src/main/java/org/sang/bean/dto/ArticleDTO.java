package org.sang.bean.dto;

import lombok.Data;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;

/**
 * 文章DTO
 * 用于文章的增删改查操作
 */
@Getter
@Setter
public class ArticleDTO {
    private Integer id;
    private String title;
    private String mdContent;
    private String htmlContent;
    private String summary;
    private Integer cid;
    private Integer uid;
    private Timestamp publishDate;
    private Timestamp editTime;
    private Integer state;
    private Integer pageView;
    private String[] dynamicTags; // 动态标签
}
