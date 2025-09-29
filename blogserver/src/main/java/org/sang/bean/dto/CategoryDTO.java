package org.sang.bean.dto;

import lombok.Data;
import java.sql.Date;

import lombok.Getter;
import lombok.Setter;
/**
 * 分类DTO
 * 用于分类的增删改查操作
 */
@Getter
@Setter
public class CategoryDTO {
    private Integer id;
    private String cateName;
    private Date date;
}
