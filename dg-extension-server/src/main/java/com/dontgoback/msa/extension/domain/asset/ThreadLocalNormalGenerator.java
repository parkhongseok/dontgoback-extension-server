package com.dontgoback.msa.extension.domain.asset;

import org.springframework.stereotype.Component;



/**
 * 표준정규 난수 Z ~ N(0,1) 생성
 * Box–Muller 변환: Z = sqrt(-2 ln U1) * cos(2πU2)
 * U1, U2 ~ Uniform(0,1), 단 U1=0 회피를 위해 작은 하한을 둔다.
 */
// 2) 운영용 기본 구현 (기존 Box–Muller)
@Component
public class ThreadLocalNormalGenerator implements NormalGenerator {
    @Override
    public double nextZ() {
        var r = java.util.concurrent.ThreadLocalRandom.current();
        double u1;
        do { u1 = r.nextDouble(); } while (u1 <= 1e-12);
        double u2 = r.nextDouble();
        return Math.sqrt(-2.0 * Math.log(u1)) * Math.cos(2 * Math.PI * u2);
    }
}