# dontgoback-extension-server

![msa-system-architecture-overview](/docs/architecture/src/msa-system-architecture-overview.png)

# 1. 프로젝트 개요

### 소개

`dontgoback-extension-server`는 `DontGoBack` 프로젝트의 확장 마이크로서비스로,  
코어 서버의 부가 기능을 담당하는 백엔드 서비스입니다.

인증 서버(`dontgoback-auth-server`)로부터 JWT를 발급받은 코어 서버는,  
이 토큰을 포함하여 확장 서버에 요청을 보냅니다.

확장 서버는 모든 요청에 대해 해당 JWT의 유효성을 검사 후 비즈니스 로직을 수행합니다.

개발 및 배포는 **경량화된 인프라(Raspberry Pi)** 를 기반으로 하며, 단위, 통합 테스트를 실행하고,  
Docker + GitHub Actions 기반 CI/CD 자동화도 함께 구축하고 있습니다.

### 기간

- 2025.08.02 \~ (진행 중)

### 인원

- 개인 프로젝트

### 기술 스탭

|      번류      |            도구            |  버전  |
| :------------: | :------------------------: | :----: |
|      언어      |            Java            |   21   |
|    Backend     |        Spring Boot         | 3.4.0  |
|  인증/암호화   |        jjwt (RS256)        | 0.11.5 |
|     테스트     | JUnit5 / Mockito / MockMvc |  최신  |
| Infrastructure |        Raspberry Pi        |   -    |
|     DevOps     |  GitHub Actions / Docker   |   -    |

### 연관 프로젝트

- 중심 서비스 서버 GitHub 주소:
  [https://github.com/parkhongseok/projectDontGoBack](https://github.com/parkhongseok/projectDontGoBack)

- 인증 서비스 서버 GitHub 주소:
  [https://github.com/parkhongseok/dontgoback-auth-server](https://github.com/parkhongseok/dontgoback-auth-server)

<br/>
<br/>
<br/>

# 2. 주요 기능

![msa-system-architecture](/docs/architecture/src/msa-system-architecture.png)

### ① 공개키 기반 JWT 인증 구조

- 인증 서버에서 `/msa/auth/api/public-key` 로부터 1회 요청을 통해 공개키를 로딩
- `X509EncodedKeySpec` 방식으로 파싱 후 메모리에 저장하여, 모든 요청 검증에 재사용

<br/>

### ② 로그정규 기반 닉네임 갱신 서비스

- 코어 서버로부터 `/msa/auth/api/asset` 요청을 받아, 자산 정보를 반환
- 코어 서버는 이를 바탕으로 유저 자산 이력을 갱신하고, 최신 자산을 닉네임에 자동 반영
- 로그 정규를 통한 기존 자산의 갱신값을 반환

<br/>

### ③ 통합 인증 테스트 구성

- 실제 인증 서버로부터 공개키 및 JWT를 발급받고, 검증까지 포함한 흐름을 테스트
- 정상 및 실패 케이스 모두 포함하며, CI 환경에서도 확장 가능

<br/>

### ④ 빌드 및 배포 자동화 (진행 중)

- Docker 기반 컨테이너화 예정
- GitHub Actions 기반 CI/CD 자동화 구축 중

<br/><br/><br/>

# 3. 아키텍처

### 목차

- 01.JWT 검증을 위한 공개키 로딩 전략
- 02.Spring Security 기반 인증 필터에서의 JWT 검증 처리
- 03.인증 서버 연동을 위한 통합 테스트 전략
- 04.로그정규 분포 기반 일일 자산 임의 갱신

  <br/>

본 프로젝트의 주요 결정 기록은 [`docs/architecture/decisions`](./docs/architecture/decisions) 디렉터리에 정리되어 있습니다.

<br/>
<br/>
