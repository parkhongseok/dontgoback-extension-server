package com.dontgoback.msa.extension.domain.asset;

import com.dontgoback.msa.extension.domain.asset.dto.UpdateAssetRequest;
import com.dontgoback.msa.extension.domain.asset.dto.UpdateAssetResponse;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 유저별·일별 로그정규 배수(multiplier)를 Caffeine 캐시에 저장하여
 * 같은 날에는 같은 multiplier를 재사용하도록 한다.
 *
 * multiplier는 로그정규 분포 exp(μ + σZ)를 사용하며,
 * 평균 1을 맞추기 위해 μ = -0.5 * σ^2 를 사용한다.
 * 극단값 방지를 위해 최종 multiplier는 [1+min%, 1+max%]로 클램프한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetProperties props;
    private final NormalGenerator normal; // 주입
    private final Clock clock; // 이제 이 Clock은 ClockConfiguration에 의해 타임존이 설정된 상태로 주입됩니다.

    /**
     * userId|YYYY-MM-DD -> multiplier(double)
     * Caffeine 캐시는 expireAfterWrite + maximumSize 정책으로 메모리 위생을 관리한다.
     */
    private Cache<String, Double> dailyMultiplier;

    @PostConstruct
    void init() {
        double sigma = props.getVolatility().getSigma();
        if (sigma < 0) throw new IllegalArgumentException("asset.volatility.sigma must be >= 0");
        if (props.getClamp().getMinPercent() > props.getClamp().getMaxPercent())
            throw new IllegalArgumentException("asset.clamp.minPercent must be <= maxPercent");

        var caf = props.getCaffeine();
        Caffeine<Object, Object> builder = Caffeine.newBuilder()
                .expireAfterWrite(caf.getExpireAfterWriteDays(), TimeUnit.DAYS)
                .maximumSize(caf.getMaxSize());
        if (caf.isRecordStats()) builder = builder.recordStats();
        this.dailyMultiplier = builder.build();
    }


    /**
     * 자산 갱신.
     * - 원금이 0일 수는 있지만, 음수는 허용하지 않는다.
     * - 같은 유저, 같은 날짜에서는 동일 배수(multiplier)를 적용한다.
     */
    public UpdateAssetResponse updateAsset(long userId, UpdateAssetRequest req) {
        Objects.requireNonNull(req, "request must not be null");
        Long originalBoxed = req.getAsset();
        if (originalBoxed == null) throw new IllegalArgumentException("asset must not be null");
        long original = originalBoxed;
        if (original < 0) throw new IllegalArgumentException("asset must be >= 0");

        // clock.withZone(zoneId)가 더 이상 필요 없습니다. clock 자체가 이미 올바른 타임존을 가집니다.
        LocalDate today = LocalDate.now(clock);
        String key = userId + "|" + today;

        double multiplier = dailyMultiplier.get(key, k -> generateMultiplier());
        long updated = Math.max(0L, Math.round(original * multiplier));

        double pct = (multiplier - 1.0) * 100.0;
        log.info("updateAsset userId={} date={} original={} multiplier={} updated={} change={}%",
                userId, today, original, String.format("%.6f", multiplier), updated, String.format("%.2f", pct));

        return new UpdateAssetResponse(userId, original, multiplier, updated, today.toString());
    }

    /**
     * 로그정규 배수 생성:
     *  - Z ~ N(0,1)
     *  - μ = -0.5 * σ^2
     *  - multiplier = exp(μ + σZ)
     *  - 클램프: [1+min%, 1+max%]
     */
    private double generateMultiplier() {
        double sigma = props.getVolatility().getSigma();
        double mu = -0.5 * sigma * sigma;

        double z = normal.nextZ(); // 테스트에서 고정값 주입 가능
        double raw = Math.exp(mu + sigma * z);

        double minMul = 1.0 + props.getClamp().getMinPercent() / 100.0;
        double maxMul = 1.0 + props.getClamp().getMaxPercent() / 100.0;
        return Math.min(maxMul, Math.max(minMul, raw));
    }
}
