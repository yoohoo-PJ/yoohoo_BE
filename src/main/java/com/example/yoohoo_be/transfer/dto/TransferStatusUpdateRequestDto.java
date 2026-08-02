package com.example.yoohoo_be.transfer.dto;

import com.example.yoohoo_be.dashboard.domain.TransferStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TransferStatusUpdateRequestDto {
    private TransferStatus status;
}
