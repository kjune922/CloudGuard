# CloudGuard 백엔드 포트폴리오

AWS에서 사용한 비용을 날짜별로 저장하고, 한 달의 총비용이 월 예산을 얼마나 사용했는지 알려 주는 개인 프로젝트입니다. 직접 입력한 비용과 AWS에서 가져온 비용을 함께 조회할 수 있습니다.

- 작성자: 이경준, GitHub [kjune922](https://github.com/kjune922)
- 역할: Java/Spring API, 비용과 예산 처리, DB 저장과 조회, AWS 연동, 테스트, 실행 환경 구현
- 기술: Java 17, Spring Boot, Spring Data JPA, MySQL, Flyway, AWS SDK v2

## 전체 흐름

![비용 수집과 월 예산 조회](diagrams/architecture.svg)

AWS에서 비용을 가져오고, 내부에서 사용할 이름과 금액으로 바꾼 뒤 저장합니다. 월 비용 조회에서는 해당 월의 기록을 합산합니다. 예산 상태 조회에서는 월 비용과 예산을 비교하여 사용률과 상태를 반환합니다.

현재 월 비용은 DB에서 기록을 조회한 뒤 Java에서 합산합니다. 월 합계에는 직접 입력한 비용과 AWS 비용이 모두 포함됩니다.

## 1. 같은 비용을 다시 가져와도 두 번 계산하지 않도록 처리

![기존 AWS 기록 조회와 갱신](diagrams/reimport.svg)

### Situation, 문제 상황

예를 들어 8월 10일 EC2 비용 10.5를 저장한 뒤 같은 비용을 또 추가하면 합계가 21이 됩니다. 겹치는 기간을 다시 수집할 때 같은 비용이 두 번 계산되는 문제를 막아야 했습니다. 같은 날짜에 직접 입력한 비용도 보존해야 했습니다.

### Task, 해결 목표

비용을 실제 발생한 날짜에 저장하고, 같은 AWS 기록이 있으면 금액을 갱신해야 했습니다. 바뀐 금액은 월 합계와 예산 상태에도 반영되어야 했습니다.

### Action, 구현

AWS 비용을 일별로 조회하고 날짜와 서비스별로 합산했습니다. 직접 입력한 비용과 AWS 비용은 출처를 구분했습니다.

서비스, 발생 날짜, AWS 출처가 같은 기록을 먼저 찾습니다. 기록이 없으면 새로 저장하고, 있으면 기존 기록의 금액을 바꿉니다. 따라서 AWS 비용 10.5가 12로 바뀌면 기존 기록을 12로 갱신합니다. 직접 입력한 비용은 AWS 기록을 찾는 조건에 포함되지 않습니다.

테스트에서는 실제 AWS 대신 정해 둔 응답을 사용하고, 저장과 조회는 실제 Repository와 H2 테스트 DB로 실행했습니다.

### Result, 확인한 결과

| 테스트 상황 | 확인한 결과 |
| --- | --- |
| 같은 기간 두 번 수집 | 기록 1건과 금액 10.5 유지 |
| 금액 10.5에서 12로 변경 | 기존 ID 유지, 금액 12로 갱신 |
| 8/10~8/11, 8/11~8/12 순차 수집 | 8/10은 3 유지, 8/11은 5에서 7로 변경, 8/12는 2 추가 |
| 수동 100과 AWS 10.5가 함께 존재 | 수동 100 유지, AWS만 12로 변경 |
| 예산 100, 비용 80에서 110으로 변경 | 80% CAUTION에서 110% EXCEEDED로 변경 |

저장 변경을 DB에 보낸 뒤 JPA가 관리하던 객체를 분리하고 다시 조회했습니다. 코드에서는 `flush()`와 `clear()`를 사용하여 변경한 객체만 확인하는 것을 피했습니다.

이 결과는 첫 수집이 끝난 뒤 다음 수집을 실행하는 순차 처리에서 확인했습니다. 동시 요청의 중복 방지를 보장하는 결과는 아닙니다. 표의 금액은 테스트 입력과 기댓값입니다.

근거: [수집 서비스](../src/main/java/com/cloudguard/cloudguard/cost/aws/service/AwsCostImportService.java), [저장 서비스](../src/main/java/com/cloudguard/cloudguard/cost/service/CostService.java), [재수집 통합 테스트](../src/test/java/com/cloudguard/cloudguard/cost/aws/service/AwsCostImportServiceIntegrationTest.java)

## 2. AWS 응답을 내부 데이터로 바꾸고 작은 금액을 보존

![외부 응답 변환과 금액 저장](diagrams/precision.svg)

### Situation, 문제 상황

AWS는 긴 서비스명과 문자열 형태의 금액을 보냅니다. CloudGuard는 EC2, RDS, S3 같은 정해진 서비스 이름을 사용하므로 이름을 연결해야 했습니다. 비용에는 0.0000000488처럼 작은 금액도 포함됩니다.

### Task, 해결 목표

AWS 응답을 내부에서 사용할 데이터로 바꾸고, 여러 페이지로 나뉜 비용을 모두 읽어야 했습니다. 작은 금액도 저장 후 유지되어야 했습니다.

### Action, 구현

AWS 응답을 담는 DTO와 서비스명을 바꾸는 Mapper를 분리했습니다. 분류되지 않은 서비스는 OTHER로 묶어 합산하고 USD가 아닌 응답은 저장 전에 거부했습니다.

다음 페이지 토큰이 있으면 같은 조회 조건으로 다음 페이지를 요청했습니다. 금액은 Java의 `BigDecimal`로 변환하고 DB 저장 형식은 `DECIMAL(38,18)`로 지정했습니다.

### Result, 확인한 결과

다음 페이지 조회 조건과 토큰 전달, 전체 페이지 결과 반환, 날짜별 합산, OTHER 매핑을 단위 테스트로 확인했습니다. H2 통합 테스트에서는 0.0000000488을 저장한 뒤 다시 조회하여 같은 값이 유지되는 것을 확인했습니다.

OTHER는 미분류 서비스의 합계입니다. 현재 음수 비용은 거부하므로 환불과 크레딧을 포함한 전체 청구 데이터를 지원하는 범위는 아닙니다.

근거: [AWS 조회 서비스](../src/main/java/com/cloudguard/cloudguard/cost/aws/service/AwsCostExplorerService.java), [서비스명 Mapper](../src/main/java/com/cloudguard/cloudguard/cost/aws/mapper/AwsServiceNameMapper.java), [페이지 조회 테스트](../src/test/java/com/cloudguard/cloudguard/cost/aws/service/AwsCostExplorerServiceTest.java)

## 3. DB 구조 변경을 기록하고 실제 AWS 호출 없이 테스트

### Situation, 문제 상황

Hibernate가 DB 구조를 자동 변경하면 어떤 SQL로 테이블을 만들었는지 Git에서 확인하기 어려웠습니다. 테스트가 실제 AWS를 호출하면 자격 증명과 외부 환경에 영향을 받습니다.

### Task, 해결 목표

테이블을 만드는 SQL을 파일로 관리하고, 실제 AWS 호출 없이도 저장과 조회, 예산 규칙, HTTP 동작을 확인하도록 구성했습니다.

### Action, 구현

Flyway를 사용해 테이블, 월 예산 고유 제약, 금액 제약, 조회 인덱스를 SQL 파일로 관리했습니다. 배포 프로필은 Flyway와 `ddl-auto=validate`를 사용합니다. 이 설정은 실행할 때 엔티티와 DB 구조가 맞는지 검사합니다.

일반 테스트는 H2 DB와 AWS 대역을 사용합니다. Docker Compose에는 MySQL 볼륨과 healthcheck를 설정했습니다. Actions에는 테스트, AWS 자격 증명 설정, ECR 이미지 업로드, SSM 배포 순서를 작성했습니다.

### Result, 확인한 범위

문서에 연결된 [Actions 실행](https://github.com/kjune922/CloudGuard/actions/runs/33743715476)에서 테스트 단계는 통과했습니다. AWS 자격 증명 설정은 실패했으므로 자동 배포 성공 사례로 설명하지 않습니다.

일반 H2 테스트는 Flyway를 실행하지 않습니다. 따라서 H2 테스트 통과와 실제 MySQL의 SQL 실행 검증은 구분합니다.

근거: [마이그레이션 SQL](../src/main/resources/db/migration/V1__init_schema.sql), [배포 프로필](../src/main/resources/application-prod.properties), [테스트 프로필](../src/test/resources/application-test.properties), [워크플로](../.github/workflows/deploy.yml)

## 회고

API 호출이 성공하는 것과 데이터를 올바르게 저장하는 것은 별도로 확인해야 한다는 점을 배웠습니다. 비용 발생 날짜와 출처, 갱신 기준을 정하면서 다시 수집할 때 어떤 기록을 바꿔야 하는지 명확해졌습니다.

저장 직후의 객체뿐 아니라 다시 조회한 값까지 확인했고, 외부 응답 변환과 DB 저장 중 어느 부분을 테스트했는지 구분하여 기록했습니다.
