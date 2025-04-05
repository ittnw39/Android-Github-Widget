package com.example.myapplication.model

import com.google.gson.annotations.SerializedName

/**
 * GitHub 사용자 정보를 나타내는 데이터 클래스
 */
data class User(
    @SerializedName("login") val username: String,
    @SerializedName("name") val name: String?,
    @SerializedName("avatar_url") val avatarUrl: String,
    @SerializedName("bio") val bio: String?,
    @SerializedName("public_repos") val publicRepos: Int,
    @SerializedName("followers") val followers: Int,
    @SerializedName("following") val following: Int
) 