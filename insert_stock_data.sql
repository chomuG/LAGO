-- STOCK_INFO 테스트 더미 데이터
INSERT INTO "STOCK_INFO" (code, name, market) VALUES 
('005930', '삼성전자', 'KOSPI'),
('000660', 'SK하이닉스', 'KOSPI'),
('035420', 'NAVER', 'KOSPI'),
('051910', 'LG화학', 'KOSPI'),
('006400', '삼성SDI', 'KOSPI')
ON CONFLICT (code) DO NOTHING;