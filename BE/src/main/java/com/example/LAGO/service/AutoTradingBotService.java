package com.example.LAGO.service;

import com.example.LAGO.constants.TradingConstants;
import com.example.LAGO.dto.request.MockTradeRequest;
import com.example.LAGO.domain.*;
import com.example.LAGO.repository.*;
import com.example.LAGO.utils.TradingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * AI 자동매매 봇 서비스
 * 
 * 지침서 명세:
 * - AI 봇 계정 (is_ai = true)에 대한 자동매매 실행
 * - 기술적 분석 기반 매매 신호 생성 및 실행
 * - 전략별 리스크 관리 및 포지션 조절
 * - Virtual Thread 활용 고성능 병렬 처리
 * 
 * 주요 기능:
 * 1. 정기적 자동매매 실행 (매 1분마다)
 * 2. 기술적 분석 신호 기반 매매 판단
 * 3. 리스크 관리 및 자금 관리
 * 4. 거래 이력 및 성과 추적
 * 
 * @author D203팀 백엔드 개발자
 * @since 2025-08-05
 */
@Service
@RequiredArgsConstructor
@Slf4j
//@Transactional    // 예외처리 오류로 일단 클래스레벨 Transaction은 주석처리
public class AutoTradingBotService {

    // ======================== 의존성 주입 ========================
    
    /**
     * 보유 주식 정보 조회를 위한 리포지토리
     */
    private final StockHoldingRepository stockHoldingRepository;

    /**
     * 분봉 데이터 조회를 위한 리포지토리 (실시간 가격)
     */
    private final StockMinuteRepository stockMinuteRepository;

    /**
     * 사용자 정보 조회를 위한 리포지토리
     */
    private final UserRepository userRepository;
    
    /**
     * 계좌 정보 관리를 위한 리포지토리  
     */
    private final AccountRepository accountRepository;
    
    /**
     * 주식 실시간 데이터 조회를 위한 리포지토리
     */
    private final StockRepository stockRepository;
    
    /**
     * 모의매매 거래 실행을 위한 서비스
     */
    private final MockTradingService mockTradingService;
    
    /**
     * 기술적 분석 신호 생성을 위한 서비스
     */
    private final TechnicalAnalysisService technicalAnalysisService;
    
    /**
     * AI 봇 거래 이력 저장을 위한 리포지토리
     */
    private final AiBotTradeRepository aiBotTradeRepository;
    
    /**
     * AI 전략 관리를 위한 리포지토리
     */
    private final AiStrategyRepository aiStrategyRepository;

    // ======================== Virtual Thread Executor ========================
    
    /**
     * Java 21 Virtual Thread를 활용한 고성능 비동기 처리
     * - 경량 스레드로 대량의 동시 매매 처리 가능
     * - 기존 Platform Thread 대비 메모리 효율성 극대화
     */
    private final Executor virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    // ======================== 자동매매 스케줄러 ========================

    /**
     * AI 자동매매 봇 메인 스케줄러
     * 
     * 실행 주기: 매 1분마다 (fixedRate = 60,000ms)
     * 초기 지연: 30초 후 시작 (initialDelay = 30,000ms)
     * 
     * 처리 흐름:
     * 1. 활성화된 AI 봇 계정 조회
     * 2. 각 봇별 병렬 매매 실행
     * 3. 예외 상황 안전 처리
     * 
     * Virtual Thread 활용으로 대량 봇 동시 처리 최적화
     */
    @Scheduled(fixedRate = TradingConstants.AUTO_TRADING_INTERVAL_MS, 
              initialDelay = TradingConstants.AUTO_TRADING_INITIAL_DELAY_MS)
    public void executeAutoTrading() {
        try {
            log.info("=== AI 자동매매 실행 시작: {} ===", TradingUtils.formatTradeTime(LocalDateTime.now()));
            
            // 1. 활성화된 AI 봇 계정 조회
            List<User> aiBots = getActiveTradingBots();
            
            if (aiBots.isEmpty()) {
                log.info("실행 가능한 AI 봇이 없습니다. 다음 주기를 대기합니다.");
                return;
            }
            
            log.info("활성 AI 봇 {}개 발견, 매매 실행을 시작합니다.", aiBots.size());
            
            // 2. 각 AI 봇별 병렬 매매 실행 (Virtual Thread 활용)
            List<CompletableFuture<Void>> tradingTasks = aiBots.stream()
                .map(bot -> CompletableFuture.runAsync(
                    () -> executeTradingForBot(bot), 
                    virtualThreadExecutor
                ))
                .toList();
            
            // 3. 모든 봇의 매매 작업 완료 대기
            CompletableFuture.allOf(tradingTasks.toArray(new CompletableFuture[0]))
                .thenRun(() -> log.info("=== 모든 AI 봇 매매 실행 완료 ==="))
                .exceptionally(throwable -> {
                    log.error("AI 봇 매매 실행 중 예외 발생", throwable);
                    return null;
                });
                
        } catch (Exception e) {
            log.error("자동매매 스케줄러 실행 중 예외 발생", e);
        }
    }

    // ======================== AI 봇 조회 및 관리 ========================

    /**
     * 활성화된 AI 매매 봇 계정 조회
     * 
     * 조회 조건:
     * - is_ai = true (AI 봇 계정)
     * - deleted_at IS NULL (삭제되지 않은 계정)
     * - 활성 상태 계좌 보유
     * 
     * @return 활성 AI 봇 계정 리스트
     */
    private List<User> getActiveTradingBots() {
        try {
            List<User> aiBots = userRepository.findByIsAiTrueAndDeletedAtIsNull();
            
            // 활성 계좌를 보유한 봇만 필터링
            List<User> activeAiBots = aiBots.stream()
                .filter(this::hasActiveAccount)
                .toList();
                
            log.debug("총 AI 봇 계정: {}개, 활성 봇: {}개", aiBots.size(), activeAiBots.size());
            
            return activeAiBots;
            
        } catch (Exception e) {
            log.error("활성 AI 봇 조회 중 오류 발생", e);
            return List.of();
        }
    }

    /**
     * AI 봇의 활성 계좌 보유 여부 확인
     * 
     * @param aiBot 확인할 AI 봇 계정
     * @return 활성 계좌 보유 여부
     */
    private boolean hasActiveAccount(User aiBot) {
        try {
            return accountRepository.findByUserIdAndType(
                aiBot.getUserId(), 
                TradingConstants.ACCOUNT_TYPE_MOCK_TRADING
            ).isPresent();
            
        } catch (Exception e) {
            log.warn("AI 봇 {}의 계좌 확인 중 오류: {}", aiBot.getUserId(), e.getMessage());
            return false;
        }
    }

    // ======================== 개별 봇 매매 실행 ========================

    /**
     * 개별 AI 봇에 대한 매매 실행
     * 
     * 실행 단계:
     * 1. 봇 계좌 및 전략 정보 로드
     * 2. 매매 대상 종목 선정
     * 3. 기술적 분석 수행
     * 4. 매매 신호 판단 및 실행
     * 5. 거래 결과 기록
     * 
     * @param aiBot 매매를 실행할 AI 봇
     */
    private void executeTradingForBot(User aiBot) {
        Integer botId = aiBot.getUserId();
        
        try {
            log.info("AI 봇 {} 매매 실행 시작", botId);
            
            // 1. 봇 계좌 정보 로드
            Account botAccount = loadBotAccount(aiBot);
            if (botAccount == null) {
                log.warn("AI 봇 {} 계좌 로드 실패, 매매 건너뜀", botId);
                return;
            }
            
            // 2. 봇 전략 정보 로드
            String strategy = loadBotStrategy(aiBot);
            if (strategy == null) {
                log.warn("AI 봇 {} 전략 로드 실패, 매매 건너뜀", botId);
                return;
            }
            
            // 3. 매매 대상 종목 선정
            List<String> targetStocks = selectTradingTargets(strategy);
            if (targetStocks.isEmpty()) {
                log.info("AI 봇 {} 매매 대상 종목 없음", botId);
                return;
            }
            
            // 4. 각 종목별 매매 판단 및 실행
            for (String stockCode : targetStocks) {
                try {
                    executeStockTrading(aiBot, botAccount, strategy, stockCode);
                } catch (Exception e) {
                    log.error("AI 봇 {} 종목 {} 매매 실행 실패", botId, stockCode, e);
                    // 개별 종목 실패 시 다른 종목 매매는 계속 진행
                }
            }
            
            log.info("AI 봇 {} 매매 실행 완료", botId);
            
        } catch (Exception e) {
            log.error("AI 봇 {} 매매 실행 중 예외 발생", botId, e);
            
            // 거래 실패 로그 기록
            recordTradingFailure(aiBot, e.getMessage());
        }
    }

    /**
     * AI 봇 계좌 정보 로드
     * 
     * @param aiBot AI 봇 계정
     * @return 봇의 현재 계좌 (없으면 null)
     */
    private Account loadBotAccount(User aiBot) {
        try {
            return accountRepository.findByUserIdAndType(
                aiBot.getUserId(), 
                TradingConstants.ACCOUNT_TYPE_MOCK_TRADING
            ).orElse(null);
            
        } catch (Exception e) {
            log.error("AI 봇 {} 계좌 로드 실패", aiBot.getUserId(), e);
            return null;
        }
    }

    /**
     * AI 봇 매매 전략 로드
     * 
     * @param aiBot AI 봇 계정
     * @return 봇의 매매 전략 (없으면 기본 전략)
     */
    private String loadBotStrategy(User aiBot) {
        try {
            // AI 봇의 성향에 따른 기본 전략 매핑
            String personality = aiBot.getPersonality();
            if (personality != null) {
                switch (personality) {
                    case "공격투자형":
                        return "화끈이";
                    case "적극투자형":
                        return "적극이";
                    case "위험중립형":
                        return "균형이";
                    case "안정추구형":
                        return "조심이";
                    default:
                        return TradingConstants.DEFAULT_AI_STRATEGY;
                }
            }
            
            return TradingConstants.DEFAULT_AI_STRATEGY;
                
        } catch (Exception e) {
            log.error("AI 봇 {} 전략 로드 실패, 기본 전략 사용", aiBot.getUserId(), e);
            return TradingConstants.DEFAULT_AI_STRATEGY;
        }
    }

    // ======================== 매매 대상 선정 ========================

    /**
     * 전략별 매매 대상 종목 선정
     * 
     * 선정 기준:
     * - 거래량 충분 (일평균 거래량 기준)
     * - 가격 범위 적절 (최소/최대 가격 제한)
     * - 변동성 적정 (과도한 급등락 종목 제외)
     * 
     * @param strategy 매매 전략
     * @return 매매 대상 종목 코드 리스트
     */
    private List<String> selectTradingTargets(String strategy) {
        try {
            log.debug("매매 대상 종목 선정 시작: strategy={}", strategy);
            
            // 거래 가능한 주요 종목들을 조회
            List<Stock> availableStocks = stockRepository.findTop50ByOrderByVolumeDesc();
            
            // 전략별 필터링 적용
            List<String> targetStocks = availableStocks.stream()
                .filter(this::isValidTradingTarget)
                .map(Stock::getCode)
                .limit(TradingConstants.MAX_CONCURRENT_STOCKS)
                .toList();
            
            log.debug("매매 대상 종목 {}개 선정 완료", targetStocks.size());
            return targetStocks;
            
        } catch (Exception e) {
            log.error("매매 대상 종목 선정 실패", e);
            return List.of();
        }
    }

    /**
     * 종목의 매매 적합성 검증
     * 
     * @param stock 검증할 종목
     * @return 매매 적합 여부
     */
    private boolean isValidTradingTarget(Stock stock) {
        try {
            // 가격 범위 검증
            if (stock.getClosePrice() < TradingConstants.MIN_STOCK_PRICE ||
                stock.getClosePrice() > TradingConstants.MAX_STOCK_PRICE) {
                return false;
            }
            
            // 거래량 검증
            if (stock.getVolume() < TradingConstants.MIN_TRADING_VOLUME) {
                return false;
            }
            
            // 변동률 검증 (과도한 급등락 제외)
            Float fluctuationRate = stock.getFluctuationRate();
            if (fluctuationRate != null && 
                Math.abs(fluctuationRate) > TradingConstants.MAX_FLUCTUATION_RATE) {
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            log.warn("종목 {} 유효성 검증 실패", stock.getCode(), e);
            return false;
        }
    }

    // ======================== 개별 종목 매매 실행 ========================

    /**
     * 개별 종목에 대한 매매 실행
     * 
     * @param aiBot AI 봇 계정
     * @param account 봇 계좌
     * @param strategy 매매 전략
     * @param stockCode 종목 코드
     */
    private void executeStockTrading(User aiBot, Account account, String strategy, String stockCode) {
        Integer botId = aiBot.getUserId();
        
        try {
            log.debug("AI 봇 {} 종목 {} 매매 분석 시작", botId, stockCode);
            
            // 1. 현재 주식 정보 조회
            Stock currentStock = getCurrentStockInfo(stockCode);
            if (currentStock == null) {
                log.warn("종목 {} 정보 조회 실패", stockCode);
                return;
            }
            
            // 2. 기술적 분석 수행
            String signal = performTechnicalAnalysis(stockCode, strategy);
            if (TradingConstants.SIGNAL_HOLD.equals(signal)) {
                log.debug("AI 봇 {} 종목 {} 신호: 보유", botId, stockCode);
                return;
            }
            
            // 3. 포지션 크기 계산
            Integer positionSize = calculatePositionSize(account, currentStock, signal);
            if (positionSize <= 0) {
                log.debug("AI 봇 {} 종목 {} 포지션 크기 부족", botId, stockCode);
                return;
            }
            
            // 4. 매매 실행
            boolean success = executeTrade(aiBot, account, currentStock, signal, positionSize, strategy);
            
            // 5. 거래 결과 기록
            recordTradingResult(aiBot, stockCode, signal, positionSize, success, strategy);
            
        } catch (Exception e) {
            log.error("AI 봇 {} 종목 {} 매매 실행 실패", botId, stockCode, e);
        }
    }

    /**
     * 현재 주식 정보 조회
     * 실시간 가격 반영을 위해 STOCK_MINUTE에서 최신 가격 우선 조회
     *
     * @param stockCode 종목 코드
     * @return 주식 정보 (없으면 null)
     */
    private Stock getCurrentStockInfo(String stockCode) {
        try {
            // 1. 기본 주식 정보 조회
            Stock stock = stockRepository.findTopByCodeOrderByUpdatedAtDesc(stockCode)
                .orElse(null);
                
            if (stock == null) {
                log.warn("종목 {} 기본 정보 없음", stockCode);
                return null;
            }

            // 2. 최신 분봉 데이터에서 실시간 가격 조회
            try {
                var latestMinute = stockMinuteRepository
                    .findTopByStockInfo_CodeOrderByDateDesc(stockCode);

                if (latestMinute.isPresent()) {
                    var minuteData = latestMinute.get();
                    // 최신 분봉의 종가를 현재가로 사용
                    stock.setClosePrice(minuteData.getClosePrice());
                    log.debug("종목 {} 실시간 가격 업데이트: {}원", stockCode, minuteData.getClosePrice());
                }
            } catch (Exception minuteEx) {
                log.debug("종목 {} 분봉 데이터 조회 실패, 기본 가격 사용: {}", stockCode, minuteEx.getMessage());
            }

            return stock;

        } catch (Exception e) {
            log.error("종목 {} 정보 조회 실패", stockCode, e);
            return null;
        }
    }

    /**
     * 기술적 분석 수행
     * 
     * @param stockCode 종목 코드
     * @param strategy 분석 전략
     * @return 매매 신호 (BUY/SELL/HOLD)
     */
    private String performTechnicalAnalysis(String stockCode, String strategy) {
        try {
            return technicalAnalysisService.generateTradingSignal(stockCode, strategy);
            
        } catch (Exception e) {
            log.error("종목 {} 기술적 분석 실패", stockCode, e);
            return TradingConstants.SIGNAL_HOLD;
        }
    }

    /**
     * 포지션 크기 계산 (리스크 관리)
     * 
     * 계산 방식:
     * - 매수: 계좌 잔액의 일정 비율
     * - 매도: 보유 주식 수량의 일정 비율
     * 
     * @param account 계좌 정보
     * @param stock 주식 정보
     * @param signal 매매 신호
     * @return 거래 수량
     */
    private Integer calculatePositionSize(Account account, Stock stock, String signal) {
        try {
            if (TradingConstants.SIGNAL_BUY.equals(signal)) {
                return calculateBuyPositionSize(account, stock);
            } else if (TradingConstants.SIGNAL_SELL.equals(signal)) {
                return calculateSellPositionSize(account, stock);
            }
            return 0;
            
        } catch (Exception e) {
            log.error("포지션 크기 계산 실패: signal={}", signal, e);
            return 0;
        }
    }

    /**
     * 매수 포지션 크기 계산
     * 수수료(0.25%)를 고려한 실제 매수 가능 수량 계산
     *
     * @param account 계좌 정보
     * @param stock 주식 정보
     * @return 매수 수량
     */
    private Integer calculateBuyPositionSize(Account account, Stock stock) {
        try {
            // 계좌 잔액의 일정 비율로 매수 (10%)
            int availableAmount = (int) (account.getBalance() * TradingConstants.POSITION_SIZE_RATIO);
            int stockPrice = stock.getClosePrice();

            // 수수료를 고려한 실제 필요 금액 계산
            // 필요 금액 = 주가 * 수량 * (1 + 수수료율)
            // 수량 = 사용 가능 금액 / (주가 * (1 + 수수료율))
            double commissionRate = 0.0025; // 0.25%
            int maxQuantity = (int) (availableAmount / (stockPrice * (1 + commissionRate)));

            // 최소 1주는 매수할 수 있어야 함
            if (maxQuantity <= 0) {
                log.debug("잔액 부족으로 매수 불가: 잔액={}, 주가={}, 필요금액={}",
                    account.getBalance(), stockPrice, (int)(stockPrice * (1 + commissionRate)));
                return 0;
            }

            // 최대 거래 수량 제한 적용
            int finalQuantity = Math.min(maxQuantity, TradingConstants.MAX_POSITION_SIZE);

            log.debug("매수 수량 계산: 사용가능={}원, 주가={}원, 매수={}주",
                availableAmount, stockPrice, finalQuantity);

            return finalQuantity;

        } catch (Exception e) {
            log.error("매수 수량 계산 실패: account={}, stock={}",
                account.getAccountId(), stock.getCode(), e);
            return 0;
        }
    }

    /**
     * 매도 포지션 크기 계산
     * 실제 보유 수량을 기반으로 매도 가능 수량 계산
     *
     * @param account 계좌 정보
     * @param stock 주식 정보
     * @return 매도 수량
     */
    private Integer calculateSellPositionSize(Account account, Stock stock) {
        try {
            // 보유 주식 수량 조회
            Optional<StockHolding> holdingOpt = stockHoldingRepository
                .findByAccountIdAndStockCode(account.getAccountId(), stock.getCode());

            if (holdingOpt.isEmpty()) {
                log.debug("계좌 {} 종목 {} 보유하지 않음", account.getAccountId(), stock.getCode());
                return 0; // 보유하지 않으면 매도 불가
            }

            StockHolding holding = holdingOpt.get();
            if (holding.getQuantity() <= 0) {
                log.debug("계좌 {} 종목 {} 보유 수량 0", account.getAccountId(), stock.getCode());
                return 0;
            }

            // 보유 수량의 50% 매도 (리스크 분산)
            int sellQuantity = Math.max(1, holding.getQuantity() / 2);

            // 최대 매도 수량 제한 적용
            sellQuantity = Math.min(sellQuantity, TradingConstants.MAX_POSITION_SIZE);

            log.debug("매도 수량 계산: 보유={}주, 매도={}주", holding.getQuantity(), sellQuantity);
            return sellQuantity;
            
        } catch (Exception e) {
            log.error("매도 수량 계산 실패: account={}, stock={}",
                account.getAccountId(), stock.getCode(), e);
            return 0;
        }
    }

    // ======================== 매매 실행 ========================

    /**
     * 실제 매매 주문 실행
     * 
     * @param aiBot AI 봇 계정
     * @param account 계좌 정보
     * @param stock 주식 정보
     * @param signal 매매 신호
     * @param quantity 거래 수량
     * @param strategy 매매 전략
     * @return 거래 성공 여부
     */
    private boolean executeTrade(User aiBot, Account account, Stock stock, 
                               String signal, Integer quantity, String strategy) {
        try {
            log.info("AI 봇 {} 매매 실행: {} {} {}주", 
                    aiBot.getUserId(), stock.getCode(), signal, quantity);
            
            // 거래 요청 DTO 생성
            MockTradeRequest tradeRequest = createTradeRequest(
                stock.getCode(), signal, quantity, stock.getClosePrice()
            );
            
            // 거래 실행 로그
            TradingUtils.logTradeStart(
                aiBot.getUserId(), 
                stock.getCode(), 
                signal, 
                quantity, 
                stock.getClosePrice()
            );
            
            // 모의매매 서비스를 통한 거래 실행
            if (TradingConstants.SIGNAL_BUY.equals(signal)) {
                mockTradingService.processBuyOrder(aiBot.getUserId(), tradeRequest);
                // 매수 후 StockHolding 업데이트
                updateStockHoldingAfterBuy(account, stock, quantity, tradeRequest.getPrice());
            } else if (TradingConstants.SIGNAL_SELL.equals(signal)) {
                mockTradingService.processSellOrder(aiBot.getUserId(), tradeRequest);
                // 매도 후 StockHolding 업데이트
                updateStockHoldingAfterSell(account, stock, quantity, tradeRequest.getPrice());
            }
            
            log.info("AI 봇 {} 매매 성공: {} {} {}주", 
                    aiBot.getUserId(), stock.getCode(), signal, quantity);
            return true;
            
        } catch (Exception e) {
            log.error("AI 봇 {} 매매 실행 실패: {} {} {}주", 
                     aiBot.getUserId(), stock.getCode(), signal, quantity, e);
            return false;
        }
    }

    /**
     * 매수 후 StockHolding 업데이트
     *
     * @param account 계좌 정보
     * @param stock 주식 정보
     * @param quantity 매수 수량
     * @param price 매수 단가
     */
    private void updateStockHoldingAfterBuy(Account account, Stock stock, Integer quantity, Integer price) {
        try {
            // 수수료 계산 (0.25%)
            Integer commission = (int) Math.round(price * quantity * 0.0025);

            Optional<StockHolding> existingHolding = stockHoldingRepository
                .findByAccountIdAndStockCode(account.getAccountId(), stock.getCode());

            if (existingHolding.isPresent()) {
                // 기존 보유 주식에 추가 매수
                StockHolding holding = existingHolding.get();
                holding.addStock(quantity, price, commission);
                holding.updateCurrentValue(stock.getClosePrice());
                stockHoldingRepository.save(holding);
                log.debug("기존 보유 주식 업데이트: {}주 추가, 총 {}주", quantity, holding.getQuantity());
            } else {
                // 신규 주식 보유
                StockHolding newHolding = StockHolding.builder()
                    .account(account)
                    .stockCode(stock.getCode())
                    .quantity(quantity)
                    .averagePrice(price)
                    .totalCost(price * quantity + commission)
                    .firstPurchaseDate(LocalDateTime.now())
                    .lastTradeDate(LocalDateTime.now())
                    .build();
                newHolding.updateCurrentValue(stock.getClosePrice());
                stockHoldingRepository.save(newHolding);
                log.debug("신규 주식 보유 생성: {} {}주", stock.getCode(), quantity);
            }

        } catch (Exception e) {
            log.error("매수 후 StockHolding 업데이트 실패: account={}, stock={}, quantity={}",
                account.getAccountId(), stock.getCode(), quantity, e);
        }
    }

    /**
     * 매도 후 StockHolding 업데이트
     *
     * @param account 계좌 정보
     * @param stock 주식 정보
     * @param quantity 매도 수량
     * @param price 매도 단가
     */
    private void updateStockHoldingAfterSell(Account account, Stock stock, Integer quantity, Integer price) {
        try {
            Optional<StockHolding> holdingOpt = stockHoldingRepository
                .findByAccountIdAndStockCode(account.getAccountId(), stock.getCode());

            if (holdingOpt.isEmpty()) {
                log.warn("매도할 주식이 보유 목록에 없음: account={}, stock={}",
                    account.getAccountId(), stock.getCode());
                return;
            }

            StockHolding holding = holdingOpt.get();

            // 수수료 계산 (0.25%)
            Integer commission = (int) Math.round(price * quantity * 0.0025);

            // 매도 처리
            holding.sellStock(quantity, price, commission);
            holding.updateCurrentValue(stock.getClosePrice());

            if (holding.getQuantity() <= 0) {
                // 전량 매도 시 보유 기록 삭제
                stockHoldingRepository.delete(holding);
                log.debug("전량 매도로 보유 기록 삭제: {}", stock.getCode());
            } else {
                stockHoldingRepository.save(holding);
                log.debug("일부 매도 완료: {}주 매도, 잔여 {}주", quantity, holding.getQuantity());
            }

        } catch (Exception e) {
            log.error("매도 후 StockHolding 업데이트 실패: account={}, stock={}, quantity={}",
                account.getAccountId(), stock.getCode(), quantity, e);
        }
    }

    /**
     * 거래 요청 DTO 생성
     * 
     * @param stockCode 종목 코드
     * @param tradeType 거래 타입
     * @param quantity 거래 수량
     * @param price 거래 단가
     * @return 거래 요청 DTO
     */
    private MockTradeRequest createTradeRequest(String stockCode, String tradeType, 
                                                  Integer quantity, Integer price) {
        MockTradeRequest request = new MockTradeRequest();
        request.setStockCode(stockCode);
        request.setQuantity(quantity);
        request.setPrice(price);
        request.setTradeType(TradeType.valueOf(tradeType));
        return request;
    }

    // ======================== 거래 결과 기록 ========================

    /**
     * 전략명으로 AiStrategy 엔티티 조회
     * 
     * @param strategyName 전략명
     * @param userId 사용자 ID
     * @return AiStrategy 엔티티 (없으면 기본 전략 생성)
     */
    private AiStrategy findOrCreateStrategy(String strategyName, Integer userId) {
        try {
            // 기존 전략 조회
            Optional<AiStrategy> existingStrategy = aiStrategyRepository.findByUserIdAndStrategy(userId, strategyName);
            
            if (existingStrategy.isPresent()) {
                return existingStrategy.get();
            }
            
            // 기본 전략 생성
            AiStrategy newStrategy = AiStrategy.builder()
                .userId(userId)
                .strategy(strategyName)
                .prompt("AI 자동매매 기본 전략")
                .build();
                
            return aiStrategyRepository.save(newStrategy);
            
        } catch (Exception e) {
            log.error("전략 조회/생성 실패: strategy={}, userId={}", strategyName, userId, e);
            // 임시 전략 객체 반환 (저장하지 않음)
            return AiStrategy.builder()
                .userId(userId)
                .strategy(strategyName)
                .prompt("임시 전략")
                .build();
        }
    }

    /**
     * 거래 결과 기록
     * 
     * @param aiBot AI 봇 계정
     * @param stockCode 종목 코드
     * @param signal 매매 신호
     * @param quantity 거래 수량
     * @param success 거래 성공 여부
     * @param strategyName 사용된 전략명
     */
    private void recordTradingResult(User aiBot, String stockCode, String signal, 
                                   Integer quantity, boolean success, String strategyName) {
        try {
            // 전략 엔티티 조회/생성
            AiStrategy strategy = findOrCreateStrategy(strategyName, aiBot.getUserId());
            
            AiBotTrade tradeRecord = AiBotTrade.builder()
                .userId(aiBot.getUserId())
                .strategy(strategy)  // AiStrategy 엔티티 사용
                .stockCode(stockCode)
                .tradeType(signal)
                .quantity(quantity)
                .price(0)  // 임시값, 실제 구현시 currentStock.getClosePrice() 사용
                .totalAmount(0)  // 임시값, 실제 계산 필요
                .marketPrice(0)  // 임시값, 실제 시장가 필요
                .volume(0L)  // 임시값, 실제 거래량 필요
                .result(success ? "SUCCESS" : "FAILED")
                .log(String.format("종목: %s, 신호: %s, 수량: %d", stockCode, signal, quantity))
                .tradeAt(LocalDateTime.now())
                .signalTime(LocalDateTime.now())
                .build();
                
            aiBotTradeRepository.save(tradeRecord);
            
            log.debug("AI 봇 {} 거래 결과 기록 완료", aiBot.getUserId());
            
        } catch (Exception e) {
            log.error("AI 봇 {} 거래 결과 기록 실패", aiBot.getUserId(), e);
        }
    }

    /**
     * 거래 실패 기록
     * 
     * @param aiBot AI 봇 계정
     * @param errorMessage 실패 사유
     */
    private void recordTradingFailure(User aiBot, String errorMessage) {
        try {
            // 시스템 오류 전략 생성
            AiStrategy errorStrategy = findOrCreateStrategy("SYSTEM_ERROR", aiBot.getUserId());
            
            AiBotTrade failureRecord = AiBotTrade.builder()
                .userId(aiBot.getUserId())
                .strategy(errorStrategy)  // AiStrategy 엔티티 사용
                .stockCode("ERROR")
                .tradeType("ERROR")
                .quantity(0)
                .price(0)
                .totalAmount(0)
                .marketPrice(0)
                .volume(0L)
                .result("FAILED")
                .log("거래 실행 실패: " + errorMessage)
                .tradeAt(LocalDateTime.now())
                .signalTime(LocalDateTime.now())
                .build();
                
            aiBotTradeRepository.save(failureRecord);
            
        } catch (Exception e) {
            log.error("AI 봇 {} 실패 기록 저장 중 오류", aiBot.getUserId(), e);
        }
    }
}
