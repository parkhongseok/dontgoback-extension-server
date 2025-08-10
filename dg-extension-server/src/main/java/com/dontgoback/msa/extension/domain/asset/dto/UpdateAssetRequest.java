package com.dontgoback.msa.extension.domain.asset.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateAssetRequest {
    @NotNull
    @PositiveOrZero
    private long asset;
}
