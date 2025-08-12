package com.tradingview.lightweightcharts.api.series.enums

import com.google.gson.annotations.SerializedName

enum class SeriesMarkerShape {
    @SerializedName("circle")
    CIRCLE,
    @SerializedName("square")
    SQUARE,
    @SerializedName("arrowUp")
    ARROW_UP,
    @SerializedName("arrowDown")
    ARROW_DOWN,
    
    // AI 캐릭터 아이콘들
    @SerializedName("characterBlue")
    CHARACTER_BLUE,
    @SerializedName("characterGreen") 
    CHARACTER_GREEN,
    @SerializedName("characterRed")
    CHARACTER_RED,
    @SerializedName("characterYellow")
    CHARACTER_YELLOW
}