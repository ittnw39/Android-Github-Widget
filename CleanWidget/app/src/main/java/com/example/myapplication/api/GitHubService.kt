package com.example.myapplication.api

import com.example.myapplication.model.Repository
import com.example.myapplication.model.User
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * GitHub REST API를 호출하는 Retrofit 인터페이스
 */
interface GitHubService {
    /**
     * 사용자 정보 조회
     * @param username 사용자 이름
     * @return 사용자 정보
     */
    @GET("users/{username}")
    suspend fun getUser(
        @Path("username") username: String
    ): Response<User>

    /**
     * 사용자의 저장소 목록 조회
     * @param username 사용자 이름
     * @return 저장소 목록
     */
    @GET("users/{username}/repos")
    suspend fun getUserRepositories(
        @Path("username") username: String
    ): Response<List<Repository>>
} 