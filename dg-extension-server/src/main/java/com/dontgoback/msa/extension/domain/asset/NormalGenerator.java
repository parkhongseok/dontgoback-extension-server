package com.dontgoback.msa.extension.domain.asset;

// 1) 난수 제어를 위한 인터페이스
public interface NormalGenerator {
    /** 표준정규 Z ~ N(0,1) */
    double nextZ();
}