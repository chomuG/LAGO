"""
Date   : 2023-01-06
Author : Zetra Team
Function used to find the Double chart patterns
"""

import numpy as np
import pandas as pd 
import plotly.graph_objects as go
import logging

from tqdm import tqdm

def find_doubles_pattern(ohlc: pd.DataFrame, lookback: int = 25, double: str = "tops", 
                         tops_max_ratio: float = 1.01, bottoms_min_ratio: float = 0.98,
                         progress: bool = False) -> pd.DataFrame:
    """
    Find the Double chart patterns 
    
    :params ohlc is the OHLC dataframe 
    :type :pd.DataFrame
    
    :params lookback is the number of periods to use for back candles
    :type :int 
    
    :params double is a options string variable of the type of doubles chart pattern that needs to be identifed. ['tops', 'bottoms', 'both']
    :type :str 
    
    :params tops_max_ratio is the max ratio between the peak points in the tops chart pattern
    :type :float
    
    params bottoms_min_ratio is the min ratio between the trough points in the bottoms chart pattern
    :type :float 
    
    :params progress bar to be displayed or not 
    :type:bool
    
    :return (pd.DataFrame)
    """
    
    # Placeholders for the Double patterns     
    ohlc["double_type"]   = ""
    ohlc["chart_type"]    = ""
    ohlc["double_idx"]    = [np.array([]) for _ in range(len(ohlc)) ]
    ohlc["double_point"]  = [np.array([]) for _ in range(len(ohlc)) ]
    
    candle_iter = reversed(range(lookback, len(ohlc)))
    if progress:
        candle_iter = tqdm(candle_iter, desc="Finding doubles patterns...")
           
    for candle_idx in candle_iter:
        sub_ohlc = ohlc.loc[candle_idx-lookback: candle_idx,:]
        
        pivot_indx = [ i for i, p in zip(sub_ohlc["pivot"].index.values, sub_ohlc["pivot"].tolist()) if p != 0 ]
        if len(pivot_indx) != 5:
            continue
        
        if len(pivot_indx) == 5: # Must have only 5 pivots
            pivots = ohlc.loc[pivot_indx, "pivot_pos"].tolist() 
            
            # Find Double Tops
            if double == "tops" or double == "both":
                if (pivots[0] < pivots[1]) and (pivots[0] < pivots[3]) and (pivots[2] < pivots[1]) and \
                    (pivots[2] < pivots[3]) and (pivots[4] < pivots[1]) and (pivots[4] < pivots[3]) and \
                        (pivots[1] > pivots[3]) and (pivots[1]/pivots[3] <= tops_max_ratio):  
                        ohlc.at[candle_idx, "double_idx"]     = pivot_indx
                        ohlc.at[candle_idx, "double_point"]   = pivots
                        ohlc.loc[candle_idx, "double_type"]   = "tops"
                        ohlc.loc[candle_idx, "chart_type"]    = "double"

                        # === 판단 근거 출력 ===
                        logging.debug("\n=== Double Top Detected ===")
                        logging.debug(f"candle_idx: {candle_idx}")
                        break
                
                        # 추가로 판단 근거를 df에 컬럼으로 저장
                        
            # Find Double Bottoms            
            elif double == "bottoms" or double == "both":
              if (pivots[0] > pivots[1]) and (pivots[0] > pivots[3]) and (pivots[2] > pivots[1]) and \
                    (pivots[2] > pivots[3]) and (pivots[4] > pivots[1]) and (pivots[4] > pivots[3]) and \
                        (pivots[1] < pivots[3]) and  (pivots[1]/pivots[3] >= bottoms_min_ratio) :
                        ohlc.at[candle_idx, "double_idx"]     = pivot_indx
                        ohlc.at[candle_idx, "double_point"]   = pivots
                        ohlc.loc[candle_idx, "double_type"]   = "bottoms"                         
                        ohlc.loc[candle_idx, "chart_type"]    = "double"

                        # === 판단 근거 출력 ===
                        logging.debug("\n=== Double Bottom Detected ===")
                        logging.debug(f"candle_idx: {candle_idx}")
                        break
                        
    return ohlc
