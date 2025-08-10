#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
뉴스 시스템 빠른 테스트
"""

import requests
import json

def test_finbert_simple():
    """FinBERT 간단 테스트"""
    print("🔍 FinBERT 테스트...")
    
    try:
        # 헬스 체크
        health = requests.get("http://localhost:8000/health", timeout=5)
        if health.status_code == 200:
            print("✅ FinBERT 서버 실행 중")
        else:
            print("❌ FinBERT 서버 미실행")
            return False
            
        # 간단한 뉴스 분석 테스트
        test_url = "https://n.news.naver.com/mnews/article/022/0003872341"
        response = requests.post(
            "http://localhost:8000/analyze",
            json={"url": test_url},
            timeout=30
        )
        
        if response.status_code == 200:
            data = response.json()
            print(f"✅ 제목: {data['title'][:50]}...")
            print(f"✅ 감성: {data['label']} ({data['confidence_level']})")
            print(f"✅ 이미지: {len(data['images'])}개")
            return True
        else:
            print(f"❌ 분석 실패: {response.status_code}")
            print(response.text)
            return False
            
    except Exception as e:
        print(f"❌ FinBERT 테스트 실패: {e}")
        return False

def test_spring_boot_simple():
    """Spring Boot 간단 테스트"""
    print("\n🔍 Spring Boot 테스트...")
    
    try:
        # 헬스 체크
        health = requests.get("http://localhost:8080/actuator/health", timeout=5)
        if health.status_code == 200:
            print("✅ Spring Boot 서버 실행 중")
            return True
        else:
            print("❌ Spring Boot 서버 미실행")
            return False
            
    except Exception as e:
        print(f"❌ Spring Boot 테스트 실패: {e}")
        return False

if __name__ == "__main__":
    print("🚀 뉴스 시스템 빠른 테스트\n")
    
    finbert_ok = test_finbert_simple()
    spring_ok = test_spring_boot_simple()
    
    print(f"\n📊 결과:")
    print(f"FinBERT: {'✅' if finbert_ok else '❌'}")
    print(f"Spring Boot: {'✅' if spring_ok else '❌'}")
    
    if finbert_ok and spring_ok:
        print("\n🎉 모든 서버 정상! 뉴스 스케줄러가 20분마다 자동 실행됩니다.")
        print("💡 실제 뉴스 수집을 보려면 로그를 확인하세요:")
        print("   docker-compose logs -f spring-api")
    else:
        print("\n⚠️ 일부 서버가 실행되지 않았습니다.")
        print("💡 먼저 서버를 시작하세요:")
        if not spring_ok:
            print("   cd BE && ./gradlew bootRun")
        if not finbert_ok:
            print("   cd finbert && python app.py")