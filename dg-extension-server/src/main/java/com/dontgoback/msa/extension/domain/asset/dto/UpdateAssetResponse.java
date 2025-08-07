package com.dontgoback.msa.extension.domain.asset.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateAssetResponse {
    private long userId;
    private long asset;
}
