package com.example.yoohoo_be.checklists.service;

import com.example.yoohoo_be.checklists.domain.BookCheckBatch;
import com.example.yoohoo_be.checklists.dto.BookCheckCompletedListResponseDto;
import com.example.yoohoo_be.checklists.dto.BookWearStatusDetailResponseDto;
import com.example.yoohoo_be.checklists.repository.BookCheckBatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookWearStatusService {

    private final BookCheckBatchRepository bookCheckBatchRepository;

    /**
     * 1. [마모 처리 현황 - 첫 번째 화면] 점검 완료 도서 전체 목록 조회
     */
    public List<BookCheckCompletedListResponseDto> getCompletedBookChecks() {
        return bookCheckBatchRepository.findAllCompletedOrderByCheckedDateDesc()
                .stream()
                .map(this::toCompletedListDto)
                .collect(Collectors.toList());
    }

    /**
     * 2. [마모 처리 현황 - 두 번째 상세 모달 화면] 특정 도서의 마모 상태 점검 상세 결과 조회 (bookId 기준)
     */
    public BookWearStatusDetailResponseDto getWearStatusDetailByBookId(Long bookId) {
        BookCheckBatch batch = bookCheckBatchRepository.findLatestByBookId(bookId)
                .orElseThrow(() -> new IllegalArgumentException("해당 도서의 점검 내역이 존재하지 않습니다. Book ID: " + bookId));

        var bookInfo = BookWearStatusDetailResponseDto.BookInfoDto.builder()
                .bookId(batch.getBook().getId())
                .title(batch.getBook().getTitle())
                .author(batch.getBook().getAuthor())
                .publisher(batch.getBook().getPublisher())
                .callNumber(batch.getBook().getCallNumber())
                .coverUrl(batch.getBook().getCoverUrl())
                .status(batch.getBook().getStatus().name())
                .build();

        List<BookWearStatusDetailResponseDto.CheckResultItemDto> checkResults =
                batch.getItems().stream()
                        .map(item -> BookWearStatusDetailResponseDto.CheckResultItemDto.builder()
                                .checkItemId(item.getCheckItem().getId())
                                .title(item.getCheckItem().getTitle())
                                .isPassed(item.getIsPassed())
                                .itemScore(item.getItemScore())
                                .note(item.getNote())
                                .build())
                        .collect(Collectors.toList());

        return BookWearStatusDetailResponseDto.builder()
                .resultBatchId(batch.getId())
                .checkedDate(batch.getCheckedDate())
                .librarianCode(batch.getLibrarianCode())
                .totalScore(batch.getTotalScore())
                .bookInfo(bookInfo)
                .checkResults(checkResults)
                .build();
    }

    private BookCheckCompletedListResponseDto toCompletedListDto(BookCheckBatch batch) {
        return BookCheckCompletedListResponseDto.builder()
                .resultBatchId(batch.getId())
                .bookId(batch.getBook().getId())
                .title(batch.getBook().getTitle())
                .author(batch.getBook().getAuthor())
                .publisher(batch.getBook().getPublisher())
                .callNumber(batch.getBook().getCallNumber())
                .coverUrl(batch.getBook().getCoverUrl())
                .checkedDate(batch.getCheckedDate())
                .librarianName(batch.getLibrarianCode())
                .totalScore(batch.getTotalScore())
                .status(batch.getBook().getStatus().name())
                .build();
    }
}