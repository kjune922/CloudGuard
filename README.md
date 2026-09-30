# CloudGuard

**AWS 비용을 일별로 수집하고, 월별·서비스별 집계와 예산 상태를 제공하는 Java/Spring 백엔드입니다.**

클라우드 실습 비용을 확인하고 관리하기 위해 만들었습니다. 재수집 시 비용을 계속 더하는 대신 기존 기록을 갱신하고, 수동 등록 비용과 AWS 수집 비용을 구분했습니다.

[STAR 포트폴리오](docs/PORTFOLIO.md) · [실행 및 API 가이드](docs/RUNBOOK.md) · [개선 체크리스트](docs/READINESS.md) · [개발 기록](docs/DEVELOPMENT_LOG.md)

## 주요 기능

- Cost Explorer 일별 수집: 페이지 토큰 처리, 서비스명 매핑, USD 검증
- 순차 재수집 시 날짜·서비스·출처로 기존 기록을 찾아 금액 갱신
- 월별 총액 및 EC2·RDS·S3·OTHER별 비용 조회
- 월 예산 등록·변경 및 SAFE / CAUTION / WARNING / EXCEEDED 상태 조회
- Bean Validation과 공통 예외 응답으로 잘못된 요청·예산 중복·미등록 처리
- Flyway 스키마 관리와 Docker Compose 기반 MySQL 실행 환경

## 기술 스택

| 영역 | 기술 |
| --- | --- |
| 백엔드 | Java 17, Spring Boot 4.1.0, Spring MVC, Spring Data JPA |
| 데이터 | MySQL 8.0, Flyway, H2 테스트 DB |
| 외부 연동 | AWS SDK v2, Cost Explorer |
| 검증 | JUnit 5, AssertJ, Mockito, MockMvc |
| 실행 및 배포 구성 | Gradle, Docker Compose, GitHub Actions, ECR, EC2, SSM |

## 핵심 설계와 검증

| 문제 | 설계 | 저장소 내 검증 근거 |
| --- | --- | --- |
| 재수집 시 합계가 증가할 수 있음 | 일별 수집과 날짜·서비스·출처 기준 갱신 | 두 번 수집해도 1건 유지, 기존 ID 보존, 겹친 날짜만 갱신 |
| 작은 소수의 저장 정밀도 | BigDecimal과 DECIMAL(38,18) 사용 | `0.0000000488` 저장 후 flush/clear 및 재조회 |
| 외부 서비스명이 내부 enum과 다름 | 원본 DTO와 Mapper 분리, 미분류는 OTHER 합산 | Mapper 및 날짜별 합산 단위 테스트 |
| 환경마다 다른 DB 구조 | Flyway SQL과 배포 프로필의 `ddl-auto=validate` | V1 마이그레이션 파일 및 실행 구성 |

검증 수치는 테스트 입력·기대값입니다. 성능 개선율이나 실제 사용자 규모를 의미하지 않습니다.

## 요청 흐름

`POST /api/aws/costs/import` → AWS 일별 비용 조회 → 날짜·서비스별 합산 → DB 저장·갱신

`GET /api/budgets/status` → 월 비용 집계 → BudgetPolicy 사용률 계산·상태 판정 → JSON 응답

AWS 종료일은 조회 범위에 포함되지 않습니다. `2026-08-01`부터 `2026-09-01`까지 요청하면 8월 전체를 수집합니다. 저장된 월별 집계에는 수동 비용과 AWS 비용이 함께 포함됩니다.

## 빠른 실행

Java 17 JDK와 Docker Compose가 필요합니다. 저장소 루트에 `.env`를 만들고 로컬 DB 값을 설정합니다.

```dotenv
CLOUDGUARD_DOCKER_DB_USERNAME=cloudguard
CLOUDGUARD_DOCKER_DB_PASSWORD=choose_a_local_password
CLOUDGUARD_DOCKER_DB_ROOT_PASSWORD=choose_a_different_local_password
```

```bash
docker compose up --build -d
curl 'http://localhost:8080/api/costs/monthly/breakdown?yearMonth=2026-08'
```

이 실행 경로는 수동 비용 등록·조회에 사용할 수 있습니다. AWS 수집에는 실행 환경의 AWS 자격 증명이 별도로 필요합니다. Compose에는 자격 증명 전달 설정이 없습니다. 상세 절차는 [실행 가이드](docs/RUNBOOK.md)를 참고하세요.

```bash
# AWS 호출을 Mock으로 대체하는 일반 테스트
bash gradlew test
```

## 현재 검증 범위

기준 커밋 `7409ffc`의 [Actions 실행](https://github.com/kjune922/CloudGuard/actions/runs/33743715476)에서 테스트 단계 통과를 확인했습니다. 같은 실행의 AWS 자격 증명 설정은 실패하여 ECR 업로드·SSM 배포는 실행되지 않았습니다.

현재 중복 방지는 **순차 재수집** 범위입니다. 동시 수집 DB 제약, 예산 경계값 직전의 반올림 문제, MySQL 마이그레이션 자동 검증은 [우선 개선 항목](docs/READINESS.md)으로 관리합니다.
