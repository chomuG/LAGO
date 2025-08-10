#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import requests
import json
import time

def test_claude_integrated_pipeline():
    """
    Test complete pipeline with Claude integration:
    FinBERT collect/realtime -> includes Claude summary -> stores to DB
    """
    
    print("=== Testing Complete Pipeline with Claude Summary ===")
    print("This will:")
    print("1. Call FinBERT /collect/realtime")
    print("2. FinBERT will call Claude API for summarization") 
    print("3. FinBERT will do sentiment analysis")
    print("4. Results should be stored in PostgreSQL")
    print("")
    
    # Get initial news count
    print("Checking initial database state...")
    
    try:
        # Call FinBERT realtime news collection (which includes Claude integration)
        finbert_url = "http://localhost:8000/collect/realtime"
        payload = {"limit": 3}  # Small number for testing
        
        print(f"Sending request to: {finbert_url}")
        print(f"Payload: {payload}")
        
        start_time = time.time()
        response = requests.post(finbert_url, json=payload, timeout=120)
        end_time = time.time()
        
        print(f"Response received in {end_time - start_time:.1f} seconds")
        print(f"Status Code: {response.status_code}")
        
        if response.status_code == 200:
            result = response.json()
            print(f"Success! Collected {result.get('count', 0)} news items")
            
            # Show details of first news item
            news_list = result.get('news', [])
            if news_list:
                first_news = news_list[0]
                print("\n--- First News Item Details ---")
                print(f"Title: {first_news.get('title', 'N/A')}")
                print(f"Summary Lines: {first_news.get('summary_lines', [])}")
                print(f"Sentiment: {first_news.get('label', 'N/A')}")
                print(f"Confidence: {first_news.get('confidence', 'N/A')}")
                print(f"Trading Signal: {first_news.get('trading_signal', 'N/A')}")
                print(f"Published: {first_news.get('published_at', 'N/A')}")
                print(f"Source: {first_news.get('source', 'N/A')}")
                
                # Check if Claude summary was generated
                summary_lines = first_news.get('summary_lines', [])
                if summary_lines and len(summary_lines) > 0:
                    print(f"\nClaude Summary Generated:")
                    for i, line in enumerate(summary_lines, 1):
                        print(f"  {i}. {line}")
                else:
                    print(f"\nNo Claude summary found")
            
            return True
            
        else:
            print(f"Error Response: {response.text}")
            return False
            
    except Exception as e:
        print(f"Request failed: {e}")
        return False

def check_database_results():
    """Check what got stored in the database"""
    print("\n=== Database Check Instructions ===")
    print("Now check your DBeaver with this query:")
    print('SELECT id, title, summary, sentiment, confidence_level, created_at FROM "NEWS" ORDER BY created_at DESC LIMIT 5;')
    print("")
    print("You should see:")
    print("- New entries with recent timestamps")
    print("- 'summary' field populated with Claude-generated summaries") 
    print("- 'sentiment' showing POSITIVE/NEGATIVE/NEUTRAL")
    print("- 'confidence_level' showing HIGH/MEDIUM/LOW")

if __name__ == "__main__":
    print("Starting Claude-Integrated News Pipeline Test")
    print("=" * 60)
    
    success = test_claude_integrated_pipeline()
    
    if success:
        print("\n" + "="*60)
        print("Pipeline test completed successfully!")
        check_database_results()
    else:
        print("\nPipeline test failed!")
        
    print("\n" + "="*60)