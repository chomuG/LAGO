#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import requests
import json
import time

def test_claude_api():
    url = "http://localhost:8081/api/claude/summarize"
    
    # Test data with proper Korean encoding
    payload = {
        "newsTitle": "Samsung Q3 Earnings Report",
        "newsContent": "Samsung Electronics reported Q3 consolidated revenue of 79 trillion won, a 17% increase compared to the same period last year. The memory semiconductor division showed strength."
    }
    
    headers = {
        "Content-Type": "application/json",
        "Accept": "application/json"
    }
    
    try:
        print("Testing Claude API endpoint...")
        print(f"URL: {url}")
        print(f"Method: POST")
        print(f"Headers: {headers}")
        print(f"Payload: {json.dumps(payload, indent=2)}")
        
        print("\nSending request...")
        start_time = time.time()
        
        response = requests.post(url, json=payload, headers=headers, timeout=60)
        
        end_time = time.time()
        elapsed = end_time - start_time
        
        print(f"\nResponse received in {elapsed:.2f} seconds")
        print(f"Status Code: {response.status_code}")
        print(f"Response Headers: {dict(response.headers)}")
        
        if response.status_code == 200:
            print("\n=== SUCCESS ===")
            print(f"Summary: {response.text}")
        else:
            print(f"\n=== FAILED ===")
            print(f"Error Response: {response.text}")
            
        # Test with Korean content
        print("\n" + "="*50)
        print("Testing with Korean content...")
        
        korean_payload = {
            "newsTitle": "Samsung Q3 Financial Results",
            "newsContent": "Samsung Electronics announced Q3 consolidated revenue of 79 trillion won, representing a 17% increase year-over-year. The memory semiconductor business showed particularly strong performance."
        }
        
        response2 = requests.post(url, json=korean_payload, headers=headers, timeout=60)
        
        print(f"Korean test - Status: {response2.status_code}")
        if response2.status_code == 200:
            print(f"Korean Summary: {response2.text}")
        else:
            print(f"Korean Error: {response2.text}")
            
    except requests.exceptions.ConnectionError:
        print("Connection failed - check if backend is running")
    except requests.exceptions.Timeout:
        print("Request timed out - Claude API may be slow")
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    test_claude_api()