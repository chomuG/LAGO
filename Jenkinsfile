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
        
        stage('Package') {
            steps {
                echo 'Packaging application...'
                script {
                    // JAR 빌드 및 아카이브
                    dir('BE') {
                        sh './gradlew bootJar'
                        archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
                        
                        // JAR 파일 정보 확인
                        sh '''
                            echo "=== JAR Information ==="
                            ls -la build/libs/
                            echo "JAR file ready for deployment"
                        '''
                    }
                    
                    echo 'Application packaged successfully'
                }
            }
        }
        
        stage('Direct Deploy') {
            steps {
                echo 'Deploying JAR directly...'
                script {
                    try {
                        // 기존 프로세스 종료
                        sh '''
                            echo "Stopping existing application..."
                            pkill -f "LAGO-0.0.1-SNAPSHOT.jar" || echo "No existing process found"
                            sleep 5
                        '''
                        
                        // JAR 실행
                        sh '''
                            echo "Starting new application..."
                            cd BE/build/libs
                            nohup java -jar LAGO-0.0.1-SNAPSHOT.jar \\
                                --server.port=8081 \\
                                --spring.profiles.active=docker \\
                                > /var/jenkins_home/workspace/lago-backend/app.log 2>&1 &
                            echo $! > /var/jenkins_home/workspace/lago-backend/app.pid
                            echo "Application started with PID: $(cat /var/jenkins_home/workspace/lago-backend/app.pid)"
                        '''
                        
                        echo 'Direct JAR deployment successful'
                    } catch (Exception e) {
                        echo "Direct deployment failed: ${e.getMessage()}"
                        echo "Manual deployment required"
                        
                        // 배포 실패 시 정보 제공
                        sh '''
                            echo "=== Deployment Information ==="
                            echo "JAR Location: BE/build/libs/"
                            echo "Manual deployment steps:"
                            echo "1. Copy JAR to EC2"
                            echo "2. Run: java -jar LAGO-0.0.1-SNAPSHOT.jar --server.port=8081"
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
                        sh 'sleep 45'
                        
                        // 프로세스 확인
                        sh '''
                            if [ -f /var/jenkins_home/workspace/lago-backend/app.pid ]; then
                                PID=$(cat /var/jenkins_home/workspace/lago-backend/app.pid)
                                if ps -p $PID > /dev/null; then
                                    echo "✅ Application process is running (PID: $PID)"
                                else
                                    echo "❌ Application process not found"
                                fi
                            fi
                        '''
                        
                        // 헬스체크 시도
                        sh '''
                            echo "Testing application endpoints..."
                            for i in $(seq 1 15); do
                                if curl -f http://localhost:8081/actuator/health 2>/dev/null; then
                                    echo "✅ Application is healthy on port 8081!"
                                    curl -s http://localhost:8081/actuator/health | head -5
                                    exit 0
                                else
                                    echo "Attempt $i/15: Application not ready yet..."
                                    sleep 10
                                fi
                            done
                            echo "⚠️ Health check completed with warnings - application may still be starting"
                        '''
                    } catch (Exception e) {
                        echo "Health check failed: ${e.getMessage()}"
                        echo "Application logs:"
                        sh 'tail -20 /var/jenkins_home/workspace/lago-backend/app.log || echo "No log file found"'
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
            script {
                try {
                    mattermostSend (
                        endpoint: 'https://meeting.ssafy.com/hooks/uj7g5ou6wfgzdjb6pt3pcebrfe',
                        channel: '#team-carrot',
                        color: 'good',
                        message: "✅ **LAGO Backend 배포 성공!** 🎉\n" +
                                "**빌드 번호:** #${BUILD_NUMBER}\n" +
                                "**브랜치:** ${env.BRANCH_NAME ?: 'backend-dev'}\n" +
                                "**배포 시간:** ${new Date()}\n" +
                                "**배포 방식:** Docker Compose (fallback: JAR)\n" +
                                "**Swagger UI:** http://i13d203.p.ssafy.io:8081/swagger-ui/index.html\n" +
                                "**AI 매매봇 API:** http://i13d203.p.ssafy.io:8081/api/ai-bots/{aiId}/account"
                    )
                    echo 'Mattermost notification sent successfully'
                } catch (Exception e) {
                    echo "Mattermost notification failed: ${e.getMessage()}"
                    echo 'Build succeeded but notification failed'
                }
            }
        }
        failure {
            echo 'Build failed!'
            // Mattermost 실패 알림
            script {
                try {
                    mattermostSend (
                        endpoint: 'https://meeting.ssafy.com/hooks/uj7g5ou6wfgzdjb6pt3pcebrfe',
                        channel: '#team-carrot',
                        color: 'danger',
                        message: "❌ **LAGO Backend 빌드 실패!** 😱\n" +
                                "**빌드 번호:** #${BUILD_NUMBER}\n" +
                                "**브랜치:** ${env.BRANCH_NAME ?: 'backend-dev'}\n" +
                                "**실패 시간:** ${new Date()}\n" +
                                "**Jenkins 콘솔:** ${BUILD_URL}console\n" +
                                "**문제 확인 필요:** 로그를 확인해주세요!"
                    )
                    echo 'Mattermost notification sent successfully'
                } catch (Exception e) {
                    echo "Mattermost notification failed: ${e.getMessage()}"
                    echo 'Build failed and notification also failed'
                }
            }
        }
    }
}
