# CloudGuard AWS 실행 검증, 2026-11-19

2026-11-19의 애플리케이션 로그, API 응답과 재수집 전후 SQL 결과를 정리했습니다. 11월 20일에는 실습 종료 후 Elastic IP 반납까지 확인했습니다. API 예시와 표는 당시 결과이며 원본 터미널 캡처 전체를 저장소에 포함한 것은 아닙니다.

## 실행 버전과 환경

| 항목 | 확인한 값 |
| --- | --- |
| 소스 커밋 | `5b76a98cd54b4a424fd7b56bcc79ed09c7db5c8d` |
| ECR 이미지 태그 | 소스 커밋 전체 SHA |
| 이미지 digest | `sha256:05a40535d3229e45018a2c35212ffc8ce267cdaf0060373354a411f93242e45c` |
| 애플리케이션 | Java 17, Spring Boot 4.1.0, `prod` |
| 실행 서버 | 서울 리전 EC2, Ubuntu, t3.micro, Docker Compose |
| 데이터베이스 | 비공개 RDS MySQL 8.4.11, db.t4g.micro, Single-AZ, 암호화된 gp3 20 GiB |
| 앱 포트 | EC2의 `127.0.0.1:8080` |
| DB 연결 | `sslMode=VERIFY_IDENTITY`, Connector/J의 DB 연결 전용 PKCS12 truststore |

Windows PC에서 이미지를 ECR에 업로드하고 EC2에서 pull한 뒤 Compose로 실행했습니다. EC2의 인스턴스 역할에는 ECR 읽기, SSM과 Cost Explorer 조회에 필요한 정책이 연결돼 있었습니다.

WaterAPI와 같은 EC2와 RDS 인스턴스를 사용하되 DB와 앱 계정을 분리하고 두 앱은 번갈아 실행했습니다. 비밀번호와 실제 접속 정보는 EC2 실행 설정에서 관리했습니다. 저장소의 로컬 `compose.yaml`은 MySQL 8.0용이며, 이번 AWS 실행 설정과는 구분합니다.

## 초기화와 실제 비용 수집

RDS 연결, Flyway V1 적용, JPA 초기화와 앱 기동 후 다음 순서로 확인했습니다.

1. 월별 비용 조회: 합계 0, HTTP 200.
2. Cost Explorer 비용 수집: POST 요청, HTTP 204.
3. 같은 월 비용 조회: 합계 16.7910233479 USD, HTTP 200.

수집 조건은 일별 `UnblendedCost`, SERVICE 그룹이며, 기간은 **2026-11-01 이상, 2026-11-19 미만**입니다. 종료일인 11월 19일의 비용은 포함하지 않습니다.

확인한 수집 경로:

```text
POST /api/aws/costs/import?startDate=2026-11-01&endDate=2026-11-19
GET /api/costs/monthly/breakdown?yearMonth=2026-11
```

월별 조회 API가 반환한 값은 당시 DB에 수집된 기간의 비용 합계입니다. 11월 전체가 마감된 청구 금액을 의미하지 않습니다.

## 같은 기간의 순차 재수집

최초 수집 후 SQL로 저장 결과를 조회하고, 같은 기간의 import 요청을 한 번 더 실행한 뒤 동일 SQL을 다시 조회했습니다.

```sql
SELECT COUNT(*) AS row_count,
       COUNT(DISTINCT usage_date, service) AS unique_pairs,
       SUM(cost) AS total_cost
FROM cost_records
WHERE source = 'AWS_COST_EXPLORER'
  AND usage_date >= '2026-11-01'
  AND usage_date < '2026-11-19';
```

| 확인 항목 | 최초 수집 후 | 같은 기간 재수집 후 |
| --- | ---: | ---: |
| 행 수 | 72 | 72 |
| 서로 다른 날짜와 서비스 조합 | 72 | 72 |
| 총비용 USD | 16.791023347900000000 | 16.791023347900000000 |
| import 응답 | HTTP 204 | HTTP 204 |

이 결과는 같은 기간을 순차 재수집했을 때 중복 행이 늘어나지 않고 합계가 유지됐다는 근거입니다.

AWS 응답 금액이 달라지는 상황과 수동 비용 보존은 [H2 통합 테스트](PORTFOLIO.md#1-재수집-시-비용-중복을-방지하는-저장-구조)에서 따로 검증했습니다. 이번 실제 AWS 실행에서 금액 변경 응답을 별도로 재현한 것은 아닙니다.

## 예산과 중복 등록

2026-11의 월 예산을 20 USD로 등록한 뒤 상태를 조회했습니다.

```json
{
  "yearMonth": "2026-11",
  "monthlyLimit": 20.000000000000000000,
  "totalCost": 16.791023347900000000,
  "usageRate": 83.9600,
  "status": "CAUTION"
}
```

| 요청 | 결과 |
| --- | --- |
| `POST /api/budgets/add` | 최초 등록 HTTP 200 |
| `GET /api/budgets/status?yearMonth=2026-11` | HTTP 200, 사용률 83.96%, CAUTION |
| 같은 월 예산 재등록 | HTTP 409, `DUPLICATE_MONTHLY_BUDGET` |

월 예산은 CloudGuard 내부의 상태 계산 기준입니다. AWS Budgets에 예산이나 알림을 생성한 것은 아닙니다.

## 재기동 후 데이터 유지

앱을 다시 기동한 로그에서 `Started CloudguardApplication`을 확인하고 예산 상태를 재조회했습니다. 동일한 예산, 총비용과 CAUTION 상태가 HTTP 200으로 반환됐습니다. 이후 같은 월 예산의 중복 등록도 HTTP 409로 차단됐습니다.

마지막 확인 화면에 교체 전후 컨테이너 ID가 모두 남아 있지는 않아, 이 기록에서는 ID 변경을 직접 비교했다고 표현하지 않습니다.

## 인증서 경로 문제 해결

RDS truststore 경로를 사용하면서 파일을 열지 못하는 오류가 발생했습니다.

1. `sha256sum`으로 확인한 결과, CloudGuard의 `rds-truststore.p12` 경로가 파일이 아니라 디렉터리였습니다.
2. 디렉터리를 별도 이름으로 보관하고 정상 PKCS12 파일을 복사했습니다.
3. 원본과 복사본의 SHA-256 일치, `keytool -list`의 인증서 3개를 확인했습니다.
4. 다시 실행해 RDS 연결, Flyway, 앱 기동과 HTTP 200을 확인했습니다.

Connector/J의 `trustCertificateKeyStoreUrl`, `trustCertificateKeyStoreType=PKCS12` 등을 사용해 DB 연결에 RDS 인증서를 지정했습니다. AWS API의 HTTPS 연결에는 Java 기본 CA 저장소를 유지했습니다.

## CI와 수동 배포

| 실행 | 결과 |
| --- | --- |
| [Verify CloudGuard](https://github.com/kjune922/CloudGuard/actions/runs/44438400360) | 소스 커밋 `5b76a98`의 테스트와 bootJar 생성 성공 |
| [CloudGuard CI/CD](https://github.com/kjune922/CloudGuard/actions/runs/44438400954) | 테스트 성공, AWS 자격 증명 설정 실패, ECR 업로드와 SSM 배포 단계 미실행 |
| 11월 19일 수동 배포 | PC에서 ECR 업로드, EC2에서 pull과 Compose 실행, RDS와 API 결과 확인 |

11월 18일 작업 사본에서는 전체 테스트 110개와 bootJar 생성을 확인했습니다. 당시 [검증 문서](VERIFICATION_20261118.md)의 원격 반영과 CI 미완료 표시는 그 날짜의 상태이며, 이후 main 반영과 독립 Verify CI 성공을 확인했습니다.

수동 배포 성공을 기존 CI/CD 자동 배포 성공으로 해석하지 않습니다.

## 실습 종료와 범위

실습 종료 후 EC2와 RDS 중지 완료를 확인했습니다. 11월 20일에는 연결된 Elastic IP를 반납하고 서울 리전 할당 목록이 비어 있는 것을 확인했습니다. 계정 전체 비용이 0이라는 의미는 아니며 상시 공개 데모는 운영하지 않습니다.

- 순차 재수집을 검증했으며, 동시 수집 요청의 경쟁 조건 방지를 보장하지 않습니다.
- 현재 음수 비용을 거부하므로 환불과 크레딧을 포함한 전체 청구서 재현을 보장하지 않습니다.
- 이번 실행은 실제 RDS를 이용한 수동 검증입니다. H2 테스트 전체를 MySQL에서 자동 실행한 것과는 다릅니다.
- 부하 성능, RDS 백업 복원, 서로 다른 앱 이미지 간 롤백과 무중단 배포는 이번 검증 범위가 아닙니다.

