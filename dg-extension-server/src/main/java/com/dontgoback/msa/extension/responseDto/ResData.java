package com.dontgoback.msa.extension.responseDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ResData<T> {
    private String resultCode;
    private String message;
    private T data;

    //    클래스 메서드로 선언하여, 인스턴스가 아닌 클래스 이름으로 호출 가능
    public static <T> ResData<T> of(String resultCode, String message, T data){
        return new ResData<>(resultCode, message, data);
    }

    // 데이터가 없는 경우
    public static <T> ResData<T> of(String resultCode, String message) {
        return of(resultCode, message, null);
    }
}
