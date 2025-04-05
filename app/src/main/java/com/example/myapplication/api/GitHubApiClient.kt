package com.example.myapplication.api

import android.content.Context
import com.example.myapplication.util.Constants
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * GitHub API 통신을 위한 Retrofit 클라이언트 객체
 */
object GitHubApiClient {
    private const val PREFS_NAME = "GitHubWidgetPrefs"
    private const val KEY_TOKEN = "github_token"
    private var cachedToken: String? = null
    
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

    // API 토큰 인증 헤더 형식으로 변환
    fun getAuthHeader(context: Context? = null): String {
        val token = if (context != null) {
            getSavedToken(context)
        } else {
            cachedToken ?: Constants.GITHUB_API_TOKEN
        }
        
        return "token $token"
    }
    
    // SharedPreferences에서 토큰 읽기
    fun getSavedToken(context: Context): String {
        if (cachedToken == null) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            cachedToken = prefs.getString(KEY_TOKEN, Constants.GITHUB_API_TOKEN) ?: Constants.GITHUB_API_TOKEN
        }
        return cachedToken ?: Constants.GITHUB_API_TOKEN
    }
    
    // SharedPreferences에 토큰 저장
    fun saveToken(context: Context, token: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_TOKEN, token).apply()
        cachedToken = token
    }
} 