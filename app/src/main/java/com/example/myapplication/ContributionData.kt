package com.example.myapplication

import java.util.Collections.emptyMap

/**
 * GitHub 사용자의 컨트리뷰션 데이터를 저장하는 데이터 클래스
 */
data class ContributionData(
    val totalContributions: Int = 0,
    val todayContributions: Int = 0,
    val contributionsByDay: Map<String, Int> = emptyMap(),
    val lastUpdated: Long = System.currentTimeMillis()
) {
    /**
     * 오늘 컨트리뷰션이 있는지 확인합니다.
     */
    fun hasTodayContributions(): Boolean {
        return todayContributions > 0
    }
}
