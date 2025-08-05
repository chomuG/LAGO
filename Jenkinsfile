pipeline {
    agent any
    
    environment {
        BACKEND_SERVICE = 'backend'
        HEALTH_URL = 'http://localhost:8081/actuator/health'
        SWAGGER_URL = 'http://i13d203.p.ssafy.io:8081/swagger-ui/index.html'
        MATTERMOST_ENDPOINT = 'https://meeting.ssafy.com/hooks/uj7g5ou6wfgzdjb6pt3pcebrfe'
        MATTERMOST_CHANNEL = '#team-carrot'
        BUILD_VERSION = "${BUILD_NUMBER}-${GIT_COMMIT.take(7)}"
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build & Test') {
            parallel {
                stage('Build') {
                    steps {
                        dir('BE') {
                            sh 'chmod +x gradlew'
                            sh './gradlew clean build -x test'
                        }
                    }
                }
                stage('Test') {
                    steps {
                        dir('BE') {
                            sh './gradlew test'
                        }
                    }
                    post {
                        always {
                            dir('BE') {
                                junit testResults: 'build/test-results/test/*.xml', allowEmptyResults: true
                            }
                        }
                    }
                }
            }
        }
        
        stage('Deploy') {
            steps {
                sh '''
                    docker-compose down || true
                    docker-compose build --no-cache backend
                    docker-compose up -d
                    sleep 45
                '''
            }
        }
        
        stage('Health Check') {
            steps {
                script {
                    def healthCheckPassed = false
                    for (int i = 1; i <= 10; i++) {
                        try {
                            sh "curl -f ${HEALTH_URL}"
                            healthCheckPassed = true
                            break
                        } catch (Exception e) {
                            sleep(10)
                        }
                    }
                    
                    if (!healthCheckPassed) {
                        sh 'docker-compose ps'
                        sh 'docker logs spring-backend --tail 30 || true'
                    }
                }
            }
        }
    }
    
    post {
        success {
            script {
                try {
                    mattermostSend(
                        endpoint: env.MATTERMOST_ENDPOINT,
                        channel: env.MATTERMOST_CHANNEL,
                        color: 'good',
                        message: """배포 성공
빌드: #${BUILD_NUMBER}
브랜치: ${env.BRANCH_NAME ?: 'backend-dev'}
Swagger: ${SWAGGER_URL}
AI API: http://i13d203.p.ssafy.io:8081/api/ai-bots"""
                    )
                } catch (Exception e) {
                    echo "Mattermost notification failed: ${e.getMessage()}"
                }
            }
        }
        failure {
            script {
                try {
                    mattermostSend(
                        endpoint: env.MATTERMOST_ENDPOINT,
                        channel: env.MATTERMOST_CHANNEL,
                        color: 'danger',
                        message: """배포 실패
빌드: #${BUILD_NUMBER}
브랜치: ${env.BRANCH_NAME ?: 'backend-dev'}
로그: ${BUILD_URL}console"""
                    )
                } catch (Exception e) {
                    echo "Mattermost notification failed: ${e.getMessage()}"
                }
            }
        }
        always {
            sh 'docker system prune -f --volumes || true'
        }
    }
}
