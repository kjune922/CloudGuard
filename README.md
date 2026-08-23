# CloudGuard

AWS 비용과 장애 정보를 수집하고,  
예산 초과 및 클라우드 장애를 관리하는  
Java/Spring 기반 클라우드 운영 플랫폼입니다.

## 프로젝트 목적

AWS 환경에서는 여러 서비스의 비용과 장애 정보를 각각 확인해야 합니다.

CloudGuard는 AWS 비용과 CloudWatch 경보 정보를 수집하여
사용자가 하나의 서비스에서 비용과 장애 상황을 관리할 수 있도록 하는 것을 목표로 합니다.

## 핵심 기능

### 비용 관리

- 사용자는 AWS 계정을 등록할 수 있다.
- 사용자는 월 예산을 설정할 수 있다.
- 시스템은 AWS 서비스별 일일 비용을 수집한다.
- 시스템은 월 누적 비용을 계산한다.
- 월 누적 비용이 임계치를 넘으면 비용 경고를 생성한다.
- 동일한 조건의 비용 경고는 중복 생성되지 않는다.
- 사용자는 일별 및 서비스별 비용을 조회할 수 있다.

### 장애 관리

- 시스템은 CloudWatch 경보 정보를 수집한다.
- 경보가 발생하면 장애를 생성한다.
- 동일한 경보로 진행 중인 장애를 중복 생성하지 않는다.
- 사용자는 장애 담당자와 처리 상태를 변경할 수 있다.
- 사용자는 장애 원인과 해결 내용을 기록할 수 있다.

## 기술 스택

- Java 17
- Spring Boot
- Spring MVC
- Spring Data JPA
- MySQL
- AWS
- Gradle

## 개발 예정

- [ ] 프로젝트 초기 설정
- [ ] 비용 도메인 설계
- [ ] 예산 정책 구현
- [ ] Mock 비용 데이터 수집
- [ ] AWS Cost Explorer 연동
- [ ] CloudWatch 연동
- [ ] 장애 관리 기능
- [ ] 테스트 코드 작성
- [ ] AWS 배포


# 진행 과정

## 2026-08-08

CostRecord를 순수 Java 객체에서 JPA가 관리하는 엔티티로 바꿈

@Entity는 JPA에게 "이 클래스는 DB 테이블과 연결해서 관리할 객체" 라는 뜻

@Entity가 없으면 JPA는 CostRecord를 저장 대상이라 인식X

@Id
-> DB테이블의 기본키(Primary Key)를 의미

@GeneratedValue는 id를 우리가 직접 넣지 않고 DB가 자동으로 만들어주게 하는 것

## 2026-08-10

- CostRecordRepository는 CostRecord를 DB에 저장하고 다시 꺼내오기위해 만듬
- @Entity만 붙였다고 자동으로 우리가 원하는 데이터를 저장해주는건 아님

### 작동원리

내가 짠 자바 코드 -> CostRecordRepository -> JPA -> MySQL

### 현재 클래스들의 역할

CostRecord
→ 비용 기록이 무엇인지 표현

MonthlyCost
→ 비용을 어떻게 계산할지 표현

CostRecordRepository
→ 비용 기록을 어디에 저장하고 어떻게 가져올지 담당 == CostRecord를 프로그램이 꺼져도 사라지지 않도록 DB에 저장하고
필요할 때 DB에서 다시 조회하기 위함.


### CostService 생성

YearMonth 입력
↓
해당 월 시작일 / 마지막일 계산
↓
Repository에서 해당 기간 CostRecord 조회
↓
MonthlyCost 생성
↓
도메인 로직으로 총 비용 계산
↓
BigDecimal 반환

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
