package org.sang.bean.dto;

import lombok.Data;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * 分页结果DTO
 * 用于封装分页查询结果
 */
@Getter
@Setter
public class PageResultDTO<T> {
    private List<T> records;
    private Integer totalCount;
    private Integer currentPage;
    private Integer pageSize;
    private Integer totalPages;

    public PageResultDTO() {
    }

    public PageResultDTO(List<T> records, Integer totalCount, Integer currentPage, Integer pageSize) {
        this.records = records;
        this.totalCount = totalCount;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalPages = (totalCount + pageSize - 1) / pageSize;
    }
}


