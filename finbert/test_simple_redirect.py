#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Simple test of Google News redirect resolution
"""

def test_google_redirect():
    """Test Google News redirect resolution"""
    
    print("Test: Google News redirect resolution")
    
    # Sample Google News URL from our database
    google_url = 'https://news.google.com/rss/articles/CBMic0FVX3lxTE8tNHJ1aUlQQWZvamVjQXZRTUdrbUpJQ3F2LXZwTmlsekd0VmNSS0xaQUFTYzlDVzdPb0JBRXV1ck0wQUFqdkJFQjVsMTJGeU1peUxhZElsOGhfRXZSTVR4YlJRZFVxZ0Z6QkRITDlDY3JPRWs?oc=5'
    
    # Test Selenium imports
    try:
        print("1. Testing Selenium imports...")
        from selenium import webdriver
        from selenium.webdriver.chrome.options import Options
        from selenium.webdriver.chrome.service import Service
        from webdriver_manager.chrome import ChromeDriverManager  
        from selenium.webdriver.support.ui import WebDriverWait
        print("OK - Selenium imports successful")
    except ImportError as e:
        print(f"FAIL - Selenium import failed: {e}")
        return False
    
    # Test WebDriver setup
    try:
        print("2. Setting up Selenium WebDriver...")
        
        chrome_options = Options()
        chrome_options.add_argument("--headless")
        chrome_options.add_argument("--no-sandbox")
        chrome_options.add_argument("--disable-dev-shm-usage")

        service = Service(ChromeDriverManager().install())
        driver = webdriver.Chrome(service=service, options=chrome_options)
        
        print("OK - WebDriver setup complete")
        
        # Test Google News redirect resolution
        print("3. Testing Google News redirect...")
        print(f"   Original: {google_url[:60]}...")
        
        driver.get(google_url)
        
        # Wait for redirect (max 10 seconds)
        try:
            WebDriverWait(driver, 10).until(
                lambda d: 'news.google.com' not in d.current_url
            )
        except Exception:
            print("   WARNING - Redirect timeout")

        real_url = driver.current_url
        
        if 'news.google.com' not in real_url:
            print(f"SUCCESS - Redirect worked!")
            print(f"   Real URL: {real_url[:60]}...")
            return True
        else:
            print("FAIL - Still on Google News")
            return False
        
    except Exception as e:
        print(f"FAIL - Test failed: {e}")
        return False
    finally:
        try:
            driver.quit()
        except:
            pass

if __name__ == "__main__":
    success = test_google_redirect()
    print(f"\nTest result: {'PASS' if success else 'FAIL'}")