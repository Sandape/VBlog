package org.sang.bean.dto;

import java.util.List;

/**
 * 分页结果DTO
 * 用于封装分页查询结果
 */
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

    // Getters and Setters
    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }
}
