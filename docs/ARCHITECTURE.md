# CloudGuard 설계와 검증

AWS 비용을 가져와 저장하고, 월 비용을 예산과 비교하는 흐름을 정리했습니다. 도표와 본문은 현재 구현과 문서에 연결된 테스트 기록을 설명합니다.

## 먼저 읽는 용어

| 용어 | 뜻 |
| --- | --- |
| API | 외부에서 비용 등록과 조회 등을 요청하는 창구 |
| DTO | 요청이나 응답에 필요한 데이터를 담는 객체 |
| Mapper | AWS 서비스명을 내부 서비스 이름으로 바꾸는 코드 |
| Repository | DB 저장과 조회를 담당하는 코드 |
| 재수집 | 같은 기간의 AWS 비용을 다시 가져오는 것 |
| 출처 | AWS 비용과 직접 입력한 비용을 구분하는 값 |
| Mock | 테스트에서 실제 AWS 대신 정해 둔 응답을 주는 대역 |
| Flyway | DB 구조를 만드는 SQL을 파일로 관리하고 적용하는 도구 |

아래에서는 요청 흐름과 저장 기준을 먼저 보고, 연결된 코드와 테스트에서 구체적인 구현을 확인합니다.

## 서비스 흐름

![비용 수집과 예산 조회](diagrams/architecture.svg)

AWS 수집은 `POST /api/aws/costs/import`로 시작합니다. `AwsCostExplorerService`가 DAILY/SERVICE 조건으로 모든 페이지를 읽고, `AwsCostImportService`가 USD 검증과 서비스명 매핑 후 날짜, 서비스별 합계를 만듭니다. `CostService`는 각 합계를 기존 AWS 기록에 반영합니다.

예산 상태 조회는 `BudgetService`가 월 예산과 `CostService`의 월 비용을 가져온 뒤 `BudgetPolicy`에 판단을 맡깁니다. 현재 월 비용은 Repository에서 기간 내 기록을 조회한 뒤 `MonthlyCost`가 Java에서 합산합니다. DB 집계 쿼리로 최적화한 상태는 아닙니다.

AWS 조회 범위의 종료일은 포함되지 않습니다. 8월 전체는 `2026-08-01`부터 `2026-09-01`까지 요청합니다. 월 집계에는 수동 비용과 AWS 비용이 함께 포함됩니다.

근거: [수집 서비스](../src/main/java/com/cloudguard/cloudguard/cost/aws/service/AwsCostImportService.java), [비용 서비스](../src/main/java/com/cloudguard/cloudguard/cost/service/CostService.java), [예산 서비스](../src/main/java/com/cloudguard/cloudguard/budget/service/BudgetService.java)

## 재수집 처리

![AWS 기록 생성과 금액 갱신](diagrams/reimport.svg)

`service + usage_date + AWS_COST_EXPLORER` 조건에 맞는 기록이 있으면 금액을 갱신하고, 없으면 새 기록을 저장합니다. 수동 비용은 `MANUAL`로 저장하여 이 조회에서 제외합니다.

이 기준은 애플리케이션의 조회, 갱신 규칙입니다. V1의 `idx_cost_records_import_lookup`은 일반 인덱스이므로 DB에서 동시 INSERT를 막는 고유 제약은 아닙니다. 재수집 응답에서 사라진 항목의 처리 정책도 후속 과제입니다.

근거: [저장 서비스](../src/main/java/com/cloudguard/cloudguard/cost/service/CostService.java), [재수집 통합 테스트](../src/test/java/com/cloudguard/cloudguard/cost/aws/service/AwsCostImportServiceIntegrationTest.java)

## 외부 응답과 소수 비용

![외부 응답 변환과 저장 검증](diagrams/precision.svg)

금액 문자열은 `BigDecimal`로 변환하고, 서비스명은 `AwsServiceNameMapper`로 내부 enum에 대응시킵니다. 여러 미분류 서비스는 `OTHER`에 합산합니다. 저장 전 USD를 확인하며 현재 음수 비용은 거부합니다.

`DECIMAL(38,18)`은 엔티티와 V1 SQL에 함께 지정했습니다. 작은 금액의 저장, 재조회 검증은 H2 기반이며, 실제 MySQL의 마이그레이션과 제약 검증은 별도 과제입니다.

근거: [AWS 조회 서비스](../src/main/java/com/cloudguard/cloudguard/cost/aws/service/AwsCostExplorerService.java), [서비스명 Mapper](../src/main/java/com/cloudguard/cloudguard/cost/aws/mapper/AwsServiceNameMapper.java)

## 데이터 모델

![현재 테이블 구조](diagrams/data-model.svg)

| 테이블 | 저장 단위와 제약 |
| --- | --- |
| `cost_records` | 서비스, 발생 날짜, 출처와 금액. PK는 `id`, 금액은 0 이상 |
| `monthly_budgets` | 월별 예산. `budget_month` UNIQUE, 예산은 0 초과 |

비용의 기간 조회, 서비스, 기간 조회, 재수집 기록 조회에 필요한 인덱스가 있습니다. 두 테이블에 외래 키 관계는 없고 서비스 계층에서 월 기준으로 함께 조회합니다. `MonthlyCost`와 `BudgetPolicy`는 계산을 담당하는 객체이며 테이블이 아닙니다.

근거: [V1 SQL](../src/main/resources/db/migration/V1__init_schema.sql), [CostRecord](../src/main/java/com/cloudguard/cloudguard/cost/domain/CostRecord.java), [BudgetPolicy](../src/main/java/com/cloudguard/cloudguard/budget/domain/BudgetPolicy.java)

## 검증 환경과 배포 구성

![테스트 환경과 배포 단계의 확인 범위](diagrams/verification.svg)

| 환경 | 설정과 확인 범위 |
| --- | --- |
| 일반 테스트 | H2 `create-drop`, Flyway 비활성화, AWS Mock. 도메인, 저장, 서비스, HTTP 동작 검증 |
| 배포 프로필 | MySQL, Flyway, `ddl-auto=validate`. 일반 테스트 통과만으로 V1 실행 검증을 대신하지 않음 |
| Actions | main push 또는 수동 실행. 테스트 후 OIDC → ECR → SSM/EC2 순서로 구성 |

기준 [Actions 실행](https://github.com/kjune922/CloudGuard/actions/runs/33743715476)에서 테스트는 통과했고 AWS 자격 증명 설정은 실패했습니다. 배포 구성도는 워크플로에 작성된 순서이며 배포 성공 기록을 의미하지 않습니다. 현재 워크플로는 문서만 변경한 main push에도 실행됩니다.

근거: [테스트 프로필](../src/test/resources/application-test.properties), [배포 프로필](../src/main/resources/application-prod.properties), [Compose](../compose.yaml), [워크플로](../.github/workflows/deploy.yml)

## 도표 수정

`diagrams/`의 SVG는 편집 원본이며 README와 문서에서 직접 사용합니다. SVG를 수정하면 연결된 문서에도 같은 그림이 반영됩니다. PDF와 Word로 내보낼 때는 해당 원본을 이미지로 변환합니다. [포트폴리오](PORTFOLIO.md)도 같은 도표를 사용하므로 본문과 검증 근거를 함께 확인합니다.
