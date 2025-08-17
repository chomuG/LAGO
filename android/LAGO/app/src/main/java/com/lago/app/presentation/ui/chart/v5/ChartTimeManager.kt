package com.lago.app.presentation.ui.chart.v5

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * 차트 시간 처리 통합 관리자
 * 모든 시간 관련 변환과 포맷팅을 중앙집중화
 */
object ChartTimeManager {

    private val KST_ZONE = ZoneId.of("Asia/Seoul")
    private val UTC_ZONE = ZoneId.of("UTC")

    // API 요청용 포맷터
    private val API_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

    // 표시용 포맷터들
    private val DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private val DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    /**
     * 모든 타임스탬프를 epoch seconds로 정규화
     * @param timestamp milliseconds 또는 seconds
     * @return epoch seconds (10자리)
     */
    fun normalizeToEpochSeconds(timestamp: Long): Long {
        return if (timestamp > 9_999_999_999L) {
            timestamp / 1000 // milliseconds -> seconds
        } else {
            timestamp // 이미 seconds
        }
    }

    /**
     * 시간프레임에 따른 버킷 시작 시간 계산 (KST 기준)
     * @param epochSeconds epoch seconds
     * @param timeFrame 시간프레임 ("1", "3", "5", "15", "30", "60", "D", "W", "M", "Y")
     * @return 버킷 시작 시간 (epoch seconds)
     */
    fun getBucketStartTime(epochSeconds: Long, timeFrame: String): Long {
        val zonedDateTime = Instant.ofEpochSecond(epochSeconds).atZone(KST_ZONE)

        return when (timeFrame) {
            "1", "3", "5", "10", "15", "30", "60" -> {
                val minutes = timeFrame.toInt()
                val adjustedMinute = (zonedDateTime.minute / minutes) * minutes
                zonedDateTime
                    .withMinute(adjustedMinute)
                    .withSecond(0)
                    .withNano(0)
                    .toEpochSecond()
            }
            "D" -> {
                zonedDateTime
                    .toLocalDate()
                    .atStartOfDay(KST_ZONE)
                    .toEpochSecond()
            }
            "W" -> {
                zonedDateTime
                    .toLocalDate()
                    .with(java.time.DayOfWeek.MONDAY)
                    .atStartOfDay(KST_ZONE)
                    .toEpochSecond()
            }
            "M" -> {
                zonedDateTime
                    .withDayOfMonth(1)
                    .toLocalDate()
                    .atStartOfDay(KST_ZONE)
                    .toEpochSecond()
            }
            "Y" -> {
                zonedDateTime
                    .withDayOfYear(1)
                    .toLocalDate()
                    .atStartOfDay(KST_ZONE)
                    .toEpochSecond()
            }
            else -> epochSeconds // 알 수 없는 프레임은 원본 반환
        }
    }

    /**
     * JavaScript 차트용 시간 포맷 (항상 epoch seconds)
     * @param timestamp milliseconds 또는 seconds
     * @return epoch seconds (JavaScript 호환)
     */
    fun formatForJavaScript(timestamp: Long): Long {
        return normalizeToEpochSeconds(timestamp)
    }

    /**
     * 표시용 시간 포맷팅 (KST 기준)
     * @param epochSeconds epoch seconds
     * @param includeTime 시간 포함 여부 (false면 날짜만)
     * @return 포맷된 시간 문자열
     */
    fun formatForDisplay(epochSeconds: Long, includeTime: Boolean = true): String {
        val zonedDateTime = Instant.ofEpochSecond(epochSeconds).atZone(KST_ZONE)
        return if (includeTime) {
            zonedDateTime.format(DISPLAY_FORMATTER)
        } else {
            zonedDateTime.format(DATE_ONLY_FORMATTER)
        }
    }

    /**
     * API 요청용 시간 포맷 (KST 기준)
     * @param epochSeconds epoch seconds
     * @return API 요청용 시간 문자열 ("yyyy-MM-ddTHH:mm:ss")
     */
    fun formatForApi(epochSeconds: Long): String {
        val zonedDateTime = Instant.ofEpochSecond(epochSeconds).atZone(KST_ZONE)
        return zonedDateTime.format(API_FORMATTER)
    }

    /**
     * 현재 KST 시간을 epoch seconds로 반환
     */
    fun getCurrentKstEpochSeconds(): Long {
        return ZonedDateTime.now(KST_ZONE).toEpochSecond()
    }

    /**
     * 시간프레임별 적절한 과거 시간 계산
     * @param timeFrame 시간프레임
     * @return 과거 시간 (epoch seconds)
     */
    fun calculatePastTime(timeFrame: String): Long {
        val now = ZonedDateTime.now(KST_ZONE)
        val pastTime = when (timeFrame) {
            "1" -> now.minusHours(24) // 1분봉: 1일 전
            "3" -> now.minusDays(3) // 3분봉: 3일 전
            "5" -> now.minusDays(5) // 5분봉: 5일 전
            "10" -> now.minusDays(10) // 10분봉: 10일 전
            "15" -> now.minusDays(15) // 15분봉: 15일 전
            "30" -> now.minusDays(30) // 30분봉: 30일 전
            "60" -> now.minusDays(60) // 60분봉: 60일 전
            "D" -> now.minusMonths(6) // 일봉: 6개월 전
            "W" -> now.minusYears(2) // 주봉: 2년 전
            "M" -> now.minusYears(5) // 월봉: 5년 전
            "Y" -> now.minusYears(10) // 년봉: 10년 전
            else -> now.minusDays(30) // 기본값: 30일 전
        }
        return pastTime.toEpochSecond()
    }

    /**
     * 두 시간이 같은 버킷에 속하는지 확인
     * @param time1 첫 번째 시간 (epoch seconds)
     * @param time2 두 번째 시간 (epoch seconds)
     * @param timeFrame 시간프레임
     * @return 같은 버킷 여부
     */
    fun isSameBucket(time1: Long, time2: Long, timeFrame: String): Boolean {
        val bucket1 = getBucketStartTime(time1, timeFrame)
        val bucket2 = getBucketStartTime(time2, timeFrame)
        return bucket1 == bucket2
    }

    /**
     * 시간프레임이 분봉인지 확인
     */
    fun isMinuteFrame(timeFrame: String): Boolean {
        return timeFrame in listOf("1", "3", "5", "10", "15", "30", "60")
    }

    /**
     * 시간프레임이 일봉 이상인지 확인
     */
    fun isDailyOrAbove(timeFrame: String): Boolean {
        return timeFrame in listOf("D", "W", "M", "Y")
    }

    /**
     * 디버깅용 시간 정보 출력
     */
    fun debugTimeInfo(label: String, timestamp: Long, timeFrame: String? = null) {
        val normalized = normalizeToEpochSeconds(timestamp)
        val kstTime = formatForDisplay(normalized)
        val bucketTime = timeFrame?.let {
            formatForDisplay(getBucketStartTime(normalized, it))
        }

        android.util.Log.d("ChartTimeManager", buildString {
            append("$label: ")
            append("original=$timestamp, ")
            append("normalized=$normalized, ")
            append("KST=$kstTime")
            if (bucketTime != null) {
                append(", bucket($timeFrame)=$bucketTime")
            }
        })
    }
}