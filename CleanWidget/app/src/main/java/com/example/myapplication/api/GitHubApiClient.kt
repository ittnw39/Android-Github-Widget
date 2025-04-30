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

    // BuildConfig에서 GitHub 토큰 가져오기
    private val GITHUB_TOKEN = BuildConfig.GITHUB_TOKEN
    
    private val retrofit: Retrofit by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .header("Authorization", "Bearer $GITHUB_TOKEN")
                    .build()
                chain.proceed(request)
            }
            .build()
            
        Retrofit.Builder()
            .baseUrl(Constants.GITHUB_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    // GitHubService 인터페이스의 구현체 생성
    val service: GitHubService by lazy {
        retrofit.create(GitHubService::class.java)
    }

    val graphqlService: GitHubGraphQLService by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .header("Authorization", "Bearer $GITHUB_TOKEN")
                    .build()
                chain.proceed(request)
            }
            .build()
            
        Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(GitHubGraphQLService::class.java)
    }
} 