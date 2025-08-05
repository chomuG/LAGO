import pandas as pd 
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
    print("패턴 감지 시작")
    
    # DB 연결
    conn = pymysql.connect(host='i13D203.p.ssafy.io', port=3306, user='ssafyuser', password='ssafypw!', db='stock_db')

    # 최근 200개 데이터 조회
    ohlc = pd.read_sql("""
        SELECT date, open_price, high_price, low_price, close_price, volume
        FROM STOCK_MINUTE
        WHERE stock_info_id=26
        LIMIT 200
    """, conn)
    
    print(ohlc)
    print("DB 데이터 조회 완료")

    detected_patterns = []

    # # CSV 파일 불러오기
    # ohlc = pd.read_csv("data/eurusd-4h.csv")
    # ohlc = ohlc.iloc[400:440,:].reset_index()
    # print(ohlc)
    
    # 1-1 더블 탑
    ohlc = find_doubles_pattern(ohlc, double="tops")
    df   = ohlc[ohlc["double_idx"].str.len() > 0]

    if df.shape[0] > 0:
        print("더블 탑 패턴이 감지되었습니다!")
        result = {
            "pattern_name": "더블 탑 패턴",
            "reason": "고점이 두 번 형성되며 상승 추세가 멈추고 하락 반전될 가능성을 나타내는 패턴입니다."
        }
        detected_patterns.append(result)
        display_chart_pattern(ohlc, pattern="double", save=True)
    else:
        print("더블 탑 패턴이 감지되지 않았습니다.")

    # 1-2 더블 바텀
    ohlc = find_doubles_pattern(ohlc, double="bottoms")
    df   = ohlc[ohlc["double_idx"].str.len() > 0]

    if df.shape[0] > 0:
        print("더블 바텀 패턴이 감지되었습니다!")
        result = {
            "pattern_name": "더블 바텀 패턴",
            "reason": "저점이 두 번 형성되며 하락 추세가 멈추고 상승 반전될 가능성을 나타내는 패턴입니다."
        }
        detected_patterns.append(result)
        display_chart_pattern(ohlc, pattern="double", save=True)
    else:
        print("더블 바텀 패턴이 감지되지 않았습니다.")

    # 2 플래그
    ohlc = find_flag_pattern(ohlc)
    df   = ohlc[ohlc["flag_point"]>0]

    if df.shape[0] > 0:
        print("플래그 패턴이 감지되었습니다!")
        result = {
            "pattern_name": "플래그 패턴",
            "reason": "급등락 이후 평행한 추세선 사이에서 가격이 일시적으로 조정되는 패턴으로, 기존 추세의 지속 가능성을 시사합니다."
        }
        detected_patterns.append(result)
        display_chart_pattern(ohlc, pattern="flag", save=True)
    else:
        print("플래그 패턴이 감지되지 않았습니다.")

    # 3 헤드앤숄더
    ohlc = find_head_and_shoulders(ohlc)
    df = ohlc[ohlc["hs_idx"].str.len()>0]

    if df.shape[0] > 0:
        print("헤드앤숄더 패턴이 감지되었습니다!")
        result = {
            "pattern_name": "헤드 앤 숄더 패턴",
            "reason": "고점이 세 번 형성되며 가운데 고점이 가장 높아 하락 반전을 암시하는 패턴입니다."
        }
        detected_patterns.append(result)
        display_chart_pattern(ohlc, pattern="hs", save=True)
    else:
        print("헤드앤숄더 패턴이 감지되지 않았습니다.")

    # 4 역헤드앤숄더
    ohlc = find_inverse_head_and_shoulders(ohlc)
    df = ohlc[ohlc["ihs_idx"].str.len() > 0]
        
    if df.shape[0] > 0:
        print("역헤드앤숄더 패턴이 감지되었습니다!")
        result = {
            "pattern_name": "역 헤드 앤 숄더 패턴",
            "reason": "저점이 세 번 형성되며 가운데 저점이 가장 낮아 상승 반전을 암시하는 패턴입니다."
        }
        detected_patterns.append(result)
        display_chart_pattern(ohlc, pattern="ihs", save=True, pivot_name="short_pivot")
    else:
        print("역헤드앤숄더 패턴이 감지되지 않았습니다.")

    # 5 페넌트 패턴
    ohlc = find_pennant(ohlc)
    df   = ohlc[ohlc["pennant_point"] > 0]
    
    if df.shape[0] > 0:
        print("페넌트 패턴이 감지되었습니다!")
        result = {
            "pattern_name": "페넌트 패턴",
            "reason": "급등락 후 고점과 저점이 수렴하는 삼각형 형태로, 강한 추세 후 휴식 구간을 나타내는 지속형 패턴입니다."
        }
        detected_patterns.append(result)
        display_chart_pattern(ohlc, pattern="pennant", save=True)
    else:
        print("페넌트 패턴이 감지되지 않았습니다.")

    # 6-1 상승 삼각형 패턴
    ohlc = find_triangle_pattern(ohlc, triangle_type = "ascending")
    df   = ohlc[ohlc["triangle_point"] > 0]

    if df.shape[0] > 0:
        print("상승 삼각형 패턴이 감지되었습니다!")
        result = {
            "pattern_name": "상승 삼각형",
            "reason": "고점이 일정하게 유지되고 저점이 점점 높아지는 구조로, 매수세가 점차 강해지는 패턴입니다."
        }
        detected_patterns.append(result)
        display_chart_pattern(ohlc, pattern="triangle", save=True)
    else:
        print("상승 삼각형 패턴이 감지되지 않았습니다.")

    # 6-2 하락 삼각형 패턴
    ohlc = find_triangle_pattern(ohlc, triangle_type = "descending")
    df   = ohlc[ohlc["triangle_point"] > 0]

    if df.shape[0] > 0:
        print("하락 삼각형 패턴이 감지되었습니다!")
        result = {
            "pattern_name": "대칭 삼각형",
            "reason": "저점이 일정하게 유지되고 고점이 점점 낮아지는 구조로, 매도세가 강해지는 패턴입니다."
        }
        detected_patterns.append(result)
        display_chart_pattern(ohlc, pattern="triangle", save=True)
    else:
        print("하락 삼각형 패턴이 감지되지 않았습니다.")

    # 6-3 대칭 삼각형 패턴
    ohlc = find_triangle_pattern(ohlc, triangle_type = "symmetrical")
    df   = ohlc[ohlc["triangle_point"] > 0]

    if df.shape[0] > 0:
        print("대칭 삼각형 패턴이 감지되었습니다!")
        result = {
            "pattern_name": "대칭 삼각형",
            "reason": "고점과 저점이 점차 수렴하는 구조로, 향후 큰 방향성이 나올 가능성이 높은 패턴입니다."
        }
        detected_patterns.append(result)
        display_chart_pattern(ohlc, pattern="triangle", save=True)
    else:
        print("대칭 삼각형 패턴이 감지되지 않았습니다.")

    return detected_patterns


def generate_judgement_reason(pattern_type, slmin, slmax, rmin, rmax, dates, direction=None, neckline_break=False):
    
    if pattern_type == "double_top":
        if len(dates) >= 2:
            reason = f"{dates[0]}와 {dates[1]}에 고점이 반복 형성되었으며, "
            if neckline_break:
                reason += "넥라인을 돌파해 하락 반전 가능성이 높습니다."
            else:
                reason += "아직 넥라인 돌파는 발생하지 않았습니다."
            return reason
        else:
            return "두 개의 고점이 형성되어야 하지만 충분하지 않습니다."

    elif pattern_type == "double_bottom":
        if len(dates) >= 2:
            reason = f"{dates[0]}와 {dates[1]}에 저점이 반복 형성되었으며, "
            if neckline_break:
                reason += "넥라인을 돌파해 상승 반전 가능성이 높습니다."
            else:
                reason += "아직 넥라인 돌파는 발생하지 않았습니다."
            return reason
        else:
            return "두 개의 저점이 형성되어야 하지만 충분하지 않습니다."
    
    elif pattern_type == "flag":
        trend = "상승 추세 후" if direction == "bullish" else "하락 추세 후"
        confidence = "높은 신뢰도로" if rmax and rmax > 0.9 else "다소 불확실한 흐름 속에서"
        return f"{trend} 깃발 형태의 조정 구간이 {confidence} 나타났습니다. 추세 지속 가능성이 있습니다."

    elif pattern_type == "head_and_shoulders":
        return f"좌우 어깨와 머리 형태로 고점이 점차 낮아지는 패턴입니다. 하락 반전 가능성이 있습니다. 주요 고점: {', '.join(dates)}"

    elif pattern_type == "inverse_head_and_shoulders":
        return f"역 헤드 앤 숄더 패턴으로 바닥 다지기 후 상승세로 전환될 가능성이 있습니다. 주요 저점: {', '.join(dates)}"
    
    elif pattern_type == "pennant":
        trend = "상승세" if direction == "bullish" else "하락세"
        return f"급격한 {trend} 이후 삼각 수렴형 조정이 발생했습니다. 패턴은 {', '.join(dates)}에 형성되었습니다."

    elif pattern_type == "ascending_triangle":
        if rmax > 0.9 and abs(slmax) < 0.001:
            return f"저항선을 여러 차례 돌파 시도했으며, {', '.join(dates)}에 고점이 형성되었습니다. 상승 가능성이 높습니다."
        else:
            return "고점이 일정한 수평선을 이루며 매수세가 점차 강해지는 모습입니다."

    elif pattern_type == "descending_triangle":
        if rmin > 0.9 and abs(slmin) < 0.001:
            return f"지지선을 여러 번 시험하는 하락형 패턴입니다. {', '.join(dates)}에 저점이 반복적으로 발생했습니다."
        else:
            return "저점이 수평선을 이루며 매도 압력이 강해지는 모습입니다."

    elif pattern_type == "symmetrical_triangle":
        return f"수렴형 삼각형 패턴으로, 고점과 저점이 점점 좁아지고 있습니다. 변동성 확대가 예상됩니다. ({', '.join(dates)} 기준)"


if __name__ == "__main__":
    detected_patterns = pattern_detect()
    print(detected_patterns)
