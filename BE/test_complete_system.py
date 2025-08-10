#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
완전 개선된 뉴스 시스템 테스트
Google RSS + 순서보장 블록파싱 + FinBERT + 3줄요약
"""

import requests
import json
import time
from datetime import datetime, timedelta

class CompleteNewsSystemTester:
    def __init__(self):
        self.finbert_url = "http://localhost:8000"
        self.spring_boot_url = "http://localhost:8080"
    
    def test_google_rss_crawler(self):
        """Google RSS 크롤러 테스트"""
        print("🔍 1. Google RSS 크롤러 테스트...")
        
        try:
            # FinBERT 서버에 직접 Python 모듈 테스트
            import sys
            import os
            sys.path.append(os.path.join(os.path.dirname(__file__), '..', 'finbert'))
            
            from google_rss_crawler import GoogleRSSCrawler
            
            crawler = GoogleRSSCrawler()
            
            # 1. 실시간 뉴스 테스트
            print("   실시간 뉴스 수집...")
            realtime_news = crawler.get_realtime_news(5)
            print(f"   ✅ 실시간 뉴스 {len(realtime_news)}개 수집")
            
            if realtime_news:
                news = realtime_news[0]
                print(f"      - 제목: {news.title[:40]}...")
                print(f"      - 소스: {news.source}")
                print(f"      - 카테고리: {news.category}")
            
            # 2. 관심종목 뉴스 테스트
            print("   관심종목 뉴스 수집...")
            watchlist_news = crawler.get_watchlist_news("005930", "삼성전자", ["삼성", "Samsung"], 3)
            print(f"   ✅ 삼성전자 뉴스 {len(watchlist_news)}개 수집")
            
            # 3. 역사챌린지 뉴스 테스트
            yesterday = (datetime.now() - timedelta(days=1)).strftime('%Y-%m-%d')
            print(f"   역사챌린지 뉴스 수집 ({yesterday})...")
            historical_news = crawler.get_historical_news("005930", "삼성전자", yesterday, ["삼성"], 2)
            print(f"   ✅ {yesterday} 삼성전자 뉴스 {len(historical_news)}개 수집")
            
            return True
            
        except ImportError:
            print("   ⚠️ finbert 모듈을 직접 import할 수 없습니다. API 테스트로 진행...")
            return False
        except Exception as e:
            print(f"   ❌ Google RSS 크롤러 테스트 실패: {e}")
            return False
    
    def test_content_block_parser(self):
        """컨텐츠 블록 파서 테스트"""
        print("\n🔍 2. 컨텐츠 블록 파서 테스트...")
        
        try:
            import sys
            import os
            sys.path.append(os.path.join(os.path.dirname(__file__), '..', 'finbert'))
            
            from content_block_parser import ContentBlockParser
            
            parser = ContentBlockParser()
            
            # 네이버 뉴스 파싱 테스트
            test_url = "https://n.news.naver.com/mnews/article/022/0003872341"
            print(f"   URL 파싱: {test_url}")
            
            result = parser.parse_url(test_url)
            
            print(f"   ✅ 제목: {result.title[:40]}...")
            print(f"   ✅ 총 블록: {len(result.blocks)}개")
            print(f"   ✅ 이미지: {result.total_images}개")
            print(f"   ✅ 파서: {result.parser_used}")
            print(f"   ✅ 신뢰도: {result.confidence}")
            
            # 순서 확인
            text_count = 0
            image_count = 0
            print("   순서 확인:")
            for i, block in enumerate(result.blocks[:5]):
                if block.type == "text":
                    text_count += 1
                    print(f"      {i+1}. [텍스트] {block.content[:30]}...")
                else:
                    image_count += 1
                    print(f"      {i+1}. [이미지] {block.content}")
            
            print(f"   ✅ 텍스트 블록: {text_count}개, 이미지 블록: {image_count}개")
            return True
            
        except ImportError:
            print("   ⚠️ content_block_parser 모듈을 직접 import할 수 없습니다.")
            return False
        except Exception as e:
            print(f"   ❌ 컨텐츠 블록 파서 테스트 실패: {e}")
            return False
    
    def test_finbert_new_apis(self):
        """새로운 FinBERT API 테스트"""
        print("\n🔍 3. 새로운 FinBERT API 테스트...")
        
        try:
            # 1. 실시간 뉴스 수집 API 테스트
            print("   실시간 뉴스 수집 API...")
            response = requests.post(
                f"{self.finbert_url}/collect/realtime",
                json={"limit": 3},
                timeout=60
            )
            
            if response.status_code == 200:
                data = response.json()
                print(f"   ✅ 실시간 뉴스 {data['count']}개 수집 성공")
                
                if data['news']:
                    news = data['news'][0]
                    print(f"      제목: {news['title'][:40]}...")
                    print(f"      감성: {news['label']} ({news['confidence_level']})")
                    print(f"      블록수: {len(news['content_blocks'])}개")
                    print(f"      이미지: {news['total_images']}개")
                    print(f"      요약: {len(news['summary_lines'])}줄")
            else:
                print(f"   ❌ 실시간 뉴스 API 실패: {response.status_code}")
                return False
            
            # 2. 관심종목 뉴스 수집 API 테스트
            print("   관심종목 뉴스 수집 API...")
            response = requests.post(
                f"{self.finbert_url}/collect/watchlist",
                json={
                    "symbol": "005930",
                    "company_name": "삼성전자",
                    "aliases": ["삼성", "Samsung Electronics"],
                    "limit": 2
                },
                timeout=60
            )
            
            if response.status_code == 200:
                data = response.json()
                print(f"   ✅ {data['company_name']} 뉴스 {data['count']}개 수집 성공")
            else:
                print(f"   ❌ 관심종목 뉴스 API 실패: {response.status_code}")
            
            # 3. 역사챌린지 뉴스 수집 API 테스트
            print("   역사챌린지 뉴스 수집 API...")
            yesterday = (datetime.now() - timedelta(days=1)).strftime('%Y-%m-%d')
            response = requests.post(
                f"{self.finbert_url}/collect/historical",
                json={
                    "symbol": "005930",
                    "company_name": "삼성전자",
                    "date": yesterday,
                    "aliases": ["삼성"],
                    "limit": 2
                },
                timeout=60
            )
            
            if response.status_code == 200:
                data = response.json()
                print(f"   ✅ {data['target_date']} {data['company_name']} 뉴스 {data['count']}개 수집 성공")
            else:
                print(f"   ❌ 역사챌린지 뉴스 API 실패: {response.status_code}")
            
            return True
            
        except Exception as e:
            print(f"   ❌ 새로운 FinBERT API 테스트 실패: {e}")
            return False
    
    def test_data_structure(self):
        """데이터 구조 검증 테스트"""
        print("\n🔍 4. 데이터 구조 검증...")
        
        try:
            # 실시간 뉴스로 데이터 구조 검증
            response = requests.post(
                f"{self.finbert_url}/collect/realtime",
                json={"limit": 1},
                timeout=30
            )
            
            if response.status_code == 200:
                data = response.json()
                
                if data['news']:
                    news = data['news'][0]
                    
                    # 필수 필드 검증
                    required_fields = [
                        'title', 'url', 'source', 'published_at', 'category',
                        'content_blocks', 'summary_text', 'summary_lines', 
                        'total_images', 'label', 'confidence_level', 'trading_signal'
                    ]
                    
                    missing_fields = [field for field in required_fields if field not in news]
                    
                    if not missing_fields:
                        print("   ✅ 모든 필수 필드 존재")
                    else:
                        print(f"   ❌ 누락된 필드: {missing_fields}")
                        return False
                    
                    # content_blocks 구조 검증
                    if news['content_blocks']:
                        block = news['content_blocks'][0]
                        block_fields = ['type', 'content', 'position', 'confidence']
                        
                        missing_block_fields = [field for field in block_fields if field not in block]
                        
                        if not missing_block_fields:
                            print("   ✅ 컨텐츠 블록 구조 정상")
                        else:
                            print(f"   ❌ 블록 필드 누락: {missing_block_fields}")
                            return False
                        
                        # 순서 검증
                        positions = [b['position'] for b in news['content_blocks']]
                        if positions == sorted(positions):
                            print("   ✅ 블록 순서 정상")
                        else:
                            print("   ❌ 블록 순서 비정상")
                            return False
                    
                    # FinBERT 결과 검증
                    finbert_fields = ['label', 'confidence', 'trading_signal']
                    missing_finbert = [field for field in finbert_fields if field not in news]
                    
                    if not missing_finbert:
                        print("   ✅ FinBERT 분석 결과 정상")
                        print(f"      감성: {news['label']}")
                        print(f"      신뢰도: {news['confidence_level']}")
                        print(f"      거래신호: {news['trading_signal']}")
                    else:
                        print(f"   ❌ FinBERT 필드 누락: {missing_finbert}")
                        return False
                    
                    # 3줄 요약 검증
                    if news['summary_lines'] and len(news['summary_lines']) <= 3:
                        print(f"   ✅ 3줄 요약 정상 ({len(news['summary_lines'])}줄)")
                        for i, line in enumerate(news['summary_lines'], 1):
                            print(f"      {i}. {line[:50]}...")
                    else:
                        print("   ❌ 3줄 요약 비정상")
                        return False
                    
                    return True
                
                else:
                    print("   ❌ 뉴스 데이터가 없음")
                    return False
            else:
                print(f"   ❌ API 호출 실패: {response.status_code}")
                return False
                
        except Exception as e:
            print(f"   ❌ 데이터 구조 검증 실패: {e}")
            return False
    
    def run_complete_test(self):
        """전체 시스템 테스트 실행"""
        print("=" * 60)
        print("🚀 완전 개선된 뉴스 시스템 테스트")
        print("=" * 60)
        
        # FinBERT 서버 상태 확인
        try:
            health = requests.get(f"{self.finbert_url}/health", timeout=5)
            if health.status_code != 200:
                print("❌ FinBERT 서버가 실행되지 않았습니다.")
                print("💡 먼저 서버를 시작하세요: cd finbert && python app.py")
                return False
        except:
            print("❌ FinBERT 서버에 연결할 수 없습니다.")
            return False
        
        results = {
            'google_rss': self.test_google_rss_crawler(),
            'content_parser': self.test_content_block_parser(),
            'new_apis': self.test_finbert_new_apis(),
            'data_structure': self.test_data_structure()
        }
        
        print("\n" + "=" * 60)
        print("📊 완전 개선 시스템 테스트 결과")
        print("=" * 60)
        
        passed = 0
        total = len(results)
        
        for test_name, result in results.items():
            status = "✅ PASS" if result else "❌ FAIL"
            print(f"{status} {test_name}")
            if result:
                passed += 1
        
        print(f"\n🎯 총 {passed}/{total} 테스트 통과 ({passed/total*100:.1f}%)")
        
        if passed == total:
            print("\n🎉 모든 테스트 통과!")
            print("✨ 새로운 뉴스 시스템이 완벽하게 작동합니다:")
            print("   1. Google RSS 기반 3가지 뉴스 수집")
            print("   2. 순서 보장 블록 파싱")
            print("   3. FinBERT 호재/악재 판단")
            print("   4. 3줄 요약 생성")
            print("   5. 구조화된 데이터 저장")
        else:
            print("\n⚠️ 일부 테스트 실패. 시스템을 확인하세요.")
        
        return results


if __name__ == "__main__":
    tester = CompleteNewsSystemTester()
    tester.run_complete_test()