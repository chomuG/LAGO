#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
3단계 뉴스 수집 시스템
1단계: feedparser로 RSS 파싱
2단계: Selenium으로 Google News 리다이렉트 처리
3단계: newspaper로 기사 내용 추출
"""

import logging
import feedparser
import requests
from bs4 import BeautifulSoup
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.chrome.service import Service
from webdriver_manager.chrome import ChromeDriverManager
from selenium.webdriver.support.ui import WebDriverWait
import time
from dataclasses import dataclass
from typing import List, Optional

# newspaper 임포트 확인
try:
    from newspaper import Article

    NEWSPAPER_AVAILABLE = True
    print("✅ newspaper 사용 가능")
except ImportError as e:
    NEWSPAPER_AVAILABLE = False
    print(f"❌ newspaper 사용 불가: {e}")

# 로깅 설정
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)


@dataclass
class NewsItem:
    """뉴스 아이템 데이터 클래스"""
    title: str
    google_news_url: str
    source: str
    published_at: str
    real_url: Optional[str] = None
    content: Optional[str] = None
    authors: Optional[List[str]] = None
    keywords: Optional[List[str]] = None


class Step1_RSSParser:
    """1단계: RSS 피드 파싱"""

    BASE_URL = "https://news.google.com/rss"
    KR_PARAMS = "hl=ko&gl=KR&ceid=KR:ko"

    def parse_rss_feed(self, rss_url: str) -> List[NewsItem]:
        """RSS 피드에서 뉴스 링크들 추출"""
        logger.info(f"📡 1단계: RSS 피드 파싱 - {rss_url}")

        try:
            # feedparser로 RSS 파싱
            feed = feedparser.parse(rss_url)

            if not feed.entries:
                logger.warning("RSS 피드에서 항목을 찾을 수 없음")
                return []

            news_items = []

            for entry in feed.entries:
                try:
                    # RSS에서 기본 정보만 추출 (아직 실제 URL이나 내용은 없음)
                    news_item = NewsItem(
                        title=entry.title,
                        google_news_url=entry.link,  # 이것은 Google News 링크
                        source=getattr(entry, 'source', {}).get('title', 'Unknown'),
                        published_at=getattr(entry, 'published', 'Unknown')
                    )

                    news_items.append(news_item)

                except Exception as e:
                    logger.error(f"RSS 항목 처리 오류: {e}")
                    continue

            logger.info(f"✅ 1단계 완료: {len(news_items)}개 뉴스 링크 수집")
            return news_items

        except Exception as e:
            logger.error(f"RSS 파싱 오류: {e}")
            return []

    def get_realtime_news_links(self, max_items: int = 10) -> List[NewsItem]:
        """실시간 뉴스 링크들 가져오기"""
        main_rss_url = f"{self.BASE_URL}?{self.KR_PARAMS}"
        news_items = self.parse_rss_feed(main_rss_url)
        return news_items[:max_items]

    def get_category_news_links(self, category: str, max_items: int = 10) -> List[NewsItem]:
        """카테고리별 뉴스 링크들 가져오기"""
        category_urls = {
            'business': f"{self.BASE_URL}/headlines/section/topic/BUSINESS?{self.KR_PARAMS}",
            'technology': f"{self.BASE_URL}/headlines/section/topic/TECHNOLOGY?{self.KR_PARAMS}",
            'politics': f"{self.BASE_URL}/headlines/section/topic/POLITICS?{self.KR_PARAMS}",
            'sports': f"{self.BASE_URL}/headlines/section/topic/SPORTS?{self.KR_PARAMS}"
        }

        if category not in category_urls:
            logger.error(f"지원하지 않는 카테고리: {category}")
            return []

        news_items = self.parse_rss_feed(category_urls[category])
        return news_items[:max_items]


class Step2_URLResolver:
    """2단계: Google News 리다이렉트 처리"""

    def __init__(self):
        self.driver = None
        self.session = requests.Session()
        self.session.headers.update({
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
        })

    def setup_selenium(self):
        """Selenium WebDriver 설정"""
        if self.driver is None:
            try:
                chrome_options = Options()
                chrome_options.add_argument("--headless")
                chrome_options.add_argument("--no-sandbox")
                chrome_options.add_argument("--disable-dev-shm-usage")

                service = Service(ChromeDriverManager().install())
                self.driver = webdriver.Chrome(service=service, options=chrome_options)
                logger.info("✅ Selenium WebDriver 설정 완료")
            except Exception as e:
                logger.error(f"Selenium 설정 오류: {e}")
                self.driver = None

    def cleanup(self):
        """리소스 정리"""
        if self.driver:
            self.driver.quit()
            self.driver = None

    def resolve_google_news_url(self, google_news_url: str) -> str:
        """Google News URL을 실제 언론사 URL로 변환"""
        logger.info(f"🔄 2단계: URL 리다이렉트 처리")

        try:
            # Google News 링크가 아니면 그대로 반환
            if 'news.google.com' not in google_news_url:
                return google_news_url

            # Selenium으로 리다이렉트 처리
            if not self.driver:
                self.setup_selenium()

            if not self.driver:
                logger.error("Selenium 설정 실패")
                return google_news_url

            logger.info(f"Google News 링크 처리: {google_news_url[:80]}...")
            self.driver.get(google_news_url)

            # 리다이렉트 대기 (최대 10초)
            WebDriverWait(self.driver, 10).until(
                lambda driver: 'news.google.com' not in driver.current_url
            )

            real_url = self.driver.current_url

            if 'news.google.com' not in real_url:
                logger.info(f"✅ 실제 URL 발견: {real_url[:80]}...")
                return real_url
            else:
                logger.warning("리다이렉트 실패 - Google News에서 벗어나지 못함")
                return google_news_url

        except Exception as e:
            logger.error(f"URL 리다이렉트 처리 오류: {e}")
            return google_news_url

    def resolve_news_urls(self, news_items: List[NewsItem]) -> List[NewsItem]:
        """뉴스 아이템들의 URL을 실제 링크로 변환"""
        logger.info(f"🔄 2단계: {len(news_items)}개 URL 리다이렉트 처리 시작")

        for i, news_item in enumerate(news_items):
            logger.info(f"URL 처리 중 ({i + 1}/{len(news_items)}): {news_item.title[:50]}...")

            real_url = self.resolve_google_news_url(news_item.google_news_url)
            news_item.real_url = real_url

            time.sleep(1)  # 요청 간격 조절

        logger.info(f"✅ 2단계 완료: {len(news_items)}개 URL 처리 완료")
        return news_items


class Step3_ContentExtractor:
    """3단계: 실제 기사 내용 추출"""

    def __init__(self):
        self.session = requests.Session()
        self.session.headers.update({
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
        })

    def extract_with_newspaper(self, url: str) -> dict:
        """newspaper로 기사 내용 추출"""
        if not NEWSPAPER_AVAILABLE:
            return None

        try:
            logger.info(f"📰 newspaper로 내용 추출: {url[:50]}...")

            article = Article(url, language='ko')
            article.download()
            article.parse()

            # NLP 처리 (키워드 추출)
            try:
                article.nlp()
            except:
                pass

            content = article.text.strip() if article.text else ""

            if len(content) < 100:
                return None

            return {
                'content': content,
                'authors': list(article.authors) if article.authors else [],
                'keywords': list(article.keywords)[:5] if hasattr(article, 'keywords') else []
            }

        except Exception as e:
            logger.error(f"newspaper 추출 오류: {e}")
            return None

    def extract_with_beautifulsoup(self, url: str) -> dict:
        """BeautifulSoup으로 기사 내용 추출 (대안)"""
        try:
            logger.info(f"🍲 BeautifulSoup으로 내용 추출: {url[:50]}...")

            response = self.session.get(url, timeout=10)
            response.raise_for_status()

            soup = BeautifulSoup(response.content, 'html.parser')

            # 본문 추출
            content_selectors = [
                '.article-content', '.entry-content', '.post-content',
                '.content', 'article', '.article-body'
            ]

            content = ""
            for selector in content_selectors:
                content_elem = soup.select_one(selector)
                if content_elem:
                    # 불필요한 태그 제거
                    for tag in content_elem.find_all(['script', 'style', 'nav', 'footer']):
                        tag.decompose()

                    content = content_elem.get_text().strip()
                    if len(content) > 200:
                        break

            # p 태그로 대체
            if len(content) < 200:
                paragraphs = soup.find_all('p')
                content = '\n'.join([p.get_text().strip() for p in paragraphs if len(p.get_text().strip()) > 30])

            return {
                'content': content,
                'authors': [],
                'keywords': []
            }

        except Exception as e:
            logger.error(f"BeautifulSoup 추출 오류: {e}")
            return {
                'content': f"추출 실패: {str(e)}",
                'authors': [],
                'keywords': []
            }

    def extract_article_content(self, news_item: NewsItem) -> NewsItem:
        """뉴스 아이템의 기사 내용 추출"""
        url = news_item.real_url or news_item.google_news_url

        logger.info(f"📄 3단계: 기사 내용 추출 - {news_item.title[:50]}...")

        # newspaper 우선 시도
        if NEWSPAPER_AVAILABLE:
            result = self.extract_with_newspaper(url)
            if result and len(result['content']) > 100:
                news_item.content = result['content'][:2000] + "..." if len(result['content']) > 2000 else result[
                    'content']
                news_item.authors = result['authors']
                news_item.keywords = result['keywords']
                return news_item

        # BeautifulSoup으로 대체
        result = self.extract_with_beautifulsoup(url)
        news_item.content = result['content'][:2000] + "..." if len(result['content']) > 2000 else result['content']
        news_item.authors = result['authors']
        news_item.keywords = result['keywords']

        return news_item

    def extract_contents(self, news_items: List[NewsItem]) -> List[NewsItem]:
        """뉴스 아이템들의 내용 추출"""
        logger.info(f"📄 3단계: {len(news_items)}개 기사 내용 추출 시작")

        for i, news_item in enumerate(news_items):
            logger.info(f"내용 추출 중 ({i + 1}/{len(news_items)})")
            news_items[i] = self.extract_article_content(news_item)
            time.sleep(1)  # 요청 간격 조절

        logger.info(f"✅ 3단계 완료: {len(news_items)}개 기사 내용 추출 완료")
        return news_items


class GoogleRSSCrawler:
    """통합 Google RSS 크롤러 - 3단계 프로세스"""

    def __init__(self):
        self.step1_parser = Step1_RSSParser()
        self.step2_resolver = Step2_URLResolver()
        self.step3_extractor = Step3_ContentExtractor()

    def cleanup(self):
        """리소스 정리"""
        self.step2_resolver.cleanup()

    def get_realtime_news(self, max_items: int = 10, extract_content: bool = True) -> List[NewsItem]:
        """실시간 뉴스 수집 (3단계 프로세스)"""
        logger.info(f"🚀 실시간 뉴스 수집 시작 (최대 {max_items}개)")

        # 1단계: RSS에서 뉴스 링크들 수집
        news_items = self.step1_parser.get_realtime_news_links(max_items)

        if not news_items:
            logger.error("1단계에서 뉴스를 찾을 수 없음")
            return []

        # 2단계: Google News 링크를 실제 링크로 변환
        news_items = self.step2_resolver.resolve_news_urls(news_items)

        # 3단계: 실제 기사 내용 추출 (옵션)
        if extract_content:
            news_items = self.step3_extractor.extract_contents(news_items)

        logger.info(f"🎉 실시간 뉴스 수집 완료: {len(news_items)}개")
        return news_items

    def get_category_news(self, category: str, max_items: int = 10, extract_content: bool = True) -> List[NewsItem]:
        """카테고리별 뉴스 수집 (3단계 프로세스)"""
        logger.info(f"🚀 {category} 카테고리 뉴스 수집 시작")

        # 1단계: RSS에서 카테고리 뉴스 링크들 수집
        news_items = self.step1_parser.get_category_news_links(category, max_items)

        if not news_items:
            logger.error("1단계에서 뉴스를 찾을 수 없음")
            return []

        # 2단계: Google News 링크를 실제 링크로 변환
        news_items = self.step2_resolver.resolve_news_urls(news_items)

        # 3단계: 실제 기사 내용 추출 (옵션)
        if extract_content:
            news_items = self.step3_extractor.extract_contents(news_items)

        logger.info(f"🎉 {category} 카테고리 뉴스 수집 완료: {len(news_items)}개")
        return news_items


# 테스트 함수들
def test_step_by_step():
    """단계별 테스트"""
    print("=== 3단계 뉴스 수집 시스템 테스트 ===")

    # 1단계만 테스트
    print("\n--- 1단계: RSS 파싱 ---")
    parser = Step1_RSSParser()
    news_links = parser.get_realtime_news_links(3)

    for i, news in enumerate(news_links, 1):
        print(f"[{i}] {news.title}")
        print(f"    Google News URL: {news.google_news_url[:80]}...")
        print(f"    언론사: {news.source}")

    # 2단계 추가
    print("\n--- 2단계: URL 리다이렉트 ---")
    resolver = Step2_URLResolver()

    try:
        news_with_real_urls = resolver.resolve_news_urls(news_links[:2])  # 2개만 테스트

        for i, news in enumerate(news_with_real_urls, 1):
            print(f"[{i}] {news.title}")
            print(f"    실제 URL: {news.real_url[:80]}...")

    finally:
        resolver.cleanup()

    # 3단계 추가
    print("\n--- 3단계: 내용 추출 ---")
    extractor = Step3_ContentExtractor()

    news_with_content = extractor.extract_contents(news_with_real_urls[:1])  # 1개만 테스트

    for i, news in enumerate(news_with_content, 1):
        print(f"[{i}] {news.title}")
        print(f"    저자: {', '.join(news.authors) if news.authors else '정보 없음'}")
        print(f"    키워드: {', '.join(news.keywords) if news.keywords else '정보 없음'}")
        print(f"    내용: {news.content[:200] if news.content else '내용 없음'}...")


def test_full_process():
    """전체 프로세스 테스트"""
    print("\n=== 전체 프로세스 테스트 ===")

    crawler = GoogleRSSCrawler()

    try:
        # 실시간 뉴스 3개 (내용 추출 포함)
        news_items = crawler.get_realtime_news(3, extract_content=True)

        print(f"\n수집된 뉴스: {len(news_items)}개")

        for i, news in enumerate(news_items, 1):
            print(f"\n[{i}] {news.title}")
            print(f"    언론사: {news.source}")
            print(f"    Google News URL: {news.google_news_url[:50]}...")
            print(f"    실제 URL: {news.real_url[:50] if news.real_url else '없음'}...")
            print(f"    저자: {', '.join(news.authors) if news.authors else '정보 없음'}")
            print(f"    키워드: {', '.join(news.keywords) if news.keywords else '정보 없음'}")
            print(f"    내용: {news.content[:150] if news.content else '내용 없음'}...")

    finally:
        crawler.cleanup()


if __name__ == "__main__":
    try:
        test_step_by_step()
        test_full_process()
    except Exception as e:
        logger.error(f"테스트 실행 중 오류: {e}")
        import traceback

        traceback.print_exc()