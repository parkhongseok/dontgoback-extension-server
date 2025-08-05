# Spring Security 기반 인증 필터에서의 JWT 검증 처리

Date: 2025-08-05  
Status: Accepted

<br/>

## 맥락

앞선 공개키 자동 로딩 전략을 통해 서명 검증을 위한 키를 확보하였으나,  
모든 요청에 대해 **자동으로 토큰을 검사하고 인증 여부에 따라 흐름을 제어할 수 있는 통합 구조**는 별도로 마련되어야 했습니다.

이때, 애플리케이션 내 다양한 API 요청에 대해 **중앙에서 일관된 방식으로 인증 로직을 수행하고**,  
유효한 토큰을 가진 요청만이 실제 비즈니스 로직으로 도달하도록 제한할 필요가 있었습니다.

또한, 향후 추가될 확장 서버에서도 동일한 방식으로 인증 구조를 도입할 수 있도록,  
Spring Security의 필터 체인을 이용한 구조적 분리가 필요했습니다.

<br/>
<br/>

## 결정

#### ① OncePerRequestFilter 기반 필터 구현

- `InterServerAuthenticationFilter`를 `OncePerRequestFilter`로 구현하였습니다.
- 모든 요청에 대해 필터가 한 번만 작동하며, 중복 실행 없이 일관된 인증 흐름을 보장합니다.

#### ② JWT 토큰 추출 및 검증

- 요청 헤더에서 `Authorization: Bearer <JWT>` 값을 추출합니다.
- 유효성 검사는 `InterServerJwtVerifier`를 통해 수행되며, 서명 확인 및 claims 파싱을 포함합니다.

#### ③ 인증 성공 시 SecurityContext에 인증 정보 저장

- 인증된 요청은 `UsernamePasswordAuthenticationToken`을 생성하여  
  `SecurityContext`에 저장됩니다.
- 이후 컨트롤러 및 서비스에서는 인증 정보에 접근할 수 있습니다.

#### ④ 실패 시 401 Unauthorized 반환

- 토큰이 없거나, 만료되었거나, 서명 검증에 실패한 경우 일괄적으로 `401 Unauthorized` 응답을 반환합니다.

<br/>

## 결과

| 항목      | 내용                                                                        |
| --------- | --------------------------------------------------------------------------- |
| 적용 범위 | 모든 확장 서버 요청 인증 (`dontgoback-extension-server` 외 포함)            |
| 구조화    | 인증 로직이 필터에 집중되어 전역적인 인증 처리 구조를 확립                  |
| 일관성    | 모든 요청에서 동일한 방식으로 JWT 인증 처리 가능                            |
| 재사용성  | 확장 서버에서도 `InterServerAuthenticationFilter`만 등록하면 인증 구성 완료 |
| 보안성    | 비인가 요청에 대해 401 반환, 인증된 요청만 비즈니스 로직 도달 허용          |

> 참고 클래스: `InterServerAuthenticationFilter`, `InterServerJwtVerifier`, `InterServerSecurityConfig`
