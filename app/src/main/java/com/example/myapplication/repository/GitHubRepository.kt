package com.example.myapplication.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.myapplication.ContributionData
import com.example.myapplication.ContributionHelper
import com.example.myapplication.api.GitHubApiClient
import com.example.myapplication.model.Repository
import com.example.myapplication.model.User
import com.example.myapplication.util.Constants
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Collections.emptyList
import java.util.Collections.emptyMap

/**
 * GitHub API 데이터를 관리하는 Repository 클래스
 */
class GitHubRepository {
    private val apiService = GitHubApiClient.service

    /**
     * 사용자 정보 조회
     * @param username 사용자 이름 (비어있으면 기본값 사용)
     * @return 사용자 정보 또는 null (에러 발생 시)
     */
    suspend fun getUser(username: String = Constants.DEFAULT_GITHUB_USERNAME): User? {
        val response = apiService.getUser(
            token = GitHubApiClient.getAuthHeader(),
            username = username
        )
        
        return if (response.isSuccessful) {
            response.body()
        } else {
            null
        }
    }

    /**
     * 사용자의 저장소 목록 조회
     * @param username 사용자 이름 (비어있으면 기본값 사용)
     * @return 저장소 목록 또는 빈 목록 (에러 발생 시)
     */
    suspend fun getUserRepositories(username: String = Constants.DEFAULT_GITHUB_USERNAME): List<Repository> {
        val response = apiService.getUserRepositories(
            token = GitHubApiClient.getAuthHeader(),
            username = username
        )
        
        return if (response.isSuccessful) {
            response.body() ?: emptyList()
        } else {
            emptyList()
        }
    }
    
    /**
     * 사용자의 컨트리뷰션 데이터 조회
     * REST API를 사용하여 사용자의 최근 활동을 조회하고 컨트리뷰션으로 계산합니다.
     * @param username 사용자 이름 (비어있으면 기본값 사용)
     * @return 컨트리뷰션 데이터
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getUserContributions(username: String = Constants.DEFAULT_GITHUB_USERNAME): ContributionData {
        val response = apiService.getUserEvents(
            token = GitHubApiClient.getAuthHeader(),
            username = username
        )
        
        if (!response.isSuccessful) {
            return ContributionData(0, 0, emptyMap())
        }
        
        val events = response.body() ?: emptyList()
        
        // 이벤트 데이터에서 컨트리뷰션 맵 생성
        val contributionsByDay = calculateContributionsFromEvents(events)
        
        return ContributionHelper.parseContributions(contributionsByDay)
    }
    
    /**
     * 이벤트 데이터로부터 날짜별 컨트리뷰션 수 계산
     * @param events GitHub 이벤트 목록
     * @return 날짜별 컨트리뷰션 수 맵 (ISO 날짜 형식 -> 컨트리뷰션 수)
     */
    private fun calculateContributionsFromEvents(events: List<Map<String, Any>>): Map<String, Int> {
        val result = mutableMapOf<String, Int>()
        val formatter = DateTimeFormatter.ISO_DATE
        val today = LocalDate.now()
        
        // 최근 30일 동안의 날짜를 초기화 (모든 날짜가 표시되도록)
        for (i in 0 until 30) {
            val date = today.minusDays(i.toLong())
            result[date.format(formatter)] = 0
        }
        
        // 이벤트 처리 및 컨트리뷰션 집계
        events.forEach { event ->
            val type = event["type"] as? String
            val createdAt = event["created_at"] as? String
            
            if (type != null && createdAt != null) {
                // GitHub 타임스탬프에서 날짜 부분만 추출 (yyyy-MM-dd)
                val datePart = createdAt.substring(0, 10)
                
                // PushEvent, CreateEvent, PullRequestEvent 등 관련 이벤트를 컨트리뷰션으로 계산
                if (type in listOf("PushEvent", "CreateEvent", "PullRequestEvent", "IssuesEvent", "CommitCommentEvent")) {
                    result[datePart] = (result[datePart] ?: 0) + 1
                }
            }
        }
        
        return result
    }
} 