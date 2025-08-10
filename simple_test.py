#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import requests
import json

def test_claude_api():
    url = "http://localhost:8081/api/claude/summarize"
    
    payload = {
        "newsTitle": "Samsung Q3 Earnings Report",
        "newsContent": "Samsung Electronics reported Q3 consolidated revenue of 79 trillion won. This is a 17% increase compared to the same period last year. The memory semiconductor division showed strength, and the System LSI division also showed improvement."
    }
    
    headers = {
        "Content-Type": "application/json; charset=utf-8",
        "Accept": "application/json"
    }
    
    try:
        print("Testing Claude API endpoint...")
        print("URL:", url)
        
        response = requests.post(url, json=payload, headers=headers, timeout=30)
        
        print("Status Code:", response.status_code)
        
        if response.status_code == 200:
            print("Success!")
            print("Response:", response.text)
        else:
            print("Failed!")
            print("Error Response:", response.text)
            
    except requests.exceptions.ConnectionError:
        print("Connection failed - check if backend is running")
    except Exception as e:
        print("Error:", str(e))

if __name__ == "__main__":
    test_claude_api()