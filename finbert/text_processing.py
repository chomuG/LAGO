"""
텍스트 전처리 관련 함수들
"""
import re
from config import MAX_SENTENCES


def clean_text_advanced(text):
    """텍스트에서 노이즈를 제거하고 핵심 내용만 추출"""
    # 이메일 패턴 제거
    text = re.sub(r'[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}', '', text)

    # URL 패턴 제거
    text = re.sub(r'http[s]?://(?:[a-zA-Z]|[0-9]|[$-_@.&+]|[!*\\(\\),]|(?:%[0-9a-fA-F][0-9a-fA-F]))+', '', text)

    # 기자 정보 패턴 제거
    text = re.sub(r'[\[\(][가-힣]{2,4}\s*기자[\]\)]', '', text)  # [홍길동 기자] 형태만
    text = re.sub(r'^[가-힣]{2,4}\s*기자\s*[=]?\s*', '', text)  # 문장 시작의 기자명만
    text = re.sub(r'[\[\(][가-힣]{2,4}\s*특파원[\]\)]', '', text)  # [홍길동 특파원] 형태만

    # 날짜/시간 패턴 제거
    text = re.sub(r'입력\s*:\s*\d{4}[-./년]\s*\d{1,2}[-./월]\s*\d{1,2}[일]?\s*\d{1,2}[:시]\s*\d{1,2}[분]?', '', text)
    text = re.sub(r'수정\s*:\s*\d{4}[-./년]\s*\d{1,2}[-./월]\s*\d{1,2}[일]?\s*\d{1,2}[:시]\s*\d{1,2}[분]?', '', text)
    text = re.sub(r'발행\s*:\s*\d{4}[-./년]\s*\d{1,2}[-./월]\s*\d{1,2}[일]?\s*\d{1,2}[:시]\s*\d{1,2}[분]?', '', text)

    # 광고성 문구 제거
    text = re.sub(r'무단[\s]*전재[\s]*및[\s]*재배포[\s]*금지', '', text)
    text = re.sub(r'저작권자?\s*[ⓒ©].*?무단.*?금지', '', text)
    text = re.sub(r'^[ⓒ©]\s*[가-힣\s]+$', '', text, flags=re.MULTILINE)  # 저작권 표시만

    # 언론사 관련 패턴 제거 (더 구체적으로)
    text = re.sub(r'^\[[가-힣]+\s*=\s*[가-힣]+\]\s*', '', text)  # 문장 시작의 [서울=연합뉴스] 형태만
    text = re.sub(r'본지에.*?문의.*?주시기.*?바랍니다\.?', '', text)

    # 사진 설명 제거
    text = re.sub(r'사진\s*[=:]\s*[^\n\.]*제공', '', text)
    text = re.sub(r'\[[^\]]*사진[^\]]*\]', '', text)
    text = re.sub(r'<[^>]*>', '', text)  # HTML 태그

    # 반복되는 공백 제거
    text = re.sub(r'\s+', ' ', text)

    return text.strip()


def extract_sentences(text, max_sentences=MAX_SENTENCES):
    """핵심 문장만 추출 (처음 n개 문장)"""
    # 문장 분리
    sentences = re.split(r'[.!?]\s+', text)

    # 빈 문장 제거 및 정제
    clean_sentences = []
    for sent in sentences[:max_sentences]:
        sent = sent.strip()
        if len(sent) > 10:  # 너무 짧은 문장 제외
            clean_sentences.append(sent)

    return '. '.join(clean_sentences)


def clean_title(title):
    """제목에서 언론사명 제거"""
    title = re.sub(r'[-|]\s*[가-힣]+\s*(뉴스|신문|일보|경제|미디어|방송).*', '', title).strip()
    return title


def combine_title_and_content(title, content):
    """제목과 내용을 결합"""
    combined_text = ""
    if title and content:
        # 제목이 내용에 포함되어 있으면 제거
        if title in content[:len(title) + 50]:
            combined_text = content
        else:
            combined_text = f"{title}. {content}"
    elif title:
        combined_text = title
    elif content:
        combined_text = content
    else:
        raise ValueError("제목과 내용을 모두 추출할 수 없습니다")

    # 너무 짧으면 오류
    if len(combined_text) < 30:
        raise ValueError(f"추출된 내용이 너무 짧습니다 (길이: {len(combined_text)})")

    return combined_text