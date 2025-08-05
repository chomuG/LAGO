import pandas as pd 
import pytest
import os 
import pymysql

from doubles import find_doubles_pattern
from flag import find_flag_pattern
from head_and_shoulders import find_head_and_shoulders
from inverse_head_and_shoulders import find_inverse_head_and_shoulders
from pennant import find_pennant
from triangles import find_triangle_pattern

from plotting import display_chart_pattern

def pattern_detect():
    """ Detect chart pattern """
    print("test pattern detect")
    
    # # DB 연결
    # conn = pymysql.connect(host='i13D203.p.ssafy.io', port=3306, user='ssafyuser', password='ssafypw!', db='stock_db')

    # # 최근 200개 데이터 조회
    # ohlc = pd.read_sql("""
    #     SELECT date, open_price, high_price, low_price, close_price, volume
    #     FROM STOCK_YEAR
    #     WHERE stock_info_id=1
    # """, conn)
    
        # ORDER BY date DESC
        # LIMIT 200
    # Date, Open, High, Low, Close, Volume
    # 04.05.2003 21:00:00.000,1.12354,1.12354,1.12166,1.12274,95533.0976
    
    detected_patterns = []

    # CSV 파일 불러오기
    ohlc = pd.read_csv("data/eurusd-4h.csv")
    ohlc = ohlc.iloc[400:440,:].reset_index()
    # print(ohlc)
    
    # 1-1 더블 탑
    ohlc = find_doubles_pattern(ohlc, double="tops")
    df   = ohlc[ohlc["double_idx"].str.len() > 0]

    if df.shape[0] > 0:
        print("더블 탑 패턴이 감지되었습니다!")
        detected_patterns.append("더블 탑 패턴")
        display_chart_pattern(ohlc, pattern="double", save=True)
    else:
        print("더블 탑 패턴이 감지되지 않았습니다.")

    # 1-2 더블 바텀
    ohlc = find_doubles_pattern(ohlc, double="bottoms")
    df   = ohlc[ohlc["double_idx"].str.len() > 0]

    if df.shape[0] > 0:
        print("더블 바텀 패턴이 감지되었습니다!")
        detected_patterns.append("더블 바텀 패턴")
        display_chart_pattern(ohlc, pattern="double", save=True)
    else:
        print("더블 바텀 패턴이 감지되지 않았습니다.")

    # 2 플래그
    ohlc = find_flag_pattern(ohlc)
    df   = ohlc[ohlc["flag_point"]>0]

    if df.shape[0] > 0:
        print("플래그 패턴이 감지되었습니다!")
        detected_patterns.append("플래그 패턴")
        display_chart_pattern(ohlc, pattern="flag", save=True)
    else:
        print("플래그 패턴이 감지되지 않았습니다.")

    # 3 헤드앤숄더
    ohlc = find_head_and_shoulders(ohlc)
    df = ohlc[ohlc["hs_idx"].str.len()>0]

    if df.shape[0] > 0:
        print("헤드앤숄더 패턴이 감지되었습니다!")
        detected_patterns.append("헤드 앤 숄더 패턴")
        display_chart_pattern(ohlc, pattern="hs", save=True)
    else:
        print("헤드앤숄더 패턴이 감지되지 않았습니다.")

    # 4 역헤드앤숄더
    ohlc = find_inverse_head_and_shoulders(ohlc)
    df = ohlc[ohlc["ihs_idx"].str.len() > 0]
        
    if df.shape[0] > 0:
        print("역헤드앤숄더 패턴이 감지되었습니다!")
        detected_patterns.append("역 헤드 앤 숄더 패턴")
        display_chart_pattern(ohlc, pattern="ihs", save=True, pivot_name="short_pivot")
    else:
        print("역헤드앤숄더 패턴이 감지되지 않았습니다.")

    # 5 페넌트 패턴
    ohlc = find_pennant(ohlc)
    df   = ohlc[ohlc["pennant_point"] > 0]
    
    if df.shape[0] > 0:
        print("페넌트 패턴이 감지되었습니다!")
        detected_patterns.append("페넌트 패턴")
        display_chart_pattern(ohlc, pattern="pennant", save=True)
    else:
        print("페넌트 패턴이 감지되지 않았습니다.")

    # 6-1 상승 삼각형 패턴
    ohlc = find_triangle_pattern(ohlc, triangle_type = "ascending")
    df   = ohlc[ohlc["triangle_point"] > 0]

    if df.shape[0] > 0:
        print("상승 삼각형 패턴이 감지되었습니다!")
        detected_patterns.append("상승 삼각형 패턴")
        display_chart_pattern(ohlc, pattern="triangle", save=True)
    else:
        print("상승 삼각형 패턴이 감지되지 않았습니다.")

    # 6-2 하락 삼각형 패턴
    ohlc = find_triangle_pattern(ohlc, triangle_type = "descending")
    df   = ohlc[ohlc["triangle_point"] > 0]

    if df.shape[0] > 0:
        print("하락 삼각형 패턴이 감지되었습니다!")
        detected_patterns.append("하락 삼각형 패턴")
        display_chart_pattern(ohlc, pattern="triangle", save=True)
    else:
        print("하락 삼각형 패턴이 감지되지 않았습니다.")

    # 6-3 대칭 삼각형 패턴
    ohlc = find_triangle_pattern(ohlc, triangle_type = "symmetrical")
    df   = ohlc[ohlc["triangle_point"] > 0]

    if df.shape[0] > 0:
        print("대칭 삼각형 패턴이 감지되었습니다!")
        detected_patterns.append("대칭 삼각형 패턴")
        display_chart_pattern(ohlc, pattern="triangle", save=True)
    else:
        print("대칭 삼각형 패턴이 감지되지 않았습니다.")

    
    # # 1-1 더블 탑
    # ohlc = pd.read_csv("data/eurusd-4h.csv")
    # ohlc = ohlc.iloc[:37,:].reset_index()

    # ohlc = find_doubles_pattern(ohlc, double="tops")
    # df   = ohlc[ohlc["double_idx"].str.len() > 0]

    # print(f"double top found: {df.shape[0]}")
    # if df.shape[0] > 0:
    #     print("더블 탑 패턴이 감지되었습니다!")
    # else:
    #     print("더블 탑 패턴이 감지되지 않았습니다.")

    # display_chart_pattern(ohlc, pattern="double", save=True)

    # # 1-2 더블 바텀
    # ohlc = pd.read_csv("data/eurusd-4h.csv")
    # ohlc = ohlc.iloc[400:440,:].reset_index()

    # ohlc = find_doubles_pattern(ohlc, double="bottoms")
    # df   = ohlc[ohlc["double_idx"].str.len() > 0]

    # print(f"double bottom found: {df.shape[0]}")
    # if df.shape[0] > 0:
    #     print("더블 바텀 패턴이 감지되었습니다!")
    # else:
    #     print("더블 바텀 패턴이 감지되지 않았습니다.")

    # display_chart_pattern(ohlc, pattern="double", save=True)

    # # 2 플래그
    # ohlc = pd.read_csv("./data/eurusd-4h.csv")
    # ohlc = ohlc.iloc[900:1200,:].reset_index()
    
    # ohlc = find_flag_pattern(ohlc)
    # df   = ohlc[ohlc["flag_point"]>0]

    # print(f"double bottom found: {df.shape[0]}")
    # if df.shape[0] > 0:
    #     print("플래그 패턴이 감지되었습니다!")
    # else:
    #     print("플래그 패턴이 감지되지 않았습니다.")

    # display_chart_pattern(ohlc, pattern="flag", save=True)

    # # 3 헤드앤숄더
    # ohlc = pd.read_csv("./data/eurusd-4h.csv")
    # ohlc = ohlc.iloc[4100:4400,:].reset_index()

    # ohlc = find_head_and_shoulders(ohlc)
    # df = ohlc[ohlc["hs_idx"].str.len()>0]

    # print(f"head and shoulder found: {df.shape[0]}")
    # if df.shape[0] > 0:
    #     print("헤드앤숄더 패턴이 감지되었습니다!")
    # else:
    #     print("헤드앤숄더 패턴이 감지되지 않았습니다.")

    # display_chart_pattern(ohlc, pattern="hs", save=True, pivot_name="short_pivot")

    # # 4 역헤드앤숄더
    # ohlc = pd.read_csv("./data/eurusd-4h.csv")
    # ohlc = ohlc.iloc[4700:5000,:].reset_index()

    # ohlc = find_inverse_head_and_shoulders(ohlc)
    # df = ohlc[ohlc["ihs_idx"].str.len() > 0]
        
    # print(f"inverse head and shoulder found: {df.shape[0]}")
    # if df.shape[0] > 0:
    #     print("역헤드앤숄더 패턴이 감지되었습니다!")
    # else:
    #     print("역헤드앤숄더 패턴이 감지되지 않았습니다.")

    # display_chart_pattern(ohlc, pattern="ihs", save=True, pivot_name="short_pivot")

    # # 5 페넌트 패턴
    # ohlc = pd.read_csv("./data/eurusd-4h.csv")
    # ohlc = ohlc.iloc[3400:3600,:].reset_index()

    # ohlc = find_pennant(ohlc)
    # df   = ohlc[ohlc["pennant_point"] > 0]
    
    # print(f"pennant found: {df.shape[0]}")
    # if df.shape[0] > 0:
    #     print("페넌트 패턴이 감지되었습니다!")
    # else:
    #     print("페넌트 패턴이 감지되지 않았습니다.")

    # display_chart_pattern(ohlc, pattern="pennant", save=True)

    # # 6-1 상승 삼각형 패턴
    # ohlc = pd.read_csv("data/eurusd-4h.csv")
    # ohlc = ohlc.iloc[7200:7400,:].reset_index()

    # ohlc = find_triangle_pattern(ohlc, triangle_type = "ascending")
    # df   = ohlc[ohlc["triangle_point"] > 0]

    # print(f"ascending triangle found: {df.shape[0]}")
    # if df.shape[0] > 0:
    #     print("상승 삼각형 패턴이 감지되었습니다!")
    # else:
    #     print("상승 삼각형 패턴이 감지되지 않았습니다.")

    # display_chart_pattern(ohlc, pattern="triangle", save=True)

    # # 6-2 하락 삼각형 패턴
    # ohlc = pd.read_csv("data/eurusd-4h.csv")
    # ohlc = ohlc.iloc[19100:19280,:].reset_index()

    # ohlc = find_triangle_pattern(ohlc, triangle_type = "descending")
    # df   = ohlc[ohlc["triangle_point"] > 0]

    # print(f"descending triangle found: {df.shape[0]}")
    # if df.shape[0] > 0:
    #     print("하락 삼각형 패턴이 감지되었습니다!")
    # else:
    #     print("하락 삼각형 패턴이 감지되지 않았습니다.")

    # display_chart_pattern(ohlc, pattern="triangle", save=True)

    # # 6-3 대칭 삼각형 패턴
    # ohlc = pd.read_csv("data/eurusd-4h.csv")
    # ohlc = ohlc.iloc[:160,:].reset_index()

    # ohlc = find_triangle_pattern(ohlc, triangle_type = "symmetrical")
    # df   = ohlc[ohlc["triangle_point"] > 0]

    # print(f"symmetrical triangle found: {df.shape[0]}")
    # if df.shape[0] > 0:
    #     print("대칭 삼각형 패턴이 감지되었습니다!")
    # else:
    #     print("대칭 삼각형 패턴이 감지되지 않았습니다.")

    # display_chart_pattern(ohlc, pattern="triangle", save=True)

    return detected_patterns

if __name__ == "__main__":
    detected_patterns = pattern_detect()
    print(detected_patterns)
