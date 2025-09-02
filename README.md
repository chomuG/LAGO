<div align="center">

# 📈 라고할때 - 모의투자 학습 서비스
주식투자 초보자를 위한 모의투자 학습 어플리케이션

![슬라이드1.JPG](attachment:56f77bd2-0a32-4515-aa7f-2d2165824fd8:슬라이드1.jpg)

</div>


# ✍️ 개요
- **서비스명**: 라고할때
- **프로젝트 기간:** 2025. 07.07 ~ 2025.08.18
- **SSAFY 13기 공통 프로젝트**
- **목표:** 주식 투자 초보자를 위해 알기 쉬운 용어로 직관적이게 표현한 학습 기능과 실제 주가 정보를 연동한 모의 투자 기능으로 실전 경험을 쌓을 수 있는 어플리케이션

[▶️ 영상 포트폴리오 보러가기](https://youtu.be/wsXnRQZ3P1M)

## ✍️ 프로젝트 소개

## 📽️ 주요 기능 시연

## ✨기술적 특징
### 1. 웹소켓 구독 기능을 사용한 부하 방지

![슬라이드12.JPG](attachment:e8f8c8f8-8528-4a33-ac78-fb84bef1a7e4:슬라이드12.jpg)

- 한국 투자 증권 API를 이용해 실제 주가 정보를 연동하고 웹소켓을 통해 사용자 화면에 지연 없이 전달 → 한 번에 많은 양의 데이터를 실시간으로 전달하게 되면 네트워크와 렌더링에 부하 발생
- 웹소켓 구독 기능을 활용해 보이는 종목은 구독하고 스크롤 되어 화면 밖으로 사라진 종목은 구독 해제하는 방식으로 렌더링 최적화 → 자원을 효율적으로 사용하고 매끄러운 사용 흐름 제공

### 2. PostgreSQL + TimescaleDB 활용

![슬라이드20.JPG](attachment:888541c5-b839-4fb9-a87b-cacdbab80089:슬라이드20.jpg)

- 소규모(18만건) 부하 테스트
    - MySQL Insert : 26분
    - PosetgreSQL : 7초
    - 생산성 + 성능 향상

### 3. 차트 패턴 분석

- 단순 가격 비교가 아닌 선형회귀와 추세선이라는 통계적 지표를 확인하여 추세선을 그리고 기울기와 결정 계수를 활용해 차트 패턴을 탐지 → 사용자가 상승/하락 추세를 직관적으로 파악할 수 있음

### 4. 데일리 퀴즈

- 매일 랜덤한 시간에 데일리 퀴즈 알람을 사용자에게 전송

### 5. 뉴스

- 국내 금융 특화 모델인 KR-FinBERT를 사용해 호재악재 분석
- 로컬에서는 잘 실행 됐지만 서버로 통합하면서 타임아웃 + 성능 저하 문제 (여러 서버 동시 실행으로 인한 과도한 리소스 사용) → 도커 컨테이너별로 메모리에 제한을 두고 서버를 분리 하여 최적화
- 서버 메모리 사용량 개선
    - 스프링 : 8.1GB → 3.4GB
    - 시스템 부하 : 47% → 21%

### 6. 성향별 매매봇

- 매매로직
    - 뉴스 : 구글 RSS + Selenium + Finbert
        - 호재/악재 점수 산출 → 점수 정규화
    - 기술적 분석
        - RSI, MACD, 볼린저밴드, 이동평균선, 골든/데드 크로스
- 뉴스 감정 점수 + 기술적 분석 점수 + 실시간 가격 변동으로 종합 매매 신호를 생성하고 캐릭터 별 다른 가중치를 두어 매매 신호 생성

# ⚙️ 기술 스택


<!-- LAGO 기술 스택 (핵심 기술만) -->
<!-- LAGO 기술 스택 -->
<table>
  <tr>
    <th>분류</th>
    <th>기술 스택</th>
  </tr>

  <!-- 모바일(Android) -->
  <tr>
    <td><b>모바일(Android)</b></td>
    <td>
      <img src="https://img.shields.io/badge/Android%20Studio-3DDC84?style=flat&logo=androidstudio&logoColor=white"/>
      <img src="https://img.shields.io/badge/Kotlin-0095D5?style=flat&logo=kotlin&logoColor=white"/>
      <img src="https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat&logo=jetpackcompose&logoColor=white"/>
      <img src="https://img.shields.io/badge/Dagger%20Hilt-5A29E4?style=flat&logo=dagger&logoColor=white"/>
      <img src="https://img.shields.io/badge/Room-4285F4?style=flat&logo=android&logoColor=white"/>
      <img src="https://img.shields.io/badge/Coroutines-1E90FF?style=flat&logo=kotlin&logoColor=white"/>
      <img src="https://img.shields.io/badge/TradingView%20Charting%20Library-2962FF?style=flat&logo=tradingview&logoColor=white"/>
    </td>
  </tr>

  <!-- 백엔드(Spring) -->
  <tr>
    <td><b>백엔드</b></td>
    <td>
      <img src="https://img.shields.io/badge/Java%2021-ED8B00?style=flat&logo=openjdk&logoColor=white"/>
      <img src="https://img.shields.io/badge/Spring%20Boot-6DB33F?style=flat&logo=springboot&logoColor=white"/>
      <img src="https://img.shields.io/badge/Spring%20Security-6DB33F?style=flat&logo=springsecurity&logoColor=white"/>
      <img src="https://img.shields.io/badge/WebFlux-6DB33F?style=flat"/>
      <img src="https://img.shields.io/badge/Spring%20Data%20JPA-6DB33F?style=flat"/>
      <img src="https://img.shields.io/badge/OAuth2-000000?style=flat"/>
      <img src="https://img.shields.io/badge/JWT-000000?style=flat&logo=jsonwebtokens&logoColor=white"/>
    </td>
  </tr>

  <!-- 네트워크 & API -->
  <tr>
    <td><b>네트워크·API</b></td>
    <td>
      <img src="https://img.shields.io/badge/한국투자증권%20API-003F7C?style=flat"/>
      <img src="https://img.shields.io/badge/Retrofit-3A3A3A?style=flat"/>
      <img src="https://img.shields.io/badge/OkHttp-3A3A3A?style=flat"/>
      <img src="https://img.shields.io/badge/WebSocket-555?style=flat"/>
      <img src="https://img.shields.io/badge/OpenAPI%203-85EA2D?style=flat&logo=swagger&logoColor=black"/>
    </td>
  </tr>

  <!-- 데이터베이스 -->
  <tr>
    <td><b>데이터베이스</b></td>
    <td>
      <img src="https://img.shields.io/badge/PostgreSQL%2014-316192?style=flat&logo=postgresql&logoColor=white"/>
      <img src="https://img.shields.io/badge/TimescaleDB-FDB515?style=flat&logo=timescale&logoColor=white"/>
      <img src="https://img.shields.io/badge/Redis%207.2-DC382D?style=flat&logo=redis&logoColor=white"/>
      <img src="https://img.shields.io/badge/Firebase%20Storage-FFCA28?style=flat&logo=firebase&logoColor=black"/>
    </td>
  </tr>

  <!-- AI/데이터 -->
  <tr>
    <td><b>AI·데이터</b></td>
    <td>
      <img src="https://img.shields.io/badge/Flask-000000?style=flat&logo=flask&logoColor=white"/>
      <img src="https://img.shields.io/badge/OpenAI-412991?style=flat&logo=openai&logoColor=white"/>
      <img src="https://img.shields.io/badge/PyTorch-EE4C2C?style=flat&logo=pytorch&logoColor=white"/>
      <img src="https://img.shields.io/badge/Transformers-FFD21E?style=flat&logo=huggingface&logoColor=black"/>
      <img src="https://img.shields.io/badge/ko--FinBERT-111?style=flat"/>
      <img src="https://img.shields.io/badge/Pandas-150458?style=flat&logo=pandas&logoColor=white"/>
      <img src="https://img.shields.io/badge/Selenium-43B02A?style=flat&logo=selenium&logoColor=white"/>
    </td>
  </tr>

  <!-- 인프라 -->
  <tr>
    <td><b>인프라·DevOps</b></td>
    <td>
      <img src="https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker&logoColor=white"/>
      <img src="https://img.shields.io/badge/Docker%20Compose-2496ED?style=flat&logo=docker&logoColor=white"/>
      <img src="https://img.shields.io/badge/AWS%20EC2-FF9900?style=flat&logo=amazonec2&logoColor=white"/>
      <img src="https://img.shields.io/badge/Ubuntu-E95420?style=flat&logo=ubuntu&logoColor=white"/>
      <img src="https://img.shields.io/badge/Spring%20Actuator-6DB33F?style=flat"/>
    </td>
  </tr>

  <!-- 협업도구 -->
  <tr>
    <td><b>협업도구</b></td>
    <td>
      <img src="https://img.shields.io/badge/GitLab-FC6D26?style=flat&logo=gitlab&logoColor=white"/>
      <img src="https://img.shields.io/badge/Jira-0052CC?style=flat&logo=jira&logoColor=white"/>
      <img src="https://img.shields.io/badge/Notion-000000?style=flat&logo=notion&logoColor=white"/>
      <img src="https://img.shields.io/badge/Mattermost-0058CC?style=flat&logo=mattermost&logoColor=white"/>
    </td>
  </tr>
</table>

# ⚒️ 시스템 아키텍처
![image.png](attachment:a0020bc6-978d-4b8f-98c3-f326462bc699:image.png)

## Android 패키지 구조


## BackEnd 패키지 구조


## 프로젝트 산출물 및 메뉴얼

- 포팅 메뉴얼 (exec 폴더 참고)
- [기능 명세서](https://www.notion.so/237085cabd3480fea27ce09b3c59625c?pvs=21)
- [와이어 프레임](https://www.figma.com/design/utXuqPzMOWMkrvwPFUTlTF/%EB%9D%BC%EA%B3%A0%ED%95%A0%EB%95%8C-%EC%99%80%EC%9D%B4%EC%96%B4%ED%94%84%EB%A0%88%EC%9E%84?node-id=0-1&t=ge4MRh3gd1kZeJVe-1)
- [API 명세서](https://www.notion.so/API-22a085cabd348039bc81edc6ec3eeeec?pvs=21)
- [ERD](https://dbdiagram.io/d/689d9e061d75ee360a90baed)
- [시퀀스 다이어그램](https://www.notion.so/22a085cabd3480678fd8cd9a8e29707a?pvs=21)
- [간트 차트](https://docs.google.com/spreadsheets/d/1wQ9IBqwJeYietFdrB4bGjVusZWbprBePGl_xuRKNgzI/edit?usp=sharing)

## 팀원 소개


<table>
  <tr>
    <th>김수진</th>
    <th>김해민</th>
    <th>박경찬</th>
    <th>박승균</th>
    <th>박준형</th>
    <th>최혜림</th>
  </tr>
  <tr>
    <td><img src="![증명2 추가.jpg](attachment:2133777f-edbf-40a2-987e-2e161886cb85:증명2_추가.jpg)" width="120"/></td>
    <td><img src="이미지URL" width="120"/></td>
    <td><img src="![증명사진.jpg](attachment:6d12fe58-8166-41b7-8f34-60ec6df67905:증명사진.jpg)" width="120"/></td>
    <td><img src="![image.png](attachment:8f5857f5-07fd-4c86-9e3d-5b16c406b554:image.png)" width="120"/></td>
    <td><img src="이미지URL" width="120"/></td>
    <td><img src="이미지URL" width="120"/></td>
  </tr>
  <tr>
    <td>
      - 팀장, PM<br>- 중간/최종 발표<br>- PPT 제작<br>- UI/UX 디자인<br>- 기능 명세서 작성<br>- 간트 차트 작성<br><br>
      <b>[Android]</b><br>- 마이페이지 화면<br>- 포트폴리오 화면<br>- 랭킹 화면<br>- 뉴스 화면<br><br>
      <b>[BE]</b><br>- 계좌 조회 API<br>- 관심 뉴스 API
    </td>
    <td>
      - 캐릭터 디자인<br><br>
      <b>[BE]</b><br>- 실시간 주가 API<br>- 차트 조회 API<br>- 관심 종목 추가 API<br>- Entity 생성
    </td>
    <td>
      - 프로젝트 기획 및 PL <br>- 시퀀스 다이어그램 작성<br><br>
      <b>[Android]</b><br>- 투자 메인 화면<br>- 모의투자 화면<br>- 역사적 챌린지 화면<br><br>
      <b>[BE]</b><br>- 주식 거래 API<br>- 실시간 뉴스 처리 API
    </td>
    <td>
      <b>[Android]</b><br>- 메인화면<br>- 학습 화면<br>- 포트폴리오 화면<br>- 랭킹<br>- FCM 알림<br><br>
      <b>[Backend]</b><br>- 단어장/퀴즈<br>- 소셜 로그인<br><br>
      <b>[AI]</b><br>- 뉴스 감정 분석
    </td>
    <td>
      <b>[Infra]</b><br>- 서버환경 구축<br>- 서버 최적화<br><br>
      <b>[DB]</b><br>- DB 설계 및 최적화<br><br>
      <b>[BE]</b><br>- 성향별 매매봇 API
    </td>
    <td>
      - 문서 작성<br>- API 설계 및 명세서 작성<br><br>
      <b>[BE]</b><br>- 차트 패턴 분석 API<br>- 역사적 챌린지 API
    </td>
  </tr>
</table>







