package com.example.myapplication.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.myapplication.model.Repository
import com.example.myapplication.model.User
import com.example.myapplication.api.GitHubApiClient
import com.example.myapplication.util.Constants
import java.time.LocalDate
import java.util.Collections.emptyList
import java.util.Collections.emptyMap

class GitHubRepository {
    private val apiService = GitHubApiClient.service

    suspend fun getUser(username: String = Constants.DEFAULT_GITHUB_USERNAME): User? {
        val response = apiService.getUser(username)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun getUserRepositories(username: String = Constants.DEFAULT_GITHUB_USERNAME): List<Repository> {
        val response = apiService.getUserRepositories(username)
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getContributionYearData(
        username: String = Constants.DEFAULT_GITHUB_USERNAME,
        year: Int = LocalDate.now().year
    ): Pair<Int, Map<String, Int>> {
        val query = """
        {
          user(login: "$username") {
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
            val errorBody = response.errorBody()?.string() ?: "Unknown error"
            Log.e("GitHubRepository", "❌ GraphQL 실패: $errorBody")
            throw RuntimeException("GraphQL 호출 실패: $errorBody")
        }

        val calendar = response.body()?.data?.user?.contributionsCollection?.contributionCalendar
            ?: throw IllegalStateException("GraphQL 응답 구조 누락")

        val total = calendar.totalContributions
        val dayMap = mutableMapOf<String, Int>()
        calendar.weeks.forEach { week ->
            week.contributionDays.forEach { day ->
                dayMap[day.date] = day.contributionCount
            }
        }

        return total to dayMap
    }
}
