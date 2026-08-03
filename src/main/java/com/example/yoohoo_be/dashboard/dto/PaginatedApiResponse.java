package com.example.yoohoo_be.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;
import java.util.List;

@Getter
public class PaginatedApiResponse<T> {
    private boolean success;
    private PageInfo pageInfo;
    private List<T> data;

    public PaginatedApiResponse(boolean success, Page<T> pageData) {
        this.success = success;
        this.pageInfo = new PageInfo(pageData.getNumber(), pageData.getTotalPages(), pageData.getTotalElements());
        this.data = pageData.getContent();
    }

    @Getter
    @AllArgsConstructor
    public static class PageInfo {
        private int currentPage;
        private int totalPages;
        private long totalElements;
    }
}
