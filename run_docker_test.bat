@echo off
echo ================================
echo 도커 뉴스 시스템 테스트 시작
echo ================================
echo.

echo 1. 기존 컨테이너 정리...
docker-compose down

echo.
echo 2. 이미지 새로 빌드...
docker-compose build

echo.
echo 3. 서비스 시작 (백그라운드)...
docker-compose up -d

echo.
echo 4. 서비스 시작 대기 (60초)...
echo    - FinBERT 모델 로딩 시간 고려
echo    - PostgreSQL 초기화 시간 고려
timeout /t 60

echo.
echo 5. 컨테이너 상태 확인...
docker-compose ps

echo.
echo 6. FinBERT 헬스체크...
curl -f http://localhost:8000/health
echo.

echo.
echo 7. Spring Boot 헬스체크...  
curl -f http://localhost:8081/actuator/health
echo.

echo.
echo 8. 통합 테스트 실행...
python test_docker_system.py

echo.
echo ================================
echo 테스트 완료!
echo ================================
echo.
echo 컨테이너 로그 확인:
echo   docker-compose logs finbert
echo   docker-compose logs backend
echo.
echo 컨테이너 중지:
echo   docker-compose down
echo.
pause