package com.example.yoohoo_be.transfer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class TransferPageResponseDto {
    private Summary summary;
    private List<TransferResponseDto> content;
    private PageableInfo pageable;
    private long totalElements;
    private int totalPages;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Summary {
        private int totalPending;
        private int totalSent;
        private int totalReceived;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class PageableInfo {
        private int pageNumber;
        private int pageSize;
    }
}
