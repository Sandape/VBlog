package org.sang.bean.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 文章查询参数DTO
 * 用于文章列表查询
 */
@Getter
@Setter
public class ArticleQueryDTO {
    private Integer state = -1;
    private Integer page = 1;
    private Integer count = 6;
    private String keywords;
    private Integer uid;
}
