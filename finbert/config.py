"""
설정 파일
"""
import logging

# 로깅 설정
LOG_LEVEL = logging.INFO
LOG_FORMAT = '%(asctime)s - %(name)s - %(levelname)s - %(message)s'

# 모델 설정
MODEL_NAME = "snunlp/KR-FinBert-SC"
MAX_SEQUENCE_LENGTH = 512
TEXT_MAX_LENGTH = 2000

# 서버 설정
HOST = '0.0.0.0'
PORT = 8000
DEBUG = False

# 웹 스크래핑 설정
REQUEST_TIMEOUT = 10
USER_AGENT = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'

# 감정 분석 설정
SENTIMENT_LABELS = ["NEGATIVE", "NEUTRAL", "POSITIVE"]
KEYWORD_WEIGHT = 0.3  # 키워드 영향력
MAX_SENTENCES = 15

# 거래 신호 임계값
BUY_THRESHOLD = 30
WEAK_BUY_THRESHOLD = 15
SELL_THRESHOLD = -30
WEAK_SELL_THRESHOLD = -15

# 신뢰도 임계값
CONFIDENCE_THRESHOLDS = {
    'very_high': {'min_prob': 0.8, 'min_gap': 0.5},
    'high': {'min_prob': 0.6, 'min_gap': 0.3},
    'medium': {'min_prob': 0.5, 'min_gap': 0.2},
    'low': {'min_prob': 0.4, 'min_gap': 0.0}
}