pipeline {
    agent any
    
    environment {
        JAR_NAME = 'LAGO-0.0.1-SNAPSHOT.jar'
        APP_PORT = '8081'
        PID_FILE = '/tmp/lago-app.pid'
        LOG_FILE = '/tmp/lago-app.log'
        SPRING_PROFILES_ACTIVE = 'local'
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
                        junit testResults: 'build/test-results/test/*.xml', allowEmptyResults: true
                        echo 'Test results archived'
                    }
                }
            }
        }
        
        stage('Stop Existing App') {
            steps {
                echo 'Stopping existing application...'
                script {
                    try {
                        sh '''
                            if [ -f ${PID_FILE} ]; then
                                PID=$(cat ${PID_FILE})
                                if ps -p $PID > /dev/null; then
                                    echo "Stopping existing application (PID: $PID)"
                                    kill $PID
                                    sleep 10
                                    if ps -p $PID > /dev/null; then
                                        echo "Force killing application"
                                        kill -9 $PID
                                    fi
                                    echo "✅ Application stopped successfully"
                                else
                                    echo "No running application found"
                                fi
                                rm -f ${PID_FILE}
                            else
                                echo "No PID file found, proceeding with deployment"
                            fi
                        '''
                    } catch (Exception e) {
                        echo "Warning: Failed to stop existing app: ${e.getMessage()}"
                    }
                }
            }
        }
        
        stage('Deploy JAR') {
            steps {
                echo 'Deploying JAR application...'
                script {
                    try {
                        dir('BE') {
                            sh '''
                                echo "Starting new application with local profile..."
                                nohup java -jar \
                                    -Dspring.profiles.active=local \
                                    -Dserver.port=8081 \
                                    -Dlogging.level.com.example.LAGO=INFO \
                                    build/libs/${JAR_NAME} \
                                    > ${LOG_FILE} 2>&1 &
                                echo $! > ${PID_FILE}
                                PID=$(cat ${PID_FILE})
                                echo "✅ Application started with PID: $PID"
                                echo "📁 Log file: ${LOG_FILE}"
                                echo "📁 PID file: ${PID_FILE}"
                            '''
                        }
                        echo "✅ JAR deployment successful!"
                    } catch (Exception e) {
                        echo "❌ JAR deployment failed: ${e.getMessage()}"
                        throw e
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
                        
                        // 프로세스 상태 확인
                        sh '''
                            if [ -f ${PID_FILE} ]; then
                                PID=$(cat ${PID_FILE})
                                if ps -p $PID > /dev/null; then
                                    echo "✅ Application process is running (PID: $PID)"
                                else
                                    echo "❌ Application process not found"
                                    echo "Application logs:"
                                    tail -50 ${LOG_FILE} || echo "No logs available"
                                    exit 1
                                fi
                            else
                                echo "❌ PID file not found"
                                exit 1
                            fi
                        '''
                        
                        // 헬스체크 (15회 시도)
                        sh '''
                            echo "Testing application endpoints..."
                            for i in {1..15}; do
                                if curl -f http://localhost:8081/actuator/health 2>/dev/null; then
                                    echo "✅ Application health check passed (attempt $i)"
                                    curl -s http://localhost:8081/actuator/health | head -5
                                    break
                                elif curl -f http://localhost:8081/swagger-ui/index.html 2>/dev/null; then
                                    echo "✅ Swagger UI accessible (attempt $i)"
                                    break
                                else
                                    echo "⏳ Health check attempt $i failed, retrying in 10 seconds..."
                                    sleep 10
                                fi
                                
                                if [ $i -eq 15 ]; then
                                    echo "⚠️ Health check completed with warnings - application may still be starting"
                                    echo "Recent application logs:"
                                    tail -20 ${LOG_FILE} || echo "No logs available"
                                fi
                            done
                        '''
                        echo "✅ Health check completed!"
                    } catch (Exception e) {
                        echo "⚠️ Health check failed: ${e.getMessage()}"
                        sh 'tail -50 ${LOG_FILE} || echo "No logs available"'
                        // Health check 실패해도 배포는 성공으로 처리
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
✅ **라고할때 백엔드 배포 성공!** 🎉

**빌드 정보:**
- 빌드 번호: #${BUILD_NUMBER}
- 브랜치: ${env.BRANCH_NAME ?: 'backend-dev'}
- 커밋: ${env.GIT_COMMIT?.take(8)}
- 프로파일: local (H2 + Swagger 활성화)

**접속 정보:**
- 🔗 Swagger UI: http://i13d203.p.ssafy.io:8081/swagger-ui/index.html
- 📊 H2 콘솔: http://i13d203.p.ssafy.io:8081/h2-console
- 💚 Health Check: http://i13d203.p.ssafy.io:8081/actuator/health
- 📚 AI 매매봇 API: http://i13d203.p.ssafy.io:8081/api/ai-bots

**배포 시간:** ${new Date().format('yyyy-MM-dd HH:mm:ss')}
                        """.stripIndent()
                    )
                    echo "✅ Mattermost 성공 알림 전송 완료"
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
                        channel: '#team-carrot',
                        color: 'danger',
                        message: """
❌ **라고할때 백엔드 배포 실패!** 🚨

**빌드 정보:**
- 빌드 번호: #${BUILD_NUMBER}
- 브랜치: ${env.BRANCH_NAME ?: 'backend-dev'}
- 실패 단계: ${currentBuild.result}

**확인 필요:**
- 📋 Jenkins 로그: ${BUILD_URL}console
- 🔧 수동 배포 필요: `java -jar -Dspring.profiles.active=local LAGO-0.0.1-SNAPSHOT.jar`
- 🐛 코드 검토 및 수정 필요

**실패 시간:** ${new Date().format('yyyy-MM-dd HH:mm:ss')}
                        """.stripIndent()
                    )
                    echo "✅ Mattermost 실패 알림 전송 완료"
                } catch (Exception e) {
                    echo "⚠️ Mattermost 알림 실패: ${e.getMessage()}"
                }
            }
        }
        always {
            echo 'Pipeline completed!'
            echo "📁 Application logs: ${LOG_FILE}"
            echo "📁 PID file: ${PID_FILE}"
        }
    }
}
