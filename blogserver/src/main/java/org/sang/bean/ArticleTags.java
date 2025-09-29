package org.sang.bean;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 文章标签关联表实体类
 */
@Getter
@Setter
public class ArticleTags {
    private Integer id;
    private Integer aid;
    private Integer tid;
}
