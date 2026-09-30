# 실행 및 API 가이드

## Docker Compose 실행

Java 17 JDK, Docker와 Docker Compose가 필요합니다. 앱은 `prod` 프로필로 실행되며 Flyway가 MySQL 스키마를 생성합니다. 루트 `.env`는 커밋하지 않습니다.

```dotenv
CLOUDGUARD_DOCKER_DB_USERNAME=cloudguard
CLOUDGUARD_DOCKER_DB_PASSWORD=choose_a_local_password
CLOUDGUARD_DOCKER_DB_ROOT_PASSWORD=choose_a_different_local_password
```

```bash
docker compose up --build -d
docker compose ps
docker compose logs app
# 종료, DB 볼륨 보존
docker compose down
```

앱은 8080, 호스트에서 MySQL에 접속할 때는 3307입니다.

## AWS 없이 기능 확인

비어 있는 DB에서 실행하면 예산 사용률 80%, CAUTION 상태를 확인할 수 있습니다. 예산 반복 등록은 409입니다.

```bash
curl -X POST 'http://localhost:8080/api/costs/add-cost' \
  -H 'Content-Type: application/json' \
  -d '{"cloudService":"EC2","cost":80,"usageDate":"2026-08-10"}'
curl -X POST 'http://localhost:8080/api/budgets/add' \
  -H 'Content-Type: application/json' \
  -d '{"yearMonth":"2026-08","monthlyLimit":100}'
curl 'http://localhost:8080/api/costs/monthly/breakdown?yearMonth=2026-08'
curl 'http://localhost:8080/api/budgets/status?yearMonth=2026-08'
```

## 호스트 JVM에서 AWS 연동

Compose에는 AWS 자격 증명 전달 설정이 없습니다. 로컬 AWS 프로필을 사용하는 경우 DB만 컨테이너로, 앱은 호스트에서 실행할 수 있습니다.

```bash
docker compose up -d db
export SPRING_PROFILES_ACTIVE=prod
export CLOUDGUARD_DB_URL='jdbc:mysql://localhost:3307/cloudguard?useSSL=false&allowPublicKeyRetrieval=true'
export CLOUDGUARD_DB_USERNAME='cloudguard'
export CLOUDGUARD_DB_PASSWORD='your_local_db_password'
export AWS_PROFILE='your_aws_profile'
bash gradlew bootRun
```

DB 비밀번호는 `.env`와 동일하게, AWS 프로필명은 실제 설정에 맞게 변경합니다. 실행 환경에 Cost Explorer 조회 권한이 필요합니다. SDK 기본 자격 증명 체인을 사용하며 키를 소스나 properties에 작성하지 않습니다. EC2에서는 인스턴스 역할을 사용합니다.

```bash
# 종료일 미포함: 8월 전체 수집
curl -X POST 'http://localhost:8080/api/aws/costs/import?startDate=2026-08-01&endDate=2026-09-01'
```

정상 수집은 204입니다. 실제 AWS 호출에는 계정의 Cost Explorer 이용 조건이 적용됩니다.

## API 목록

| Method | 경로 | 동작 |
| --- | --- | --- |
| POST | `/api/costs/add-cost` | 수동 비용 등록 |
| GET | `/api/costs/monthly?yearMonth=2026-08` | 월 비용 |
| GET | `/api/costs/monthly/by-service?yearMonth=2026-08&service=EC2` | 서비스별 월 비용 |
| GET | `/api/costs/monthly/breakdown?yearMonth=2026-08` | 서비스별 비용과 합계 |
| POST | `/api/budgets/add` | 월 예산 등록 |
| PUT | `/api/budgets/2026-08` | 예산 변경, 본문 `{"monthlyLimit":200}` |
| GET | `/api/budgets/status?yearMonth=2026-08` | 예산·비용·사용률·상태 |
| GET | `/api/aws/costs?startDate=2026-08-01&endDate=2026-09-01` | AWS 월 단위 조회, 저장 없음 |
| POST | `/api/aws/costs/import?startDate=2026-08-01&endDate=2026-09-01` | AWS 일별 저장·갱신 |

월 집계는 수동 비용과 AWS 비용을 모두 더합니다. 같은 청구 금액을 두 출처에 입력하면 모두 포함됩니다. AWS 월 단위 조회 DTO에는 발생 월 필드가 없어 여러 달의 결과를 구분하는 데 제한이 있습니다.

## 테스트 및 배포 상태

```bash
bash gradlew test
```

JRE만으로는 컴파일할 수 없으므로 Java 17 JDK가 필요합니다. 일반 테스트는 AWS Client 또는 조회 서비스를 Mock으로 교체합니다. H2 테스트에서 Flyway는 실행하지 않습니다.

배포 워크플로는 main push 또는 수동 실행으로 동작합니다. Actions 변수 `AWS_REGION`, `ECR_REPOSITORY`, `EC2_INSTANCE_ID`, `AWS_ROLE_ARN`, EC2 환경 파일 `/home/ubuntu/cloudguard-prod.env`, 인스턴스 역할과 SSM 구성이 필요합니다.

2026-09-03 실행은 테스트 이후 자격 증명 설정이 실패했습니다. [실행 기록](https://github.com/kjune922/CloudGuard/actions/runs/33743715476)에서 ECR·SSM 단계 성공 여부를 확인해야 합니다.
