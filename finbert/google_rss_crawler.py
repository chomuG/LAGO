# google_rss_crawler.py
import feedparser
import requests
from datetime import datetime, timedelta
from typing import List, Optional
from dataclasses import dataclass
import logging
import re
from urllib.parse import urlparse, parse_qs, unquote

logger = logging.getLogger(__name__)


@dataclass
class NewsItem:
    """뉴스 아이템 데이터 클래스"""
    title: str
    url: str
    source: str
    published_at: datetime
    category: str = "경제"
    symbol: Optional[str] = None


class GoogleRSSCrawler:
    """Google RSS 뉴스 크롤러"""

    def __init__(self):
        self.base_url = "https://news.google.com/rss/search"
        self.session = requests.Session()
        self.session.headers.update({
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
        })

    def extract_real_url_from_google(self, google_url: str) -> str:
        """Google News URL에서 실제 뉴스 URL 추출"""
        try:
            # Google News URL 디코딩
            if 'news.google.com' not in google_url:
                return google_url

            # 방법 1: URL 파라미터에서 추출
            parsed = urlparse(google_url)
            params = parse_qs(parsed.query)

            # 'url' 파라미터 확인
            if 'url' in params:
                return unquote(params['url'][0])

            # 방법 2: 경로에서 base64 디코딩 시도
            if '/articles/' in google_url:
                # Google News는 때때로 base64로 인코딩된 URL 사용
                try:
                    import base64
                    # URL의 마지막 부분 추출
                    parts = google_url.split('/')
                    for part in parts:
                        if len(part) > 20:  # base64 인코딩된 부분은 보통 길다
                            try:
                                decoded = base64.b64decode(part + '==').decode('utf-8')
                                if decoded.startswith('http'):
                                    return decoded
                            except:
                                continue
                except:
                    pass

            # 방법 3: 실제로 요청해서 리디렉션 따라가기
            try:
                # HEAD 요청으로 빠르게 처리
                response = self.session.head(google_url, allow_redirects=False, timeout=5)

                # Location 헤더 확인
                if 'Location' in response.headers:
                    location = response.headers['Location']
                    if not 'google.com' in location:
                        return location

                # 리디렉션 따라가기
                response = self.session.head(google_url, allow_redirects=True, timeout=5)
                final_url = response.url

                # Google 도메인이 아니면 반환
                if 'google.com' not in final_url:
                    return final_url

            except Exception as e:
                logger.debug(f"리디렉션 실패: {e}")

            # 실패하면 원본 반환
            return google_url

        except Exception as e:
            logger.error(f"URL 추출 오류: {e}")
            return google_url

    def parse_rss_feed(self, rss_url: str, limit: int = 10) -> List[NewsItem]:
        """RSS 피드 파싱"""
        try:
            logger.info(f"RSS 피드 파싱: {rss_url[:100]}...")

            # RSS 피드 가져오기
            feed = feedparser.parse(rss_url)

            if not feed.entries:
                logger.warning("RSS 피드에 항목이 없습니다")
                return []

            news_items = []
            processed_urls = set()  # 중복 URL 체크

            for entry in feed.entries[:limit * 2]:  # 여유있게 가져오기
                try:
                    # Google News URL
                    google_url = entry.get('link', '')

                    if not google_url:
                        continue

                    # 실제 URL 추출
                    real_url = self.extract_real_url_from_google(google_url)

                    # Google News 페이지는 스킵
                    if 'news.google.com' in real_url:
                        logger.debug(f"Google News URL 스킵: {real_url[:80]}")
                        continue

                    # 중복 체크
                    if real_url in processed_urls:
                        continue
                    processed_urls.add(real_url)

                    # 제목 추출
                    title = entry.get('title', '').strip()
                    if not title or len(title) < 10:
                        continue

                    # 소스 추출 (Google RSS는 제목에 소스 포함)
                    source = "Unknown"
                    if ' - ' in title:
                        parts = title.rsplit(' - ', 1)
                        if len(parts) == 2:
                            title = parts[0]
                            source = parts[1]

                    # 발행 시간
                    published = entry.get('published_parsed', None)
                    if published:
                        published_at = datetime.fromtimestamp(
                            datetime(*published[:6]).timestamp()
                        )
                    else:
                        published_at = datetime.now()

                    news_item = NewsItem(
                        title=title,
                        url=real_url,
                        source=source,
                        published_at=published_at,
                        category="경제"
                    )

                    news_items.append(news_item)
                    logger.debug(f"뉴스 추가: {title[:50]}... ({source})")

                    if len(news_items) >= limit:
                        break

                except Exception as e:
                    logger.error(f"항목 파싱 오류: {e}")
                    continue

            logger.info(f"RSS 피드에서 {len(news_items)}개 뉴스 수집")
            return news_items

        except Exception as e:
            logger.error(f"RSS 피드 파싱 실패: {e}")
            return []

    def get_realtime_news(self, limit: int = 20) -> List[NewsItem]:
        """실시간 경제 뉴스 수집"""
        try:
            # 한국 경제 뉴스 검색어들
            queries = [
                "경제 OR 증시 OR 주식",
                "코스피 OR 코스닥",
                "금융 OR 투자"
            ]

            all_news = []
            items_per_query = max(limit // len(queries), 5)

            for query in queries:
                params = {
                    'q': query,
                    'hl': 'ko',
                    'gl': 'KR',
                    'ceid': 'KR:ko'
                }

                # 쿼리 파라미터를 URL로 변환
                query_string = '&'.join(f"{k}={v}" for k, v in params.items())
                rss_url = f"{self.base_url}?{query_string}"

                news_items = self.parse_rss_feed(rss_url, items_per_query)
                all_news.extend(news_items)

            # 중복 제거 및 시간순 정렬
            seen_urls = set()
            unique_news = []

            for item in sorted(all_news, key=lambda x: x.published_at, reverse=True):
                if item.url not in seen_urls:
                    seen_urls.add(item.url)
                    unique_news.append(item)

            return unique_news[:limit]

        except Exception as e:
            logger.error(f"실시간 뉴스 수집 실패: {e}")
            return []

    def get_watchlist_news(self, symbol: str, company_name: str,
                           aliases: List[str], limit: int = 5) -> List[NewsItem]:
        """관심종목 뉴스 수집"""
        try:
            # 검색어 구성
            search_terms = [company_name] + (aliases if aliases else [])
            query = ' OR '.join(f'"{term}"' for term in search_terms)

            params = {
                'q': query,
                'hl': 'ko',
                'gl': 'KR',
                'ceid': 'KR:ko'
            }

            query_string = '&'.join(f"{k}={v}" for k, v in params.items())
            rss_url = f"{self.base_url}?{query_string}"

            news_items = self.parse_rss_feed(rss_url, limit)

            # 종목 정보 설정
            for item in news_items:
                item.symbol = symbol
                item.category = "관심종목"

            return news_items

        except Exception as e:
            logger.error(f"관심종목 뉴스 수집 실패 {company_name}: {e}")
            return []

    def get_historical_news(self, symbol: str, company_name: str,
                            date: str, aliases: List[str], limit: int = 10) -> List[NewsItem]:
        """특정 날짜 뉴스 수집"""
        try:
            # 날짜 범위 설정 (해당일 ± 1일)
            target_date = datetime.strptime(date, "%Y-%m-%d")
            date_before = (target_date - timedelta(days=1)).strftime("%Y-%m-%d")
            date_after = (target_date + timedelta(days=1)).strftime("%Y-%m-%d")

            # 검색어 구성
            search_terms = [company_name] + (aliases if aliases else [])
            query = ' OR '.join(f'"{term}"' for term in search_terms)
            query += f' after:{date_before} before:{date_after}'

            params = {
                'q': query,
                'hl': 'ko',
                'gl': 'KR',
                'ceid': 'KR:ko'
            }

            query_string = '&'.join(f"{k}={v}" for k, v in params.items())
            rss_url = f"{self.base_url}?{query_string}"

            news_items = self.parse_rss_feed(rss_url, limit)

            # 종목 정보 설정
            for item in news_items:
                item.symbol = symbol
                item.category = "역사챌린지"

            return news_items

        except Exception as e:
            logger.error(f"역사적 뉴스 수집 실패 {company_name} ({date}): {e}")
            return []

    def cleanup(self):
        """리소스 정리"""
        try:
            self.session.close()
        except:
            pass