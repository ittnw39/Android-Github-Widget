package com.example.myapplication.api

import android.content.Context
import com.example.myapplication.BuildConfig
import com.example.myapplication.util.Constants
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * GitHub API 통신을 위한 Retrofit 클라이언트 객체
 */
object GitHubApiClient {
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.GITHUB_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // GitHubService 인터페이스의 구현체 생성
    val service: GitHubService by lazy {
        retrofit.create(GitHubService::class.java)
    }

    val graphqlService: GitHubGraphQLService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder().build())
            .build()
            .create(GitHubGraphQLService::class.java)
    }
} 