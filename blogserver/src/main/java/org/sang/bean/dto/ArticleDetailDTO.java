package org.sang.bean.dto;

import java.sql.Timestamp;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
/**
 * 文章详情DTO
 * 包含文章基本信息以及连表查询的关联数据
 */
@Getter
@Setter
public class ArticleDetailDTO {
    private Long id;
    private String title;
    private String mdContent;
    private String htmlContent;
    private String summary;
    private Long cid;
    private Long uid;
    private Timestamp publishDate;
    private Timestamp editTime;
    private Integer state;
    private Integer pageView;
    private String nickname; // 用户昵称
    private String cateName; // 分类名称
    private List<TagsDTO> tags; // 标签列表
}
