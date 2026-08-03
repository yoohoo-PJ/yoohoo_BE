package com.example.yoohoo_be.checklists.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DecisionConfirmRequestDto {

    // 프론트 명세 값: DISPOSAL(폐기) / RELOCATION(이관) / KEEP(보존)
    // 내부 BookStatus(DISCARDED/TRANSFERRED/PRESERVED)와는 별도 값이라 서비스에서 매핑한다.
    @NotBlank(message = "decision 값은 필수입니다.")
    private String decision;

    @NotBlank(message = "librarianCode는 필수입니다.")
    private String librarianCode;

    @NotBlank(message = "decidedDate는 필수입니다.")
    private String decidedDate; // yyyy-MM-dd
}