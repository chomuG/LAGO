package com.lago.app.util

object Constants {
    
    // API Constants - 로컬 개발 서버 설정
//    const val BASE_URL = "http://192.168.219.134:8081/"  // Android 에뮬레이터용 localhost
     const val BASE_URL = "http://i13d203.p.ssafy.io:8081/"  // EC2 운영 서버
    // const val BASE_URL = "http://211.107.153.145:8081/"  // 실제 디바이스용 (사용자 IP)
    
    // 차트용 WebSocket
//    const val WS_BASE_URL = "ws://10.0.2.2:8081/ws/chart"  // 에뮬레이터용 localhost
     const val WS_BASE_URL = "ws://i13d203.p.ssafy.io:8081/ws/chart"  // EC2 운영 서버
    // const val WS_BASE_URL = "ws://211.107.153.145:8081/ws/chart"  // 실제 디바이스용
    
    // 실시간 주식 데이터용 WebSocket  
//    const val WS_STOCK_URL = "ws://10.0.2.2:8081/ws-stock/websocket"  // 로컬 서버
     const val WS_STOCK_URL = "ws://i13d203.p.ssafy.io:8081/ws-stock/websocket"  // EC2 운영 서버
    // const val WS_STOCK_URL = "ws://211.107.153.145:8081/ws-stock/websocket"  // 실제 디바이스용
    const val API_VERSION = "v1"
    const val TIMEOUT_SECONDS = 30L
    
    // Shared Preferences Keys
    const val PREF_NAME = "lago_preferences"
    const val KEY_ACCESS_TOKEN = "access_token"
    const val KEY_REFRESH_TOKEN = "refresh_token"
    const val KEY_USER_ID = "user_id"
    const val KEY_IS_LOGGED_IN = "is_logged_in"
    
    // Database Constants
    const val DATABASE_NAME = "lago_database"
    const val DATABASE_VERSION = 1
    
    // Navigation Constants
    const val DEEP_LINK_SCHEME = "lago"
    const val DEEP_LINK_HOST = "app"
    
    // UI Constants
    const val ANIMATION_DURATION = 300
    const val DEBOUNCE_TIME = 500L


    // Business Logic Constants
    const val MIN_PASSWORD_LENGTH = 8
    const val MAX_RETRY_ATTEMPTS = 3
    const val CACHE_EXPIRY_HOURS = 24
}