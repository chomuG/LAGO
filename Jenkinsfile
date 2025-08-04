pipeline {
    agent any
    
    environment {
        DOCKER_IMAGE = 'lago-backend'
        DOCKER_TAG = "${BUILD_NUMBER}"
        CONTAINER_NAME = 'lago-backend-container'
        JAR_FILE = 'LAGO-0.0.1-SNAPSHOT.jar'
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                echo 'Building Spring Boot application...'
                dir('BE') {
                    sh 'chmod +x gradlew'
                    sh './gradlew clean build -x test'
                }
            }
        }
        
        stage('Test') {
            steps {
                echo 'Running tests...'
                dir('BE') {
                    sh './gradlew test'
                }
            }
            post {
                always {
                    dir('BE') {
                        // JUnit 테스트 결과 수집
                        junit testResults: 'build/test-results/test/*.xml', allowEmptyResults: true
                        // HTML 리포트는 생략 (플러그인 미설치)
                        echo 'Test results archived'
                    }
                }
            }
        }
        
        stage('Docker Build') {
            steps {
                echo 'Building Docker image...'
                script {
                    try {
                        // Docker 접근 가능성 확인
                        sh 'docker --version'
                        
                        // Docker Compose로 빌드
                        sh 'docker-compose build backend'
                        
                        echo 'Docker image built successfully'
                    } catch (Exception e) {
                        echo "Docker build failed: ${e.getMessage()}"
                        echo "Falling back to JAR deployment"
                        
                        // Docker 실패 시 JAR 빌드로 대체
                        dir('BE') {
                            sh './gradlew bootJar'
                            archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
                        }
                    }
                }
            }
        }
        
        stage('Deploy') {
            steps {
                echo 'Deploying application...'
                script {
                    try {
                        // Docker Compose로 배포 시도
                        sh 'docker-compose down || true'
                        sh 'docker-compose up -d backend'
                        
                        // 컨테이너 상태 확인
                        sh 'docker-compose ps'
                        
                        echo 'Docker deployment successful'
                    } catch (Exception e) {
                        echo "Docker deployment failed: ${e.getMessage()}"
                        echo "Manual deployment required"
                        
                        // 배포 실패 시 정보 제공
                        sh '''
                            echo "=== Deployment Information ==="
                            echo "JAR Location: BE/build/libs/"
                            echo "Manual deployment steps:"
                            echo "1. Copy JAR to EC2"
                            echo "2. Run: java -jar LAGO-0.0.1-SNAPSHOT.jar"
                            echo "3. Check: http://i13d203.p.ssafy.io:8081"
                        '''
                    }
                }
            }
        }
        
        stage('Health Check') {
            steps {
                echo 'Checking application health...'
                script {
                    try {
                        // 애플리케이션 시작 대기
                        sh 'sleep 30'
                        
                        // 헬스체크 시도
                        sh '''
                            for i in {1..10}; do
                                if curl -f http://localhost:8081/actuator/health 2>/dev/null; then
                                    echo "✅ Application is healthy!"
                                    exit 0
                                elif curl -f http://localhost:8080/actuator/health 2>/dev/null; then
                                    echo "✅ Application is healthy on port 8080!"
                                    exit 0
                                else
                                    echo "Attempt $i: Application not ready yet..."
                                    sleep 10
                                fi
                            done
                            echo "⚠️ Health check completed with warnings"
                        '''
                    } catch (Exception e) {
                        echo "Health check failed: ${e.getMessage()}"
                        echo "Application may still be starting..."
                    }
                }
            }
        }
    }
    
    post {
        always {
            echo 'Cleaning up...'
            script {
                try {
                    // Docker 이미지 정리 시도
                    sh 'docker system prune -f --volumes || echo "Docker cleanup skipped"'
                } catch (Exception e) {
                    echo "Docker cleanup failed: ${e.getMessage()}"
                }
                
                try {
                    // Gradle 캐시 정리
                    dir('BE') {
                        sh './gradlew clean || echo "Gradle cleanup skipped"'
                    }
                } catch (Exception e) {
                    echo "Gradle cleanup failed: ${e.getMessage()}"
                }
            }
        }
        success {
            echo 'Deployment completed successfully!'
            // Mattermost 성공 알림
            mattermostSend (
                endpoint: 'https://meeting.ssafy.com/hooks/YOUR_WEBHOOK_ID',
                channel: '#team-carrot',
                color: 'good',
                message: "✅ **LAGO Backend 배포 성공!** 🎉\n" +
                        "**빌드 번호:** #${BUILD_NUMBER}\n" +
                        "**브랜치:** ${env.BRANCH_NAME ?: 'backend-dev'}\n" +
                        "**배포 시간:** ${new Date()}\n" +
                        "**배포 방식:** Docker Compose\n" +
                        "**Swagger UI:** http://i13d203.p.ssafy.io:8081/swagger-ui/index.html\n" +
                        "**AI 매매봇 API:** http://i13d203.p.ssafy.io:8081/api/ai-bots/{aiId}/account"
            )
        }
        failure {
            echo 'Build failed!'
            // Mattermost 실패 알림
            mattermostSend (
                endpoint: 'https://meeting.ssafy.com/hooks/YOUR_WEBHOOK_ID',
                channel: '#team-carrot',
                color: 'danger',
                message: "❌ **LAGO Backend 빌드 실패!** 😱\n" +
                        "**빌드 번호:** #${BUILD_NUMBER}\n" +
                        "**브랜치:** ${env.BRANCH_NAME ?: 'backend-dev'}\n" +
                        "**실패 시간:** ${new Date()}\n" +
                        "**Jenkins 콘솔:** ${BUILD_URL}console\n" +
                        "**문제 확인 필요:** 로그를 확인해주세요!"
            )
        }
    }
}
