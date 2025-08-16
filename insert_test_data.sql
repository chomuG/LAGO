-- 매매 테스트용 더미 데이터 삽입

-- STOCK_INFO 테이블에 테스트용 주식 정보 삽입
INSERT INTO "STOCK_INFO" (code, name, market) VALUES 
('005930', '삼성전자', 'KOSPI'),
('000660', 'SK하이닉스', 'KOSPI'),
('035420', 'NAVER', 'KOSPI')
ON CONFLICT (code) DO NOTHING;

-- USERS 테이블에 테스트 사용자 삽입 
INSERT INTO "USERS" (user_id, username, email, fcm_token, is_active, created_at) VALUES 
(1, 'testuser1', 'test1@example.com', 'test_fcm_token_1', true, NOW()),
(2, 'testuser2', 'test2@example.com', 'test_fcm_token_2', true, NOW())
ON CONFLICT (user_id) DO NOTHING;

-- ACCOUNTS 테이블에 테스트 계좌 삽입 (1천만원 가상 자금)
INSERT INTO "ACCOUNTS" (account_id, user_id, account_number, total_asset, available_cash, created_at, updated_at) VALUES 
(1001, 1, 'TEST_ACCOUNT_001', 10000000, 10000000, NOW(), NOW()),
(1002, 2, 'TEST_ACCOUNT_002', 10000000, 10000000, NOW(), NOW())
ON CONFLICT (account_id) DO NOTHING;

-- 현재가 정보 (STOCK_MINUTE 테이블에 테스트용 현재가 삽입)
INSERT INTO "STOCK_MINUTE" (stock_code, trade_time, open_price, high_price, low_price, close_price, volume, trade_value) VALUES 
('005930', NOW(), 75000, 76000, 74500, 75500, 1000000, 75500000000),
('000660', NOW(), 120000, 121000, 119500, 120500, 500000, 60250000000),
('035420', NOW(), 180000, 182000, 179000, 181000, 300000, 54300000000)
ON CONFLICT (stock_code, trade_time) DO NOTHING;