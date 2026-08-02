package com.example.yoohoo_be.checklists.service;

import com.example.yoohoo_be.checklists.domain.CheckItem;
import com.example.yoohoo_be.checklists.dto.CheckItemRequestDto;
import com.example.yoohoo_be.checklists.dto.CheckItemResponseDto;
import com.example.yoohoo_be.checklists.repository.CheckItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckItemService {

    private final CheckItemRepository checkItemRepository;

    @Transactional
    public Long createCheckItem(CheckItemRequestDto requestDto) {
        CheckItem checkItem = CheckItem.builder()
                .title(requestDto.getTitle())
                .category(requestDto.getCategory())
                .description(requestDto.getDescription())
                .maxScore(requestDto.getMaxScore())
                .build();
        return checkItemRepository.save(checkItem).getId();
    }

    @Transactional(readOnly = true)
    public List<CheckItemResponseDto> getAllCheckItems() {
        return checkItemRepository.findAll().stream()
                .map(item -> CheckItemResponseDto.builder()
                        .id(item.getId())
                        .title(item.getTitle())
                        .category(item.getCategory())
                        .description(item.getDescription())
                        .maxScore(item.getMaxScore())
                        .build())
                .collect(Collectors.toList());
    }
}