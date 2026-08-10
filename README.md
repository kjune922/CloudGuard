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

- Java 21
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

