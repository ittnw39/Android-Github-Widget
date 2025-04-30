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
            username = username
        )
        
        return if (response.isSuccessful) {
            response.body() ?: emptyList()
        } else {
            emptyList()
        }
    }
    
    /**
     * GraphQL을 사용하여 연도별 전체 컨트리뷰션과 날짜별 컨트리뷰션 맵을 한 번에 조회합니다.
     * @param username GitHub 사용자 이름
     * @param year 조회할 연도
     * @return Pair(전체 컨트리뷰션 수, 날짜별 컨트리뷰션 맵)
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getContributionYearData(
        username: String = Constants.DEFAULT_GITHUB_USERNAME,
        year: Int = LocalDate.now().year
    ): Pair<Int, Map<String, Int>> {
        val query = """
        {
          user(login: "${username}") {
            contributionsCollection(from: "${year}-01-01T00:00:00Z", to: "${year}-12-31T23:59:59Z") {
              contributionCalendar {
                totalContributions
                weeks {
                  contributionDays {
                    date
                    contributionCount
                  }
                }
              }
            }
          }
        }
    """.trimIndent()

        val response = GitHubApiClient.graphqlService.queryGraphQL(mapOf("query" to query))
        if (!response.isSuccessful) {
            println("❌ GraphQL 실패 (getContributionYearData): ${response.errorBody()?.string()}")
            return 0 to emptyMap()
        }
        val calendar = response.body()?.data?.user?.contributionsCollection?.contributionCalendar
        val total = calendar?.totalContributions ?: 0
        val dayMap = mutableMapOf<String, Int>()
        calendar?.weeks?.forEach { week ->
            week.contributionDays.forEach { day ->
                dayMap[day.date] = day.contributionCount
            }
        }
        return total to dayMap
    }

} 