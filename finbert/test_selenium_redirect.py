#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Test Selenium-based Google News redirect resolution
"""

import logging

# Test Google News URL resolution with user's verified approach
def test_google_redirect():
    """Test Google News redirect resolution"""
    
    print("테스트: Google News 리디렉션 해결")
    
    # Sample Google News URL from our database
    google_url = 'https://news.google.com/rss/articles/CBMic0FVX3lxTE8tNHJ1aUlQQWZvamVjQXZRTUdrbUpJQ3F2LXZwTmlsekd0VmNSS0xaQUFTYzlDVzdPb0JBRXV1ck0wQUFqdkJFQjVsMTJGeU1peUxhZElsOGhfRXZSTVR4YlJRZFVxZ0Z6QkRITDlDY3JPRWs?oc=5'
    
    # Test if basic imports work
    try:
        print("1. 기본 라이브러리 가져오기 테스트...")
        import requests
        from bs4 import BeautifulSoup
        import re
        import base64
        print("✓ 기본 라이브러리 확인 완료")
    except ImportError as e:
        print(f"✗ 기본 라이브러리 실패: {e}")
        return False
    
    # Test Selenium imports
    try:
        print("2. Selenium 라이브러리 가져오기 테스트...")
        from selenium import webdriver
        from selenium.webdriver.chrome.options import Options
        from selenium.webdriver.chrome.service import Service
        from webdriver_manager.chrome import ChromeDriverManager  
        from selenium.webdriver.support.ui import WebDriverWait
        print("✓ Selenium 라이브러리 확인 완료")
    except ImportError as e:
        print(f"✗ Selenium 라이브러리 실패: {e}")
        print("  pip install selenium webdriver-manager 로 설치하세요")
        return False
    
    # Test Selenium WebDriver setup
    try:
        print("3. Selenium WebDriver 설정 테스트...")
        
        chrome_options = Options()
        chrome_options.add_argument("--headless")  # 헤드리스 모드
        chrome_options.add_argument("--no-sandbox")
        chrome_options.add_argument("--disable-dev-shm-usage")
        chrome_options.add_argument("--disable-gpu")
        chrome_options.add_argument("--window-size=1920,1080")
        chrome_options.add_argument("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")

        service = Service(ChromeDriverManager().install())
        driver = webdriver.Chrome(service=service, options=chrome_options)
        
        print("✓ Selenium WebDriver 설정 완료")
        
        # Test Google News redirect resolution
        print("4. Google News 리디렉션 해결 테스트...")
        print(f"   원본 URL: {google_url[:80]}...")
        
        driver.get(google_url)
        
        # 리디렉트 대기 (최대 10초)
        try:
            WebDriverWait(driver, 10).until(
                lambda d: 'news.google.com' not in d.current_url
            )
        except Exception:
            print("   ⚠️ 리디렉트 대기 시간 초과")

        real_url = driver.current_url
        
        if 'news.google.com' not in real_url:
            print(f"✓ 리디렉션 성공!")
            print(f"   실제 URL: {real_url[:80]}...")
            
            # Check if it's a valid Korean news portal
            korean_portals = [
                'naver.com', 'daum.net', 'hankyung.com', 'mk.co.kr', 'chosun.com', 
                'donga.com', 'joongang.co.kr', 'hani.co.kr', 'khan.co.kr', 'mt.co.kr',
                'edaily.co.kr', 'fnnews.com', 'news1.kr', 'inews24.com', 'etnews.com',
                'yonhapnews.co.kr', 'yna.co.kr', 'newsis.com', 'pinpointnews.co.kr'
            ]
            
            is_valid = any(portal in real_url.lower() for portal in korean_portals)
            if is_valid:
                print("✓ 유효한 한국 뉴스 포털로 리디렉션됨")
            else:
                print(f"⚠️ 알려지지 않은 포털: {real_url}")
            
        else:
            print("✗ 리디렉션 실패 - 여전히 Google News")
        
        driver.quit()
        print("✓ WebDriver 정리 완료")
        
        return True
        
    except Exception as e:
        print(f"✗ Selenium 테스트 실패: {e}")
        try:
            driver.quit()
        except:
            pass
        return False

if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)
    success = test_google_redirect()
    print(f"\n테스트 결과: {'성공' if success else '실패'}")