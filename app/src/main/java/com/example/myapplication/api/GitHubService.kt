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
     * @param token GitHub API 인증 토큰
     * @param username 사용자 이름
     * @return 사용자 정보
     */
    @GET("users/{username}")
    suspend fun getUser(
        @Header("Authorization") token: String,
        @Path("username") username: String
    ): Response<User>

    /**
     * 사용자의 저장소 목록 조회
     * @param token GitHub API 인증 토큰
     * @param username 사용자 이름
     * @return 저장소 목록
     */
    @GET("users/{username}/repos")
    suspend fun getUserRepositories(
        @Header("Authorization") token: String,
        @Path("username") username: String
    ): Response<List<Repository>>
    
    /**
     * 사용자의 최근 이벤트 조회 (컨트리뷰션용)
     * 이 API는 사용자의 최근 활동을 가져와 컨트리뷰션 계산에 사용합니다.
     * @param token GitHub API 인증 토큰
     * @param username 사용자 이름
     * @param perPage 페이지당 결과 수
     * @return 이벤트 목록
     */
    @GET("users/{username}/events")
    suspend fun getUserEvents(
        @Header("Authorization") token: String,
        @Path("username") username: String,
        @Query("per_page") perPage: Int = 100
    ): Response<List<Map<String, Any>>>
} 