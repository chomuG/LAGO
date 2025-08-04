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
                        publishTestResults testResultsPattern: 'build/test-results/test/*.xml'
                        publishHTML([
                            allowMissing: false,
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
                        # Swagger UI 접근 테스트
                        curl -f http://localhost:8080/swagger-ui/index.html || echo "Swagger UI not accessible"
                        
                        # API 엔드포인트 테스트
                        curl -f http://localhost:8080/api/ai-bots/1/account || echo "API endpoint test failed"
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
            // 성공 알림 (Slack, Email 등)
            slackSend(
                channel: '#lago-deployment',
                color: 'good',
                message: "✅ LAGO Backend deployment successful! Build #${BUILD_NUMBER}"
            )
        }
        failure {
            echo 'Deployment failed!'
            // 실패 알림
            slackSend(
                channel: '#lago-deployment',
                color: 'danger',
                message: "❌ LAGO Backend deployment failed! Build #${BUILD_NUMBER}"
            )
        }
    }
}
