# -*- coding: utf-8 -*-
"""
순서 보장 컨텐츠 블록 파서
텍스트와 이미지를 원본 순서대로 블록 단위로 파싱
"""

import requests
from requests.adapters import HTTPAdapter, Retry
from bs4 import BeautifulSoup
from urllib.parse import urljoin, urlparse
from typing import List, Dict, Optional, Tuple
import logging
from dataclasses import dataclass, asdict
import json
import re
import time

# Selenium imports for Google News redirect resolution
try:
    from selenium import webdriver
    from selenium.webdriver.chrome.options import Options
    from selenium.webdriver.chrome.service import Service
    from webdriver_manager.chrome import ChromeDriverManager
    from selenium.webdriver.support.ui import WebDriverWait
    SELENIUM_AVAILABLE = True
except ImportError:
    SELENIUM_AVAILABLE = False

logger = logging.getLogger(__name__)

@dataclass
class ContentBlock:
    """컨텐츠 블록 데이터 클래스"""
    type: str  # "text" or "image"
    content: str
    position: int
    caption: Optional[str] = None
    confidence: float = 1.0

@dataclass
class ParsedContent:
    """파싱된 컨텐츠 결과"""
    title: str
    blocks: List[ContentBlock]
    summary_text: str  # FinBERT 분석용 텍스트
    total_images: int
    parser_used: str
    confidence: float

class ContentBlockParser:
    """순서 보장 컨텐츠 블록 파서"""
    
    def __init__(self):
        self.session = requests.Session()
        self.session.headers.update({
            'User-Agent': ('Mozilla/5.0 (Windows NT 10.0; Win64; x64) '
                          'AppleWebKit/537.36 (KHTML, like Gecko) '
                          'Chrome/120.0.0.0 Safari/537.36'),
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
            'Accept-Language': 'ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7',
            'Accept-Encoding': 'gzip, deflate, br',
            'Referer': 'https://news.google.com/',  # 구글 뉴스 경유 페이지 대응
            'Cache-Control': 'no-cache',
            'Pragma': 'no-cache'
        })
        
        # HTTP 리트라이 설정
        retries = Retry(
            total=3,
            backoff_factor=0.5,
            status_forcelist=[429, 500, 502, 503, 504],
            allowed_methods=["GET", "HEAD", "OPTIONS"]
        )
        adapter = HTTPAdapter(max_retries=retries)
        self.session.mount("http://", adapter)
        self.session.mount("https://", adapter)
        
        # Selenium WebDriver for Google News redirect resolution
        self.driver = None
    
    def __del__(self):
        """소멸자: Selenium WebDriver 정리"""
        self._cleanup_selenium()
    
    def _setup_selenium(self):
        """Selenium WebDriver 설정 (Google News 리디렉트용)"""
        if not SELENIUM_AVAILABLE:
            logger.warning("Selenium이 설치되지 않아 Google News 리디렉트 해결을 건너뜁니다")
            return False
            
        if self.driver is None:
            try:
                chrome_options = Options()
                chrome_options.add_argument("--headless")
                chrome_options.add_argument("--no-sandbox")
                chrome_options.add_argument("--disable-dev-shm-usage")
                chrome_options.add_argument("--disable-gpu")
                chrome_options.add_argument("--window-size=1920,1080")
                chrome_options.add_argument("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                # 안정성 개선 옵션
                chrome_options.add_argument("--disable-extensions")
                chrome_options.add_argument("--disable-plugins")
                chrome_options.add_argument("--disable-images")  # 이미지 로딩 비활성화로 속도 향상
                chrome_options.add_argument("--disable-javascript")  # JS 비활성화로 속도 향상 (일부 사이트에서는 필요)
                chrome_options.add_argument("--disable-web-security")
                chrome_options.add_argument("--ignore-certificate-errors")
                chrome_options.add_argument("--ignore-ssl-errors")
                # 타임아웃 설정
                chrome_options.add_argument("--page-load-strategy=eager")  # 빠른 로딩

                service = Service(ChromeDriverManager().install())
                self.driver = webdriver.Chrome(service=service, options=chrome_options)
                logger.info("✅ Selenium WebDriver 설정 완료 (Google News 리디렉트용)")
                return True
            except Exception as e:
                logger.error(f"Selenium 설정 오류: {e}")
                self.driver = None
                return False
        return True
    
    def _cleanup_selenium(self):
        """Selenium WebDriver 정리"""
        if self.driver:
            try:
                self.driver.quit()
                self.driver = None
                logger.debug("Selenium WebDriver 정리 완료")
            except Exception as e:
                logger.warning(f"Selenium WebDriver 정리 중 오류: {e}")
    
    def parse_url(self, url: str) -> ParsedContent:
        """URL에서 순서 보장 컨텐츠 블록 파싱"""
        logger.info(f"컨텐츠 블록 파싱 시작: {url}")
        
        # Google News URL인 경우 실제 기사 URL로 리디렉션 해결
        if 'news.google.com' in url:
            resolved_url = self._resolve_google_redirect(url)
            if resolved_url:
                logger.info(f"Google News 리디렉션 해결: {url[:50]}... -> {resolved_url[:50]}...")
                url = resolved_url
            else:
                logger.warning(f"Google News 리디렉션 해결 실패, 원본 URL 사용: {url[:50]}...")
        
        try:
            # HTML 다운로드
            response = self.session.get(url, timeout=15)
            response.raise_for_status()
            
            # 인코딩 교정 - 잘못 감지된 인코딩 수정
            if not response.encoding or response.encoding.lower() == 'iso-8859-1':
                # apparent_encoding 또는 utf-8 사용
                response.encoding = response.apparent_encoding or 'utf-8'
            
            # 일부 한국 사이트는 euc-kr 사용
            if 'euc-kr' in response.headers.get('Content-Type', '').lower():
                response.encoding = 'euc-kr'
            
            html = response.text
            
            # 포털별 파서 선택 (확장된 도메인 커버리지)
            domain = urlparse(url).netloc.lower()
            
            if 'naver.com' in domain or 'n.news.naver.com' in domain:
                result = self._parse_naver(html, url)
            elif 'hankyung.com' in domain:
                result = self._parse_hankyung(html, url)
            elif 'mk.co.kr' in domain:
                result = self._parse_maekyung(html, url)
            elif 'chosun.com' in domain or 'chosunbiz.com' in domain or 'biz.chosun.com' in domain:
                result = self._parse_chosun(html, url)
            elif 'daum.net' in domain or 'v.daum.net' in domain:
                result = self._parse_generic(html, url)  # 필요시 _parse_daum 구현
            elif 'yna.co.kr' in domain or 'yonhapnews.co.kr' in domain:
                result = self._parse_generic(html, url)  # 필요시 _parse_yonhap 구현
            elif 'donga.com' in domain:
                result = self._parse_generic(html, url)  # 필요시 _parse_donga 구현
            elif 'joongang.co.kr' in domain:
                result = self._parse_generic(html, url)  # 필요시 _parse_joongang 구현
            elif 'hani.co.kr' in domain:
                result = self._parse_generic(html, url)  # 필요시 _parse_hani 구현
            elif 'khan.co.kr' in domain:
                result = self._parse_generic(html, url)  # 필요시 _parse_khan 구현
            elif 'mt.co.kr' in domain:
                result = self._parse_generic(html, url)  # 필요시 _parse_mt 구현
            elif 'edaily.co.kr' in domain:
                result = self._parse_generic(html, url)  # 필요시 _parse_edaily 구현
            elif 'fnnews.com' in domain:
                result = self._parse_generic(html, url)  # 필요시 _parse_fnnews 구현
            elif 'news1.kr' in domain:
                result = self._parse_generic(html, url)  # 필요시 _parse_news1 구현
            elif 'inews24.com' in domain:
                result = self._parse_generic(html, url)  # 필요시 _parse_inews24 구현
            elif 'etnews.com' in domain:
                result = self._parse_generic(html, url)  # 필요시 _parse_etnews 구현
            elif 'pinpointnews.co.kr' in domain:
                result = self._parse_pinpoint(html, url)  # pinpointnews 전용 파서
            else:
                result = self._parse_generic(html, url)
            
            logger.info(f"파싱 완료: 제목={result.title[:30]}..., 블록={len(result.blocks)}개, 이미지={result.total_images}개")
            return result
            
        except Exception as e:
            logger.error(f"컨텐츠 블록 파싱 실패 {url}: {e}")
            raise
    
    def _resolve_google_redirect(self, google_url: str) -> Optional[str]:
        """Google News 리디렉션 URL을 실제 URL로 해결 (Selenium 기반)"""
        if 'news.google.com' not in google_url:
            return google_url
            
        try:
            # Selenium으로 리디렉트 처리 (사용자의 test.py에서 검증된 방식)
            if not self.driver:
                if not self._setup_selenium():
                    logger.warning("Selenium 사용 불가, Google News 리디렉트 건너뜀")
                    return None

            if not self.driver:
                return None

            logger.info(f"🔄 Selenium으로 Google News 리디렉트 처리: {google_url[:60]}...")
            
            # 타임아웃 설정
            self.driver.set_page_load_timeout(15)
            self.driver.implicitly_wait(5)
            
            # Google News URL로 이동
            self.driver.get(google_url)

            # 리디렉트 대기 (최대 15초) - Google News에서 벗어날 때까지 대기
            try:
                WebDriverWait(self.driver, 15).until(
                    lambda driver: 'news.google.com' not in driver.current_url and driver.current_url != 'about:blank'
                )
                # 추가로 페이지 로딩 완료 대기
                time.sleep(1)  # 페이지 안정화 대기
            except Exception as e:
                logger.warning(f"Google News 리디렉트 대기 시간 초과: {e}")

            real_url = self.driver.current_url

            # 최종 URL이 유효한 뉴스 포털인지 확인
            if 'news.google.com' not in real_url and self._is_valid_news_url(real_url):
                logger.info(f"✅ Google News 리디렉트 성공: {google_url[:50]}... -> {real_url[:60]}...")
                return real_url
            else:
                logger.warning(f"Google News 리디렉트 실패 (여전히 Google): {real_url[:60]}...")
                return None
                
        except Exception as e:
            logger.warning(f"Google News 리디렉트 처리 오류: {e}")
            return None
    
    def _is_valid_news_url(self, url: str) -> bool:
        """유효한 뉴스 URL인지 확인"""
        if not url or len(url) < 10:
            return False
        
        if not url.startswith(('http://', 'https://')):
            return False
        
        # Google 관련 URL 제외
        if any(domain in url.lower() for domain in ['google.com', 'googleusercontent.com', 'googlesyndication.com']):
            return False
        
        # 한국 뉴스 포털 확인
        korean_portals = [
            'naver.com', 'daum.net', 'hankyung.com', 'mk.co.kr', 'chosun.com', 
            'donga.com', 'joongang.co.kr', 'hani.co.kr', 'khan.co.kr', 'mt.co.kr',
            'edaily.co.kr', 'fnnews.com', 'news1.kr', 'inews24.com', 'etnews.com',
            'yonhapnews.co.kr', 'yna.co.kr', 'newsis.com', 'pinpointnews.co.kr'
        ]
        
        return any(portal in url.lower() for portal in korean_portals)
    
    def _parse_naver(self, html: str, url: str) -> ParsedContent:
        """네이버 뉴스 전용 파서 - 순서 보장"""
        soup = BeautifulSoup(html, 'lxml')
        
        # 제목 추출
        title_elem = soup.select_one('h2.media_end_head_headline, h3.tts_head, .article_info h3')
        title = title_elem.get_text(strip=True) if title_elem else "제목 없음"
        
        # 본문 영역 찾기
        article_selectors = [
            'div#dic_area',
            'div#articeBody', 
            'div#newsEndContents',
            'div.news_end_main'
        ]
        
        article = None
        for selector in article_selectors:
            article = soup.select_one(selector)
            if article:
                break
        
        if not article:
            raise Exception("네이버 뉴스 본문을 찾을 수 없음")
        
        # 노이즈 제거
        noise_selectors = [
            '.vod_area', '.link_news', '.reporter_area',
            '.copyright', '.promotion', '.news_function'
        ]
        for selector in noise_selectors:
            for elem in article.select(selector):
                elem.decompose()
        
        # 순서 보장 블록 파싱
        blocks = []
        position = 0
        text_parts = []
        
        # DOM 순서대로 순회하면서 텍스트와 이미지 처리
        for elem in article.find_all(['p', 'div', 'span', 'img'], recursive=False):
            
            if elem.name == 'img':
                # 이미지 블록 처리
                img_block = self._process_naver_image(elem, position)
                if img_block:
                    blocks.append(img_block)
                    position += 1
            
            else:
                # 텍스트 블록 처리
                # 하위 이미지 먼저 처리
                for img in elem.find_all('img'):
                    img_block = self._process_naver_image(img, position)
                    if img_block:
                        blocks.append(img_block)
                        position += 1
                        # 이미지는 텍스트에서 제거
                        img.decompose()
                
                # 텍스트 처리
                text = elem.get_text(strip=True)
                if self._is_valid_text(text):
                    text_block = ContentBlock(
                        type="text",
                        content=text,
                        position=position,
                        confidence=0.95
                    )
                    blocks.append(text_block)
                    text_parts.append(text)
                    position += 1
        
        # 결과 구성
        summary_text = '\n\n'.join(text_parts)
        total_images = len([b for b in blocks if b.type == "image"])
        
        return ParsedContent(
            title=title,
            blocks=blocks,
            summary_text=summary_text,
            total_images=total_images,
            parser_used="naver_specialized",
            confidence=0.95
        )
    
    def _parse_hankyung(self, html: str, url: str) -> ParsedContent:
        """한국경제 전용 파서"""
        soup = BeautifulSoup(html, 'lxml')
        
        # 제목
        title_elem = soup.select_one('h1.headline, .article-headline h1, .news-headline')
        title = title_elem.get_text(strip=True) if title_elem else "제목 없음"
        
        # 본문
        article = soup.select_one('.article-body, .news-body, #articletxt')
        if not article:
            raise Exception("한국경제 본문을 찾을 수 없음")
        
        return self._parse_generic_structure(article, title, "hankyung_specialized", 0.85)
    
    def _parse_maekyung(self, html: str, url: str) -> ParsedContent:
        """매일경제 전용 파서"""
        soup = BeautifulSoup(html, 'lxml')
        
        # 제목
        title_elem = soup.select_one('h1.news_ttl, .art_hd h1, .article_head h1')
        title = title_elem.get_text(strip=True) if title_elem else "제목 없음"
        
        # 본문
        article = soup.select_one('.news_cnt_detail_wrap, .art_txt, .article_content')
        if not article:
            raise Exception("매일경제 본문을 찾을 수 없음")
        
        return self._parse_generic_structure(article, title, "maekyung_specialized", 0.85)
    
    def _parse_chosun(self, html: str, url: str) -> ParsedContent:
        """조선일보 전용 파서"""
        soup = BeautifulSoup(html, 'lxml')
        
        # 제목
        title_elem = soup.select_one('h1.article-title, .news_title_text, h1')
        title = title_elem.get_text(strip=True) if title_elem else "제목 없음"
        
        # 본문
        article = soup.select_one('.article-body, .par, .news_body')
        if not article:
            raise Exception("조선일보 본문을 찾을 수 없음")
        
        return self._parse_generic_structure(article, title, "chosun_specialized", 0.80)
    
    def _parse_pinpoint(self, html: str, url: str) -> ParsedContent:
        """핀포인트뉴스 전용 파서"""
        soup = BeautifulSoup(html, 'lxml')
        
        # 제목 추출 - 다양한 선택자 시도
        title = ""
        title_selectors = [
            'h1.article-title',
            'h1.news-title',
            'div.article-header h1',
            '.news-header h1',
            'h1',
            'title'
        ]
        for selector in title_selectors:
            title_elem = soup.select_one(selector)
            if title_elem:
                title = title_elem.get_text(strip=True)
                if title and len(title) > 10:
                    break
        
        if not title or len(title) < 10:
            title = "제목 없음"
        
        # 본문 추출 - 다양한 선택자 시도
        content = ""
        article_selectors = [
            '.article-view',
            '.article-body',
            '.news-content',
            '.content-area',
            'article',
            'main',
            '#article-body',
            '.article_body'
        ]
        
        for selector in article_selectors:
            body = soup.select_one(selector)
            if body:
                # 불필요한 요소 제거
                for tag in body.select('script, style, nav, aside, figure figcaption, .sns, .share, .ad, .advertisement, .related'):
                    tag.decompose()
                
                # 본문 텍스트 추출
                paragraphs = []
                for elem in body.find_all(['p', 'div', 'li']):
                    text = elem.get_text(" ", strip=True)
                    if len(text) > 20 and not any(skip in text.lower() for skip in ['copyright', '무단전재', '재배포', '기자']):
                        paragraphs.append(text)
                
                temp_content = " ".join(paragraphs)
                if len(temp_content) > len(content):
                    content = temp_content
        
        # 블록 구조 생성
        blocks = []
        position = 0
        
        # 텍스트 블록 추가
        if content:
            text_block = ContentBlock(
                type="text",
                content=content,
                position=position,
                confidence=0.85
            )
            blocks.append(text_block)
            position += 1
        
        # 이미지 추출
        image_selectors = [
            'article img',
            '.article img',
            '.news_body img',
            '.article_body img',
            '.article-view img',
            'main img'
        ]
        
        for selector in image_selectors:
            for img in soup.select(selector):
                src = img.get('src') or img.get('data-src')
                if src and self._is_content_image(src):
                    # 상대 URL을 절대 URL로 변환
                    if src.startswith('//'):
                        src = 'https:' + src
                    elif src.startswith('/'):
                        from urllib.parse import urljoin
                        src = urljoin(url, src)
                    
                    img_block = ContentBlock(
                        type="image",
                        content=src,
                        position=position,
                        confidence=0.80
                    )
                    blocks.append(img_block)
                    position += 1
        
        # 결과 생성
        summary_text = content[:500] if content else ""
        total_images = len([b for b in blocks if b.type == "image"])
        
        return ParsedContent(
            title=title,
            blocks=blocks,
            summary_text=summary_text,
            total_images=total_images,
            parser_used="pinpoint_specialized",
            confidence=0.85
        )
    
    def _parse_generic(self, html: str, url: str) -> ParsedContent:
        """범용 파서 (지원하지 않는 포털)"""
        from readability import Document
        
        doc = Document(html)
        clean_html = doc.summary()
        soup = BeautifulSoup(clean_html, 'lxml')
        
        title = doc.title() or "제목 없음"
        
        return self._parse_generic_structure(soup, title, "generic_parser", 0.60)
    
    def _parse_generic_structure(self, article_elem, title: str, parser_name: str, confidence: float) -> ParsedContent:
        """범용 구조 파서"""
        blocks = []
        position = 0
        text_parts = []
        
        # 모든 p, div, img 태그를 순서대로 처리
        for elem in article_elem.find_all(['p', 'div', 'img', 'figure']):
            
            if elem.name == 'img':
                img_block = self._process_generic_image(elem, position)
                if img_block:
                    blocks.append(img_block)
                    position += 1
            
            elif elem.name == 'figure':
                # figure 내부 이미지 처리
                img = elem.find('img')
                if img:
                    caption_elem = elem.find('figcaption')
                    caption = caption_elem.get_text(strip=True) if caption_elem else None
                    
                    img_block = self._process_generic_image(img, position, caption)
                    if img_block:
                        blocks.append(img_block)
                        position += 1
            
            else:
                # 텍스트 처리 (p, div)
                text = elem.get_text(strip=True)
                if self._is_valid_text(text):
                    text_block = ContentBlock(
                        type="text",
                        content=text,
                        position=position,
                        confidence=confidence
                    )
                    blocks.append(text_block)
                    text_parts.append(text)
                    position += 1
        
        summary_text = '\n\n'.join(text_parts)
        total_images = len([b for b in blocks if b.type == "image"])
        
        return ParsedContent(
            title=title,
            blocks=blocks,
            summary_text=summary_text,
            total_images=total_images,
            parser_used=parser_name,
            confidence=confidence
        )
    
    def _process_naver_image(self, img_elem, position: int) -> Optional[ContentBlock]:
        """네이버 이미지 처리"""
        src = img_elem.get('data-src') or img_elem.get('src')
        if not src or not self._is_content_image(src):
            return None
        
        # 네이버 이미지 URL 정규화
        if src.startswith('//'):
            src = 'https:' + src
        elif src.startswith('/'):
            src = 'https://imgnews.pstatic.net' + src
        
        # 캡션 찾기
        caption = self._find_naver_caption(img_elem)
        
        return ContentBlock(
            type="image",
            content=src,
            position=position,
            caption=caption,
            confidence=0.90
        )
    
    def _process_generic_image(self, img_elem, position: int, caption: str = None) -> Optional[ContentBlock]:
        """범용 이미지 처리"""
        src = img_elem.get('src') or img_elem.get('data-src')
        if not src or not self._is_content_image(src):
            return None
        
        # 상대 URL을 절대 URL로 변환
        if src.startswith('//'):
            src = 'https:' + src
        elif src.startswith('/'):
            # 베이스 도메인 추가 필요한 경우
            pass
        
        return ContentBlock(
            type="image",
            content=src,
            position=position,
            caption=caption,
            confidence=0.70
        )
    
    def _is_content_image(self, src: str) -> bool:
        """본문 이미지인지 판별"""
        if not src:
            return False
        
        # 제외할 이미지 패턴
        exclude_patterns = [
            'icon', 'logo', 'banner', 'ad', 'btn',
            'arrow', 'bullet', 'emoticon', 'profile'
        ]
        
        src_lower = src.lower()
        return not any(pattern in src_lower for pattern in exclude_patterns)
    
    def _is_valid_text(self, text: str) -> bool:
        """유효한 텍스트인지 판별"""
        if not text or len(text) < 20:
            return False
        
        # 제외할 텍스트 패턴
        exclude_patterns = [
            r'^[\s\n]*$',  # 공백만
            r'▶.*바로가기',
            r'Copyright.*reserved',
            r'^.*@.*\.(com|co\.kr)$',  # 이메일만
            r'^\d{4}-\d{2}-\d{2}.*\d{2}:\d{2}$'  # 날짜시간만
        ]
        
        for pattern in exclude_patterns:
            if re.search(pattern, text, re.IGNORECASE):
                return False
        
        return True
    
    def _find_naver_caption(self, img_elem) -> Optional[str]:
        """네이버 이미지 캡션 찾기"""
        # 다음 형제 요소에서 캡션 찾기
        next_elem = img_elem.find_next_sibling(['em', 'span', 'p'])
        if next_elem:
            caption = next_elem.get_text(strip=True)
            if len(caption) < 100 and any(word in caption for word in ['사진', '제공', '출처']):
                return caption
        
        # 부모 요소의 캡션 찾기
        parent = img_elem.parent
        if parent:
            caption_elem = parent.find('figcaption') or parent.find('.caption')
            if caption_elem:
                return caption_elem.get_text(strip=True)
        
        return None
    
    def to_json(self, parsed_content: ParsedContent) -> str:
        """ParsedContent를 JSON으로 변환"""
        data = {
            'title': parsed_content.title,
            'blocks': [asdict(block) for block in parsed_content.blocks],
            'summary_text': parsed_content.summary_text,
            'total_images': parsed_content.total_images,
            'parser_used': parsed_content.parser_used,
            'confidence': parsed_content.confidence
        }
        return json.dumps(data, ensure_ascii=False, indent=2)


# 테스트 코드
if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)
    
    parser = ContentBlockParser()
    
    # 네이버 뉴스 테스트
    test_url = "https://n.news.naver.com/mnews/article/022/0003872341"
    
    try:
        result = parser.parse_url(test_url)
        
        print(f"제목: {result.title}")
        print(f"총 블록: {len(result.blocks)}개")
        print(f"이미지: {result.total_images}개")
        print(f"파서: {result.parser_used}")
        print(f"신뢰도: {result.confidence}")
        print("\n=== 블록 순서 ===")
        
        for i, block in enumerate(result.blocks[:5]):  # 처음 5개만 출력
            if block.type == "text":
                print(f"{i+1}. [TEXT] {block.content[:50]}...")
            else:
                print(f"{i+1}. [IMAGE] {block.content}")
                if block.caption:
                    print(f"    캡션: {block.caption}")
        
        print(f"\n=== 요약 텍스트 ===")
        print(result.summary_text[:200] + "...")
        
    except Exception as e:
        print(f"테스트 실패: {e}")