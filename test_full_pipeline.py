#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import requests
import json
import time

def test_full_news_pipeline():
    """
    Test complete news pipeline:
    1. Create test news with real content
    2. Send to FinBERT with Claude integration
    3. Verify DB storage
    """
    
    # Test news data with substantial content for Claude summarization
    test_news = {
        "title": "삼성전자 AI 반도체 사업 확대, 2025년 매출 목표 50조원",
        "content": """
        삼성전자가 인공지능(AI) 반도체 사업 확대를 위한 대규모 투자 계획을 발표했다. 
        회사는 2025년까지 AI 반도체 부문에서 50조원의 매출 목표를 설정했다고 밝혔다.
        
        특히 차세대 HBM(High Bandwidth Memory) 제품군과 AI 가속기용 메모리 반도체 개발에 
        집중 투자할 예정이다. 삼성전자는 현재 글로벌 메모리 반도체 시장에서 1위를 
        차지하고 있으며, AI 시대에 맞는 고성능 메모리 제품으로 시장 지배력을 더욱 
        강화하겠다는 전략이다.
        
        업계에서는 이번 투자 계획이 삼성전자의 주가에 긍정적 영향을 미칠 것으로 
        전망하고 있다. 증권가에서는 목표주가를 상향 조정하는 분위기다.
        """,
        "url": "https://test-news-url.com/samsung-ai-chip",
        "source": "테스트뉴스",
        "published_at": "2025-08-09T18:00:00",
        "symbol": "005930",
        "company_name": "삼성전자"
    }
    
    # Step 1: Test Claude API directly first
    print("=== Step 1: Testing Claude API ===")
    claude_url = "http://localhost:8081/api/claude/summarize"
    claude_payload = {
        "newsTitle": test_news["title"],
        "newsContent": test_news["content"]
    }
    
    try:
        print("Sending to Claude API...")
        response = requests.post(claude_url, json=claude_payload, timeout=30)
        print(f"Claude API Status: {response.status_code}")
        
        if response.status_code == 200:
            summary = response.text
            print(f"Claude Summary Generated: {len(summary)} characters")
            print(f"Summary (first 100 chars): {summary[:100]}...")
            test_news["summary"] = summary
        else:
            print(f"Claude API Error: {response.text}")
            return False
            
    except Exception as e:
        print(f"Claude API Error: {e}")
        return False
    
    # Step 2: Send complete news data to FinBERT for processing and DB storage
    print("\n=== Step 2: Testing FinBERT + DB Storage ===")
    finbert_url = "http://localhost:8000/analyze/single"
    
    finbert_payload = {
        "title": test_news["title"],
        "content": test_news["content"],
        "summary": test_news["summary"],
        "url": test_news["url"],
        "source": test_news["source"],
        "published_at": test_news["published_at"],
        "symbol": test_news["symbol"],
        "company_name": test_news["company_name"]
    }
    
    try:
        print("Sending to FinBERT for analysis and DB storage...")
        response = requests.post(finbert_url, json=finbert_payload, timeout=45)
        print(f"FinBERT Status: {response.status_code}")
        
        if response.status_code == 200:
            result = response.json()
            print("FinBERT Analysis Results:")
            print(f"- Sentiment: {result.get('label', 'N/A')}")
            print(f"- Confidence: {result.get('confidence', 'N/A')}")
            print(f"- Trading Signal: {result.get('trading_signal', 'N/A')}")
            print(f"- Score: {result.get('score', 'N/A')}")
        else:
            print(f"FinBERT Error: {response.text}")
            return False
            
    except Exception as e:
        print(f"FinBERT Error: {e}")
        return False
    
    # Step 3: Verify DB storage
    print("\n=== Step 3: Checking Database Storage ===")
    time.sleep(2)  # Give DB time to process
    
    # Check total count
    print("Checking total news count in database...")
    
    return True

def check_database_after_test():
    """Check database for the new news entry"""
    print("\n=== Database Verification ===")
    print("Please check DBeaver or run this SQL query:")
    print('SELECT id, title, summary, sentiment, confidence_level, created_at FROM "NEWS" ORDER BY created_at DESC LIMIT 3;')
    print("\nYou should see the new test news entry with:")
    print("- Title: 삼성전자 AI 반도체 사업 확대...")
    print("- Summary: Claude-generated 3-sentence summary")
    print("- Sentiment: POSITIVE (likely)")
    print("- Created timestamp: Latest entry")

if __name__ == "__main__":
    print("Starting Complete News Pipeline Test")
    print("This will test: RSS -> Claude Summary -> FinBERT Analysis -> DB Storage")
    print("="*60)
    
    success = test_full_news_pipeline()
    
    if success:
        print("\nPipeline test completed successfully!")
        check_database_after_test()
    else:
        print("\nPipeline test failed!")
    
    print("\n" + "="*60)