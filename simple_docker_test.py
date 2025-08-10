#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
간단한 도커 환경 테스트 스크립트
"""

import requests
import json
import sys
import time

def test_finbert_services():
    """FinBERT 서비스 테스트"""
    print("=== FinBERT 서비스 테스트 ===")
    
    # 1. 헬스체크
    try:
        response = requests.get("http://localhost:8000/health", timeout=10)
        if response.status_code == 200:
            print("OK FinBERT Health Check")
            print(f"   Response: {response.json()}")
        else:
            print("FAIL FinBERT Health Check")
            return False
    except Exception as e:
        print(f"ERROR FinBERT Health Check: {e}")
        return False
    
    # 2. 텍스트 분석 테스트
    try:
        payload = {"text": "삼성전자 주가가 상승했습니다. 실적이 개선되고 있어 투자자들의 관심이 높아지고 있습니다."}
        response = requests.post(
            "http://localhost:8000/analyze-text",
            json=payload,
            timeout=30,
            headers={"Content-Type": "application/json"}
        )
        
        if response.status_code == 200:
            data = response.json()
            print("OK Text Analysis")
            print(f"   Label: {data.get('label', 'N/A')}")
            print(f"   Confidence: {data.get('confidence_level', 'N/A')}")
        else:
            print(f"FAIL Text Analysis: {response.status_code}")
            print(f"   Response: {response.text}")
            return False
    except Exception as e:
        print(f"ERROR Text Analysis: {e}")
        return False
    
    # 3. 실시간 뉴스 수집 테스트  
    try:
        payload = {"limit": 3}
        response = requests.post(
            "http://localhost:8000/collect/realtime",
            json=payload,
            timeout=60,
            headers={"Content-Type": "application/json"}
        )
        
        if response.status_code == 200:
            data = response.json()
            print("OK Realtime News Collection")
            print(f"   News Count: {data.get('count', 0)}")
            return True
        else:
            print(f"FAIL Realtime News Collection: {response.status_code}")
            print(f"   Response: {response.text}")
            return False
    except Exception as e:
        print(f"ERROR Realtime News Collection: {e}")
        return False

def test_container_status():
    """컨테이너 상태 확인"""
    print("\n=== Container Status Check ===")
    
    import subprocess
    try:
        result = subprocess.run(['docker-compose', 'ps'], 
                              capture_output=True, text=True)
        print("Container Status:")
        print(result.stdout)
        return True
    except Exception as e:
        print(f"ERROR Container Status Check: {e}")
        return False

def main():
    """메인 테스트 함수"""
    print("Docker News System Test")
    print("=" * 50)
    
    # 컨테이너 상태 확인
    test_container_status()
    
    # 서비스 준비 대기
    print("\nWaiting for services (5 seconds)...")
    time.sleep(5)
    
    # FinBERT 테스트
    finbert_success = test_finbert_services()
    
    print("\n" + "=" * 50)
    print("Test Results")
    print("=" * 50)
    
    if finbert_success:
        print("SUCCESS: All tests passed!")
        print("FinBERT Service Working:")
        print("   - Health check OK")
        print("   - Text sentiment analysis OK")
        print("   - Realtime news collection OK")
        return True
    else:
        print("WARNING: Some tests failed")
        return False

if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1)