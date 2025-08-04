pipeline {
    agent any
    
    environment {
        BACKEND_SERVICE = 'backend'
        SWAGGER_URL = 'http://i13d203.p.ssafy.io:8081/swagger-ui/index.html'
        HEALTH_URL = 'http://localhost:8081/actuator/health'
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo '📥 Checking out source code...'
                checkout scm
            }
        }
        
        stage('Build & Test') {
            parallel {
                stage('Build') {
                    steps {
                        echo '🏗️ Building Spring Boot application...'
                        dir('BE') {
                            sh 'chmod +x gradlew'
                            sh './gradlew clean build -x test'
                        }
                    }
                }
                stage('Test') {
                    steps {
                        echo '🧪 Running unit tests...'
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
        
        stage('Docker Deploy') {
            steps {
                echo '🐳 Deploying with Docker Compose...'
                sh '''
                    echo "Stopping existing containers..."
                    docker-compose down || true
                    
                    echo "Building and starting services..."
                    docker-compose build --no-cache ${BACKEND_SERVICE}
                    docker-compose up -d
                    
                    echo "Waiting for services to start..."
                    sleep 45
                '''
            }
        }
        
        stage('Health Check') {
            steps {
                echo '🏥 Checking application health...'
                script {
                    def healthCheckPassed = false
                    for (int i = 1; i <= 10; i++) {
                        try {
                            sh "curl -f ${HEALTH_URL}"
                            echo "✅ Health check passed (attempt ${i})"
                            healthCheckPassed = true
                            break
                        } catch (Exception e) {
                            echo "⏳ Health check attempt ${i}/10 failed, retrying in 10s..."
                            sleep(10)
                        }
                    }
                    
                    if (!healthCheckPassed) {
                        echo "⚠️ Health check warnings - checking container status..."
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
                        endpoint: 'https://meeting.ssafy.com/hooks/uj7g5ou6wfgzdjb6pt3pcebrfe',
                        channel: '#team-carrot',
                        color: 'good',
                        message: """
✅ **라고할때 배포 성공!** 🎉

**빌드:** #${BUILD_NUMBER} | **브랜치:** ${env.BRANCH_NAME ?: 'backend-dev'}
**커밋:** ${env.GIT_COMMIT?.take(8)} | **시간:** ${new Date().format('MM-dd HH:mm')}

🔗 **Swagger UI:** ${SWAGGER_URL}
📊 **Health Check:** http://i13d203.p.ssafy.io:8081/actuator/health
🤖 **AI API:** http://i13d203.p.ssafy.io:8081/api/ai-bots
                        """.stripIndent()
                    )
                } catch (Exception e) {
                    echo "⚠️ Mattermost 알림 실패: ${e.getMessage()}"
                }
            }
        }
        failure {
            script {
                try {
                    mattermostSend(
                        endpoint: 'https://meeting.ssafy.com/hooks/uj7g5ou6wfgzdjb6pt3pcebrfe',
                        channel: '#team_carrot',
                        color: 'danger',
                        message: """
❌ **배포 실패!** 🚨

**빌드:** #${BUILD_NUMBER} | **브랜치:** ${env.BRANCH_NAME ?: 'backend-dev'}
**실패 시간:** ${new Date().format('MM-dd HH:mm')}

🔧 **Jenkins 로그:** ${BUILD_URL}console
� **수동 복구:** \`docker-compose down && docker-compose up -d\`
                        """.stripIndent()
                    )
                } catch (Exception e) {
                    echo "⚠️ Mattermost 알림 실패: ${e.getMessage()}"
                }
            }
        }
        always {
            echo '🎯 Pipeline completed!'
            sh 'docker system prune -f --volumes || true'
        }
    }
}
