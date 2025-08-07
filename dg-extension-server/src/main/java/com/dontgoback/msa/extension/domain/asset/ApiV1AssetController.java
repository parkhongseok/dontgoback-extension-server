package com.dontgoback.msa.extension.domain.asset;

import com.dontgoback.msa.extension.domain.asset.dto.UpdateAssetResponse;
import com.dontgoback.msa.extension.domain.asset.dto.UpdateAssetRequest;
import com.dontgoback.msa.extension.responseDto.ResData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/msa/ext/api")
@RestController
public class ApiV1AssetController {
    private final AssetService assetService;

    @PostMapping("/update-asset/{user-id}")
    public ResponseEntity<ResData<UpdateAssetResponse>> updateAsset(
            @PathVariable("user-id") long userId,
            @Valid  @RequestBody UpdateAssetRequest request
    ) {
        try {
            UpdateAssetResponse response = assetService.updateAsset(userId, request);
            return ResponseEntity.ok(ResData.of("S", "Update asset success", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(ResData.of("F", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResData.of("F", "Unexpected error occurred."));
        }
    }
}
