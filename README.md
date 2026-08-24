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

### CostService Test 진행 중 에러

초기에
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
를 켜서 DB없어도 Test를 진행하기위해 설정해놓을걸 까먹고 안지웠다가
Test진행중 Spring Boot와 DB 연결을 자동으로 구성하는걸 막아
CostRecordRepository Bean을 만들수 없는 에러 발생

지우고 다시 실행 -> 성공

## 2026-08-11

* `CostController`와 `CostCreateRequest` DTO 구현
* `POST /api/costs/add-cost` 비용 등록 API 구현
*  Postman으로 비용 저장 및 예산 상태 조회 흐름 검증

    * 비용 `800`, 월 예산 `1000` → `CAUTION` 반환 확인
* `CostControllerTest` 작성 및 통과

    * `MockMvc`로 POST 요청 검증
    * `@MockitoBean`으로 `CostService` 모킹
    * `given().willReturn()`으로 가짜 반환값 설정
    * HTTP 상태 코드와 응답 JSON 검증
* `CostServiceTest`에 `@Transactional`을 적용해 테스트 데이터 롤백 처리

## 2026-08-13

![img.png](img.png)

POSTMAN 활용해서 request보내고 잘 보내졌는지 확인

![img_1.png](img_1.png)

POSTMAN 활용해서 DB 중복 데이터에 대해 에러발생하는지 확인

## 2026-08-14

![img_2.png](img_2.png)

반환 에러메시지 500 -> 409 conflict로 변경

## 2026-08-22

![img_3.png](img_3.png)

![img_4.png](img_4.png)

![img_5.png](img_5.png)


## 2026-08-23

### EnumMap과 merge를 이용한 서비스별 비용 합산 <개념숙지체크>

`EnumMap`은 enum 타입을 키로 사용할 때 특화된 `Map` 구현체다.

Map<CloudService, BigDecimal> totals =
        new EnumMap<>(CloudService.class);

여기서 각 타입의 의미는 다음과 같다.

* `CloudService`: Map의 키 타입
* `BigDecimal`: Map에 저장되는 비용의 타입
* `CloudService.class`: EnumMap이 어떤 enum을 키로 사용하는지 알려주는 정보

`CloudService.values()`는 `EnumMap`의 메서드가 아니라 모든 enum에 자동으로 제공되는 정적 메서드다. 선언된 모든 enum 상수를 순서대로 배열로 반환한다.

for (CloudService service : CloudService.values()) {
    totals.put(service, BigDecimal.valueOf(0));
}

현재 `CloudService`에는 `EC2`, `RDS`, `S3`가 있으므로 처음 Map은 다음 상태가 된다.

EC2 = 0
RDS = 0
S3 = 0

이렇게 모든 서비스를 0으로 초기화하면 해당 월에 비용 기록이 없는 서비스도 결과에서 누락되지 않고 0으로 반환할 수 있다.

`Map.merge()`는 기존 값의 존재 여부에 따라 값을 저장하거나 합산한다.

totals.merge(
        costRecord.getService(),
        costRecord.getCost(),
        BigDecimal::add
);

`merge()`의 동작은 다음과 같다.

* 키가 없으면 전달받은 값을 새로 저장한다.
* 키가 있으면 기존 값과 새로운 값에 지정한 함수를 적용한다.

`BigDecimal::add`는 다음 람다식과 같은 의미의 메서드 참조다.

existingCost = 이미 존재하고있는 비용 (기존비용)
newCost = 새로운 비용 (추가한 비용)

(existingCost, newCost) -> existingCost.add(newCost)

예를 들어 EC2의 초기값이 0이고 비용 기록이 3000, 1000 순서로 들어오면 다음처럼 계산된다.

0 + 3000 = 3000
3000 + 1000 = 4000

`BigDecimal`은 값을 직접 변경하지 않는 불변 객체이므로 `add()`가 새로운 합계 객체를 반환하고, 
`merge()`가 그 결과를 다시 Map에 저장함

--------------------------------

### Stream과 reduce를 이용한 비용 합산

다음 코드는 서비스별 비용이 들어 있는 Map에서 모든 비용을 더해 총비용을 계산한다.

BigDecimal totalCost = serviceCosts.values()
        .stream()
        .reduce(BigDecimal.ZERO, BigDecimal::add);
#### `values()`의 역할

`serviceCosts`의 타입은 다음과 같다.

Map<CloudService, BigDecimal>

예를 들어 Map에 다음 값이 저장돼 있다고 가정한다.

EC2 = 4000
RDS = 2000
S3 = 0

`values()`를 호출하면 키를 제외하고 비용 값들만 가져온다.

serviceCosts.values()

결과는 다음과 같은 값의 모음이다.
[4000, 2000, 0]

#### `stream()`의 역할

`stream()`은 컬렉션의 값을 하나씩 순서대로 처리할 수 있는 Stream 흐름으로 변환한다.

serviceCosts.values().stream()

Stream은 데이터를 직접 저장하는 자료구조가 아니다. 
기존 컬렉션의 데이터를 대상으로 필터링, 변환, 합산 등의 연산을 연결해서 실행할 수 있도록 만든 처리 흐름이다.

`stream()`을 호출하는 것만으로는 값이 변경되거나 합산되지 않는다. 실제 처리는 `reduce()`와 같은 최종 연산이 호출될 때 실행된다.

#### `reduce()`의 역할

`reduce()`는 여러 값을 하나의 결과로 줄이는 최종 연산이다.

.reduce(BigDecimal.ZERO, BigDecimal::add)

첫 번째 인자인 `BigDecimal.ZERO`는 합산을 시작할 초기값이다.

두 번째 인자인 `BigDecimal::add`는 기존 합계와 다음 비용을 더하는 방법이다.

(currentTotal, nextCost) -> currentTotal.add(nextCost)

실제 계산 흐름은 다음과 같다.

초기값: 0

0 + 4000 = 4000
4000 + 2000 = 6000
6000 + 0 = 6000

최종 결과: 6000

초기값으로 0을 사용하는 이유는 어떤 값에 0을 더해도 원래 값이 유지되기 때문이다. 비용 데이터가 하나도 없는 빈 Stream이어도 결과로 0을 반환할 수 있다.

#### 반복문과 비교

Stream을 사용하지 않으면 다음 반복문과 같은 의미다.

BigDecimal totalCost = BigDecimal.ZERO;

for (BigDecimal cost : serviceCosts.values()) {
    totalCost = totalCost.add(cost);
}

두 코드는 같은 결과를 만든다.

Stream 방식은 “값들을 순서대로 반복한다”는 과정 대신 “여러 비용을 하나의 합계로 줄인다”는 목적을 표현할 수 있다는 장점이 있다.

또한 Stream으로 합산해도 기존 `serviceCosts` Map의 값은 변경되지 않는다.

-------------------------------------------

![img_6.png](img_6.png)

월별 비용 상세 기능 도메인,Repository,Service,Controller,실제 DB 까지 완료

--------------------------------------------

다음단계로 현재 예산 상태 API를 상세 응답으로 업데이트 진행

현재는 `SAFE` 라는 STATUS만 나옴

목표는

`{
    "yearMonth" : "2028-02",
    "monthlyLimit": 10000,
    "totalCost": 5000,
    "usageRate": 50.0000,
    "status": "SAFE"
}
`

이제 사용률을 외부에서도 가져올수 있도록 BudgetPolicy의 기존 private을 public으로 교체
- 왜? 사용률 계산 규칙의 책임은 BudgetPolicy에 있기 떄문
- Controller : HTTP 요청 & 응답
- Service : 예산과 비용 조회 및 흐름 조정
- BudgetPolicy : 사용률 계산과 상태 판정

------------------------------------------

### 예산 상태 상세 조회 검증

- 월 예산: 10,000
- 월 누적 비용: 8,000
- 사용률: 80%
- 예산 상태: `CAUTION`

월 예산을 20,000으로 변경한 후:

- 월 누적 비용: 8,000
- 사용률: 40%
- 예산 상태: `SAFE`

예산 변경값이 DB에 반영되고, 
상세 상태 조회 시 예산,비용,사용률,상태가 함께 재계산되는 전체 흐름을 Postman으로 검증.

![img_8.png](img_8.png)
상세 예산 설정 성공

![img_7.png](img_7.png)
상세 예산 업데이트 설정 성공

# 2026-08-24

현재 예외마다 @ResponseStatus가 있어서 HTTP 상태 코드는 맞지만, 응답 본문은 Spring 기본 형식
이에 따라 다음처럼 일관된 응답으로 업데이트
```json
{
"timestamp": "2026-08-24T14:00:00",
"status": 404,
"code": "MONTHLY_BUDGET_NOT_FOUND",
"message": "해당 연월의 예산이 등록되어 있지 않습니다.",
"path": "/api/budgets/status"
}
```

common << 공통 모듈을 모아놓는 패키지로 설정
common.exception 으로 공통예외를 모아놓을 예정

- 예산 예외
- 비용 예외
- 잘못된 요청 파라미터
- AWS 연동 예외
- CloudWatch 연동 예외

----각 변수 설명----

- timestamp : 예외가 응답된 시각
- status : HTTP 상태 코드
- code : 클라이언트가 구분할 애플리케이션 오류 코드
- message : 사람이 확인할 오류 설명
- path : 예외가 발생한 요청 경로

### @RestControllerAdvice는 무엇인가?

- 여러 Controller에서 발생하는 예외를 한곳에서 공통 처리하도록 등록하는 어노테이션
- @ControllerAdvice + @ResponseBody 와 같음
- @ControllerAdvice : 모든 Controller에서 발생한 예외 감시
- @ResponseBody : 반환한 ErrorResponse를 JSON으로 변환

- BudgetController 실행
  → BudgetService에서 MonthlyBudgetNotFoundException 발생
  → Controller 밖으로 예외 전달
  → @RestControllerAdvice가 예외 감지
  → 해당 @ExceptionHandler 메서드 실행
  → ErrorResponse를 JSON으로 반환

- @ExceptionHandler는 어떤 예외를 처리할지 지정

```java
@ExceptionHandler(MonthlyBudgetNotFoundException.class)
```
`MonthlyBudgetNotFoundException`이 발생하면 바로 Handler밑의 메소드가 처리

클래스에 `Global`를 붙인 이유는?
- 특정 Controller 전용이 아닌 애플리케이션 전체 Controller에 적용할 예외 처리 클래스란 뜻
전역 동작을 만드는건 `@RestControllerAdvice` 덕분

----------------------------------

### POSTMAN 테스트 진행
1. 미등록 예산 조회 - 404
![img_9.png](img_9.png)
2. 중복 예산 등록 -409
![img_10.png](img_10.png)
3. 잘못된 예산 변경 - 400
![img_11.png](img_11.png)


### `@Valid` 검증 오류 메시지 추출 과정 <새로알게된 개념>

요청 DTO에 `@NotNull`, `@Positive` 등의 검증 규칙을 선언하고 Controller의 `@RequestBody` 앞에 `@Valid`를 적용했다.

잘못된 요청이 들어오면 Controller와 Service를 실행하기 전에 DTO 검증이 수행되고, 검증 실패 시 `MethodArgumentNotValidException`이 발생한다.

```text
JSON 요청
→ Jackson이 요청 DTO 생성
→ @Valid가 DTO 검증
→ 검증 실패
→ MethodArgumentNotValidException 발생
→ GlobalExceptionHandler가 공통 오류 응답 생성
```

검증 오류 메시지는 다음 코드로 추출한다.

```java
String message = exception.getBindingResult()
        .getFieldErrors()
        .stream()
        .findFirst()
        .map(DefaultMessageSourceResolvable::getDefaultMessage)
        .orElse("잘못된 요청입니다.");
```

각 메서드의 역할은 다음과 같다.

* `getBindingResult()`: `@Valid`가 수행한 전체 검증 결과를 가져온다.
* `getFieldErrors()`: 전체 검증 결과 중 DTO 필드에서 발생한 오류들을 `List<FieldError>`로 가져온다.
* `stream()`: 필드 오류 목록을 순서대로 처리할 수 있는 Stream으로 변환한다.
* `findFirst()`: 오류 목록 중 첫 번째 오류를 `Optional<FieldError>`로 가져온다.
* `map()`: `FieldError`에서 어노테이션에 작성한 검증 메시지만 추출한다.
* `orElse()`: 검증 메시지를 가져오지 못한 경우 기본 메시지를 반환한다.

다음 메서드 참조는:

```java
DefaultMessageSourceResolvable::getDefaultMessage
```

아래 람다식과 같은 의미다.

```java
fieldError -> fieldError.getDefaultMessage()
```

예를 들어 다음 요청이 들어오면:

```json
{
  "monthlyLimit": 0
}
```

`@Positive` 검증이 실패하고 다음 메시지가 추출된다.

```text
월 예산은 0보다 커야 합니다.
```

최종 처리 흐름은 다음과 같다.

```text
monthlyLimit = 0
→ @Positive 검증 실패
→ FieldError 생성
→ 첫 번째 FieldError 선택
→ 검증 메시지 추출
→ ErrorResponse의 message에 저장
→ 400 Bad Request 반환
```

현재 `ErrorResponse`는 메시지를 하나만 가지므로 여러 필드가 동시에 실패하더라도 첫 번째 오류 메시지만 반환한다. 추후 필요하면 모든 필드 오류를 목록으로 반환하는 구조로 확장할 수 있다.


## POSTMAN에서 테스트 진행

1. 등록 요청에서 연월 누락 케이스

![img_12.png](img_12.png)

2. 변경 요청에서 월 예산 누락 케이스

![img_13.png](img_13.png)

