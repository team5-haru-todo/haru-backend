# Haru Backend
## Architecture Design Document

* [백엔드 노션](https://app.notion.com/p/37a2b87c6aec80ad9f56e1c5b8fb1c03)
* [DB Schema](https://app.notion.com/p/DB-Schema-3862b87c6aec80719d76ed44e317e25e)
* [API 명세서](https://app.notion.com/p/API-3862b87c6aec80f8851acb2d4e506ec2)

## Backend Setup

현재 백엔드는 다음 기준으로 세팅되어 있다.

* Java 17
* Spring Boot 3.5.15
* Gradle
* Spring Web
* Spring Data JPA
* Spring Security
* Validation
* PostgreSQL 16
* Flyway
* Swagger / Springdoc OpenAPI
* Docker / Docker Compose
* GitHub Actions

로컬 DB는 Docker PostgreSQL을 사용한다.

* Spring Boot server port: `8080`
* Local PostgreSQL port: `5433`
* DB name: `haru`
* DB username: `haru`
* DB password: `haru`

환경변수 예시는 `.env.example`에 둔다.
실제 `.env` 파일은 Git에 올리지 않는다.

---

## Role Assignment

백엔드는 도메인 기준으로 역할을 분리한다.

| 담당자 | 담당 도메인                               | 주요 범위                              |
| --- | ------------------------------------ | ---------------------------------- |
| 김다은 | `auth`, `user`                       | 카카오/애플 로그인, JWT 발급, 사용자 정보, 사용자 설정 |
| 조아영 | `task`                               | 할 일 생성/조회/수정/삭제, 반복 할 일, 표시 순서     |
| 최희원 | `record`, `global`                   | 오늘의 한 개, 완료 처리, 스트릭, 공통 응답/예외/설정   |
| 정윤서 | `calendar`, `report`, `notification` | 캘린더 조회, 주간 리포트, 기기 토큰, 푸시 알림       |

역할 기준은 다음과 같다.

* 각 담당자는 본인 도메인 안에서 `controller`, `service`, `repository`, `entity`, `dto`를 관리한다.
* 다른 도메인의 테이블이나 로직을 직접 수정해야 하는 경우 먼저 담당자와 확인한다.
* 공통 설정, 공통 응답, 공통 예외, Security 기본 설정은 `global`에서 관리한다.
* 완료 처리는 `record` 도메인에서만 담당한다.
* `task` 도메인은 할 일 원본만 관리하고 완료 기록을 생성하지 않는다.
* `calendar` 도메인은 완료 기록을 조회만 하며, 기록을 새로 생성하지 않는다.
* DB 변경이 필요한 경우 Flyway migration 파일을 추가하고, 기존에 공유된 migration은 수정하지 않는다.
* API 명세가 변경될 경우 Swagger와 README 또는 별도 API 문서를 함께 갱신한다.

---

## Package Structure

패키지는 도메인 기준으로 분리한다.

```text
src/main/java/com/haru/backend
├─ global
├─ auth
├─ user
├─ task
├─ record
├─ calendar
├─ report
└─ notification
```

각 도메인 내부는 필요에 따라 다음 구조를 사용한다.

```text
controller
service
repository
entity
dto
```

단, 모든 도메인이 항상 모든 하위 패키지를 가질 필요는 없다.
예를 들어 `calendar`는 별도 엔티티 없이 조회 전용 도메인으로 둘 수 있다.

---

## Domain Responsibility

### global

전역 설정과 공통 기능을 담당한다.

현재 구성된 주요 파일은 다음과 같다.

| 파일 | 역할 |
| --- | --- |
| `SwaggerConfig.java` | Swagger/OpenAPI 문서 설정 |
| `JpaAuditingConfig.java` | 생성일/수정일 자동 관리를 위한 JPA Auditing 설정 |
| `SecurityConfig.java` | Spring Security 기본 설정 |
| `ApiResponse.java` | 공통 성공 응답 형식 |
| `ErrorResponse.java` | 공통 실패 응답 형식 |
| `ErrorCode.java` | 공통 에러 코드 정의 |
| `BusinessException.java` | 서비스 로직에서 사용하는 커스텀 예외 |
| `GlobalExceptionHandler.java` | 전역 예외 처리 |

현재 Security 설정은 초기 개발 편의를 위해 대부분의 요청을 허용한다.  
JWT 인증/인가 적용은 `auth` 도메인의 로그인 구현 이후 진행한다.

JWT 관련 클래스는 아직 작성하지 않는다.  
로그인 정책과 사용자 엔티티가 확정된 뒤 `global.security`에 추가한다.

### auth

로그인과 인증을 담당한다.

* 카카오 로그인
* 애플 로그인
* JWT 발급
* 로그아웃
* 회원 탈퇴
* 외부 인증 서버 통신

외부 인증 서버와 통신하는 코드는 `auth.client`에 둔다.

### user

사용자 정보와 사용자 설정을 담당한다.

관련 테이블:

```text
USERS
USER_SETTINGS
```

### task

할 일 원본 목록을 담당한다.

관련 테이블:

```text
TASKS
```

`task`는 할 일 생성, 조회, 수정, 삭제, 반복 설정, 표시 순서를 담당한다.

완료 처리는 `task`에서 하지 않는다.

### record

오늘의 한 개, 완료 처리, 스트릭을 담당한다.

관련 테이블:

```text
DAILY_RECORDS
TASK_COMPLETIONS
USER_STATS
```

첫 완료는 불꽃과 스트릭에 반영한다.
추가 완료는 완료 기록만 생성하고 불꽃과 스트릭은 변경하지 않는다.

### calendar

완료 기록을 조회해 캘린더 화면에 필요한 데이터를 제공한다.

조회 대상:

```text
DAILY_RECORDS
TASK_COMPLETIONS
```

`calendar`는 기록을 생성하지 않는다.
기록 생성은 `record`에서만 처리한다.

### report

주간 리포트를 담당한다.

관련 테이블:

```text
WEEKLY_REPORTS
```

리포트는 실제 완료 기록을 기준으로 집계한다.
미완료 항목은 집계에 포함하지 않는다.

### notification

기기 토큰과 푸시 알림을 담당한다.

관련 테이블:

```text
DEVICE_TOKENS
```

`DEVICE_TOKENS`는 `auth`가 아니라 `notification` 도메인의 소유로 본다.

---

## Development Rules

* Entity를 API 응답으로 직접 반환하지 않는다.
* Controller에는 비즈니스 로직을 넣지 않는다.
* 비즈니스 로직은 Service에 작성한다.
* DB 접근은 Repository를 통해 처리한다.
* 사용자 데이터는 반드시 현재 사용자의 소유인지 검증한다.
* 할 일 삭제는 기본적으로 `deleted_at`을 사용하는 soft delete로 처리한다.
* 하루 날짜 기준은 `Asia/Seoul`로 처리한다.
* 완료 기록은 사용자가 완료 버튼을 누른 경우에만 생성한다.
* 미완료 기록을 별도 row로 저장하지 않는다.
* `task` 도메인에 완료 처리 로직을 넣지 않는다.
* `calendar` 도메인에서 완료 기록을 생성하지 않는다.
  
공통 응답과 예외 처리 기준은 다음을 따른다.
* 성공 응답은 `ApiResponse.ok(...)`를 사용한다.
* 실패 상황은 직접 `ResponseEntity`를 만들기보다 `BusinessException`을 발생시킨다.
* 에러 종류는 `ErrorCode`에 정의한 값을 사용한다.
* 새로운 예외 상황이 필요하면 임의 문자열을 쓰지 말고 `ErrorCode`를 먼저 추가한다.



---

## DB Migration

DB 스키마는 Flyway로 관리한다.

Migration 파일 위치:

```text
src/main/resources/db/migration
```

파일명 규칙:

```text
V버전__설명.sql
```

예시:

```text
V1__create_users.sql
V2__create_user_settings.sql
V3__create_user_stats.sql
V4__create_tasks.sql
V5__create_daily_records.sql
V6__create_task_completions.sql
V7__create_weekly_reports.sql
V8__create_device_tokens.sql
```

초기 로컬 개발 중에는 migration 파일을 수정하고 로컬 DB를 초기화할 수 있다.

단, `develop`에 merge되었거나 팀원이 이미 pull 받은 migration은 수정하지 않는다.
그 이후 DB 변경은 새 migration 파일로 추가한다.

---

## Branch Rule

브랜치 흐름은 다음 기준을 따른다.

```text
feature/*
→ develop
→ main
```

* `feature/*`: 기능 개발 브랜치
* `develop`: 통합 개발 브랜치
* `main`: 배포 기준 브랜치

기능 작업은 `feature/*`에서 진행하고, `develop`으로 PR을 생성한다.

---

## CI

PR 검증은 GitHub Actions로 수행한다.

Workflow 위치:

```text
.github/workflows/ci.yml
```

실행 조건:

* PR to `develop`
* PR to `main`

CI에서 확인하는 항목:

* PostgreSQL 16 실행
* Java 17 설정
* Gradle test
* Spring Boot build

현재 CI는 배포가 아니라 PR 검증용이다.
CD는 EC2, Dockerfile, Nginx, 서버 환경변수 구성이 완료된 뒤 추가한다.

### 프로젝트 진행 계획
```
1단계: 로컬 개발 환경 세팅
2단계: CI 설정
3단계: Docker 실행 구조 만들기
4단계: EC2 서버 준비
5단계: Staging 배포 자동화
6단계: Production 배포 자동화
7단계: 롤백/모니터링 고도화
```

---

## Current Status

현재 초기 세팅 완료 항목:

* Spring Boot 프로젝트 생성
* 패키지 구조 정리
* PostgreSQL Docker Compose 로컬 환경 구성
* `local`, `staging`, `prod` profile 구성
* Flyway 초기 migration 구성
* Swagger 설정
* Spring Security 기본 설정
* 공통 응답 구조
* 공통 예외 처리 구조
* JPA Auditing 설정
* GitHub Actions CI 설정
* `.env.example` 작성

