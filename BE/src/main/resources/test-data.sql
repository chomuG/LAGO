-- 라고할때 프로젝트 테스트 데이터
-- 지침서 명세에 따른 AI 매매봇 계좌 조회 API 테스트용 데이터

-- AI 봇 사용자 데이터 (is_ai = true)
INSERT INTO USER (user_id, email, firebase_uid, nickname, personality, created_at, profile_img, frame_id, deleted_at, is_ai, ai_id) 
VALUES 
(1001, 'aibot1@lago.com', 'firebase_ai_1', '워렌버핏봇', '보수적', NOW(), null, 1, null, true, 1),
(1002, 'aibot2@lago.com', 'firebase_ai_2', '피터린치봇', '적극적', NOW(), null, 2, null, true, 2),
(1003, 'aibot3@lago.com', 'firebase_ai_3', '벤자민그레이엄봇', '가치투자', NOW(), null, 3, null, true, 3);

-- AI 봇 계좌 데이터 (type = 'ai_bot')
INSERT INTO ACCOUNT (account_id, user_id, balance, total_asset, profit, profit_rate, created_at, type)
VALUES 
(2001, 1001, 1500000, 2000000, 500000, 33.33, NOW(), 'ai_bot'),
(2002, 1002, 800000, 1200000, 200000, 20.00, NOW(), 'ai_bot'),
(2003, 1003, 2200000, 2500000, 300000, 13.64, NOW(), 'ai_bot');

-- 테스트용 모의거래 데이터
INSERT INTO MOCK_TRADE (trade_id, account_id, price, quantity)
VALUES 
(3001, 2001, 50000, 10),
(3002, 2001, 75000, 8),
(3003, 2001, 100000, 5),
(3004, 2002, 120000, 6),
(3005, 2002, 80000, 12),
(3006, 2003, 200000, 3),
(3007, 2003, 150000, 4);

-- 일반 사용자 데이터 (is_ai = false)
INSERT INTO USER (user_id, email, firebase_uid, nickname, personality, created_at, profile_img, frame_id, deleted_at, is_ai, ai_id) 
VALUES 
(1, 'user1@lago.com', 'firebase_user_1', '투자왕김', '중립적', NOW(), null, 1, null, false, null),
(2, 'user2@lago.com', 'firebase_user_2', '주식달인', '공격적', NOW(), null, 2, null, false, null);

-- 일반 사용자 계좌 데이터
INSERT INTO ACCOUNT (account_id, user_id, balance, total_asset, profit, profit_rate, created_at, type)
VALUES 
(1, 1, 1000000, 1500000, 500000, 50.00, NOW(), '현시점'),
(2, 2, 500000, 800000, 300000, 60.00, NOW(), '현시점');
