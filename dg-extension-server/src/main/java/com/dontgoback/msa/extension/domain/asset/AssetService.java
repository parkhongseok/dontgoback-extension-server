package com.dontgoback.msa.extension.domain.asset;

import com.dontgoback.msa.extension.domain.asset.dto.UpdateAssetRequest;
import com.dontgoback.msa.extension.domain.asset.dto.UpdateAssetResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetService {
    private final int MAX_RATIO = 10;
    private final int MIN_RATIO = -10;

    public UpdateAssetResponse updateAsset(long userId, UpdateAssetRequest request) {
        long originalAsset = request.getAsset();

        Random random = new Random();
        int percent = random.nextInt(MAX_RATIO - MIN_RATIO + 1) + MIN_RATIO;

        long updatedAsset = originalAsset*percent;

        log.info("💰 [userId: {}] 자산 갱신 완료 - 기존: {}, 갱신 후: {} (변동률: {}%)",
                userId, originalAsset, updatedAsset, percent);

        return new UpdateAssetResponse(userId, updatedAsset);
    }
}
