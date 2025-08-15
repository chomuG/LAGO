#!/usr/bin/env python3
"""
삼성전자 실시간 가격 변동 시뮬레이터
AI 자동매매봇 동작 테스트용
"""

import time
import random
import requests
import json
from datetime import datetime
import threading
import psycopg2
from psycopg2.extras import RealDictCursor

class SamsungPriceSimulator:
    def __init__(self):
        self.db_config = {
            'host': 'i13d203.p.ssafy.io',
            'database': 'stock_db', 
            'user': 'ssafyuser',
            'password': 'ssafypw!!'
        }
        self.stock_code = '005930'
        self.current_price = 75000  # 시작 가격
        self.base_price = 75000
        self.running = False
        
    def connect_db(self):
        """데이터베이스 연결"""
        return psycopg2.connect(**self.db_config)
    
    def update_ticks_data(self, price):
        """ticks 테이블에 새로운 가격 데이터 삽입"""
        try:
            conn = self.connect_db()
            cur = conn.cursor()
            
            # 현재 시간으로 새로운 tick 데이터 삽입
            cur.execute("""
                INSERT INTO ticks (ts, code, open_price, high_price, low_price, close_price, volume) 
                VALUES (NOW(), %s, %s, %s, %s, %s, %s)
            """, (self.stock_code, price, price + 500, price - 500, price, random.randint(100000, 500000)))
            
            conn.commit()
            print(f"🔄 ticks 업데이트: {price:,}원")
            
        except Exception as e:
            print(f"❌ DB 업데이트 실패: {e}")
        finally:
            if conn:
                conn.close()
    
    def generate_price_movement(self):
        """실제 주식처럼 가격 변동 생성"""
        # 변동률: -3% ~ +3%
        change_percent = random.uniform(-0.03, 0.03)
        change_amount = int(self.base_price * change_percent)
        
        # 점진적 변동 (이전 가격에서 최대 2% 변동)
        max_change = int(self.current_price * 0.02)
        actual_change = random.randint(-max_change, max_change)
        
        new_price = self.current_price + actual_change
        
        # 가격 범위 제한 (70000 ~ 80000)
        new_price = max(70000, min(80000, new_price))
        
        return new_price
    
    def start_simulation(self):
        """가격 변동 시뮬레이션 시작"""
        self.running = True
        print(f"🚀 삼성전자({self.stock_code}) 실시간 가격 시뮬레이션 시작!")
        print(f"📊 시작 가격: {self.current_price:,}원")
        print("=" * 60)
        
        iteration = 0
        while self.running:
            try:
                iteration += 1
                old_price = self.current_price
                
                # 새 가격 생성
                self.current_price = self.generate_price_movement()
                
                # 변동 정보 출력
                change = self.current_price - old_price
                change_percent = (change / old_price) * 100
                direction = "📈" if change > 0 else "📉" if change < 0 else "➡️"
                
                print(f"{direction} [{iteration:03d}] {datetime.now().strftime('%H:%M:%S')} | "
                      f"{self.current_price:,}원 ({change:+,}원, {change_percent:+.2f}%)")
                
                # DB 업데이트
                self.update_ticks_data(self.current_price)
                
                # 큰 변동이 있을 때 특별 표시
                if abs(change_percent) > 1.0:
                    signal_type = "🔥 강한 상승" if change_percent > 1.0 else "❄️ 급락"
                    print(f"   ⚡ {signal_type} 신호! ({change_percent:+.2f}%) - AI 봇 반응 예상")
                
                # 30초마다 구분선
                if iteration % 6 == 0:
                    print("-" * 60)
                
                time.sleep(5)  # 5초마다 가격 변동
                
            except KeyboardInterrupt:
                print("\n⏸️  시뮬레이션 중단 요청...")
                break
            except Exception as e:
                print(f"❌ 오류 발생: {e}")
                time.sleep(1)
        
        self.running = False
        print("🏁 시뮬레이션 종료")
    
    def stop_simulation(self):
        """시뮬레이션 중단"""
        self.running = False

def show_current_bots():
    """현재 AI 봇 상태 표시"""
    try:
        response = requests.get("http://localhost:8081/api/ai-bots")
        if response.status_code == 200:
            bots = response.json()
            print("\n🤖 현재 AI 봇 상태:")
            for bot in bots:
                print(f"   {bot['nickname']}: {bot['totalAsset']:,}원 (수익률: {bot['profitRate']:.2f}%)")
        else:
            print("❌ AI 봇 상태 조회 실패")
    except Exception as e:
        print(f"❌ AI 봇 상태 조회 오류: {e}")

def monitor_bot_trades():
    """AI 봇 거래 모니터링"""
    print("\n📊 AI 봇 거래 모니터링 (Ctrl+C로 종료)")
    
    last_trade_ids = {}
    
    try:
        while True:
            for bot_id in [1, 2, 3, 4]:
                try:
                    response = requests.get(f"http://localhost:8081/api/accounts/ai/{bot_id}/transactions")
                    if response.status_code == 200:
                        trades = response.json()
                        if trades:
                            latest_trade = trades[0]
                            trade_id = latest_trade['tradeId']
                            
                            # 새로운 거래 발견 시 알림
                            if bot_id not in last_trade_ids or last_trade_ids[bot_id] != trade_id:
                                last_trade_ids[bot_id] = trade_id
                                bot_names = {1: "화끈이", 2: "적극이", 3: "균형이", 4: "조심이"}
                                action = "🛒 매수" if latest_trade['buySell'] == 'BUY' else "💰 매도"
                                
                                print(f"🚨 {bot_names[bot_id]} {action}: {latest_trade['quantity']}주 @ {latest_trade['price']:,}원 "
                                      f"({latest_trade['tradeAt'][-8:-3]})")
                
                except:
                    pass
            
            time.sleep(10)  # 10초마다 체크
            
    except KeyboardInterrupt:
        print("\n📊 거래 모니터링 종료")

if __name__ == "__main__":
    simulator = SamsungPriceSimulator()
    
    print("🎯 삼성전자 실시간 가격 시뮬레이터")
    print("=" * 60)
    
    # AI 봇 현재 상태 표시
    show_current_bots()
    
    print(f"\n📋 시뮬레이션 설정:")
    print(f"   종목: 삼성전자 ({simulator.stock_code})")  
    print(f"   시작가격: {simulator.current_price:,}원")
    print(f"   변동주기: 5초마다")
    print(f"   변동범위: 70,000 ~ 80,000원")
    print("\n🚀 시뮬레이션을 시작하려면 Enter를 누르세요...")
    input()
    
    # 거래 모니터링을 별도 스레드에서 실행
    monitor_thread = threading.Thread(target=monitor_bot_trades, daemon=True)
    monitor_thread.start()
    
    # 가격 시뮬레이션 시작
    simulator.start_simulation()