package com.dontgoback.msa.extension.domain.asset.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class UpdateAssetRequest {
    @NotNull
    @PositiveOrZero
    private long asset;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate snapshotDay; // 선택적 필드
}
