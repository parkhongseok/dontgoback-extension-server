package com.dontgoback.msa.extension.domain.asset;

import com.dontgoback.msa.extension.domain.asset.dto.UpdateAssetRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.*;

import static org.assertj.core.api.Assertions.*;

class AssetServiceTest {

    AssetProperties props;
    ZoneId KST = ZoneId.of("Asia/Seoul");

    @BeforeEach
    void setUp() {
        props = new AssetProperties();
        props.getVolatility().setSigma(0.02);
        props.getClamp().setMinPercent(-5.0);
        props.getClamp().setMaxPercent(5.0);
        props.getCache().setZoneId("Asia/Seoul");
        props.getCaffeine().setExpireAfterWriteDays(1);
        props.getCaffeine().setMaxSize(1000);
        props.getCaffeine().setRecordStats(false);
    }

    @Test
    void zeroOriginal_isAllowed_andRemainsZero() {
        // given: Z=0 으로 설정 → multiplier ≈ exp(-0.5σ^2) ≈ 0.9998.. 클램프 범위 내
        FakeNormal normal = new FakeNormal(0.0);
        FixedClock clock = new FixedClock(java.time.LocalDate.of(2025, 8, 9), KST);
        AssetService svc = new AssetService(props, normal, clock);
        svc.init();

        var req = new UpdateAssetRequest();
        // DTO는 Long이므로 박싱 값 지정
        java.lang.reflect.Field f;
        try {
            f = req.getClass().getDeclaredField("asset");
            f.setAccessible(true);
            f.set(req, 0L);
        } catch (Exception e) { throw new RuntimeException(e); }

        // when
        var res = svc.updateAsset(1L, req);

        // then
        assertThat(res.getOriginalAsset()).isEqualTo(0L);
        assertThat(res.getUpdatedAsset()).isEqualTo(0L); // 0 * multiplier = 0
        assertThat(res.getMultiplier()).isBetween(0.95, 1.05);
    }

    @Test
    void negativeOriginal_shouldThrow() {
        FakeNormal normal = new FakeNormal(0.0);
        FixedClock clock = new FixedClock(java.time.LocalDate.of(2025, 8, 9), KST);
        AssetService svc = new AssetService(props, normal, clock);
        svc.init();

        var req = new UpdateAssetRequest();
        try {
            var f = req.getClass().getDeclaredField("asset");
            f.setAccessible(true);
            f.set(req, -1L);
        } catch (Exception e) { throw new RuntimeException(e); }

        assertThatThrownBy(() -> svc.updateAsset(1L, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("asset must be >= 0");
    }

    @Test
    void sameUser_sameDay_usesCachedMultiplier_once() {
        // given: 첫 호출 Z=2.0 (꽤 큰 값이지만 클램프로 제한), 두 번째 호출은 캐시 사용
        FakeNormal normal = new FakeNormal(2.0);
        FixedClock clock = new FixedClock(java.time.LocalDate.of(2025, 8, 9), KST);
        AssetService svc = new AssetService(props, normal, clock);
        svc.init();

        var req = new UpdateAssetRequest();
        try {
            var f = req.getClass().getDeclaredField("asset");
            f.setAccessible(true);
            f.set(req, 100_000L);
        } catch (Exception e) { throw new RuntimeException(e); }

        // when
        var r1 = svc.updateAsset(10L, req);
        var r2 = svc.updateAsset(10L, req);

        // then: multiplier가 동일, normal.nextZ()는 한 번만 호출됨
        assertThat(r1.getMultiplier()).isEqualTo(r2.getMultiplier());
        assertThat(normal.calls()).isEqualTo(1);
    }

    @Test
    void sameUser_nextDay_newMultiplier() {
        // given: 첫날 Z=0.0, 다음날 Z=1.0
        FakeNormal normal = new FakeNormal(0.0, 1.0);
        FixedClock clock = new FixedClock(java.time.LocalDate.of(2025, 8, 9), KST);
        AssetService svc = new AssetService(props, normal, clock);
        svc.init();

        var req = new UpdateAssetRequest();
        try {
            var f = req.getClass().getDeclaredField("asset");
            f.setAccessible(true);
            f.set(req, 100_000L);
        } catch (Exception e) { throw new RuntimeException(e); }

        var d1 = svc.updateAsset(7L, req);
        clock.plusDays(1); // 날짜 변경
        var d2 = svc.updateAsset(7L, req);

        assertThat(d1.getDate()).isNotEqualTo(d2.getDate());
        assertThat(d1.getMultiplier()).isNotEqualTo(d2.getMultiplier()); // 높은 확률로 다름
        assertThat(normal.calls()).isEqualTo(2);
    }

    @Test
    void clampIsApplied_onExtremeZ() {
        // given: 매우 큰 Z로 raw multiplier가 상한을 초과하도록 유도
        FakeNormal normal = new FakeNormal(10.0);
        FixedClock clock = new FixedClock(java.time.LocalDate.of(2025, 8, 9), KST);
        AssetService svc = new AssetService(props, normal, clock);
        svc.init();

        var req = new UpdateAssetRequest();
        try {
            var f = req.getClass().getDeclaredField("asset");
            f.setAccessible(true);
            f.set(req, 100_000L);
        } catch (Exception e) { throw new RuntimeException(e); }

        var res = svc.updateAsset(99L, req);

        double maxMul = 1.0 + props.getClamp().getMaxPercent() / 100.0;
        assertThat(res.getMultiplier()).isEqualTo(maxMul);
    }

    class FakeNormal implements NormalGenerator {
        private final double[] values;
        private int idx = 0;
        int calls() { return idx; }
        FakeNormal(double... values) { this.values = values; }
        @Override public double nextZ() { return values[Math.min(idx++, values.length - 1)]; }
    }

    class FixedClock extends Clock {
        private final ZoneId zone;
        private Instant instant;
        FixedClock(LocalDate date, ZoneId zone) {
            this.zone = zone;
            this.instant = date.atStartOfDay(zone).toInstant();
        }

        void plusDays(long days) { instant = instant.plus(days, java.time.temporal.ChronoUnit.DAYS); }
        @Override public ZoneId getZone() { return zone; }
        @Override public Clock withZone(ZoneId zone) { return new FixedClock(LocalDateTime.ofInstant(instant, zone).toLocalDate(), zone); }
        @Override public Instant instant() { return instant; }
    }
}