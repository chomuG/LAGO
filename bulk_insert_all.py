import pandas as pd
import psycopg2
from psycopg2.extras import execute_batch
import os
import re
from datetime import datetime

# DB 연결 정보
DB_CONFIG = {
    'host': 'i13d203.p.ssafy.io',
    'port': 5432,
    'database': 'stock_db', 
    'user': 'ssafyuser',
    'password': 'ssafypw!!'
}

# 완전한 매핑 테이블 (CSV 코드 -> DB 코드 -> stock_info_id)
CSV_TO_DB_MAPPING = {
    '005930': '5930',   # 삼성전자 -> 1
    '000270': '270',    # 기아 -> 2  
    '003670': '3670',   # 포스코퓨처엠 -> 3
    '005380': '5380',   # 현대차 -> 4
    '005490': '5490',   # 포스코홀딩스 -> 5
    '006400': '6400',   # 삼성SDI -> 6
    '012330': '12330',  # 현대모비스 -> 7
    '015760': '15760',  # 한국전력 -> 8
    '017670': '17670',  # SK텔레콤 -> 9
    '017810': '17810',  # 풀무원 -> 10
    '028260': '28260',  # 삼성물산 -> 11
    '032640': '32640',  # LG유플러스 -> 12
    '032830': '32830',  # 삼성생명 -> 13
    '033780': '33780',  # KT&G -> 14
    '034020': '34020',  # 두산에너빌리티 -> 15
    '035420': '35420',  # 네이버 -> 16
    '036460': '36460',  # 한국가스공사 -> 17
    '058470': '58470',  # 리노공업 -> 18
    '005940': '5940',   # NH투자증권 -> 19
    '066570': '66570',  # LG전자 -> 20
    '066970': '66970',  # 엘앤에프 -> 21
    '068270': '68270',  # 셀트리온 -> 22
    '086790': '86790',  # 하나금융지주 -> 23
    '088980': '88980',  # 맥쿼리인프라 -> 24
    '090430': '90430',  # 아모레퍼시픽 -> 25
    '097950': '97950',  # CJ제일제당 -> 26
    '105560': '105560', # KB금융 -> 27
    '196170': '196170', # 알테오젠 -> 28
    '207940': '207940', # 삼성바이오로직스 -> 29
    '247540': '247540', # 에코프로비엠 -> 30
    '252670': '252670', # 코스피200TR -> 31
    '263750': '263750', # 펄어비스 -> 32
    '267260': '267260', # HD현대일렉트릭 -> 33
    '293490': '293490', # 카카오게임즈 -> 34
    '316140': '316140', # 우리금융지주 -> 35
    '373220': '373220', # LG에너지솔루션 -> 36
    '000660': '660',    # SK하이닉스 -> 37
    '051910': '51910',  # LG화학 -> 38
    '055550': '55550',  # 신한지주 -> 39
    '030200': '30200'   # KT -> 40
}

DB_STOCK_INFO = {
    '5930': 1, '270': 2, '3670': 3, '5380': 4, '5490': 5,
    '6400': 6, '12330': 7, '15760': 8, '17670': 9, '17810': 10,
    '28260': 11, '32640': 12, '32830': 13, '33780': 14, '34020': 15,
    '35420': 16, '36460': 17, '58470': 18, '5940': 19, '66570': 20,
    '66970': 21, '68270': 22, '86790': 23, '88980': 24, '90430': 25,
    '97950': 26, '105560': 27, '196170': 28, '207940': 29, '247540': 30,
    '252670': 31, '263750': 32, '267260': 33, '293490': 34, '316140': 35,
    '373220': 36, '660': 37, '51910': 38, '55550': 39, '30200': 40
}

def process_all_csv_files():
    """모든 CSV 파일 처리"""
    csv_dir = r"C:\Users\SSAFY\Desktop\project\차트 데이터\분봉 최신화(25. 8. 14.)"
    csv_files = [f for f in os.listdir(csv_dir) if f.endswith('.csv')]
    
    print(f"🚀 전체 {len(csv_files)}개 CSV 파일 대량 삽입 시작!")
    print(f"📅 시작 시간: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    
    all_data = []
    processed_files = 0
    skipped_files = 0
    
    for csv_file in csv_files:
        try:
            # 파일명에서 종목코드 추출
            match = re.search(r'\((\d{6})\)', csv_file)
            if not match:
                print(f"⚠️  코드 추출 실패: {csv_file}")
                skipped_files += 1
                continue
                
            csv_code = match.group(1)
            
            # 매핑 확인
            if csv_code not in CSV_TO_DB_MAPPING:
                print(f"⚠️  매핑 없음: {csv_file} ({csv_code})")
                skipped_files += 1
                continue
                
            db_code = CSV_TO_DB_MAPPING[csv_code]
            if db_code not in DB_STOCK_INFO:
                print(f"⚠️  DB에 없음: {csv_file} ({db_code})")
                skipped_files += 1
                continue
                
            stock_info_id = DB_STOCK_INFO[db_code]
            
            # CSV 파일 처리
            filepath = os.path.join(csv_dir, csv_file)
            df = pd.read_csv(filepath, encoding='utf-8-sig')
            
            # 데이터 변환
            for _, row in df.iterrows():
                ts = pd.to_datetime(row['datetime']).strftime('%Y-%m-%d %H:%M:%S+09')
                all_data.append((
                    stock_info_id,
                    ts,
                    int(row['open']),
                    int(row['high']), 
                    int(row['low']),
                    int(row['close']),
                    int(row['volume'])
                ))
            
            processed_files += 1
            print(f"✅ {processed_files:2d}/40 {csv_file[:25]}... -> ID:{stock_info_id} ({len(df):,}건)")
            
            # 메모리 관리 (50만건마다 DB 삽입)
            if len(all_data) >= 500000:
                print(f"💾 중간 삽입: {len(all_data):,}건")
                bulk_insert_to_db(all_data)
                all_data = []
            
        except Exception as e:
            print(f"❌ {csv_file} 처리 실패: {e}")
            skipped_files += 1
            continue
    
    # 남은 데이터 삽입
    if all_data:
        print(f"💾 최종 삽입: {len(all_data):,}건")
        bulk_insert_to_db(all_data)
    
    print(f"\n🎉 처리 완료!")
    print(f"✅ 성공: {processed_files}/40개 파일")
    print(f"❌ 실패: {skipped_files}/40개 파일")
    print(f"📅 종료 시간: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    
    return processed_files, skipped_files

def bulk_insert_to_db(data_rows):
    """대량 데이터 삽입"""
    if not data_rows:
        return
        
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        cursor = conn.cursor()
        
        # 배치 삽입 (충돌시 업데이트) - execute_batch 호환 형식
        insert_query = """
            INSERT INTO ticks (stock_info_id, ts, open_price, high_price, low_price, close_price, volume) 
            VALUES (%s, %s, %s, %s, %s, %s, %s)
            ON CONFLICT (stock_info_id, ts) DO UPDATE SET
                open_price = EXCLUDED.open_price,
                high_price = EXCLUDED.high_price,
                low_price = EXCLUDED.low_price,
                close_price = EXCLUDED.close_price,
                volume = EXCLUDED.volume
        """
        
        # 5000개씩 배치 처리
        batch_size = 5000
        total_inserted = 0
        
        for i in range(0, len(data_rows), batch_size):
            batch = data_rows[i:i + batch_size]
            execute_batch(cursor, insert_query, batch, page_size=batch_size)
            total_inserted += len(batch)
            
            if total_inserted % 50000 == 0:
                print(f"   진행: {total_inserted:,}건 삽입 완료")
        
        conn.commit()
        cursor.close()
        conn.close()
        
        print(f"   ✅ 총 {total_inserted:,}건 삽입 완료")
        
    except Exception as e:
        print(f"   ❌ DB 삽입 실패: {e}")
        if 'conn' in locals():
            conn.rollback()

def check_final_results():
    """최종 결과 확인"""
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        cursor = conn.cursor()
        
        # 전체 데이터 수
        cursor.execute("SELECT COUNT(*) FROM ticks;")
        total_count = cursor.fetchone()[0]
        
        # 종목별 데이터 수
        cursor.execute("""
            SELECT stock_info_id, COUNT(*) as count
            FROM ticks 
            GROUP BY stock_info_id 
            ORDER BY stock_info_id
        """)
        stock_counts = cursor.fetchall()
        
        cursor.close()
        conn.close()
        
        print(f"\n📊 최종 결과:")
        print(f"전체 데이터: {total_count:,}건")
        print(f"종목별 현황:")
        for stock_id, count in stock_counts:
            print(f"  stock_info_id {stock_id:2d}: {count:,}건")
            
    except Exception as e:
        print(f"❌ 결과 확인 실패: {e}")

if __name__ == "__main__":
    print("=" * 60)
    print("🚀 LAGO 프로젝트 - 전체 CSV 데이터 대량 삽입")
    print("=" * 60)
    
    # 확인
    response = input("40개 파일 전체를 삽입하시겠습니까? (y/N): ").lower()
    if response != 'y':
        print("❌ 사용자가 취소했습니다.")
        exit()
    
    # 실행
    success_count, fail_count = process_all_csv_files()
    
    # 결과 확인
    if success_count > 0:
        check_final_results()
    
    print("\n🎉 모든 작업이 완료되었습니다!")