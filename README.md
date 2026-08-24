# CloudGuard

Java/Spring 기반 클라우드 비용 관리 백엔드 프로젝트입니다.

현재는 EC2, RDS, S3 비용 데이터를 직접 등록하고 월별·서비스별로 집계하며,
월 예산 대비 사용률과 상태를 조회하는 기능까지 구현했습니다.
AWS Cost Explorer와 CloudWatch 연동, 장애 관리는 다음 단계로 개발할 예정입니다.

## 개발 목적

클라우드 운영에서는 비용 데이터와 예산 기준을 함께 확인해야 합니다.
CloudGuard는 비용 기록, 월별 집계, 예산 정책을 하나의 서비스에서 관리하고
향후 AWS 비용·장애 정보까지 통합하는 것을 목표로 합니다.

## 현재 구현 범위

### 비용 관리

- EC2, RDS, S3 비용 기록 등록
- 특정 월의 전체 누적 비용 조회
- 특정 월·서비스의 누적 비용 조회
- 특정 월의 서비스별 비용과 전체 비용 상세 조회
- 비용 기록이 없는 서비스도 0으로 반환

### 예산 관리

- 월 예산 등록 및 변경
- 동일 연월 예산 중복 등록 시 409 Conflict 반환
- 등록되지 않은 연월 조회·변경 시 404 Not Found 반환
- 0 이하의 예산 등록·변경 시 400 Bad Request 반환
- 월 누적 비용과 예산을 이용한 사용률 계산
- 사용률에 따른 SAFE, CAUTION, WARNING, EXCEEDED 상태 판정
- 연월, 월 예산, 누적 비용, 사용률, 상태를 포함한 상세 응답 제공

### 예산 상태 기준

| 사용률 | 상태 |
| --- | --- |
| 70% 미만 | SAFE |
| 70% 이상 85% 미만 | CAUTION |
| 85% 이상 100% 미만 | WARNING |
| 100% 이상 | EXCEEDED |

## API

| Method | Endpoint | 설명 |
| --- | --- | --- |
| POST | `/api/costs/add-cost` | 비용 기록 등록 |
| GET | `/api/costs/monthly?yearMonth=yyyy-MM` | 월 누적 비용 조회 |
| GET | `/api/costs/monthly/by-service?yearMonth=yyyy-MM&service=EC2` | 월·서비스별 비용 조회 |
| GET | `/api/costs/monthly/breakdown?yearMonth=yyyy-MM` | 서비스별 비용과 전체 비용 상세 조회 |
| POST | `/api/budgets/add` | 월 예산 등록 |
| PUT | `/api/budgets/{yearMonth}` | 월 예산 변경 |
| GET | `/api/budgets/status?yearMonth=yyyy-MM` | 예산 사용률과 상태 상세 조회 |

## 설계 기준

- **Controller**: HTTP 요청과 응답 처리
- **Service**: 비용·예산 조회와 전체 흐름 조정
- **Domain**: 비용 합산, 사용률 계산, 상태 판정 등 비즈니스 규칙 수행
- **Repository**: JPA를 이용한 비용·예산 데이터 저장 및 조회

사용률 계산과 상태 판정은 `BudgetPolicy`가 담당합니다.
월별 비용 합산과 서비스별 집계는 `MonthlyCost`가 담당하도록 책임을 분리했습니다.

## 테스트

- **도메인 테스트**: 비용·예산 값 검증, 월별 합산, 상태 경계값 검증
- **Repository 테스트**: 연월과 서비스 조건에 따른 데이터 조회 검증
- **Service 통합 테스트**: 실제 Repository와 테스트 DB를 이용한 비용 집계·예산 상태 검증
- **Controller 테스트**: MockMvc를 이용한 요청, 응답 JSON, HTTP 상태 코드 검증
- **수동 검증**: Postman으로 예산 등록·변경 후 상태 재계산 흐름 확인

검증 시 월 예산 10,000, 누적 비용 8,000에서 사용률 80%와 CAUTION을 확인했습니다.
이후 예산을 20,000으로 변경했을 때 동일한 비용에서 사용률 40%와 SAFE로
재계산되는 전체 흐름을 확인했습니다.

## 기술 스택

- Java 17
- Spring Boot
- Spring MVC
- Spring Data JPA
- MySQL
- H2
- JUnit 5
- AssertJ
- MockMvc
- Gradle
- Postman

## 개발 현황

- [x] 프로젝트 초기 설정
- [x] 비용 도메인 설계
- [x] 예산 정책과 상태 경계값 구현
- [x] 비용 기록 등록
- [x] 월 누적 비용 조회
- [x] 서비스별 비용 조회
- [x] 월별 비용 상세 조회
- [x] 월 예산 등록·변경
- [x] 예산 사용률과 상태 상세 조회
- [x] 도메인·Repository·Service·Controller 테스트
- [ ] AWS 계정 등록
- [ ] AWS Cost Explorer 자동 수집
- [ ] CloudWatch 경보 연동
- [ ] 비용 경고 생성 및 중복 방지
- [ ] 장애 생성·담당자·처리 상태 관리
- [ ] AWS 배포

## 개발 기록

### 2026-08-05

- 프로젝트 초기 설정
- 비용·예산 도메인 설계
- `BudgetPolicy` 상태 경계값 테스트 작성

### 2026-08-08

- `CostRecord`를 JPA Entity로 전환
- 비용 값, 서비스, 발생일 검증 추가

### 2026-08-10

- `CostRecordRepository`와 `CostService` 구현
- 특정 월의 비용을 DB에서 조회해 합산하는 흐름 구현
- Spring Boot와 DB 자동 구성 문제를 분석하고 테스트 환경 수정

### 2026-08-11

- 비용 등록 API 구현
- MockMvc와 Postman으로 비용 등록·예산 상태 조회 검증
- 통합 테스트 데이터 롤백 적용

### 2026-08-13 ~ 2026-08-14

- 월 예산 등록·변경 구현
- 중복 등록 409, 미등록 연월 404, 잘못된 예산 400 응답 구현
- JPA 변경 감지 후 DB 반영 흐름 검증

### 2026-08-22 ~ 2026-08-23

- 월·서비스별 비용 조회 구현
- EC2, RDS, S3 비용 상세 집계 구현
- 예산 상태 API를 연월, 예산, 누적 비용, 사용률, 상태를 포함한 응답으로 확장
- 실제 DB, MockMvc, Postman을 이용한 전체 흐름 검증

## 검증 화면

### 월별 비용 상세 조회

![월별 비용 상세 조회](img_6.png)

### 예산 상태 상세 조회

![예산 상태 상세 조회](img_8.png)

### 예산 변경 후 상태 재계산

![예산 변경 후 상태 재계산](img_7.png)
