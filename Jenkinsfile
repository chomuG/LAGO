pipeline {
    agent any
    
    environment {
        DOCKER_IMAGE = 'lago-backend                    # 컨테이너 헬스체크
                    sh '''
                        echo "Waiting for application to start..."
                        sleep 30
                        
                        # 헬스체크 (8081 포트 사용)
                        for i in {1..10}; do
                            if curl -f http://localhost:8081/actuator/health; then
                                echo "Application is healthy!"
                                break
                            else
                                echo "Attempt $i: Application not ready yet..."
                                sleep 10
                            fi
                        done
                    '''R_TAG = "${BUILD_NUMBER}"
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
                        junit testResultsPattern: 'build/test-results/test/*.xml', allowEmptyResults: true
                        // HTML 테스트 리포트 수집
                        publishHTML([
                            allowMissing: true,
                            alwaysLinkToLastBuild: true,
                            keepAll: true,
                            reportDir: 'build/reports/tests/test',
                            reportFiles: 'index.html',
                            reportName: 'Test Report'
                        ])
                    }
                }
            }
        }
        
        stage('Docker Build') {
            steps {
                echo 'Building Docker image...'
                dir('BE') {
                    script {
                        docker.build("${DOCKER_IMAGE}:${DOCKER_TAG}")
                        docker.build("${DOCKER_IMAGE}:latest")
                    }
                }
            }
        }
        
        stage('Stop Previous Container') {
            steps {
                echo 'Stopping and removing previous container...'
                script {
                    try {
                        sh "docker stop ${CONTAINER_NAME} || true"
                        sh "docker rm ${CONTAINER_NAME} || true"
                    } catch (Exception e) {
                        echo "No previous container to stop: ${e.getMessage()}"
                    }
                }
            }
        }
        
        stage('Deploy') {
            steps {
                echo 'Deploying new container...'
                script {
                    // Docker Compose로 전체 스택 재시작
                    sh 'docker-compose down || true'
                    sh 'docker-compose up -d --build'
                    
                    // 컨테이너 헬스체크
                    sh '''
                        echo "Waiting for application to start..."
                        sleep 30
                        
                        # 헬스체크
                        for i in {1..10}; do
                            if curl -f http://localhost:8080/actuator/health; then
                                echo "Application is healthy!"
                                break
                            else
                                echo "Attempt $i: Application not ready yet..."
                                sleep 10
                            fi
                        done
                    '''
                }
            }
        }
        
        stage('Swagger Test') {
            steps {
                echo 'Testing Swagger UI and API endpoints...'
                script {
                    sh '''
                        # Swagger UI 접근 테스트 (8081 포트 사용)
                        curl -f http://localhost:8081/swagger-ui/index.html || echo "Swagger UI not accessible"
                        
                        # API 엔드포인트 테스트 (8081 포트 사용)
                        curl -f http://localhost:8081/api/ai-bots/1/account || echo "API endpoint test failed"
                    '''
                }
            }
        }
    }
    
    post {
        always {
            echo 'Cleaning up...'
            // 오래된 Docker 이미지 정리
            sh 'docker image prune -f || true'
        }
        success {
            echo 'Deployment completed successfully!'
            // Mattermost 성공 알림
            mattermostSend (
                endpoint: 'https://meeting.ssafy.com/hooks/YOUR_WEBHOOK_ID', // Mattermost Webhook URL
                channel: '#lago-deployment', // 알림받을 채널
                color: 'good',
                message: "✅ **LAGO Backend 배포 성공!** 🎉\n" +
                        "**빌드 번호:** #${BUILD_NUMBER}\n" +
                        "**브랜치:** ${BRANCH_NAME}\n" +
                        "**배포 시간:** ${new Date()}\n" +
                        "**Swagger UI:** http://i13d203.p.ssafy.io:8081/swagger-ui/index.html\n" +
                        "**AI 매매봇 API:** http://i13d203.p.ssafy.io:8081/api/ai-bots/{aiId}/account"
            )
        }
        failure {
            echo 'Deployment failed!'
            // Mattermost 실패 알림
            mattermostSend (
                endpoint: 'https://meeting.ssafy.com/hooks/YOUR_WEBHOOK_ID', // Mattermost Webhook URL
                channel: '#lago-deployment', // 알림받을 채널
                color: 'danger',
                message: "❌ **LAGO Backend 배포 실패!** 😱\n" +
                        "**빌드 번호:** #${BUILD_NUMBER}\n" +
                        "**브랜치:** ${BRANCH_NAME}\n" +
                        "**실패 시간:** ${new Date()}\n" +
                        "**Jenkins 콘솔:** ${BUILD_URL}console\n" +
                        "**문제 확인 필요:** 로그를 확인해주세요!"
            )
        }
    }
}
