#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import requests
import json

def create_test_news_with_claude():
    """
    Create a test news entry that goes through:
    1. Claude summarization 
    2. FinBERT sentiment analysis
    3. Spring Boot database storage
    """
    
    print("=== Creating Test News with Full Pipeline ===")
    
    # Step 1: Test news data
    test_news = {
        "title": "삼성전자 AI 반도체 투자 확대, 주가 급등 전망",
        "content": """
        삼성전자가 AI 반도체 시장 선점을 위해 대규모 투자를 발표했습니다.
        회사는 향후 3년간 30조원을 투자하여 차세대 AI 메모리와 프로세서 개발에 
        집중한다고 밝혔습니다. 특히 HBM4와 차세대 GDDR7 메모리 개발에 
        우선순위를 두고 있습니다.
        
        증권가에서는 이번 투자 계획이 삼성전자의 장기적 성장 동력이 될 것으로 
        평가하고 있으며, 목표 주가를 상향 조정하는 분위기입니다.
        AI 시장 확대에 따른 수혜주로 주목받고 있습니다.
        """,
        "url": "https://example.com/samsung-ai-investment",
        "source": "테스트뉴스",
        "stock_code": "005930",
        "stock_name": "삼성전자"
    }
    
    # Step 2: Get Claude summary
    print("Step 1: Getting Claude summary...")
    claude_url = "http://localhost:8081/api/claude/summarize"
    claude_payload = {
        "newsTitle": test_news["title"],
        "newsContent": test_news["content"]
    }
    
    try:
        response = requests.post(claude_url, json=claude_payload, timeout=30)
        if response.status_code == 200:
            claude_summary = response.text
            print(f"Claude summary received: {len(claude_summary)} characters")
            test_news["summary"] = claude_summary
        else:
            print(f"Claude API failed: {response.status_code} - {response.text}")
            return False
    except Exception as e:
        print(f"Claude API error: {e}")
        return False
    
    # Step 3: Get FinBERT sentiment analysis
    print("Step 2: Getting FinBERT sentiment analysis...")
    finbert_url = "http://localhost:8000/analyze/text"
    finbert_payload = {
        "text": test_news["content"]
    }
    
    try:
        response = requests.post(finbert_url, json=finbert_payload, timeout=30)
        if response.status_code == 200:
            finbert_result = response.json()
            print(f"FinBERT analysis: {finbert_result.get('label')} ({finbert_result.get('confidence'):.3f})")
            
            # Add sentiment data to news
            test_news["sentiment"] = finbert_result.get('label')
            test_news["confidence_level"] = finbert_result.get('confidence_level', 'MEDIUM')
            test_news["trading_signal"] = finbert_result.get('trading_signal', 'HOLD')
            
        else:
            print(f"FinBERT API failed: {response.status_code} - {response.text}")
            return False
    except Exception as e:
        print(f"FinBERT API error: {e}")
        return False
    
    # Step 4: Save to database via Spring Boot (we need to create this endpoint)
    print("Step 3: Saving to database...")
    print("Final test news data:")
    print(f"- Title: {test_news['title']}")
    print(f"- Summary: {test_news['summary'][:100]}...")
    print(f"- Sentiment: {test_news['sentiment']}")
    print(f"- Confidence: {test_news['confidence_level']}")
    print(f"- Trading Signal: {test_news['trading_signal']}")
    
    # For now, let's manually insert this into database
    print("\n=== Manual Database Insertion ===")
    print("You can manually run this SQL in DBeaver:")
    
    sql = f'''
INSERT INTO "NEWS" (title, content, summary, sentiment, confidence_level, trading_signal, 
                   url, source, stock_code, stock_name, created_at, updated_at)
VALUES (
    '{test_news["title"]}',
    '{test_news["content"].replace("'", "''")}',
    '{test_news["summary"].replace("'", "''")}',
    '{test_news["sentiment"]}',
    '{test_news["confidence_level"]}',
    '{test_news["trading_signal"]}',
    '{test_news["url"]}',
    '{test_news["source"]}',
    '{test_news["stock_code"]}',
    '{test_news["stock_name"]}',
    NOW(),
    NOW()
);
'''
    
    print(sql)
    
    return True

if __name__ == "__main__":
    success = create_test_news_with_claude()
    
    if success:
        print("\n" + "="*60)
        print("Test news created successfully!")
        print("Run the SQL above in DBeaver to see the complete pipeline result.")
    else:
        print("\nTest failed!")