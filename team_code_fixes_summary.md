# 팀원 코드(PR) 병합 후 추가 수정 사항 요약

팀원의 코드를 머지한 이후, 로컬 환경에서 발생하는 몇 가지 크리티컬한 에러와 설정 문제를 방지하기 위해 제가 추가로 코드를 수정한 내역입니다. 팀원 분께 아래 내용을 공유하셔서 다음 개발 시 참고하실 수 있도록 해주세요.

---

## 1. Spring Boot 3.2 파라미터 바인딩 에러 해결 (`@PathVariable`, `@RequestParam`)

### 🔍 문제 원인
팀원 분이 작성하신 `BookCheckController` 및 `BookWearStatusController` 코드에서 아래와 같이 어노테이션의 이름을 생략하여 변수를 매핑하고 있었습니다.
```java
// AS-IS
@GetMapping("/{bookId}/detail")
public ResponseEntity<BookWearStatusDetailResponseDto> getWearStatusDetail(@PathVariable Integer bookId) { ... }
```
Spring Boot 3.2 (Spring Framework 6.1)부터는 컴파일러에 `-parameters` 플래그가 없으면 자바 리플렉션을 통해 파라미터 이름을 추론하지 못합니다. 이로 인해 IDE에서 그냥 실행하면 `IllegalArgumentException`과 함께 **409 / 500 에러**가 발생했습니다.

### ✅ 수정 내역
팀원들이 각자 IDE(IntelliJ 등)의 컴파일러 설정을 수동으로 바꿔야 하는 번거로움을 없애기 위해, **코드 자체에 파라미터 이름을 명시적으로 선언**하도록 전체적으로 수정했습니다.
```java
// TO-BE
@GetMapping("/{bookId}/detail")
public ResponseEntity<BookWearStatusDetailResponseDto> getWearStatusDetail(@PathVariable("bookId") Integer bookId) { ... }
```

---

## 2. JWT 필터 한글 깨짐 현상 해결

### 🔍 문제 원인
유효하지 않은 JWT 토큰을 보냈을 때 필터 단에서 에러 응답을 내려보내는데, 응답 인코딩이 UTF-8로 강제되어 있지 않아서 클라이언트(Postman 등)에서 `??? JWT ?????` 처럼 **한글이 깨지는 현상**이 발생했습니다.

### ✅ 수정 내역
`JwtAuthenticationFilter.java`의 에러 응답 처리 부분에 `charset=UTF-8` 설정을 명시적으로 추가하여, JSON 형태의 예외 메시지가 정상적인 한글로 출력되도록 수정했습니다.
```java
// 수정된 코드
response.setContentType("application/json;charset=UTF-8");
```

---

## 3. DB 스키마 충돌 방지 (`ddl-auto` 및 `SchemaPatcher.java` 제거)

### 🔍 문제 원인
팀원 코드가 병합되는 과정에서 기존 DB의 테이블 구조 및 외래키(FK) 설정과 충돌이 일어났습니다. Hibernate의 `ddl-auto` 속성이 `update`로 되어 있거나, 기존에 작성된 `SchemaPatcher.java` 스크립트가 실행되면서 실제 DB 테이블들을 마음대로 삭제/재생성하거나 관계를 끊어버리는 문제가 있었습니다.

### ✅ 수정 내역
현재 DB에는 이미 올바른 형태의 테이블이 구성되어 있으므로, 어플리케이션 구동 시 DB 구조를 건드리지 않도록 차단했습니다.
1. `application.yml` 파일에서 `spring.jpa.hibernate.ddl-auto` 값을 `none`으로 고정했습니다.
2. 스키마를 강제로 변경하던 `SchemaPatcher.java` 클래스를 완전히 삭제하여 예상치 못한 DB 사이드 이펙트를 차단했습니다.

---

## 4. 상세 점검 내역 엔티티(`BookCheckResultItem`)의 DB 컬럼 매핑 버그 해결

### 🔍 문제 원인
점검 내역 등록 후, 상세 조회 API(`GET /api/checklists/{bookId}/detail`)를 호출하면 `SQLGrammarException: Unknown column 'i1_0.result_item_id' in 'field list'` 에러와 함께 **500 Internal Server Error**가 발생했습니다.
팀원 분이 작성하신 `BookCheckResultItem` 엔티티 코드에서는 Primary Key를 `@Column(name = "result_item_id")`로 매핑해두셨으나, **실제 MySQL 데이터베이스의 `book_check_result_items` 테이블을 확인해 본 결과 PK 컬럼명은 `id`였습니다.**
데이터를 저장(INSERT)할 때는 Auto Increment 때문에 컬럼명이 생략되어 정상적으로 작동하는 것처럼 보였지만, 데이터를 조회(SELECT)할 때는 Hibernate가 존재하지 않는 `result_item_id` 컬럼을 쿼리하여 에러가 발생한 것입니다.

### ✅ 수정 내역
`BookCheckResultItem.java` 파일에서 엔티티의 ID 컬럼 매핑을 실제 DB 구조에 맞게 수정했습니다.
```java
// AS-IS
@Column(name = "result_item_id")
private Long id;

// TO-BE
@Column(name = "id")
private Long id;
```
이 수정을 통해 점검 상세 내역 조회 시 발생하던 500 에러를 완전히 해결했습니다.
