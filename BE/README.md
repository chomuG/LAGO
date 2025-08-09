# LAGO Backend 공통 실행 가이드

팀원 모두 동일한 방식으로 로컬/CI에서 에러 없이 실행하도록 최소 공통 규칙과 절차를 정리합니다.

## 요구사항
- Java 21 (JDK 21)
- Gradle Wrapper 사용 (동봉된 `gradlew`)
- Redis (테스트 시 필요 없음)

## 환경 구성
1) 환경변수 파일 준비
```
cp .env.example .env  # Windows는 수동 복사
```
2) 로컬 개발용 프로필: `application-dev.properties` (H2 메모리) 자동 사용

## 실행
- 로컬 실행 (dev 프로필, H2)
```
./gradlew -p BE bootRun
```
- 빌드
```
./gradlew -p BE clean build
```
- 테스트
```
./gradlew -p BE clean test
```

## 주의사항
- 케이스-민감 파일명: DTO/클래스 파일명은 대문자 시작(`AccountDto.java`).
- Entity/DTO/Repository 명세는 팀 명세서/ERD와 동일하게 유지.
- 테스트에서 Redis 연결은 필요하지 않도록 설정되어 있음(`application-test.properties`).

## 트러블슈팅
- 빌드 실패 시 `BE/build/reports/problems/problems-report.html` 참고
- Windows에서 대소문자 리네임은 `git mv -f` 사용
