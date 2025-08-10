#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import requests
import time

def test_spring_news_endpoints():
    """
    Test Spring Boot news collection endpoints 
    to see if they properly save to database
    """
    
    print("=== Testing Spring Boot News Collection ===")
    
    # Get initial count
    print("Checking initial database state...")
    
    # Test Spring Boot realtime news collection
    print("\n1. Testing Spring Boot realtime collection...")
    spring_url = "http://localhost:8081/api/news/collect/realtime"
    
    try:
        response = requests.post(spring_url, timeout=120)
        print(f"Spring Boot Status: {response.status_code}")
        print(f"Response: {response.text}")
        
        if response.status_code == 200:
            print("   ✅ Spring Boot collection completed")
        else:
            print(f"   ❌ Spring Boot collection failed")
            
    except Exception as e:
        print(f"   ❌ Spring Boot error: {e}")
    
    # Wait a bit for processing
    print("\nWaiting 5 seconds for processing...")
    time.sleep(5)
    
    # Check database after
    print("\n2. Now check database count:")
    print("Run this in terminal:")
    print("docker exec timescaledb psql -U ssafyuser -d stock_db -c 'SELECT COUNT(*) FROM \"NEWS\";'")
    print("")
    print("If count is still 9, then Spring Boot is NOT saving to database.")
    print("If count increased, then Spring Boot IS saving to database.")

if __name__ == "__main__":
    test_spring_news_endpoints()