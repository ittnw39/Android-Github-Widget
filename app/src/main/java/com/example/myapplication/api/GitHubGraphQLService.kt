package com.example.myapplication.api

import com.example.myapplication.model.ContributionCalendarResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface GitHubGraphQLService {
    @Headers("Content-Type: application/json")
    @POST("graphql")
    suspend fun queryGraphQL(
        @Body body: Map<String, String>
    ): Response<ContributionCalendarResponse>
}