package com.example.myapplication

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Collections.emptyMap

/**
 * GitHub 컨트리뷰션 데이터를 처리하는 헬퍼 클래스
 */
object ContributionHelper {
    /**
     * 오늘 날짜의 컨트리뷰션 수를 찾습니다.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun findTodayContributions(contributionsByDay: Map<String, Int>): Int {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        return contributionsByDay[today] ?: 0
    }
    
    /**
     * 최근 컨트리뷰션 데이터를 파싱하여 ContributionData 객체를 생성합니다.
     * JSON 응답에서 컨트리뷰션 정보를 추출합니다.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun parseContributions(contributionsJson: Map<String, Int>?): ContributionData {
        if (contributionsJson == null) {
            return createEmptyData()
        }
        
        val totalContributions = contributionsJson.values.sum()
        val todayContributions = findTodayContributions(contributionsJson)
        
        return ContributionData(
            totalContributions = totalContributions,
            todayContributions = todayContributions,
            contributionsByDay = contributionsJson,
            lastUpdated = System.currentTimeMillis()
        )
    }
    
    /**
     * 비어있는 데이터 객체를 생성합니다.
     */
    private fun createEmptyData(): ContributionData {
        return ContributionData(
            totalContributions = 0, 
            todayContributions = 0,
            contributionsByDay = emptyMap(),
            lastUpdated = System.currentTimeMillis()
        )
    }
}