#!/bin/bash

# 라고할때 프로젝트 Jenkins 자동 배포 스크립트
# EC2에서 실행되는 배포 스크립트

echo "🚀 Starting LAGO Backend deployment..."

# 환경 변수 설정
export DOCKER_BUILDKIT=1
export COMPOSE_DOCKER_CLI_BUILD=1

# 프로젝트 디렉토리로 이동
cd /var/jenkins_home/workspace/lago-backend || exit 1

# Git에서 최신 코드 Pull
echo "📦 Pulling latest code from Git..."
git pull origin develop || git pull origin main

# 기존 컨테이너 중지 및 제거
echo "🛑 Stopping existing containers..."
docker-compose down || true

# Docker 이미지 및 컨테이너 정리
echo "🧹 Cleaning up old Docker resources..."
docker system prune -f || true

# Spring Boot 애플리케이션 빌드
echo "🔨 Building Spring Boot application..."
cd BE
chmod +x gradlew
./gradlew clean build -x test

if [ $? -ne 0 ]; then
    echo "❌ Gradle build failed!"
    exit 1
fi

echo "✅ Gradle build successful!"

# Docker Compose로 전체 스택 빌드 및 시작
echo "🐳 Building and starting Docker containers..."
cd ..
docker-compose up -d --build

if [ $? -ne 0 ]; then
    echo "❌ Docker deployment failed!"
    exit 1
fi

# 애플리케이션 시작 대기
echo "⏳ Waiting for application to start..."
sleep 45

# 헬스체크
echo "🏥 Performing health check..."
for i in {1..10}; do
    if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "✅ Application is healthy!"
        break
    else
        echo "⏳ Attempt $i: Application not ready yet..."
        if [ $i -eq 10 ]; then
            echo "❌ Health check failed after 10 attempts!"
            docker-compose logs backend
            exit 1
        fi
        sleep 15
    fi
done

# API 테스트
echo "🧪 Testing API endpoints..."
if curl -f http://localhost:8080/api/ai-bots/1/account > /dev/null 2>&1; then
    echo "✅ API endpoints are working!"
else
    echo "⚠️ API endpoint test failed, but deployment continues..."
fi

# Swagger UI 테스트
echo "📚 Testing Swagger UI..."
if curl -f http://localhost:8080/swagger-ui/index.html > /dev/null 2>&1; then
    echo "✅ Swagger UI is accessible!"
else
    echo "⚠️ Swagger UI test failed, but deployment continues..."
fi

echo "🎉 LAGO Backend deployment completed successfully!"
echo "🌐 Application is running at: http://i13d203.p.ssafy.io:8080"
echo "📚 Swagger UI: http://i13d203.p.ssafy.io:8080/swagger-ui/index.html"
echo "🔍 API Test: http://i13d203.p.ssafy.io:8080/api/ai-bots/1/account"

# 배포 로그 남기기
echo "$(date): LAGO Backend deployment completed successfully" >> /var/log/lago-deployment.log
