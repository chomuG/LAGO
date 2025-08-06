# 라고할때 Copilot Agent용 프로젝트 지침서
Think of Thought으로 상세하게 절차적으로 사고하며 작업
커밋은 항상 한국어로
Java 21의 가상스레드(virtual Thread)기능을 최대한 사용한 코드 작성해줘
vscode extention으로 연동된 EC2 db를 조회해서 항상 테이블 명을 확인하고 작업할 것
주석으로 기능 설명 상세히 할것
---

## 📚 프로젝트 개요 및 역할

- **프로젝트명:** 라고할때 (D203팀)
- **구성:** Spring Boot 백엔드 + MySQL + Redis + Android 앱
- **내 역할:** 6인 팀의 백엔드 담당 (DevOps/Infra/CI/CD/DB 설계/코드 리뷰 포함)
- **이슈 관리:** Jira, **개발툴:** IntelliJ, **배포/인프라:** Docker/Compose, Jenkins, AWS EC2 등

---



> **테이블/컬럼/PK-FK/관계/명명법은 반드시 연동된 db 기반. 변경/생략/단순화 절대 금지**

---

## 🚦 프로젝트 현재 상태/진행사항

- 스켈레톤/DB 설계/JPA/CI/CD/테스트/Swagger/도커 등 직접 세팅 및 코드 작성
- Workbench/JPA 혼용 → ERD와 코드/엔티티 완전 일치 필요
- 기존 코드/변수명/API/DTO/Entity 등 어떤 경우에도 **연동된 EC2 DB와 다른 이름/타입/구조 사용 금지**
- AI 봇: 별도 테이블 없음, is\_ai 컬럼으로 구분. AI 거래/전략은 별도 테이블

---

## 🧩 프로젝트 컨벤션/개발 지침

- **변수명/컬럼/엔티티/DTO/Controller/Service 이름:** DB 기준만 사용 (임의 변경 금지)
- **새 구조/변수 필요 시:** 기존 구조와 연결성·일관성·관계 우선 (기존 구조 재사용)
- **API:** 명세서 기준, URL/메서드/입출력 모두 일치 (파라미터/반환 구조 임의 변경 금지)
- **Validation/Exception:** 모든 입력/출력/관계/필수값/에러 꼼꼼히 처리. Exception/Validation 코드 필수
- **Infra/DevOps:** 도커·젠킨스·.env·테스트/운영 환경 분리, 계층적 코드 구조
- **Swagger/Javadoc:** 모든 API/메서드/엔티티에 상세 주석 작성

---

## 📝 주요 기능 요약/API

- **회원/인증:** 소셜로그인, Firebase, JWT, 프로필/성향/탈퇴 관리 (POST /api/auth/login, /api/auth/join 등)
- **투자/모의투자:** 실시간 종목/매수/매도/내역/관심종목/랭킹/계좌 등 (GET/POST /api/stocks, /api/accounts 등)
- **AI봇:** 전략/거래/판단근거/개인화 (GET/POST /api/ai-bots/… 등)
- **차트/학습/퀴즈:** 차트 실시간 확인, 패턴분석(1일3회), 퀴즈, streak, 용어장 (GET /api/study/chart, /api/study/quiz/daily 등)
- **뉴스/공지:** 실시간/관심종목/역사챌린지/LLM요약/감정/공지 등 (GET /api/news, /api/news/{newsId}, /api/news/interest 등)
- **마이페이지:** 포트폴리오, 랭킹, 프로필/테두리, Recap (GET /api/users/me/portfolio, /api/frames, /api/recaps 등)

> 모든 엔드포인트/입출력/파라미터/반환 타입은 **API 명세서**와 일치해야 하며, 일부라도 임의 변경 불가

---

## 🪜 업무 흐름/상세 컨텍스트

- 로그인 → 성향 분석 → 투자(모의투자/AI/실시간 데이터) → 차트학습/패턴분석/퀴즈 → 포트폴리오/랭킹/테두리 관리
- 모든 업무 흐름은 시퀀스 다이어그램/와이어프레임 및 기능 명세서의 흐름과 **완전히 동일하게 구현/유지**
- 예시 업무흐름, API 흐름 등은 본문/표/리스트로 명확히 기술됨

---

## 🛑 Copilot에서 반드시 지켜야 할 핵심사항

1. **pdf/pptx/csv 파일 자체는 읽지 못함. 본 md의 설명·표·코드블록만 사용.**
2. **모든 변수명/테이블/구조/관계/API/엔드포인트는 반드시 명세서/본문/표 기준.**
3. **임의 변경·생략·단순화·신규 구조 추가 절대 금지.**
4. **코드 자동완성/수정/리팩토링 시 일관성/정합성/명세 일치 최우선.**
5. **추가 작업/테스트/Swagger/Javadoc 등도 본 구조에 맞춰 문서화.**
6. **예외처리/Validation/테스트코드 필수.**
7. **불명확한 부분/애매한 부분은 TODO/WARN/주석/질문 등으로 반드시 명시.**

---

## 📑 API 명세 예시 (일부)

```
GET /api/stocks
POST /api/stocks/buy
GET /api/accounts/{accountId}/transactions
GET /api/ai-bots/{strategyId}/{stockId}
POST /api/ai-bots/{strategyId}/customize
GET /api/study/chart
GET /api/study/chart/{patternId}
GET /api/news
GET /api/news/interest
GET /api/users/me/portfolio
```

- 위 API를 포함한 모든 명세의 엔드포인트/메서드/파라미터/반환타입/DTO 구조 등은 **API 명세서, 표, 본문 설명**을 정확히 따라야 함

---

## 🛠️ 작업해야 할 내용 (2025-08-04 기준)

1. DB-Entity-Repository 일치 검증 및 코드리뷰
2. 차트 학습/패턴 분석, AI 매매봇, 퀴즈 등 API 구현/테스트
3. 실시간 데이터/뉴스/알림 등 소켓/스케줄러 처리
4. 코드 일관성/예외처리/Swagger/Javadoc 강화
5. 테스트코드/Mock 데이터/QA
6. DevOps: 도커/젠킨스 기반 CI/CD, 환경변수/운영-테스트 분리

---

## 🔗 참고(중요)

- DB/ERD, 시퀀스 다이어그램, 기능 명세서, API 명세, 와이어프레임 등 모든 자료는 이 md파일에 요약 및 표/코드/설명으로 직접 작성됨
- csv, pdf, pptx 등 파일명만 언급하는 코드/주석/표현 불가. 실제 테이블/구조/업무플로우 모두 md 내에 직접 기재함

---

## ✅ Copilot에게 명령/권장사항

- 자동완성/리팩토링/코드설명/추론 시, 위 명세/표/리스트/설명을 절대 기준으로 삼고, 변경·생략 없이 최대한 구체적이고 명확하게 결과 생성
- 불명확하거나, 신규구조/변수/필요시에는 반드시 TODO, WARN, 주석, 질문 등으로 표기
- 모든 코드/로직/테스트/문서화는 명세/표/컨벤션/관계의 일관성을 최우선으로

---

**(지침 업데이트 및 피드백 필요시 이 문서에 직접 보완, 수정할 것)**\
**최종 업데이트: 2025-08-04**

---

> Copilot은 이 .md 파일 내용만을 사용하여 코드 자동완성/설명/리팩토링/테스트/문서화 작업을 하라!

