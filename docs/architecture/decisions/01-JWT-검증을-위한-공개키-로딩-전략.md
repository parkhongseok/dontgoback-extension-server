# JWT 검증을 위한 공개키 로딩 전략

Date: 2025-08-05  
Status: Accepted

<br/>

## 맥락

`DontGoBack` 프로젝트는 인증 서버를 중심으로 여러 확장 서버 간의 신뢰 기반 통신을 위해  
비대칭키 기반의 JWT 인증 구조를 도입하였습니다.

- 확장 서버는 토큰 **발급이 아닌 검증**에 집중하며,
- 인증 서버로부터 제공받은 **공개키로 JWT 서명**을 검증합니다.

이때 매 요청마다 공개키를 재요청하거나, 복잡한 키 관리 로직을 도입하는 것은  
성능 및 유지보수 측면에서 비효율적이므로,  
**서버 시작 시 공개키를 자동으로 로딩하고, 이를 재사용하는 구조**가 필요했습니다.

또한, 이 구조는 추후 새로운 확장 서버가 추가되더라도,  
별다른 설정 없이 쉽게 인증 검증 기능을 도입할 수 있어야 했습니다.

<br/>
<br/>

## 결정

#### ① 서버 시작 시 공개키 자동 로딩

- Spring 애플리케이션 초기 구동 시점에 `InterServerPublicKeyManager`가  
  인증 서버(`/msa/auth/api/public-key`)로부터 **공개키를 1회 요청**하여 로딩합니다.
- 응답은 BASE64 인코딩된 문자열이며, `X509EncodedKeySpec` 방식으로 파싱합니다.

<br/>

#### ② 메모리 기반 캐싱 구조

- 로딩된 공개키는 **서버 메모리 내에 저장**되며,  
  이후 모든 토큰 검증은 해당 키를 재사용하여 수행됩니다.
- 이를 통해 매 요청마다 네트워크 호출 없이 검증이 가능하며,  
  인증 서버가 일시적으로 중단되더라도 기존 키로 검증을 지속할 수 있습니다.

<br/>

#### ③ 오류 시 graceful degradation 전략

- 공개키 로딩 실패 시 예외를 전파하지 않고, 로깅만 수행한 뒤 키가 없는 상태로 서버는 동작을 지속합니다.
- 단, 이후 들어오는 토큰 검증 요청은 키가 없으므로 실패하게 됩니다.
- 예시:
  ```java
  // InterServerPublicKeyManager 내부
  @PostConstruct
  public void init() {
      try {
          // 인증 서버로부터 공개키 요청
          String encodedKey = httpClient.requestPublicKey();
          this.publicKey = parseKey(encodedKey);
          log.info("인증서버로부터 공개키 로딩 완료");
      } catch (Exception e) {
          log.error("공개키 로딩 실패 - 서버는 계속 동작하지만, 토큰 검증은 불가합니다.", e);
          // 예외를 throw 하지 않음
      }
  }
  ```

<br/>

#### ④ 구조적 재사용성을 위한 모듈화

- 공개키 기반의 인증 검증 로직은 `interserverauth` 패키지에 모듈화되어 있습니다.

  ```java
  interserverauth/
    │
    ├── client/
    │   ├── HttpClientConfig.java
    │   └── InterServerClientProperties.java
    │
    ├── jwt/
    │   ├── InterServerJwtProperties.java
    │   └── InterServerJwtVerifier.java
    │
    ├── key/
    │   ├── InterServerKeyProperties.java
    │   └── InterServerPublicKeyManager.java
    │
    ├── InterServerAuthenticationFilter.java
    └── InterServerSecurityConfig.java
  ```

- 다른 확장 서버에서도 이 패키지를 주입만 하면,  
  공개키 자동 로딩과 JWT 검증을 그대로 사용할 수 있습니다.
- 이로써 **확장 가능한 인증 구조**를 확보하였습니다.

<br/>
<br/>

## 결과

<br/>

| 항목      | 내용                                                                      |
| --------- | ------------------------------------------------------------------------- |
| 적용 범위 | 확장 서버 전반 (`dontgoback-extension-server`, 이후 확장 서버 포함)       |
| 재사용성  | `InterServerPublicKeyManager` 및 `JwtVerifier` 재사용 가능 구조 확립      |
| 성능      | 매 요청마다 네트워크 호출 없이 공개키 캐싱을 통한 검증 성능 확보          |
| 확장성    | 새로운 서버에서도 해당 모듈만 주입하면 인증 검증 구조 바로 사용 가능      |
| 보안성    | 인증 서버의 키 변경 시 대응 로직은 추후 고도화 예정 (재로딩 전략 등 고려) |

> 참고: `InterServerPublicKeyManager`, `InterServerJwtVerifier`, `PemKeyLoader`
