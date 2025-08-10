#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Google RSS 크롤러 수정사항 테스트
"""

import logging
from google_rss_crawler import GoogleRSSCrawler

# 로깅 설정
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

def test_realtime_news():
    """실시간 뉴스 수집 테스트"""
    print("=== Google RSS 실시간 뉴스 수집 테스트 ===")
    
    crawler = GoogleRSSCrawler()
    
    # 실시간 뉴스 수집 (소량 테스트)
    news_items = crawler.get_realtime_news(10)
    
    print(f"\n수집된 뉴스: {len(news_items)}개")
    
    for i, news in enumerate(news_items[:3], 1):
        print(f"\n[{i}] {news.title}")
        print(f"    언론사: {news.source}")
        print(f"    URL: {news.url}")
        print(f"    발행시간: {news.published_at}")

def test_single_rss_feed():
    """단일 RSS 피드 테스트"""
    print("\n=== 단일 RSS 피드 테스트 ===")
    
    crawler = GoogleRSSCrawler()
    
    # 비즈니스 RSS 피드 테스트
    business_url = f"{crawler.BASE_URL}/headlines/section/topic/BUSINESS?{crawler.KR_PARAMS}"
    print(f"테스트 URL: {business_url}")
    
    news_items = crawler._fetch_rss_feed(business_url, 'realtime')
    
    print(f"수집된 뉴스: {len(news_items)}개")
    
    if news_items:
        for i, news in enumerate(news_items[:3], 1):
            print(f"\n[{i}] {news.title}")
            print(f"    언론사: {news.source}")
            print(f"    URL: {news.url}")

if __name__ == "__main__":
    try:
        test_single_rss_feed()
        test_realtime_news()
    except Exception as e:
        logger.error(f"테스트 실행 중 오류: {e}")
        import traceback
        traceback.print_exc()